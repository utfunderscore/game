package org.readutf.game.engine.types

data class Result<T>(
    private val value: T?,
    private val error: String?,
) {
    fun getValue() = value!!

    fun getError() = error!!

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

    fun <U> mapError(): Result<U> = failure(getError())

    inline fun onFailure(block: (String) -> Unit): T {
        if (isFailure) {
            block(getError())
        }
        return this.getValue()
    }

    val isSuccess: Boolean
        get() = value != null

    val isFailure: Boolean
        get() = error != null

    companion object {
        fun <T> success(value: T): Result<T> = Result(value, null)

        fun <T> failure(error: String): Result<T> = Result(null, error)

        fun <T> fromInternal(result: kotlin.Result<T>): Result<T> {
            if (result.isFailure) return failure(result.exceptionOrNull()?.message ?: "null")
            return success(result.getOrNull()!!)
        }

        fun empty() = success(Unit)
    }
}

fun <T> Exception.toResult(): Result<T> = Result.failure(this.message ?: "Unknown error")
