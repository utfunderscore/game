package org.readutf.game.minestom.arena.store.schematic.schem

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.github.oshai.kotlinlogging.KotlinLogging
import org.readutf.game.engine.arena.utils.ArenaFolder
import org.readutf.game.engine.utils.SResult
import java.io.File

class FileSchematicStore(
    private val workDir: File,
) : RawSchematicStore() {
    private val logger = KotlinLogging.logger { }

    override fun saveData(
        arenaId: String,
        data: ByteArray,
    ): SResult<Unit> = try {
        val containerFile = getContainerFile(arenaId)
        if (!containerFile.exists()) containerFile.createNewFile()
        containerFile.writeBytes(data)
        Ok(Unit)
    } catch (e: Throwable) {
        logger.error(e) { }
        Err("Failed to save schematic file: ${e.message}")
    }

    override fun loadData(arenaId: String): SResult<ByteArray> {
        val containerFile = getContainerFile(arenaId)
        if (!containerFile.exists()) {
            logger.error { "Container file is missing." }
            return Err("Container file is missing.")
        }
        return try {
            Ok(containerFile.readBytes())
        } catch (e: Throwable) {
            logger.error(e) { }
            Err("Could not read container file.")
        }
    }

    private fun getContainerFile(arenaId: String) = File(ArenaFolder.getArenaFolder(workDir, arenaId), "container.schem")
}
