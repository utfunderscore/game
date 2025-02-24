package org.readutf.game.minestom.arena.store.schematic.polar

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import org.readutf.game.engine.arena.utils.ArenaFolder
import org.readutf.game.engine.utils.SResult
import java.io.File

class FilePolarStore(
    private val workDir: File,
) : PolarSchematicStore() {
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
        Err("Failed to read polar file: ${e.message}")
    }

    override fun loadData(arenaId: String): SResult<ByteArray> {
        val containerFile = getContainerFile(arenaId)
        if (!containerFile.exists()) {
            logger.error { "Could not find container file." }
            return Err("Could not find container file.")
        }
        return try {
            Ok(containerFile.readBytes())
        } catch (e: Throwable) {
            logger.error(e) { }
            Err("(Polar) Could not read container file.")
        }
    }

    private fun getContainerFile(arenaId: String) = File(ArenaFolder.getArenaFolder(workDir, arenaId), "container.polar")
}
