package me.hyena.hyenartp

import org.bukkit.*
import org.bukkit.block.Block
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.plugin.java.JavaPlugin
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random

object TeleportManager {
    private lateinit var plugin: JavaPlugin
    private val cooldowns = ConcurrentHashMap<UUID, Long>()
    private val usedLocations = mutableSetOf<Triple<String, Int, Int>>() // World, x, z

    fun init(plugin: JavaPlugin) {
        this.plugin = plugin
    }

    private fun getCooldownTime(player: Player): Int {
        return when {
            player.hasPermission("hyenartp.rank.elite") -> 30
            player.hasPermission("hyenartp.rank.mvp") -> 75
            player.hasPermission("hyenartp.rank.vip") -> 150
            else -> 300
        }
    }

    private fun getAllowedWorlds(player: Player): List<String> {
        return when {
            player.hasPermission("hyenartp.rank.elite") -> listOf("world", "world_nether", "world_the_end")
            player.hasPermission("hyenartp.rank.mvp") -> listOf("world", "world_nether", "world_the_end")
            player.hasPermission("hyenartp.rank.vip") -> listOf("world", "world_nether")
            else -> listOf("world")
        }
    }

    private fun isLocationUsed(world: String, x: Int, z: Int): Boolean {
        for (loc in usedLocations) {
            if (loc.first == world &&
                Math.abs(loc.second - x) < 20 &&
                Math.abs(loc.third - z) < 20
            ) return true
        }
        return false
    }

    private fun reserveLocation(world: String, x: Int, z: Int) {
        usedLocations.add(Triple(world, x, z))
    }

    fun handleTeleport(sender: CommandSender, player: Player, worldName: String) {
        if (worldName in plugin.config.getStringList("disabled-worlds")) {
            sender.sendMessage("§cRTP is disabled in this world.")
            return
        }

        if (worldName !in getAllowedWorlds(player)) {
            sender.sendMessage("§cYou don’t have access to RTP in this world.")
            return
        }

        if (cooldowns.containsKey(player.uniqueId)) {
            val remaining = (cooldowns[player.uniqueId]!! - System.currentTimeMillis()) / 1000
            if (remaining > 0) {
                sender.sendMessage("§cYou must wait $remaining seconds before using RTP again.")
                return
            }
        }

        val world = Bukkit.getWorld(worldName)
        if (world == null) {
            sender.sendMessage("§cWorld not found.")
            return
        }

        val radius = 10000
        var found = false
        var attempts = 0
        var loc = world.spawnLocation

        while (!found && attempts < 1000) {
            val x = Random.nextInt(-radius, radius)
            val z = Random.nextInt(-radius, radius)
            if (isLocationUsed(world.name, x, z)) {
                attempts++
                continue
            }

            val y = world.getHighestBlockYAt(x, z)
            val block: Block = world.getBlockAt(x, y - 1, z)
            if (block.type in listOf(Material.LAVA, Material.CACTUS, Material.AIR, Material.FIRE, Material.MAGMA_BLOCK)) {
                attempts++
                continue
            }

            found = true
            loc = Location(world, x.toDouble(), y.toDouble(), z.toDouble())
            reserveLocation(world.name, x, z)
        }

        if (!found) {
            sender.sendMessage("§cFailed to find a safe location.")
            return
        }

        CountdownUtil.startCountdown(plugin, player, 5) {
            cooldowns[player.uniqueId] = System.currentTimeMillis() + getCooldownTime(player) * 1000
            player.addPotionEffect(PotionEffect(PotionEffectType.BLINDNESS, 160, 1))
            player.teleport(loc)
            player.playSound(loc, Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1f, 1f)
            player.sendActionBar(Component.text("Teleported to: ${loc.blockX}, ${loc.blockY}, ${loc.blockZ}").color(NamedTextColor.GREEN))
        }
    }
}