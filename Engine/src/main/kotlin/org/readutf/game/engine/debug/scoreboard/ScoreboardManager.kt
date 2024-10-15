package org.readutf.neolobby.scoreboard

import io.github.oshai.kotlinlogging.KotlinLogging
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.minestom.server.MinecraftServer
import net.minestom.server.entity.Player
import net.minestom.server.scoreboard.Sidebar
import net.minestom.server.timer.TaskSchedule
import java.util.UUID

object ScoreboardManager {
    private val boardTracker = LinkedHashMap<UUID, Scoreboard>()
    private val sidebarTracker = LinkedHashMap<UUID, Sidebar>()
    private val titleTracker = LinkedHashMap<UUID, Component>()

    private val logger = KotlinLogging.logger { }

    init {
        scheduleUpdateTask()
    }

    fun setScoreboard(
        player: Player,
        board: Scoreboard,
    ) {
        boardTracker[player.identity().uuid()] = board
    }

    private fun scheduleUpdateTask() {
        logger.info { "Scheduling scoreboard update task" }

        MinecraftServer.getSchedulerManager().scheduleTask(
            {
                LinkedHashMap(boardTracker).forEach { (uuid, board) ->
                    val player = MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(uuid)

                    if (player != null) {
                        try {
                            val sidebar = getSidebar(player, board.getTitle(player))
                            updateScoreboard(
                                sidebar,
                                board.getTitle(player),
                                board.getLines(player).reversed(),
                            )
                        } catch (e: Exception) {
                            logger.error(e) { "Failed to update scoreboard for player ${player.username}" }
                            removeBoard(player)
                        }
                    } else {
                        removeBoard(uuid)
                    }
                }
            },
            TaskSchedule.seconds(0),
            TaskSchedule.seconds(1),
        )
    }

    private fun getSidebar(
        player: Player,
        currentTitle: Component,
    ): Sidebar {
        val sidebar =
            sidebarTracker.getOrPut(player.identity().uuid()) {
                val sidebar = Sidebar(currentTitle)
                sidebar.addViewer(player)
                return@getOrPut sidebar
            }
        return sidebar
    }

    fun removeBoard(playerId: UUID): Sidebar? = sidebarTracker.remove(playerId)

    fun removeBoard(player: Player) {
        val sidebar = removeBoard(player.identity().uuid())
        sidebar?.removeViewer(player)
    }

    fun updateScoreboard(
        sidebar: Sidebar,
        title: Component,
        lines: List<Component>,
    ) {
        val previousTitle =
            titleTracker.getOrPut(
                sidebar.viewers
                    .first()
                    .identity()
                    .uuid(),
            ) { title }

        if (previousTitle != title) {
            sidebar.setTitle(title)
            titleTracker[
                sidebar.viewers
                    .first()
                    .identity()
                    .uuid(),
            ] = title
        }

        for (i in lines.indices) {
            val lineId = "line-$i"

            if (i >= sidebar.lines.size) {
                sidebar.createLine(
                    Sidebar.ScoreboardLine(
                        lineId,
                        lines[i],
                        i,
                    ),
                )
                continue
            }

            val currentLine = sidebar.getLine(lineId)

            if (currentLine == null || !toLegacy(currentLine.content).equals(toLegacy(lines[i]), false)) {
                println(sidebar.viewers.map { it.username })
                sidebar.updateLineContent(
                    lineId,
                    lines[i],
                )
            }
        }
    }

    private val legacy = LegacyComponentSerializer.legacy('&')

    fun toLegacy(component: Component): String = legacy.serialize(component)
}
