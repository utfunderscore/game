package org.readutf.game.engine.types

import io.github.oshai.kotlinlogging.KotlinLogging

data class Result<T>(
    private val value: T?,
    private val error: String?,
    private val causedBy: Result<*>? = null,
) {
    private val calledFrom: StackTraceElement = Thread.currentThread().stackTrace[4]

    fun getValue() = value!!

    fun getError() = error!!

    fun getErrorOrNull() = error

    fun throwIfFailed(): T {
        if (isFailure) {
            throw IllegalStateException("Result failed: $error")
        }
        return getValue()
    }

    fun debug(context: () -> Unit) =
        apply {
            var previous: Result<*> = this

            val trace = mutableListOf<Result<*>>()
            while (true) {
                trace.add(previous)
                previous = previous.causedBy ?: break
            }
            for (result in trace.reversed()) {
                KotlinLogging.logger(context)
                logger.debug { " ↪ ${result.calledFrom.className}:${result.calledFrom.lineNumber} - ${result.error}" }
            }
        }

    fun getOrNull(): T? {
        if (isSuccess) {
            return getValue()
        }
        return null
    }

    fun <U> map(mapper: (T) -> U): Result<U> {
        if (isFailure) return failure(getError())
        return success(mapper(getValue()))
    }

    inline fun <U> mapError(supplier: (Result<U>) -> Unit): T {
        if (isFailure) {
            debug { }
            supplier(failure(getError(), this))
        }
        return getValue()
    }

    inline fun onFailure(block: (Result<T>) -> Unit): T {
        if (isFailure) {
            block(this)
        }
        return this.getValue()
    }

    fun getOrThrow(): T {
        if (isFailure) {
            throw IllegalStateException(error)
        }
        return getValue()
    }

    val isSuccess: Boolean
        get() = value != null

    val isFailure: Boolean
        get() = error != null

    companion object {
        private val logger = KotlinLogging.logger { }

        fun <T> success(value: T): Result<T> = Result(value, null)

        fun <T> failure(
            error: String,
            causedBy: Result<*>? = null,
        ): Result<T> = Result(null, error, causedBy)

        fun <T> fromInternal(result: kotlin.Result<T>): Result<T> {
            if (result.isFailure) return failure(result.exceptionOrNull()?.message ?: "null")
            return success(result.getOrNull()!!)
        }

        fun empty() = success(Unit)
    }
}

fun <T> Exception.toResult(): Result<T> = Result.failure(this.message ?: "Unknown error")

fun <T> T.toSuccess(): Result<T> = Result.success(this)

fun <T> String.toFailure(): Result<T> = Result.failure(this)
