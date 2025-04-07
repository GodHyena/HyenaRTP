package me.hyena.hyenartp

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable

object CountdownUtil {
    fun startCountdown(plugin: JavaPlugin, player: Player, seconds: Int, onFinish: () -> Unit) {
        object : BukkitRunnable() {
            var timeLeft = seconds
            override fun run() {
                if (!player.isOnline) {
                    cancel()
                    return
                }

                player.sendActionBar(Component.text("Teleporting in $timeLeft seconds...").color(NamedTextColor.YELLOW))
                if (timeLeft <= 0) {
                    cancel()
                    onFinish()
                }
                timeLeft--
            }
        }.runTaskTimer(plugin, 0L, 20L)
    }
}