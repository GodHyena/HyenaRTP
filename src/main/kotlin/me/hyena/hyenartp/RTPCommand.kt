package me.hyena.hyenartp

import org.bukkit.*
import org.bukkit.command.*
import org.bukkit.entity.Player
import java.util.*
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

        // Console or op using /rtp <player> <world>
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

            val loc = getSafeLocation(world, target)
            if (loc != null) {
                usedLocations.getOrPut(target.uniqueId) { mutableListOf() }.add(loc)
                target.teleport(loc)
                target.sendMessage("§aYou were randomly teleported to a safe location!")
                sender.sendMessage("§aTeleported ${target.name} to ${world.name}")
            } else {
                sender.sendMessage("§cCould not find a safe location.")
            }
            return true
        }

        // Player using /rtp <world>
        if (sender is Player && args.size == 1) {
            val player = sender
            val worldName = args[0]
            val world = Bukkit.getWorld(worldName)

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

            val loc = getSafeLocation(world, player)
            if (loc != null) {
                cooldowns[player.uniqueId] = now
                usedLocations.getOrPut(player.uniqueId) { mutableListOf() }.add(loc)
                player.teleport(loc)
                player.sendMessage("§aYou were randomly teleported to a safe location!")
            } else {
                player.sendMessage("§cCould not find a safe location.")
            }

            return true
        }

        sender.sendMessage("§eUsage: /rtp <world> or /rtp <player> <world>")
        return true
    }

    private fun getSafeLocation(world: World, player: Player): Location? {
        val border = world.worldBorder
        val prevLocations = usedLocations[player.uniqueId] ?: emptyList()

        repeat(50) {
            val x = Random.nextDouble(border.center.x - border.size / 2, border.center.x + border.size / 2)
            val z = Random.nextDouble(border.center.z - border.size / 2, border.center.z + border.size / 2)
            val y = world.getHighestBlockYAt(x.toInt(), z.toInt()) + 1
            val loc = Location(world, x, y.toDouble(), z)

            val block = loc.clone().subtract(0.0, 1.0, 0.0).block
            val isSafe = block.type.isSolid && block.type != Material.LAVA && !block.isLiquid

            if (isSafe && prevLocations.none { it.distance(loc) < 5 }) {
                return loc
            }
        }

        return null
    }
}