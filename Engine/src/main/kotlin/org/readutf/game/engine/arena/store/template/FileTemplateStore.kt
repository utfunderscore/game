package org.readutf.game.engine.arena.store.template

import com.fasterxml.jackson.core.type.TypeReference
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.getOrElse
import io.github.oshai.kotlinlogging.KotlinLogging
import org.readutf.game.engine.Game
import org.readutf.game.engine.arena.ArenaTemplate
import org.readutf.game.engine.arena.store.ArenaTemplateStore
import org.readutf.game.engine.utils.SResult
import java.io.File

class FileTemplateStore(
    private val arenasDirectory: File,
) : ArenaTemplateStore {
    private val logger = KotlinLogging.logger { }

    override fun save(arenaTemplate: ArenaTemplate): SResult<Unit> {
        val templateFile = getTemplateFile(arenaTemplate.name)

        if (!templateFile.exists()) templateFile.createNewFile()
        if (templateFile.isDirectory) return Err("Arena file is a directory")

        Game.objectMapper.writeValue(templateFile, arenaTemplate)
        return Ok(Unit)
    }

    override fun load(name: String): SResult<ArenaTemplate> {
        val arenaFile = getTemplateFile(name)
        if (!arenaFile.exists()) return Err("Could not find template file.")
        return try {
            Ok(Game.objectMapper.readValue(arenaFile, object : TypeReference<ArenaTemplate>() {}))
        } catch (e: Throwable) {
            logger.error(e) { }
            Err("Could not read template file.")
        }
    }

    private fun getTemplateFile(arenaName: String): File {
        val arenaFolder = File(arenasDirectory, arenaName)
        if (!arenaFolder.exists()) arenaFolder.mkdirs()
        return File(arenaFolder, "template.json")
    }

    override fun loadAllByGameType(gameType: String): List<ArenaTemplate> {
        val storageFolder = File(arenasDirectory, gameType)

        val files =
            storageFolder.list() ?: let {
                logger.error { "Could not list files in storage folder" }
                return emptyList()
            }

        return files.mapNotNull { load(it).getOrElse { null } }
    }
}
