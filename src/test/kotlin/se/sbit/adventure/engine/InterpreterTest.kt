package se.sbit.adventure.engine

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

@DisplayName("The interpreter that parses the user's input can:")
class InterpreterTest {

    val gibberishCommand = object:CommandType{}

    @Test
    fun `parse user input with regexp`(){
        val input2Command = mapOf<Regex, CommandType>("(go )?n(orth)?".toRegex(RegexOption.IGNORE_CASE) to GoCommand.GoNorth)

        // These should be parsed ok and return back the GoNorth command
        expectThat(Interpreter.interpret("go north", input2Command, gibberishCommand)).isEqualTo(GoCommand.GoNorth)
        expectThat(Interpreter.interpret("North", input2Command, gibberishCommand)).isEqualTo(GoCommand.GoNorth)
        expectThat(Interpreter.interpret("go n", input2Command, gibberishCommand)).isEqualTo(GoCommand.GoNorth)

        // These are not expected to be parsed ok and will return back the  command
        expectThat(Interpreter.interpret("gonorth", input2Command, gibberishCommand)).isEqualTo(gibberishCommand)
        expectThat(Interpreter.interpret("go", input2Command, gibberishCommand)).isEqualTo(gibberishCommand)
    }

    @Test
    fun `use regexp, but I as a programmer must tinker a bit to see what works`(){

        //----------- go ---------------
        // optional chars at start
        var regex = ".*go north".toRegex(RegexOption.IGNORE_CASE)
        expectThat(regex.matches("abgo north")).isEqualTo(true)


        // optional exact "go " at start
        regex = "(go )?north".toRegex(RegexOption.IGNORE_CASE)
        expectThat(regex.matches("go north")).isEqualTo(true)
        expectThat(regex.matches("North")).isEqualTo(true)

        // ... adding Either "n" or "north"
        regex = "(go )?n(orth)?".toRegex(RegexOption.IGNORE_CASE)
        expectThat(regex.matches("go north")).isEqualTo(true)
        expectThat(regex.matches("North")).isEqualTo(true)
        expectThat(regex.matches("go N")).isEqualTo(true)
        expectThat(regex.matches("N")).isEqualTo(true)

        // should NOT match
        expectThat(regex.matches("drinNk")).isEqualTo(false)
        expectThat(regex.matches("gonorth")).isEqualTo(false)
        expectThat(regex.matches("go")).isEqualTo(false)

        // ------- pick up/drop ------------
        regex = "pick (up )?(the )?sword".toRegex(RegexOption.IGNORE_CASE)
        expectThat(regex.matches("pick up sword")).isEqualTo(true)
        expectThat(regex.matches("pick sword")).isEqualTo(true)
        expectThat(regex.matches("pick up sword")).isEqualTo(true)
        expectThat(regex.matches("pick up the sword")).isEqualTo(true)

        // ------- exit game ------------
        regex = "((exit( game)?)|bye)".toRegex(RegexOption.IGNORE_CASE)
        expectThat(regex.matches("exit")).isEqualTo(true)
        expectThat(regex.matches("exit game")).isEqualTo(true)
        expectThat(regex.matches("bye")).isEqualTo(true)
    }
}