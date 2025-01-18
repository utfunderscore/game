package org.readutf.arena.minestom.arena.store.polar.impl

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import org.readutf.arena.minestom.arena.store.polar.PolarSchematicStore
import org.readutf.game.engine.utils.SResult
import java.io.File

class FilePolarStore(
    private val arenasDirectory: File,
) : PolarSchematicStore() {
    override fun saveData(
        arenaId: String,
        data: ByteArray,
    ): SResult<Unit> = try {
        val containerFile = getPolarFile(arenaId)
        if (!containerFile.exists()) containerFile.createNewFile()
        containerFile.writeBytes(data)
        Ok(Unit)
    } catch (e: Throwable) {
        logger.error(e) { }
        Err("Failed to read polar file: ${e.message}")
    }

    override fun loadData(arenaId: String): SResult<ByteArray> {
        val containerFile = getPolarFile(arenaId)
        if (!containerFile.exists()) return Err("Could not find container file.")
        return try {
            Ok(containerFile.readBytes())
        } catch (e: Throwable) {
            logger.error(e) { }
            Err("(Polar) Could not read container file.")
        }
    }

    private fun getPolarFile(arenaName: String): File {
        val arenaFolder = File(arenasDirectory, arenaName)
        if (!arenaFolder.exists()) arenaFolder.mkdirs()
        return File(arenaFolder, "container.polar")
    }
}
