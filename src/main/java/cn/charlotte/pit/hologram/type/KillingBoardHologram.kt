package cn.charlotte.pit.hologram.type

import cn.charlotte.pit.ThePit
import cn.charlotte.pit.data.KillBoardEntry
import cn.charlotte.pit.hologram.AbstractHologram
import cn.charlotte.pit.util.rank.RankUtil
import org.bukkit.Location
import org.bukkit.entity.Player
import java.text.DecimalFormat

class KillingBoardHologram : AbstractHologram() {
    private val killFormat = DecimalFormat("#,###")

    override fun getInternalName(): String {
        return "killing_board"
    }

    override fun getText(player: Player): List<String> {
        val hologramText = mutableListOf<String>()
        hologramText.add("&c&l全局击杀榜")
        hologramText.add("&7累计击杀玩家排名")
        hologramText.add("")

        val entries = ArrayList(KillBoardEntry.getKillBoardEntries())
        for (index in 0 until 10) {
            if (index >= entries.size) {
                hologramText.add("&e${index + 1}&7. &7暂无")
                continue
            }

            val entry = entries[index]
            hologramText.add("&e${entry.rank}&7. ${RankUtil.getPlayerRealColoredName(entry.uuid)} &7- &c${killFormat.format(entry.kills.toLong())} 击杀")
        }

        val currentPlayerEntry = KillBoardEntry.getKillBoardEntries()
            .firstOrNull { it.uuid == player.uniqueId }

        hologramText.add("")
        if (currentPlayerEntry != null) {
            val top = 100.0 * currentPlayerEntry.rank / KillBoardEntry.getKillBoardEntries().size
            hologramText.add("&7你的击杀数: &c${killFormat.format(currentPlayerEntry.kills.toLong())} 击杀")
            hologramText.add("&7排名: &e#${currentPlayerEntry.rank} &7(前&e${DecimalFormat("0.0").format(top)}%&7)")
        } else {
            hologramText.add("&7&o还没有你的击杀排行数据，请稍后再来...")
        }
        hologramText.add("&7本排名仅显示 &f7天&7内登录过的玩家(&f${entries.size}&7名)")

        return hologramText
    }

    override fun shouldLoop(): Boolean {
        return true
    }

    override fun loopTicks(): Int {
        return 20
    }

    override fun getHologramHighInterval(): Double {
        return 0.3
    }

    override fun getLocation(): Location {
        return ThePit.getInstance().pitConfig.killingBoardHologramLocation
    }
}
