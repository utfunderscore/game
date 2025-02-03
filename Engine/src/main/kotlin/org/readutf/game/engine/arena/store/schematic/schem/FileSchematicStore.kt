package org.readutf.game.engine.arena.store.schematic.schem

// import result
import io.github.oshai.kotlinlogging.KotlinLogging
import org.readutf.game.engine.arena.utils.ArenaFolder
import org.readutf.game.engine.types.Result
import java.io.File

class FileSchematicStore(
    private val workDir: File,
) : RawSchematicStore() {
    private val logger = KotlinLogging.logger { }

    override fun saveData(
        arenaId: String,
        data: ByteArray,
    ): org.readutf.game.engine.types.Result<Unit> =
        try {
            val containerFile = getContainerFile(arenaId)
            if (!containerFile.exists()) containerFile.createNewFile()
            containerFile.writeBytes(data)
            Result.empty()
        } catch (e: Throwable) {
            logger.error(e) { }
            Result.failure("Failed to save schematic file: ${e.message}")
        }

    override fun loadData(arenaId: String): Result<ByteArray> {
        val containerFile = getContainerFile(arenaId)
        if (!containerFile.exists()) return Result.failure("Container file is missing.")
        return try {
            Result.success(containerFile.readBytes())
        } catch (e: Throwable) {
            logger.error(e) { }
            Result.failure("(Schem) Could not read container file.")
        }
    }

    private fun getContainerFile(arenaId: String) = File(ArenaFolder.getArenaFolder(workDir, arenaId), "container.schem")
}
