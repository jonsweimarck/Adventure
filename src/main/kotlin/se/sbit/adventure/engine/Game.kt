package se.sbit.adventure.engine


interface CommandType

enum class GoCommand: CommandType {
    GoNorth, GoEast, GoSouth, GoWest,
}

data class Input(val command: CommandType)

data class Room(val states: List<Pair<(Input, Room) ->Boolean, State>>)
data class State (val description: String)

open class Event(val gameText: String)
open class EndEvent(gameEndText:String):Event(gameEndText)

open class RoomEvent(gameText: String, val newRoom: Room, val newState: State) : Event("${gameText}\n${newState.description}")
class NewRoomEvent(gameText: String, newRoom: Room, newState: State): RoomEvent(gameText, newRoom, newState)
class SameRoomEvent(gameText: String, room: Room, state: State): RoomEvent(gameText, room, state)

typealias RoomConnectionsMap =  Map<Room, List<Pair<Guard, Room>>>


class Game(val connections: RoomConnectionsMap,
           itemsPlacementMap: ItemsPlacementMap = emptyMap(),
           val actionMap: Map<CommandType, (Input, Room, State, Items) -> Event> = emptyMap(),
           itemUsageRoomMap: Map<ItemType, Room> = emptyMap(),
           val eventlog: EventLog = EventLog(),
           val startRoom: Room,
           val startState:State
){

    val allItems: Items = Items(itemsPlacementMap, itemUsageRoomMap)


    fun playerDo(input: Input, currentRoom: Room, currentState: State): Event {
        return actionMap.getOrElse(input.command) {
            throw Exception("Mama Mia! Undefined command in input ${input.command}")
        }.invoke(input, currentRoom, currentState, allItems)
    }

}

