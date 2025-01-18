package org.readutf.game.server.game.impl

import net.kyori.adventure.text.format.TextColor
import org.readutf.game.engine.team.GameTeam
import java.util.UUID

class TheBridgeTeam(
    gameName: String,
    val textColor: TextColor,
    players: MutableList<UUID> = mutableListOf(),
) : GameTeam(gameName, players)
