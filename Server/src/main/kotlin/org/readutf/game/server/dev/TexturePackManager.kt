package org.readutf.game.server.dev

import net.kyori.adventure.key.Key
import net.kyori.adventure.resource.ResourcePackInfo
import net.kyori.adventure.resource.ResourcePackRequest
import net.minestom.server.MinecraftServer
import net.minestom.server.event.player.PlayerSpawnEvent
import org.readutf.game.engine.utils.addListener
import org.readutf.game.engine.utils.toComponent
import ru.brikster.glyphs.compile.GlyphCompiler
import ru.brikster.glyphs.compile.ResourceProducer
import ru.brikster.glyphs.glyph.Glyph
import ru.brikster.glyphs.glyph.image.ImageGlyph
import ru.brikster.glyphs.glyph.image.TextureProperties
import ru.brikster.glyphs.glyph.space.SpacesGlyphResourceProducer
import ru.brikster.glyphs.glyph.space.mojang.MojangSpacesGlyph
import team.unnamed.creative.BuiltResourcePack
import team.unnamed.creative.ResourcePack
import team.unnamed.creative.base.Writable
import team.unnamed.creative.part.ResourcePackPart
import team.unnamed.creative.serialize.minecraft.MinecraftResourcePackWriter
import team.unnamed.creative.server.ResourcePackServer
import team.unnamed.creative.texture.Texture
import java.net.URI
import java.util.concurrent.Executors

object TexturePackManager {
    val spaces: SpacesGlyphResourceProducer = MojangSpacesGlyph.create()

    val fullHeartIcon =
        createChatIcon(
            "full_heart",
            Writable.resource(javaClass.classLoader, "textures/heart/full-heart.png"),
        )

//    val halfHeartIcon =
//        createChatIcon(
//            "half_heart",
//            Writable.resource(javaClass.classLoader, "textures/heart/half-heart.png"),
//        )

    val emptyHeartIcon =
        createChatIcon(
            "half_heart",
            Writable.resource(javaClass.classLoader, "textures/heart/container-heart.png"),
        )

    init {

        val resourcePack = createResourcePack()

        val resources =
            createChatParts(
                fullHeartIcon,
                emptyHeartIcon,
//                halfHeartIcon,
            )

        resources.forEach {
            resourcePack.part(it)
        }

        val pack = MinecraftResourcePackWriter.minecraft().build(resourcePack)

        startPackServer(pack)

        requirePackOnJoin(pack)
    }

    private fun createResourcePack(): ResourcePack {
        val resourcePack = ResourcePack.resourcePack()
        resourcePack.packMeta(34, "Example texture pack")
        resourcePack.icon(Writable.resource(javaClass.classLoader, "textures/heart.png"))
        return resourcePack
    }

    private fun createChatParts(vararg resourceProducer: ResourceProducer): MutableCollection<ResourcePackPart> {
        val resources =
            GlyphCompiler
                .instance()
                .compile(
                    spaces,
                    *resourceProducer,
                )
        return resources
    }

    private fun startPackServer(pack: BuiltResourcePack) {
        val server =
            ResourcePackServer
                .server()
                .address("0.0.0.0", 7270)
                .pack(pack)
                .executor(Executors.newSingleThreadExecutor())
                .path("/")
                .build()

        server.start()
    }

    private fun requirePackOnJoin(pack: BuiltResourcePack) {
        val packInfo =
            ResourcePackInfo
                .resourcePackInfo()
                .uri(URI("http://0.0.0.0:7270/"))
                .hash(pack.hash())
                .build()

        val packRequest =
            ResourcePackRequest
                .resourcePackRequest()
                .packs(packInfo)
                .prompt("Download the texture pack".toComponent())
                .required(false)
                .build()

        MinecraftServer.getGlobalEventHandler().addListener<PlayerSpawnEvent> {
            if (!it.isFirstSpawn) return@addListener
            val player = it.player

            player.sendResourcePacks(packRequest)
        }
    }

    fun createChatIcon(
        name: String,
        writable: Writable,
    ) = ImageGlyph.of(
        Key.key(Glyph.DEFAULT_NAMESPACE, "chat/$name"),
        Texture
            .texture()
            .key(Key.key(Glyph.DEFAULT_NAMESPACE, "chat/$name"))
            .data(writable)
            .build(),
        TextureProperties(8, 8),
    )
}
