package org.readutf.game.server.commands.completions

import org.readutf.game.server.game.GameTypeManager
import revxrsal.commands.Lamp
import revxrsal.commands.annotation.list.AnnotationList
import revxrsal.commands.autocomplete.SuggestionProvider
import revxrsal.commands.command.CommandActor
import java.lang.reflect.Type

class GameTypeCompleterFactory(
    val gameTypeManager: GameTypeManager,
) : SuggestionProvider.Factory<CommandActor> {
    override fun create(
        type: Type,
        annotations: AnnotationList,
        lamp: Lamp<CommandActor>,
    ): SuggestionProvider<CommandActor>? {
        annotations.get(GameTypeCompleter::class.java) ?: return null

        return SuggestionProvider {
            return@SuggestionProvider gameTypeManager.creators.keys
        }
    }
}
