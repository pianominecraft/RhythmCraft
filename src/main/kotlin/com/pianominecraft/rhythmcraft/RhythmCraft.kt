package com.pianominecraft.rhythmcraft

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.*
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.util.Vector
import java.util.regex.Pattern

class RhythmCraft : JavaPlugin(), Listener {

    private var dj: Player? = null
    private var isPlaying = false
    private var center = Location(Bukkit.getWorld("world")!!, 1.5, 244.0, 0.5)
    private var east: Player? = null
    private var west: Player? = null
    private var south: Player? = null
    private var north: Player? = null
    private var speed = 0.2
    private val plugin by lazy { this }
    private val eastNotes = arrayListOf<Entity>()
    private val westNotes = arrayListOf<Entity>()
    private val southNotes = arrayListOf<Entity>()
    private val northNotes = arrayListOf<Entity>()

    override fun onEnable() {
        saveDefaultConfig()
        server.pluginManager.registerEvents(this, this)

        repeat {
            center.world!!.entities.filterIsInstance<ArmorStand>().forEach {
                if (it.hasMetadata("facing")) {
                    when (it.getMetadata("facing")[0].asInt()) {
                        0 -> {
                            it.velocity = Vector(speed, 0.0, 0.0)
                            if (it.location.x - center.x > 24) {
                                it.remove()
                                server.scoreboardManager!!.mainScoreboard.getObjective("Score")!!.getScore(east!!.name).score -= 3000
                                eastNotes.remove(it)
                            }
                        }
                        1 -> {
                            it.velocity = Vector(-speed, 0.0, 0.0)
                            if (center.x - it.location.x > 24) {
                                it.remove()
                                server.scoreboardManager!!.mainScoreboard.getObjective("Score")!!.getScore(west!!.name).score -= 3000
                                westNotes.remove(it)
                            }
                        }
                        2 -> {
                            it.velocity = Vector(0.0, 0.0, speed)
                            if (it.location.z - center.z > 24) {
                                it.remove()
                                server.scoreboardManager!!.mainScoreboard.getObjective("Score")!!.getScore(south!!.name).score -= 3000
                                southNotes.remove(it)
                            }
                        }
                        3 -> {
                            it.velocity = Vector(0.0, 0.0, -speed)
                            if (center.z - it.location.z > 24) {
                                it.remove()
                                server.scoreboardManager!!.mainScoreboard.getObjective("Score")!!.getScore(north!!.name).score -= 3000
                                northNotes.remove(it)
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler fun onNote(e: PlayerItemHeldEvent) {
        if (isPlaying) {
            if (e.player == dj) {
                if (e.newSlot != 4) {
                    val slot = e.newSlot
                    val block =
                        if (slot == 0 || slot == 8) Material.RED_CONCRETE
                        else if (slot == 1 || slot == 7) Material.YELLOW_CONCRETE
                        else if (slot == 2 || slot == 6) Material.LIME_CONCRETE
                        else Material.LIGHT_BLUE_CONCRETE
                    val facing = arrayListOf<Int>()
                    if (east != null) facing.add(0)
                    if (west != null) facing.add(1)
                    if (south != null) facing.add(2)
                    if (north != null) facing.add(3)
                    facing.forEach {
                        val loc = when (it) {
                            0 -> {
                                center.clone().apply {
                                    x += 5
                                    z -= 4 - slot
                                }
                            }
                            1 -> {
                                center.clone().apply {
                                    x -= 5
                                    z += 4 - slot
                                }
                            }
                            2 -> {
                                center.clone().apply {
                                    z += 5
                                    x += 4 - slot
                                }
                            }
                            3 -> {
                                center.clone().apply {
                                    z -= 5
                                    x -= 4 - slot
                                }
                            }
                            else -> center
                        }
                        val note =
                            center.world!!.spawnEntity(loc.apply { yaw += 45f; y -= 1 }, EntityType.ARMOR_STAND).apply {
                                with(this as ArmorStand) {
                                    isInvisible = true
                                    equipment!!.helmet = ItemStack(block)
                                }
                                setMetadata("facing", FixedMetadataValue(plugin, it))
                                setMetadata("Key", FixedMetadataValue(plugin, 8 - slot))
                            }

                        when (it) {
                            0 -> eastNotes.add(note)
                            1 -> westNotes.add(note)
                            2 -> southNotes.add(note)
                            3 -> northNotes.add(note)
                        }
                    }
                }
            }
            else {
                val p = e.player
                if (e.newSlot != 4) {
                    val slot = e.newSlot
                    when (p) {
                        east -> {
                            if (eastNotes.isNotEmpty()) {
                                eastNotes.forEach loop@{ note ->
                                    if (note.hasMetadata("Key")) {
                                        if (note.getMetadata("Key")[0].asInt() == slot) {
                                            server.scoreboardManager!!.mainScoreboard.getObjective("Score")!!.getScore(p.name).score += ((note.location.x - center.x) * 100).toInt() - 1000
                                            note.remove()
                                            eastNotes.remove(note)
                                            println(1)
                                            return@loop
                                        }
                                    }
                                }
                            }
                        }
                        west -> {
                            if (westNotes.isNotEmpty()) {
                                westNotes.forEach loop@{ note ->
                                    if (note.hasMetadata("Key")) {
                                        if (note.getMetadata("Key")[0].asInt() == slot) {
                                            server.scoreboardManager!!.mainScoreboard.getObjective("Score")!!.getScore(p.name).score += ((center.x - note.location.x) * 100).toInt() - 1000
                                            note.remove()
                                            westNotes.remove(note)
                                            println(2)
                                            return@loop
                                        }
                                    }
                                }
                            }
                        }
                        south -> {
                            if (southNotes.isNotEmpty()) {
                                southNotes.forEach loop@{ note ->
                                    if (note.hasMetadata("Key")) {
                                        if (note.getMetadata("Key")[0].asInt() == slot) {
                                            server.scoreboardManager!!.mainScoreboard.getObjective("Score")!!.getScore(p.name).score += ((note.location.z - center.z) * 100).toInt() - 1000
                                            note.remove()
                                            southNotes.remove(note)
                                            println(3)
                                            return@loop
                                        }
                                    }
                                }
                            }
                        }
                        north -> {
                            if (northNotes.isNotEmpty()) {
                                northNotes.forEach loop@{ note ->
                                    if (note.hasMetadata("Key")) {
                                        if (note.getMetadata("Key")[0].asInt() == slot) {
                                            server.scoreboardManager!!.mainScoreboard.getObjective("Score")!!.getScore(p.name).score += ((center.z - note.location.z) * 100).toInt() - 1000
                                            note.remove()
                                            northNotes.remove(note)
                                            println(4)
                                            return@loop
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            e.isCancelled = true
        }
    }

    override fun onCommand(s: CommandSender, c: Command, l: String, a: Array<out String>): Boolean {

        if (c.name.equals("dj", ignoreCase = true)) {
            if (s.isOp) {
                if (a.isNotEmpty()) {
                    dj = Bukkit.getPlayer(a[0])
                    if (dj == null) s.sendMessage("%red%${a[0]}라는 플레이어가 서버에 없습니다".t)
                    else server.broadcastMessage("%green%이제 ${a[0]}님이 DJ 입니다".t)
                }
            }
        }
        else if (c.name.equals("start", ignoreCase = true)) {
            if (s.isOp) {
                if (isPlaying) {
                    s.sendMessage("%red%이미 게임이 진행중입니다".t)
                    return false
                }
                isPlaying = true
                if (server.scoreboardManager!!.mainScoreboard.getObjective("Score") == null) server.scoreboardManager!!.mainScoreboard.registerNewObjective("Score", "dummy", "%aqua%점수".t).apply {
                    displaySlot = DisplaySlot.SIDEBAR
                }
                if (east != null) server.scoreboardManager!!.mainScoreboard.getObjective("Score")!!.getScore(east!!.name).score = 0
                if (west != null) server.scoreboardManager!!.mainScoreboard.getObjective("Score")!!.getScore(west!!.name).score = 0
                if (south != null) server.scoreboardManager!!.mainScoreboard.getObjective("Score")!!.getScore(south!!.name).score = 0
                if (north != null) server.scoreboardManager!!.mainScoreboard.getObjective("Score")!!.getScore(north!!.name).score = 0
                if (dj != null) {
                    server.onlinePlayers.forEach { p ->
                        p.sendTitle("%aqua%Rhythm Craft".t, "%gray%DJ ${dj?.name}".t, 10, 40, 10)
                    }
                }
                for (i in 0..2) {
                    delay (i * 20L + 60L) {
                        server.onlinePlayers.forEach { p ->
                            p.sendTitle("%aqua%Rhythm Craft".t, "%gray%${3 - i}".t, 0, 21, 0)
                            p.playSound(p.location, Sound.UI_BUTTON_CLICK, 1f, 1f)
                        }
                    }
                }
                delay (120) {
                    if (a.isNotEmpty()) {
                        val pitch = if (a.size > 1) a[1].toFloat() else 1f
                        dj!!.playSound(dj!!.location, a[0], 1f, pitch)
                        delay((20 / speed).toLong()) {
                            server.onlinePlayers.forEach { p ->
                                if (p != dj) p.playSound(p.location, a[0], 1f, pitch)
                            }
                        }
                    }
                }
            }
        }
        else if (c.name.equals("finish", ignoreCase = true)) {
            if (s.isOp) {
                if (!isPlaying) {
                    s.sendMessage("%red%진행중인 게임이 없습니다".t)
                    return false
                }
                isPlaying = false
                server.onlinePlayers.forEach { p ->
                    p.sendTitle("%aqua%Rhythm Craft".t, "%gray%게임 종료".t, 20, 60, 20)
                }
            }
        }
        else if (c.name.equals("center", ignoreCase = true)) {
            if (s.isOp && s is Player) {
                center = s.location
            }
        }
        else if (c.name.equals("east", ignoreCase = true)) {
            if (s.isOp) {
                if (Bukkit.getPlayer(a[0]) != null) {
                    east = Bukkit.getPlayer(a[0])
                }
            }
        }
        else if (c.name.equals("west", ignoreCase = true)) {
            if (s.isOp) {
                if (Bukkit.getPlayer(a[0]) != null) {
                    west = Bukkit.getPlayer(a[0])
                }
            }
        }
        else if (c.name.equals("south", ignoreCase = true)) {
            if (s.isOp) {
                if (Bukkit.getPlayer(a[0]) != null) {
                    south = Bukkit.getPlayer(a[0])
                }
            }
        }
        else if (c.name.equals("north", ignoreCase = true)) {
            if (s.isOp) {
                if (Bukkit.getPlayer(a[0]) != null) {
                    north = Bukkit.getPlayer(a[0])
                }
            }
        }
        else if (c.name.equals("speed", ignoreCase = true)) {
            if (s.isOp) {
                try {
                    speed = a[0].toDouble()
                } catch (e: Exception) {
                }
            }
        }

        return false
    }

    private fun repeat(delay: Long = 1, task: () -> Unit) {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, task, 0, delay)
    }
    private fun delay(delay: Long = 1, task: () -> Unit) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(this, task, delay)
    }

}

val String.t : String
    get() {
        var s = this
        val rgb = Pattern.compile("#[0-9a-f]{6}").matcher(this)
        while (rgb.find()) {
            try {
                s = s.replaceFirst(rgb.group(), net.md_5.bungee.api.ChatColor.of(rgb.group()).toString())
            } catch (e: Exception) {
            }
        }
        val color = Pattern.compile("%[a-zA-Z_]*%").matcher(this)
        while (color.find()) {
            try {
                s = s.replaceFirst(
                    color.group(),
                    net.md_5.bungee.api.ChatColor.of(color.group().replace("%", "")).toString()
                )
            } catch (e: Exception) {
            }
        }
        return s
    }