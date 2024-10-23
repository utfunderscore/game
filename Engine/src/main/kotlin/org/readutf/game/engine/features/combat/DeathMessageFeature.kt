package org.readutf.game.engine.features.combat

import net.kyori.adventure.text.TextComponent
import net.minestom.server.MinecraftServer
import net.minestom.server.entity.Player
import net.minestom.server.entity.damage.DamageType
import net.minestom.server.registry.DynamicRegistry
import org.readutf.game.engine.event.impl.GameDeathEvent
import org.readutf.game.engine.stage.Stage

fun interface KillMessageSupplier {
    fun getKillMessage(
        attacker: Player?,
        victim: Player,
        damage: DynamicRegistry.Key<DamageType>?,
    ): TextComponent
}

fun Stage.enableKillMessage(
    damageTracker: DamageTracker,
    killMessageSupplier: KillMessageSupplier,
) {
    registerListener<GameDeathEvent> { e ->
        val target = e.player

        val lastDamager = damageTracker.getLastDamager(target)?.let { MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(it) }

        e.game.messageAll(killMessageSupplier.getKillMessage(lastDamager, target, e.damageType))
    }
}
