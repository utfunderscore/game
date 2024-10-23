package org.readutf.game.engine.event.impl

import net.minestom.server.entity.Player
import net.minestom.server.entity.damage.DamageType
import net.minestom.server.registry.DynamicRegistry
import org.readutf.game.engine.GenericGame
import org.readutf.game.engine.event.GameEvent

class GameDeathEvent(
    game: GenericGame,
    val player: Player,
    val damageType: DynamicRegistry.Key<DamageType>,
) : GameEvent(game)
