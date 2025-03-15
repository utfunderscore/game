package org.readutf.game.minestom.arena.store.schematic.schem

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import io.github.oshai.kotlinlogging.KotlinLogging
import org.readutf.game.engine.arena.utils.ArenaFolder
import java.io.File

class FileSchematicStore(
    private val workDir: File,
) : RawSchematicStore() {
    private val logger = KotlinLogging.logger { }

    override fun saveData(
        arenaId: String,
        data: ByteArray,
    ): Result<Unit, Throwable> = try {
        val containerFile = getContainerFile(arenaId)
        if (!containerFile.exists()) containerFile.createNewFile()
        containerFile.writeBytes(data)
        Ok(Unit)
    } catch (e: Throwable) {
        logger.error(e) { }
        Err(e)
    }

    override fun loadData(arenaId: String): Result<ByteArray, Throwable> {
        val containerFile = getContainerFile(arenaId)
        if (!containerFile.exists()) {
            logger.error { "Container file is missing." }
            return Err(Exception("Container file is missing."))
        }
        return try {
            Ok(containerFile.readBytes())
        } catch (e: Throwable) {
            logger.error(e) { }
            Err(e)
        }
    }

    private fun getContainerFile(arenaId: String) = File(ArenaFolder.getArenaFolder(workDir, arenaId), "container.schem")
}
