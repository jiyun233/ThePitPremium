package cn.charlotte.pit.data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class KillBoardEntry {
    private static List<KillBoardEntry> killBoardEntries = new ArrayList<>();

    private final String name;
    private final UUID uuid;
    private final int rank;
    private final int kills;

    public KillBoardEntry(String name, UUID uuid, int rank, int kills) {
        this.name = name;
        this.uuid = uuid;
        this.rank = rank;
        this.kills = kills;
    }

    public static List<KillBoardEntry> getKillBoardEntries() {
        return KillBoardEntry.killBoardEntries;
    }

    public static void setKillBoardEntries(List<KillBoardEntry> killBoardEntries) {
        KillBoardEntry.killBoardEntries = killBoardEntries;
    }

    public String getName() {
        return this.name;
    }

    public UUID getUuid() {
        return this.uuid;
    }

    public int getRank() {
        return this.rank;
    }

    public int getKills() {
        return this.kills;
    }
}
