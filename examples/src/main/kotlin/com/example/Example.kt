package com.example

class Example {
    // https://detekt.dev/docs/rules/naming#variablenaming
    val Message: String
        get() = "Hello World!"

    // https://detekt.dev/docs/rules/style/#magicnumber
    val foo = 5

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            println(Example().Message)
            // https://detekt.dev/docs/rules/empty-blocks#emptyifblock
            if (true) {
            } else {
                // https://detekt.dev/docs/rules/exceptions#throwingexceptionswithoutmessageorcause
                // https://detekt.dev/docs/rules/style/#usecheckorerror
                throw IllegalStateException()
            }
        }
    }
}
