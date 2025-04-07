package me.hyena.hyenartp

import org.bukkit.plugin.java.JavaPlugin

class HyenaRTP : JavaPlugin() {
    override fun onEnable() {
        saveDefaultConfig()
        TeleportManager.init(this)
        getCommand("rtp")?.setExecutor(RTPCommand(this))
    }
}