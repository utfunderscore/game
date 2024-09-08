package org.readutf.game.engine.arena.store.impl

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.readutf.game.engine.arena.ArenaTemplate
import org.readutf.game.engine.arena.store.ArenaTemplateStore
import org.readutf.game.engine.types.Result
import java.io.File

class FileTemplateStore(
    workDir: File,
) : ArenaTemplateStore {
    private val objectMapper = ObjectMapper()

    private val templatesFolder = File(workDir, "templates")

    init {
        if (!templatesFolder.isDirectory) templatesFolder.mkdir()
    }

    override fun saveArenaTemplate(arenaTemplate: ArenaTemplate): Result<Unit> {
        val arenaFile = File(templatesFolder, "${arenaTemplate.name}.json")
        if (!arenaFile.exists()) arenaFile.createNewFile()
        if (arenaFile.isDirectory) return Result.failure("Arena file is a directory")

        objectMapper.writeValue(arenaFile, arenaTemplate)
        return Result.empty()
    }

    override fun loadArenaTemplate(name: String): Result<ArenaTemplate> {
        val arenaFile = File(templatesFolder, "$name.json")
        if (!arenaFile.exists()) return Result.failure("Could not find template file.")
        return try {
            Result.success(objectMapper.readValue(arenaFile, object : TypeReference<ArenaTemplate>() {}))
        } catch (e: Throwable) {
            Result.failure(e.message ?: "null")
        }
    }

    override fun loadAllByGameType(gameType: String): Result<List<ArenaTemplate>> {
        val files = templatesFolder.listFiles() ?: return Result.success(emptyList())

        return Result.success(
            files
                .map {
                    loadArenaTemplate(it.name)
                }.filter { it.isSuccess }
                .map {
                    it.getValue()
                }.filter { it.supportedGames.contains(gameType) },
        )
    }
}
