package org.readutf.game.engine.arena.utils

import java.io.File

object ArenaFolder {
    fun getArenaFolder(
        baseDir: File,
        arenaName: String,
    ): File {
        val arenaStorageFolder = getStorageFolder(baseDir)
        val arenaFolder = File(arenaStorageFolder, arenaName)
        arenaFolder.mkdirs()

        return arenaFolder
    }

    fun getStorageFolder(baseDir: File) = File(baseDir, "arenas")
}
