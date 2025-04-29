package coroutine

import kotlinx.coroutines.*
import kotlinx.coroutines.future.await
import kotlinx.coroutines.future.future
import java.util.concurrent.CompletableFuture

fun exceptionPropagation() {
    println("=== Case 1 ===")
    case1()
    println("\n=== Case 2 ===")
    case2()
    println("\n=== Case 3 ===")
    case3()
    println("\n=== Case 4 ===")
    case4()
}

/**
 * async throws exception -> parent coroutine is cancelled
 * You can't catch from wrapping try-catch block around async.
 * CoroutineScope does not propagate exceptions to outer thread.
 * If you await async, RuntimeException is thrown.
 */
fun case1() {
    var deferred: Deferred<String>? = null
    try {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                deferred = async {
                    delay(200)
                    throw RuntimeException("oh no!")
                }
            } catch (e: Exception) {
                println("Nothing caught without await.")
            }
            try {
                delay(9999999)
            } catch (e:CancellationException) {
                println("Cancelled when async throws")
                throw e
            }
        }
    } catch (e: Exception) {
        println("Nothing caught here because exceptions inside CoroutineScope are not propagated to outer thread.")
    }

    runBlocking {
        try {
            delay(100)
            deferred?.await()
        } catch (e: Exception) {
            println("2 This is ${e::class.simpleName}!")
        }

        delay(1_000)
    }
}

/**
 * In this case, await async, now you can catch RuntimeException.
 * You can catch RuntimeException also from second await.
 */
fun case2() {
    var deferred: Deferred<String>? = null
    CoroutineScope(Dispatchers.IO).launch {
        try {
            deferred = async {
                delay(200)
                throw RuntimeException("oh no!")
            }
            deferred?.await()
        } catch (e: Exception) {
            println("Now ${e::class.simpleName} caught.")
        }
        try {
            delay(9999999)
        } catch (e:CancellationException) {
            println("Cancelled when async throws")
            throw e
        }
    }

    runBlocking {
        try {
            delay(100)
            deferred?.await()
        } catch (e: Exception) {
            println("2 This is ${e::class.simpleName}!")
        }

        delay(1_000)
    }
}

/**
 * Same code as case 1 but using future instead of async.
 * Same result as case 1.
 */
fun case3() {
    var future: CompletableFuture<String>? = null
    try {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                future = future {
                    delay(200)
                    throw RuntimeException("oh no!")
                }
            } catch (e: Exception) {
                println("Nothing caught without await.")
            }
            try {
                delay(9999999)
            } catch (e:CancellationException) {
                println("Cancelled when async throws")
                throw e
            }
        }
    } catch (e: Exception) {
        println("Nothing caught here because exceptions inside CoroutineScope are not propagated to outer thread.")
    }

    runBlocking {
        try {
            delay(100)
            future?.await()
        } catch (e: Exception) {
            println("2 This is ${e::class.simpleName}!")
        }

        delay(1_000)
    }
}

/**
 * Same code as case 2 but using future instead of async.
 * In this case, CancellationException is caught instead of original RuntimeException.
 * Why is that?
 * And CancellationException caught from second await has null cause.
 */
fun case4() {
    var future: CompletableFuture<String>? = null
    CoroutineScope(Dispatchers.IO).launch {
        try {
            future = future {
                delay(200)
                throw RuntimeException("oh no!")
            }
            future?.await()
        } catch (e: Exception) {
            println("Now ${e::class.simpleName} caught.")
        }
        try {
            delay(9999999)
        } catch (e:CancellationException) {
            println("Cancelled when async throws")
            throw e
        }
    }

    runBlocking {
        try {
            delay(100)
            future?.await()
        } catch (e: Exception) {
            println("2 This is ${e::class.simpleName}!")
        }

        delay(1_000)
    }
}
