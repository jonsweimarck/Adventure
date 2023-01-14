package se.sbit.adventure.engine

object Interpreter {
    fun interpret(input: String, input2Command: Map<String, CommandType>, gibberishCommand: CommandType): CommandType =
        input2Command.getOrDefault(input, gibberishCommand)

}