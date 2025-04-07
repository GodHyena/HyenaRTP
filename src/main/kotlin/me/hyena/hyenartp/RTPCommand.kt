package me.hyena.hyenartp

import me.hyena.hyenartp.utils.CountdownUtil
import org.bukkit.*
import org.bukkit.command.*
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import java.util.*
import kotlin.math.abs
import kotlin.random.Random

class RTPCommand : CommandExecutor {

    private val cooldowns = mutableMapOf<UUID, Long>()
    private val usedLocations = mutableMapOf<UUID, MutableList<Location>>()

    private val rankCooldowns = mapOf(
        "default" to 300,
        "vip" to 150,
        "mvp" to 75,
        "elite" to 30
    )

    private val rankWorlds = mapOf(
        "default" to listOf("world"),
        "vip" to listOf("world", "world_nether"),
        "mvp" to listOf("world", "world_nether", "world_the_end"),
        "elite" to listOf("world", "world_nether", "world_the_end")
    )