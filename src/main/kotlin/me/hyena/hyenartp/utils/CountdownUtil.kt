object CountdownUtil {
    fun startCountdown(player: Player, seconds: Int, onComplete: () -> Unit) {
        object : BukkitRunnable() {
            var count = seconds
            override fun run() {
                if (count <= 0) {
                    cancel()
                    onComplete()
                    return
                }
                player.sendTitle("Teleporting in $count...", "", 0, 20, 0)
                count--
            }
        }.runTaskTimer(pluginInstance, 0L, 20L)
    }
}