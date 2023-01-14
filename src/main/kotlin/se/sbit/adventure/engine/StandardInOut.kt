package se.sbit.adventure.engine

object StandardInOut {
    fun showText(string: String) {
        println(string)
    }

    fun waitForInput(): String {
        return readln()
    }
}