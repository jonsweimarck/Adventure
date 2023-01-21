package se.sbit.adventure.engine

object Interpreter {
    fun interpret(input: String, input2Command: Map<String, CommandType>, gibberishCommand: CommandType): CommandType =
        when (val keyValue = input2Command.entries.find { it.key.toRegex(RegexOption.IGNORE_CASE).matches(input)}){
            null -> gibberishCommand
            else -> keyValue.value
        }
}