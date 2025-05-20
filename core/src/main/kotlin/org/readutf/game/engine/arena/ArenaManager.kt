package org.readutf.game.engine.arena

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.getOrElse
import io.github.oshai.kotlinlogging.KotlinLogging
import org.readutf.buildformat.common.Build
import org.readutf.buildformat.common.BuildManager
import org.readutf.buildformat.common.format.BuildFormat
import org.readutf.buildformat.common.format.BuildFormatManager
import java.util.UUID
import kotlin.reflect.KClass

class ArenaManager(
    private val buildManager: BuildManager,
    private val arenaPlatform: ArenaPlatform,
) {

    private val logger = KotlinLogging.logger { }

    inline fun <reified T : BuildFormat> loadArena(name: String): Result<Arena<T>?, Throwable> = loadArena(name, T::class)

    fun <T : BuildFormat> loadArena(name: String, kClass: KClass<T>): Result<Arena<T>?, Throwable> {
        val build: Build = buildManager.getBuild(name) ?: return Ok(null)
        val buildId = UUID.randomUUID()

        val (world, origin, markers) = arenaPlatform.placeBuild(buildId, build).getOrElse { return Err(it) }
        val format = BuildFormatManager.constructBuildFormat(markers, kClass.java)

        return Ok(
            Arena(
                buildId = buildId,
                build = build,
                instance = world,
                positionSettings = format,
                positions = markers,
                freeFunc = { arenaPlatform.freeArena(it) },
            ),
        )
    }

    inline fun <reified T : BuildFormat> getByFormat(format: String): List<String> = getByFormat(format, T::class)

    fun <T : BuildFormat> getByFormat(format: String, kClass: KClass<T>): List<String> {
        val buildsByFormat = buildManager.getBuildsByFormat(format)

        val requirements = BuildFormatManager.getValidators(kClass.java)
        val targetChecksum = BuildFormatManager.generateChecksum(requirements)

        val builds = mutableListOf<String>()

        for ((build, checksum) in buildsByFormat) {
            if (checksum == targetChecksum) {
                builds.add(build)
            } else {
                logger.info { "Invalid checksum received for $build" }
            }
        }

        return builds
    }
}
