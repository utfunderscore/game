package org.readutf.game.minestom.arena.store.schematic.polar

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import org.readutf.game.engine.arena.utils.ArenaFolder
import java.io.File

class FilePolarStore(
    private val workDir: File,
) : PolarSchematicStore() {
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
            logger.error { "Could not find container file." }
            return Err(Exception("Could not find container file."))
        }
        return try {
            Ok(containerFile.readBytes())
        } catch (e: Throwable) {
            logger.error(e) { }
            Err(e)
        }
    }

    private fun getContainerFile(arenaId: String) = File(ArenaFolder.getArenaFolder(workDir, arenaId), "container.polar")
}
