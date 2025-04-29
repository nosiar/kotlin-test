package coroutine

import kotlin.coroutines.*
import kotlin.coroutines.intrinsics.COROUTINE_SUSPENDED
import kotlin.coroutines.intrinsics.intercepted
import kotlin.coroutines.intrinsics.suspendCoroutineUninterceptedOrReturn

class Generator<T>(block: suspend Generator<T>.GeneratorScope.() -> Unit) {
    private var continuation: Continuation<Unit>? = null
    private var nextValue: T? = null
    private var isCompleted = false

    // Custom scope for yield
    inner class GeneratorScope {
        suspend fun yield(value: T) = suspendCoroutineUninterceptedOrReturn { cont ->
            nextValue = value
            continuation = cont.intercepted() // Ensure proper context interception [3][4]
            COROUTINE_SUSPENDED
        }
    }

    // Start the generator coroutine
    init {
        val start = block.createCoroutine(GeneratorScope(), object : Continuation<Unit> {
            override val context: CoroutineContext = EmptyCoroutineContext
            override fun resumeWith(result: Result<Unit>) {
                isCompleted = true
                result.onFailure { throw it }
            }
        })
        start.resume(Unit)
    }

    // Get next value
    fun next(): T? {
        return if (isCompleted) {
            null
        } else {
            val result = nextValue
            nextValue = null
            continuation?.resume(Unit) // Resume the coroutine [2][4]
            result
        }
    }
}

fun generatorWithSuspendCoroutineUninterceptedOrReturn() {
    val generator = Generator {
        for (i in 1..5) {
            yield(i * 10) // Suspends here via suspendCoroutineUninterceptedOrReturn [2]
            println("Resumed for next value")
        }
    }

    while (true) {
        val value = generator.next() ?: break
        println("Generated: $value")
    }
}
