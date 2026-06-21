package net.jitse.npclib.listeners;

import cn.charlotte.pit.ThePit;
import cn.charlotte.pit.config.PitConfig;
import cn.charlotte.pit.util.random.RandomUtil;
import net.jitse.npclib.internal.NPCBase;
import net.jitse.npclib.internal.NPCManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;

public class HandleMoveBase {

    void handleMove(Player player) {
        handleMove(player, true);
    }

    void handleMove(Player player, boolean checkLowestY) {
        if (checkLowestY && teleportToSpawnIfBelowLowestY(player)) {
            return;
        }

        for (NPCBase npc : NPCManager.getAllNPCs()) {
            if (!npc.getShown().contains(player.getUniqueId())) {
                continue; // NPC was never supposed to be shown to the player.
            }

            if (!npc.isShown(player) && npc.inRangeOf(player) && npc.inViewOf(player)) {
                // The player is in range and can see the NPC, auto-show it.
                npc.show(player, true);
            } else if (npc.isShown(player) && !npc.inRangeOf(player)) {
                // The player is not in range of the NPC anymore, auto-hide it.
                npc.hide(player, true);
            }
        }
    }

    private boolean teleportToSpawnIfBelowLowestY(Player player) {
        PitConfig config = ThePit.getInstance().getPitConfig();
        if (player.getLocation().getY() >= config.getArenaLowestY()) {
            return false;
        }

        List<Location> spawnLocations = config.getSpawnLocations();
        if (!spawnLocations.isEmpty()) {
            Location location = spawnLocations.get(RandomUtil.random.nextInt(spawnLocations.size()));
            player.teleport(location);
        }
        return true;
    }

}
