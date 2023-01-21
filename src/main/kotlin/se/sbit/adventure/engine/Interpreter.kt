package se.sbit.adventure.engine

object Interpreter {
    fun interpret(input: String, input2Command: Map<Regex, CommandType>, gibberishCommand: CommandType): CommandType =
        when (val keyValue = input2Command.entries.find { it.key.matches(input)}){
            null -> gibberishCommand
            else -> keyValue.value
        }
}