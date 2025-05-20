package org.readutf.game.engine.arena

import com.github.michaelbull.result.Result
import org.readutf.buildformat.common.Build
import java.util.UUID

interface ArenaPlatform {

    /**
     * Places the build into a world, and returns the origin
     */
    fun placeBuild(buildId: UUID, build: Build): Result<BuildPlacement, Throwable>

    fun freeArena(arena: Arena<*>): Result<Unit, Throwable>
}
