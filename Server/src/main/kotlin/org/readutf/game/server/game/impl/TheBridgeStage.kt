package org.readutf.game.server.game.impl

import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.togar2.pvp.entity.projectile.AbstractArrow
import io.github.togar2.pvp.entity.projectile.Arrow
import net.minestom.server.coordinate.Vec
import net.minestom.server.entity.ItemEntity
import net.minestom.server.entity.Player
import net.minestom.server.entity.damage.DamageType
import net.minestom.server.event.entity.EntitySpawnEvent
import net.minestom.server.event.entity.projectile.ProjectileCollideWithBlockEvent
import net.minestom.server.event.player.PlayerMoveEvent
import net.minestom.server.instance.Instance
import net.minestom.server.instance.block.Block
import net.minestom.server.item.ItemComponent
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import net.minestom.server.item.component.DyedItemColor
import net.minestom.server.potion.Potion
import net.minestom.server.potion.PotionEffect
import net.minestom.server.utils.time.TimeUnit
import org.readutf.game.engine.GenericGame
import org.readutf.game.engine.event.annotation.EventListener
import org.readutf.game.engine.event.impl.GameRespawnEvent
import org.readutf.game.engine.features.*
import org.readutf.game.engine.features.combat.*
import org.readutf.game.engine.features.deaths.DeathManager
import org.readutf.game.engine.features.spectator.GameSpectateEvent
import org.readutf.game.engine.features.spectator.SpectatorManager
import org.readutf.game.engine.schedular.CountdownTask
import org.readutf.game.engine.stage.Stage
import org.readutf.game.engine.types.Result
import org.readutf.game.engine.utils.Cuboid
import org.readutf.game.engine.utils.getPlayer
import org.readutf.game.engine.utils.toComponent
import org.readutf.game.server.game.dual.stages.FightingStage
import org.readutf.game.server.game.impl.cage.CageManager
import org.readutf.game.server.game.impl.goal.GoalManager
import org.readutf.neolobby.scoreboard.ScoreboardManager
import java.time.Duration
import java.util.*

class TheBridgeStage(
    val localGame: TheBridgeGame,
    previousStage: Stage?,
    goals: Map<TheBridgeTeam, Cuboid>,
    safeZones: Map<String, Cuboid>,
) : FightingStage<TheBridgeGame>(localGame, previousStage) {
    private val logger = KotlinLogging.logger {}
    private var combatActiveAt: Long = Long.MAX_VALUE
    var hasCageDropped = false

    val spectatorManager =
        SpectatorManager(
            stage = this,
            countdownHandler =
                { player, interval ->
                    player.sendMessage("&7You will be respawned in &9$interval &7seconds".toComponent())
                },
        )

    private val cageManager = CageManager(this)
    private val goalManager = GoalManager(this, localGame, goals)

    val deathManager = DeathManager(this, spectatorManager, dropItems = false)

    init {
        localGame.round += 1

        val blockRule = TheBridgeBuildRule(this, safeZones.values)
        setBlockBreakRule(blockRule)
        setBlockPlaceRule(blockRule)
        enableItemPickup()
        disableNaturalRegen()
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
        enableKillMessage(
            localGame.enableDamageTracker(),
            TheBridgeKillMessages(localGame.settings),
        )
    }

    override fun onStart(): Result<Unit> {
        super.onStart().mapError { return it }

        game.scheduler.schedule(this, Countdown(game))
        return Result.empty()
    }

    override fun onFinish(): Result<Unit> {
        spectatorManager.shutdown()

        return Result.empty()
    }

    @EventListener
    fun onMove(e: PlayerMoveEvent) {
        val player = e.player

        val cuboid = Cuboid.fromVecs(Vec.ZERO, game.arena!!.size.asVec())

        if (!cuboid.contains(player.position) && System.currentTimeMillis() - startTime > 1000) {
            synchronized(deathManager) {
                deathManager.killPlayer(player, DamageType.OUT_OF_WORLD)
            }
        }
    }

    @EventListener
    fun onRespawn(event: GameRespawnEvent) {
        val respawnPoint = event.respawnPositionResult
        respawnPoint.position = respawnPoint.position.add(0.0, 1.0, 0.0)

        val player = event.player

        player.isAllowFlying = false
        player.isFlying = false

        player.heal()

        player.clearEffects()

        combatActiveAt = System.currentTimeMillis() + (1000 * 3)

        localGame.kit.items.forEachIndexed { index, itemStack ->
            val team = game.getTeam(player.uuid)!! as TheBridgeTeam
            var actual = itemStack

            if (itemStack.material() == Material.TERRACOTTA) {
                val material =
                    if (team.teamName.equals("red", true)) {
                        Material.RED_TERRACOTTA
                    } else {
                        Material.BLUE_TERRACOTTA
                    }
                actual = itemStack.withMaterial(material)
            } else if (itemStack.material().isArmor) {
                actual = itemStack.with(ItemComponent.DYED_COLOR, DyedItemColor(team.textColor))
            }

            player.inventory.setItemStack(index, actual)
        }

        if (!cageManager.hasSpawned(player)) {
            cageManager.generateCage(player, respawnPoint.position)
        }
    }

    @EventListener
    fun onSpectate(e: GameSpectateEvent) {
        val player = e.player

        e.respawnTime = 2
        player.heal()

        e.respawnLocation = localGame.damageTracker
            .getLastDamager(player)
            .getPlayer()
            ?.position ?: player.position

        player.inventory.clear()
        player.addEffect(Potion(PotionEffect.INVISIBILITY, 1.toByte(), 10000))
    }

    @EventListener
    fun onProjectileLaunch(e: EntitySpawnEvent) {
        val arrow = e.entity
        if (arrow !is Arrow) return
        val shooter = arrow.shooter
        if (shooter !is Player) return

        arrow.pickupMode = AbstractArrow.PickupMode.DISALLOWED

        game.scheduler.schedule(this, 3000) {
            if (spectatorManager.isSpectator(shooter)) return@schedule

            val item = ItemStack.of(Material.ARROW)
            val itemEntity = ItemEntity(item)
            itemEntity.setPickupDelay(0, TimeUnit.SERVER_TICK) // Default 0.5 seconds
            itemEntity.setInstance(
                Objects.requireNonNull<Instance>(shooter.instance),
                shooter.getPosition().add(0.0, 0.1, 0.0),
            )
        }
    }

    @EventListener
    fun onProjectileCollide(e: ProjectileCollideWithBlockEvent) {
        e.entity.scheduleRemove(Duration.ofMillis(500))
    }

    override fun toString(): String = "TheBridgeStage ${localGame.round}"

    inner class Countdown(
        game: GenericGame,
    ) : CountdownTask(game, 5, listOf(5, 4, 3, 2, 1)) {
        override fun onInterval(interval: Int) {
            if (interval == 0) {
                cageManager.clearCages(game.arena!!.instance)
                combatActiveAt = System.currentTimeMillis() + (1000 * 3)
                hasCageDropped = true
            } else {
                game.messageAll("&7Round ${localGame.round} starting in &9$interval &7seconds".toComponent())
            }
        }
    }
}
