package org.readutf.game.engine.settings.store.impl

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import org.readutf.game.engine.settings.store.GameSettingsStore
import java.io.File

class YamlSettingsStore(
    private val baseDir: File,
) : GameSettingsStore {
    private val yamlMapper =
        ObjectMapper(JsonFactory()).registerModules(kotlinModule())

    override fun <T> save(
        gameType: String,
        any: T,
    ) {
        val gameFolder = File(baseDir, gameType)
        gameFolder.mkdirs()
        val gameTypeFolder = File(gameFolder, "gamedata.json")

        yamlMapper.writeValue(gameTypeFolder, any)
    }

    override fun <T> load(
        gameType: String,
        clazz: Class<T>,
    ): T {
        val gameFolder = File(baseDir, gameType)
        gameFolder.mkdirs()
        val gameTypeFolder = File(gameFolder, "gamedata.json")

        return yamlMapper.readValue(gameTypeFolder, clazz)
    }
}
