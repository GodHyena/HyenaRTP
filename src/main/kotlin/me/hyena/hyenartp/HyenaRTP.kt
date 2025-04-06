package me.hyena.hyenartp

import org.bukkit.plugin.java.JavaPlugin

class HyenaRTP : JavaPlugin() {
    companion object {
        lateinit var instance: HyenaRTP
        val disabledWorlds = listOf("world_disabled", "lobby") // Add disabled world names here
    }

    override fun onEnable() {
        instance = this
        getCommand("rtp")?.setExecutor(RTPCommand())
        logger.info("HyenaRTP enabled.")
    }

    override fun onDisable() {
        logger.info("HyenaRTP disabled.")
    }
}