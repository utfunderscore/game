package org.readutf.game.engine.event.annotation

import jdk.jfr.Event
import org.readutf.game.engine.event.listener.GameListener
import org.readutf.game.engine.types.Result
import org.readutf.game.engine.types.toSuccess
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.isSubclassOf

/**
 * Scans the given object for methods annotated with `@EventListener` that have a single
 * parameter which is a subclass of `Event`. It returns a `Result` containing a list of
 * `GameListener` instances for each of these methods.
 *
 * @param any The object to scan for annotated methods.
 * @return A `Result` containing a list of `GameListener` instances.
 */
fun scan(any: Any): Result<List<GameListener>> {
    val listeners = mutableListOf<GameListener>()
    val clazz = any::class

    clazz.declaredFunctions
        .filter { function ->
            function.findAnnotation<EventListener>() != null &&
                function.parameters.size == 2 &&
                function.parameters[1]
                    .type.classifier
                    ?.let { it is KClass<*> && it.isSubclassOf(Event::class) } == true
        }.forEach { function ->
            listeners.add(
                GameListener {
                    function.call(any, it)
                },
            )
        }

    return listeners.toSuccess()
}
