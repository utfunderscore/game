package org.readutf.game.server.game.impl

import io.github.oshai.kotlinlogging.KotlinLogging
import net.kyori.adventure.title.Title
import net.minestom.server.coordinate.Vec
import net.minestom.server.entity.Player
import net.minestom.server.event.player.PlayerMoveEvent
import net.minestom.server.instance.block.Block
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import org.readutf.game.engine.GenericGame
import org.readutf.game.engine.event.annotation.EventListener
import org.readutf.game.engine.event.impl.GameRespawnEvent
import org.readutf.game.engine.features.*
import org.readutf.game.engine.features.deaths.DeathManager
import org.readutf.game.engine.features.spectator.GameSpectateEvent
import org.readutf.game.engine.features.spectator.SpectatorManager
import org.readutf.game.engine.schedular.CountdownTask
import org.readutf.game.engine.stage.Stage
import org.readutf.game.engine.team.GameTeam
import org.readutf.game.engine.types.Result
import org.readutf.game.engine.types.toSuccess
import org.readutf.game.engine.utils.Cuboid
import org.readutf.game.engine.utils.toComponent
import org.readutf.game.server.game.dual.stages.FightingStage
import org.readutf.game.server.game.impl.cage.CageManager
import org.readutf.neolobby.scoreboard.ScoreboardManager
import java.util.UUID

class TheBridgeStage(
    val localGame: TheBridgeGame,
    previousStage: Stage?,
) : FightingStage(localGame, previousStage) {
    private val logger = KotlinLogging.logger {}
    private val cageManager = CageManager(this)
    private var combatActiveAt: Long = Long.MAX_VALUE

    private val goals = mutableMapOf<UUID, List<TheBridgeGoal>>()

    val spectatorManager =
        SpectatorManager(
            stage = this,
            countdownHandler =
                { player, interval ->
                    player.sendMessage("&7You will be respawned in &9$interval &7seconds".toComponent())
                },
        )

    val deathManager = DeathManager(this, spectatorManager, dropItems = false)

    init {
        setBlockBreakRule(TheBridgeBuildRule)
        setBlockPlaceRule(TheBridgeBuildRule)
        enableItemPickup()
        setFoodLossRule { false }
        setDamageRule { System.currentTimeMillis() > combatActiveAt }
        dropItemOnBlockBreak {
            when (it) {
                Block.RED_TERRACOTTA -> ItemStack.of(Material.RED_TERRACOTTA)
                Block.BLUE_TERRACOTTA -> ItemStack.of(Material.BLUE_TERRACOTTA)
                else -> ItemStack.AIR
            }
        }

        game.getOnlinePlayers().forEach {
            ScoreboardManager.setScoreboard(
                it,
                TheBridgeScoreboard(localGame),
            )
        }
    }

    fun scoreGoal(
        scorer: Player,
        goalTeam: GameTeam,
    ) {
        localGame.registerStage(
            { _, previousStage -> TheBridgeStage(localGame, previousStage).toSuccess() },
        )

        val scorerTeam: GameTeam = game.getTeam(scorer.uuid)!!

        scorer.showTitle(
            Title.title(
                "".toComponent(),
                "".toComponent(),
            ),
        )

        endStage().onFailure { game.crash(it) }
    }

    override fun onStart(): Result<Unit> {
        super.onStart().mapError { return it }

        localGame.scheduler.schedule(this, Countdown(localGame))
        return Result.empty()
    }

    @EventListener
    fun onSpectate(gameSpectateEvent: GameSpectateEvent) {
        val player = gameSpectateEvent.player

        player.isAllowFlying = true
        player.isFlying = true

        combatActiveAt = System.currentTimeMillis() + (1000 * 1)
    }

    @EventListener
    fun onMove(e: PlayerMoveEvent) {
        val player = e.player

        val cuboid = Cuboid.fromVecs(Vec.ZERO, game.arena!!.size.asVec())

        if (!cuboid.contains(player.position)) {
            deathManager.killPlayer(player)
        }
    }

    @EventListener
    fun onRespawn(event: GameRespawnEvent) {
        val respawnPoint = event.respawnPositionResult
        respawnPoint.position = respawnPoint.position.add(0.0, 1.0, 0.0)

        val player = event.player

        player.isAllowFlying = false
        player.isFlying = false

        localGame.kit.items.forEachIndexed { index, itemStack ->

            val team = game.getTeam(player.uuid)!!

            var actual = itemStack

            if (itemStack.material() == Material.TERRACOTTA) {
                val material =
                    if (team.gameName.equals("red", true)) {
                        Material.RED_TERRACOTTA
                    } else {
                        Material.BLUE_TERRACOTTA
                    }

                actual = itemStack.withMaterial(material)
            }

            player.inventory.setItemStack(index, actual)
        }

        if (!cageManager.hasSpawned(player)) {
            cageManager.generateCage(player, respawnPoint.position)
        }
    }

    inner class Countdown(
        game: GenericGame,
    ) : CountdownTask(game, 5, listOf(5, 4, 3, 2, 1)) {
        override fun onInterval(interval: Int) {
            if (interval == 0) {
                cageManager.clearCages(game.arena!!.instance)
                combatActiveAt = System.currentTimeMillis() + (1000 * 3)
            } else {
                game.messageAll("&7Game starting in &9$interval &7seconds".toComponent())
            }
        }
    }
}
