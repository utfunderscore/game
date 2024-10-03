package org.readutf.game.engine.arena.store.template.impl

import com.fasterxml.jackson.core.type.TypeReference
import io.github.oshai.kotlinlogging.KotlinLogging
import org.readutf.game.engine.Game
import org.readutf.game.engine.arena.ArenaTemplate
import org.readutf.game.engine.arena.store.template.ArenaTemplateStore
import org.readutf.game.engine.arena.utils.ArenaFolder
import org.readutf.game.engine.types.Result
import java.io.File

class FileTemplateStore(
    private val workDir: File,
) : ArenaTemplateStore {
    private val logger = KotlinLogging.logger { }

    override fun save(arenaTemplate: ArenaTemplate): Result<Unit> {
        val templateFile = getTemplateFile(arenaTemplate.name)

        if (!templateFile.exists()) templateFile.createNewFile()
        if (templateFile.isDirectory) return Result.failure("Arena file is a directory")

        Game.objectMapper.writeValue(templateFile, arenaTemplate)
        return Result.empty()
    }

    override fun load(name: String): Result<ArenaTemplate> {
        val arenaFile = getTemplateFile(name)
        if (!arenaFile.exists()) return Result.failure("Could not find template file.")
        return try {
            Result.success(Game.objectMapper.readValue(arenaFile, object : TypeReference<ArenaTemplate>() {}))
        } catch (e: Throwable) {
            logger.error(e) { }
            Result.failure("Could not read template file.")
        }
    }

    private fun getTemplateFile(arenaName: String) = File(ArenaFolder.getArenaFolder(workDir, arenaName), "arena-data.json")

    override fun loadAllByGameType(gameType: String): List<ArenaTemplate> {
        val storageFolder = ArenaFolder.getStorageFolder(workDir)

        val files =
            storageFolder.list() ?: let {
                logger.error { "Could not list files in storage folder" }
                return emptyList()
            }

        return files.mapNotNull { file ->
            try {
                load(file).getOrNull()
            } catch (e: Throwable) {
                logger.error(e) { "Could not read file $file" }
                null
            }
        }
    }
}
