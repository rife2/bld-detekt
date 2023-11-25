package com.example

import java.io.IOException

/**
 * Example class.
 */
class Example {
    val message: String
        get() = "Hello World!"

    // https://detekt.dev/docs/1.22.0/rules/performance#arrayprimitive
    fun returningFunction(): Array<Double> { return arrayOf() }

    // https://detekt.dev/docs/1.22.0/rules/style#canbenonnullable
    fun foo(a: Int?) {
        val b = a!! + 2
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            // https://detekt.dev/docs/1.22.0/rules/naming#booleanpropertynaming
            val progressBar: Boolean = true
            // https://detekt.dev/docs/1.22.0/rules/potential-bugs#avoidreferentialequality
            val areEqual = "aString" === ""
            println(Example().message)
            if (false) {
                // https://detekt.dev/docs/1.22.0/rules/exceptions#exceptionraisedinunexpectedlocation
                throw IllegalStateException()
            }
        }
    }
}
