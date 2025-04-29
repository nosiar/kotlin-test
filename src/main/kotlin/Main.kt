package com.nosiar

import coroutine.exceptionPropagation
import coroutine.generatorWithSuspendCoroutineUninterceptedOrReturn
import coroutine.recursionWithSuspendCoroutineUninterceptedOrReturn

fun main() {
    generatorWithSuspendCoroutineUninterceptedOrReturn()
    recursionWithSuspendCoroutineUninterceptedOrReturn()
    exceptionPropagation()
}
