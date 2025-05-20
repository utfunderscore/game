package org.readutf.game.minestom.arena

import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import net.hollowcube.schem.reader.SpongeSchematicReader
import net.kyori.adventure.text.Component
import net.minestom.server.MinecraftServer
import net.minestom.server.coordinate.Pos
import net.minestom.server.instance.Instance
import org.readutf.buildformat.common.Build
import org.readutf.buildformat.common.markers.Position
import org.readutf.game.engine.arena.Arena
import org.readutf.game.engine.arena.ArenaPlatform
import org.readutf.game.engine.arena.BuildPlacement
import org.readutf.game.minestom.platform.MinestomWorld
import java.util.UUID

class MinestomArenaPlatform : ArenaPlatform {

    private val instances = mutableMapOf<UUID, Instance>()

    override fun placeBuild(buildId: UUID, build: Build): Result<BuildPlacement, Throwable> {
        val instance = MinecraftServer.getInstanceManager().createInstanceContainer()

        val schematic = SpongeSchematicReader().read(build.buildSchematic.buildData)

        schematic.createBatch().apply(instance, Pos.ZERO) {
        }

        instances[buildId] = instance

        return Ok(
            BuildPlacement(
                world = MinestomWorld(instance),
                origin = Position.ZERO,
                markers = emptyList(),
            ),
        )
    }

    override fun freeArena(arena: Arena<*>): Result<Unit, Throwable> {
        instances[arena.buildId]?.let { instance ->
            instance.players.forEach { it.kick(Component.empty()) }

            MinecraftServer.getInstanceManager().unregisterInstance(instance)
        }
        return Ok(Unit)
    }
}
