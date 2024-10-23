package org.readutf.game.server.game.impl.goal

import io.github.oshai.kotlinlogging.KotlinLogging
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.title.Title
import net.minestom.server.entity.Player
import net.minestom.server.event.player.PlayerMoveEvent
import net.minestom.server.instance.block.Block
import org.readutf.game.engine.event.annotation.EventListener
import org.readutf.game.engine.team.GameTeam
import org.readutf.game.engine.utils.Cuboid
import org.readutf.game.server.game.impl.TheBridgeGame
import org.readutf.game.server.game.impl.TheBridgeStage
import org.readutf.game.server.game.impl.TheBridgeTeam

class GoalManager(
    val stage: TheBridgeStage,
    private val game: TheBridgeGame,
    private val goals: Map<TheBridgeTeam, Cuboid>,
) {
    private val logger = KotlinLogging.logger {}
    private val arena = game.arena!!

    init {
        stage.registerAll(this)

        logger.info { "Goals: $goals" }

        goals.forEach { (name, cuboid) ->
            logger.info { "Generating goal for $name" }
            cuboid.getBlocks().forEach { block ->
                arena.instance.setBlock(block, Block.END_PORTAL)
            }
        }
    }

    fun scoreGoal(
        scorer: Player,
        goalTeam: GameTeam,
    ) {
        logger.info { "${scorer.name} scored a goal for ${goalTeam.teamName}" }

        val scorerTeam = game.getTeam(scorer.uuid)!! as TheBridgeTeam

        game.decreaseHealth(goalTeam)
        game.scoredGoal(scorer)

        // Send the title to your audience
        game.getOnlinePlayers().forEach { player ->

            val selfTeam = game.getTeam(player.uuid) as TheBridgeTeam

            val title =
                Title.title(
                    Component.text("${scorer.username} scored!", scorerTeam.textColor),
                    generateScoreLine(scorerTeam),
                )

            player.showTitle(title)
        }

        stage.endStage().onFailure { game.crash(it) }
    }

    @EventListener
    fun onMove(e: PlayerMoveEvent) {
        val player = e.player

        if (stage.spectatorManager.isSpectator(player)) {
            return
        }

        val team = game.getTeam(player.uuid) ?: return

        goals.forEach { (goalTeam, cuboid) ->
            if (cuboid.contains(player.position)) {
                if (goalTeam == team) return

                logger.info { "${player.name} scored a goal for $goalTeam.teamName" }

                scoreGoal(player, goalTeam)
            }
        }
    }

    /**
     * Generates the line bottom line in the goal score title
     * in the format of Self - Opponent1 - Opponent2 - Opponent3
     */
    fun generateScoreLine(selfTeam: TheBridgeTeam): TextComponent {
        val selfScore = game.getTeamHealth(selfTeam)

        var base =
            Component
                .text()
                .append(Component.text(selfScore).color(NamedTextColor.YELLOW))

        game
            .getTeams()
            .asSequence()
            .map { team -> team as TheBridgeTeam }
            .filter { team -> team != selfTeam }
            .forEach { team ->
                val score = game.getTeamHealth(team)
                base =
                    base
                        .append(Component.text(" - ").color(NamedTextColor.GRAY))
                        .append(Component.text(score).color(team.textColor))
            }

        return base.build()
    }
}
