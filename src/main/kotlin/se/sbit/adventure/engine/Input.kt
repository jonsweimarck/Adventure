package se.sbit.adventure.engine

interface CommandType
enum class GoCommand: CommandType {
    GoNorth, GoEast, GoSouth, GoWest,
}

object NPCinput: CommandType
data class Input(val command: CommandType)

object Interpreter {
    fun interpret(input: String, input2Command: Map<Regex, CommandType>, gibberishCommand: CommandType): CommandType =
        when (val keyValue = input2Command.entries.find { it.key.matches(input)}){
            null -> gibberishCommand
            else -> keyValue.value
        }
}

