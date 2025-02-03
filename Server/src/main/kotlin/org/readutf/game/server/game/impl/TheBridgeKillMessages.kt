package org.readutf.game.server.game.impl

import net.kyori.adventure.text.TextComponent
import net.minestom.server.entity.Player
import net.minestom.server.entity.damage.DamageType
import net.minestom.server.registry.DynamicRegistry
import org.readutf.game.engine.features.combat.KillMessageData
import org.readutf.game.engine.features.combat.KillMessageSupplier
import org.readutf.game.engine.utils.toComponent
import org.readutf.game.server.game.impl.settings.TheBridgeSettings

class TheBridgeKillMessages(
    theBridgeSettings: TheBridgeSettings,
) : KillMessageSupplier {
    val killMessages = theBridgeSettings.killMessagesData

    val defaultMessage =
        killMessages.getOrDefault(
            "default",
            KillMessageData(
                "&e{player} &f was killed by &e{attacker}",
                "&e{player} &f was killed",
            ),
        )

    override fun getKillMessage(
        attacker: Player?,
        victim: Player,
        damageType: DynamicRegistry.Key<DamageType>?,
    ): TextComponent {
        val killMessage =
            if (damageType == null) {
                defaultMessage
            } else {
                killMessages[damageType.name().lowercase()] ?: defaultMessage
            }

        return formatMessage(killMessage, attacker, victim)
    }

    fun formatMessage(
        killMessageData: KillMessageData,
        attacker: Player?,
        victim: Player,
    ): TextComponent =
        if (attacker == null) {
            killMessageData.generic
                .replace("{player}", victim.username)
                .toComponent()
        } else {
            killMessageData.byPlayer
                .replace("{player}", victim.username)
                .replace("{attacker}", attacker.username)
                .toComponent()
        }
}
