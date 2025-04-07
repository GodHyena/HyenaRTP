package me.hyena.hyenartp.utils

import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable

object CountdownUtil {
    fun startCountdown(plugin: JavaPlugin, player: Player, seconds: Int, onFinish: () -> Unit) {
        object : BukkitRunnable() {
            var timeLeft = seconds
            override fun run() {
                if (timeLeft <= 0) {
                    cancel()
                    onFinish()
                    return
                }

                player.sendTitle("§eTeleporting in...", "§a$timeLeft", 0, 20, 10)
                timeLeft--
            }
        }.runTaskTimer(plugin, 0L, 20L)
    }

    fun showTeleportSuccess(player: Player) {
        val loc = player.location
        player.sendTitle("§aTeleported!", "§7X:${loc.blockX} Y:${loc.blockY} Z:${loc.blockZ}", 10, 40, 10)
    }
}