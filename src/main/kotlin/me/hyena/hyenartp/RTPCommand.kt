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
    private fun getRank(player: Player): String {
        return when {
            player.hasPermission("hyenartp.rank.elite") -> "elite"
            player.hasPermission("hyenartp.rank.mvp") -> "mvp"
            player.hasPermission("hyenartp.rank.vip") -> "vip"
            else -> "default"
        }
    }

    override fun onCommand(sender: CommandSender, cmd: Command, label: String, args: Array<out String>): Boolean {
        if (!cmd.name.equals("rtp", true)) return false

        if (args.isEmpty()) {
            sender.sendMessage("§eUsage: /rtp <world> or /rtp <player> <world>")
            return true
        }

        if (args.size == 2 && (sender is ConsoleCommandSender || sender.isOp)) {
            val target = Bukkit.getPlayer(args[0])
            val world = Bukkit.getWorld(args[1])

            if (target == null || world == null) {
                sender.sendMessage("§cInvalid player or world.")
                return true
            }

            if (HyenaRTP.disabledWorlds.contains(world.name)) {
                sender.sendMessage("§cThat world is disabled for RTP.")
                return true
            }

            teleportWithEffects(target, world, sender)
            return true
        }

        if (sender is Player && args.size == 1) {
            val player = sender
            val world = Bukkit.getWorld(args[0])

            if (world == null) {
                player.sendMessage("§cWorld not found.")
                return true
            }

            if (HyenaRTP.disabledWorlds.contains(world.name)) {
                player.sendMessage("§cThat world is disabled for RTP.")
                return true
            }

            val rank = getRank(player)
            val allowedWorlds = rankWorlds[rank] ?: listOf("world")

            if (!allowedWorlds.contains(world.name)) {
                player.sendMessage("§cYour rank cannot access this world.")
                return true
            }

            val cooldown = rankCooldowns[rank] ?: 300
            val last = cooldowns[player.uniqueId] ?: 0L
            val now = System.currentTimeMillis()

            if ((now - last) < cooldown * 1000) {
                val remaining = ((cooldown * 1000 - (now - last)) / 1000)
                player.sendMessage("§cWait $remaining seconds before using RTP again.")
                return true
            }

            cooldowns[player.uniqueId] = now
            teleportWithEffects(player, world, null)
            return true
        }

        sender.sendMessage("§eUsage: /rtp <world> or /rtp <player> <world>")
        return true
    }
private fun teleportWithEffects(player: Player, world: World, sender: CommandSender?) {
        val border = world.worldBorder
        val uuid = player.uniqueId
        val prevLocs = usedLocations.getOrPut(uuid) { mutableListOf() }

        val startRadius = 100.0
        val maxRadius = 1000.0
        val blockSize = 20.0
        val reserved = blockSize * blockSize
        val maxTries = 100

        for (i in 1..maxTries) {
            val radius = if (prevLocs.size * reserved > startRadius * startRadius) maxRadius else startRadius
            val angle = Random.nextDouble(0.0, 2 * Math.PI)
            val r = Random.nextDouble(0.0, radius)
            val x = border.center.x + r * Math.cos(angle)
            val z = border.center.z + r * Math.sin(angle)
            val y = world.getHighestBlockYAt(x.toInt(), z.toInt()) + 1
            val loc = Location(world, x, y.toDouble(), z)

            if (isLocationSafe(loc) && prevLocs.none { it.distance(loc) < blockSize }) {
                prevLocs.add(loc)

                // Countdown on screen
                for (t in 0..5) {
                    Bukkit.getScheduler().runTaskLater(HyenaRTP.instance, Runnable {
                        if (t < 5) {
                            player.sendTitle("§eTeleporting in...", "§c${5 - t}", 0, 20, 0)
                        } else {
                            player.sendTitle("", "", 0, 0, 0)
                            player.teleport(loc)
                            player.sendTitle("§aTeleported!", "§7${loc.blockX}, ${loc.blockY}, ${loc.blockZ}", 0, 60, 20)
                            player.addPotionEffect(PotionEffect(PotionEffectType.BLINDNESS, 8 * 20, 1))
                            player.playSound(player.location, Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1f, 1f)
                        }
                    }, t * 20L)
                }

                sender?.sendMessage("§aTeleported ${player.name} to ${world.name}")
                return
            }
        }

        sender?.sendMessage("§cCould not find a safe location.")
        player.sendMessage("§cTeleport failed: no safe location found.")
    }

    private fun isLocationSafe(loc: Location): Boolean {
        val block = loc.clone().subtract(0.0, 1.0, 0.0).block
        return block.type.isSolid && block.type != Material.LAVA && !block.isLiquid
    }
}