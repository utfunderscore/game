package org.readutf.game.minestom.platform.scoreboard

import net.kyori.adventure.text.Component
import net.minestom.server.entity.Player
import net.minestom.server.scoreboard.Sidebar
import org.readutf.engine.features.scoreboard.Scoreboard
import org.readutf.engine.features.scoreboard.ScoreboardFeature
import org.readutf.game.minestom.utils.toPlayer
import java.util.UUID

typealias MinestomScoreboard = Scoreboard<Player>

object MinestomScoreboardFeature : ScoreboardFeature<Player>() {
    private val previousLines = mutableMapOf<UUID, List<Component>>()
    private val previousTitle = mutableMapOf<UUID, Component>()
    private val nativeSidebar = mutableMapOf<UUID, Sidebar>()

    override fun refreshScoreboard(
        playerId: UUID,
        scoreboard: Scoreboard<Player>,
    ) {
        val player = playerId.toPlayer() ?: return
        val newTitle = scoreboard.getTitle(player)

        val sideBar =
            nativeSidebar[playerId] ?: let {
                val newSideBar = Sidebar(newTitle)
                newSideBar.addViewer(player)
                nativeSidebar[playerId] = newSideBar
                return
            }

        val previousTitle = previousTitle.getOrPut(player.uuid) { Component.empty() }
        if (previousTitle != newTitle) {
            sideBar.setTitle(newTitle)
        }

        val newLines = scoreboard.getLines(player)
        val previousLines = previousLines.getOrPut(player.uuid) { emptyList() }

        if (newLines.size == previousLines.size && newLines.zip(previousLines).all { (line1, line2) -> line1 == line2 }) {
            // No need to update the scoreboard
            return
        }

        val newLinesSize = newLines.size

        newLines.forEachIndexed { index, line ->
            if (sideBar.getLine(index.toString()) == null) {
                sideBar.createLine(Sidebar.ScoreboardLine(index.toString(), line, newLinesSize - index))
                return@forEachIndexed
            }

            sideBar.updateLineContent(index.toString(), line)
        }
    }

    override fun shutdown() {
        super.shutdown()
        for (sidebar in nativeSidebar.values) {
            for (player in sidebar.viewers.toList()) {
                sidebar.removeViewer(player)
            }
        }
        nativeSidebar.clear()
    }
}
