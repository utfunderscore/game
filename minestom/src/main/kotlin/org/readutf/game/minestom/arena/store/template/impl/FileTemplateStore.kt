package org.readutf.game.minestom.arena.store.template.impl

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.getOr
import io.github.oshai.kotlinlogging.KotlinLogging
import org.readutf.game.engine.arena.ArenaTemplate
import org.readutf.game.engine.arena.utils.ArenaFolder
import org.readutf.game.minestom.arena.store.template.ArenaTemplateStore
import java.io.File

class FileTemplateStore(
    private val workDir: File,
) : ArenaTemplateStore {
    private val objectMapper = ObjectMapper().registerKotlinModule()

    private val logger = KotlinLogging.logger { }

    override fun save(arenaTemplate: ArenaTemplate): Result<Unit, Throwable> {
        val templateFile = getTemplateFile(arenaTemplate.name)

        if (!templateFile.exists()) templateFile.createNewFile()
        if (templateFile.isDirectory) {
            logger.error { "Arena file is a directory" }
            return Err(Exception("Arena file is a directory"))
        }

        objectMapper.writerWithDefaultPrettyPrinter().writeValue(templateFile, arenaTemplate)
        return Ok(Unit)
    }

    override fun load(name: String): Result<ArenaTemplate, Throwable> {
        val arenaFile = getTemplateFile(name)
        if (!arenaFile.exists()) {
            logger.error { "Could not find template file." }
            return Err(Exception("Could not find template file."))
        }
        return try {
            Ok(objectMapper.readValue(arenaFile, object : TypeReference<ArenaTemplate>() {}))
        } catch (e: Throwable) {
            logger.error(e) { "Failed to load arena template" }
            Err(e)
        }
    }

    private fun getTemplateFile(arenaName: String) = File(ArenaFolder.getArenaFolder(workDir, arenaName), "arena-data.json")

    override fun findByGameType(gameType: String): List<ArenaTemplate> {
        val storageFolder = ArenaFolder.getStorageFolder(workDir)

        val files =
            storageFolder.list() ?: let {
                logger.error { "Could not list files in storage folder" }
                return emptyList()
            }

        return files.mapNotNull { file ->
            try {
                load(file).getOr(null)
            } catch (e: Throwable) {
                logger.error(e) { "Could not read file $file" }
                null
            }
        }
    }
}
