package org.readutf.game.server.commands.complter

import org.readutf.game.engine.GameManager
import revxrsal.commands.Lamp
import revxrsal.commands.annotation.list.AnnotationList
import revxrsal.commands.autocomplete.SuggestionProvider
import revxrsal.commands.command.CommandActor
import java.lang.reflect.Type

class GameCompleterFactory : SuggestionProvider.Factory<CommandActor> {
    override fun create(
        type: Type,
        annotations: AnnotationList,
        lamp: Lamp<CommandActor>,
    ): SuggestionProvider<CommandActor>? {
        annotations.get(GameCompleter::class.java) ?: return null

        return SuggestionProvider {
            return@SuggestionProvider GameManager.activeGames.keys.toList()
        }
    }
}
