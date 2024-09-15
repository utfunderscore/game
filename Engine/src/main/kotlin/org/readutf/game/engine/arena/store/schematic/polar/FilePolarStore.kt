package org.readutf.game.engine.arena.store.schematic.polar

import org.readutf.game.engine.arena.utils.ArenaFolder
import org.readutf.game.engine.types.Result
import java.io.File

class FilePolarStore(
    val workDir: File,
) : PolarSchematicStore() {
    override fun saveData(
        arenaId: String,
        data: ByteArray,
    ): Result<Unit> =
        try {
            val containerFile = getContainerFile(arenaId)
            if (!containerFile.exists()) containerFile.createNewFile()
            containerFile.writeBytes(data)
            Result.empty()
        } catch (e: Throwable) {
            Result.failure(e.message ?: "null")
        }

    override fun loadData(arenaId: String): Result<ByteArray> {
        val containerFile = getContainerFile(arenaId)
        if (!containerFile.exists()) return Result.failure("Could not find container file.")
        return try {
            Result.success(containerFile.readBytes())
        } catch (e: Throwable) {
            Result.failure(e.message ?: "null")
        }
    }

    private fun getContainerFile(arenaId: String) = File(ArenaFolder.getArenaFolder(workDir, arenaId), "container.polar")
}
