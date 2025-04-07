package me.hyena.hyenartp

import org.bukkit.*
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.entity.Player

class RTPCommand(private val plugin: HyenaRTP) : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        if (args == null || args.isEmpty()) {
            sendHelp(sender)
            return true
        }

        if (args.size == 1 && sender is Player) {
            TeleportManager.handleTeleport(sender, sender, args[0])
            return true
        }

        if (args.size == 2 && (sender.isOp || sender is ConsoleCommandSender)) {
            val target = Bukkit.getPlayerExact(args[0])
            if (target == null) {
                sender.sendMessage("§cPlayer not found.")
                return true
            }
            TeleportManager.handleTeleport(sender, target, args[1])
            return true
        }

        sendHelp(sender)
        return true
    }

    private fun sendHelp(sender: CommandSender) {
        sender.sendMessage("§eHyenaRTP Help")
        sender.sendMessage("§6/rtp <world> §7- Teleport to random location in a world")
        sender.sendMessage("§6/rtp <player> <world> §7- Teleport another player (OP only)")
    }
}