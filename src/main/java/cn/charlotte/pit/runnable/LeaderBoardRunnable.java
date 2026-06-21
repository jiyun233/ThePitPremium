package cn.charlotte.pit.runnable;

import cn.charlotte.pit.ThePit;
import cn.charlotte.pit.data.KillBoardEntry;
import cn.charlotte.pit.data.LeaderBoardEntry;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @Author: EmptyIrony
 * @Date: 2021/1/3 12:57
 */

public class LeaderBoardRunnable implements Runnable {
    private static final Logger LOGGER = Logger.getLogger(LeaderBoardRunnable.class.getName());
    private static final long ACTIVE_PLAYER_WINDOW = 7 * 24 * 60 * 60 * 1000L;
    private final ThePit instance;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public LeaderBoardRunnable(ThePit instance) {
        this.instance = instance;
        scheduler.scheduleAtFixedRate(this, 0, 1, TimeUnit.SECONDS);
    }

    @Override
    public void run() {
        try {
            List<Document> experienceDocuments = loadDocuments("totalExp");
            List<LeaderBoardEntry> entries = processExperienceDocuments(experienceDocuments);
            updateLeaderBoardEntries(entries);

            List<Document> killDocuments = loadDocuments("kills");
            List<KillBoardEntry> killEntries = processKillDocuments(killDocuments);
            updateKillBoardEntries(killEntries);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "更新排行榜数据时发生错误：", e);
        }
    }

    private List<Document> loadDocuments(String sortField) {
        try (var cursor = instance.getMongoDB()
                .getCollection()
                .find()
                .filter(Filters.gte("lastLogoutTime", System.currentTimeMillis() - ACTIVE_PLAYER_WINDOW))
                .sort(Sorts.descending(sortField))
                .cursor()) {

            List<Document> documents = new ArrayList<>();
            while (cursor.hasNext()) {
                documents.add(cursor.next());
            }
            return documents;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "从MongoDB加载数据时发生错误：", e);
            throw e;
        }
    }

    private List<LeaderBoardEntry> processExperienceDocuments(List<Document> documents) {
        List<LeaderBoardEntry> entries = new ArrayList<>();
        int rank = 1;
        for (Document document : documents) {
            try {
                String name = document.getString("playerName");
                String uuid = document.getString("uuid");
                Double experience = getExperience(document);
                int prestige = document.getInteger("prestige");
                entries.add(new LeaderBoardEntry(name, UUID.fromString(uuid), rank, experience, prestige));
                rank++;
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "处理经验排行榜文档时发生错误：", e);
            }
        }
        return entries;
    }

    private List<KillBoardEntry> processKillDocuments(List<Document> documents) {
        List<KillBoardEntry> entries = new ArrayList<>();
        int rank = 1;
        for (Document document : documents) {
            try {
                String name = document.getString("playerName");
                String uuid = document.getString("uuid");
                int kills = getInteger(document, "kills");
                entries.add(new KillBoardEntry(name, UUID.fromString(uuid), rank, kills));
                rank++;
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "处理击杀排行榜文档时发生错误：", e);
            }
        }
        return entries;
    }

    private Double getExperience(Document document) {
        final Object expObj = document.get("experience");
        try {
            return (Double) expObj;
        } catch (ClassCastException e) {
            try {
                return Double.valueOf(((Integer) expObj));
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "无法转换experience值：", ex);
                return 0.0;
            }
        }
    }

    private int getInteger(Document document, String field) {
        Object value = document.get(field);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return 0;
    }

    private void updateLeaderBoardEntries(List<LeaderBoardEntry> entries) {
        synchronized (LeaderBoardEntry.class) {
            LeaderBoardEntry.setLeaderBoardEntries(entries);
        }
    }

    private void updateKillBoardEntries(List<KillBoardEntry> entries) {
        synchronized (KillBoardEntry.class) {
            KillBoardEntry.setKillBoardEntries(entries);
        }
    }

    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(1, TimeUnit.MINUTES)) {
                LOGGER.warning("排行榜更新任务未能在1分钟内停止，强制关闭...");
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            LOGGER.severe("在等待排行榜更新任务停止时被中断。");
            Thread.currentThread().interrupt();
        }
    }
}
