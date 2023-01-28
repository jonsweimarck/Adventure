package se.sbit.adventure.engine


interface CommandType

enum class GoCommand: CommandType {
    GoNorth, GoEast, GoSouth, GoWest,
}

data class Input(val command: CommandType)

data class Room(val states: List<Pair<(Input, Room) ->Boolean, State>>)
data class State (val description: String)

open class Event(val gameText: String, val character: Character = Player)
open class EndEvent(gameEndText:String):Event(gameEndText)

open class RoomEvent(gameText: String, val newRoom: Room, val newState: State, character: Character, ) : Event("${gameText}\n${newState.description}", character)
class NewRoomEvent(gameText: String, newRoom: Room, newState: State, character: Character, ): RoomEvent(gameText, newRoom, newState, character)
class SameRoomEvent(gameText: String, room: Room, state: State, character: Character, ): RoomEvent(gameText, room, state, character)

typealias RoomConnectionsMap =  Map<Room, List<Pair<Guard, Room>>>


class Game(val connections: RoomConnectionsMap,
           itemsPlacementMap: ItemsPlacementMap = emptyMap(),
           val actionMap: Map<CommandType, (Input, Room, State, Items) -> Event> = emptyMap(),
           val eventlog: EventLog = EventLog(),
           var nonPlayerCharacters: List<Pair<NonPlayerCharacter, EventLog>> = emptyList(),
           val startRoom: Room,
           val startState:State
){

    val allItems: Items = Items(itemsPlacementMap)


    fun playerDo(input: Input, currentRoom: Room, currentState: State): Event {
        return actionMap.getOrElse(input.command) {
            throw Exception("Mama Mia! Undefined command in input ${input.command}")
        }.invoke(input, currentRoom, currentState, allItems)
    }

}

