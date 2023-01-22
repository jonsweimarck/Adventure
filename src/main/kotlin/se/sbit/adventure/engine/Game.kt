package se.sbit.adventure.engine


interface CommandType

enum class GoCommand: CommandType {
    GoNorth, GoEast, GoSouth, GoWest,
}

data class Input(val command: CommandType)

data class Room(val description: String, val altDescription: String = "N/A", val predicateToUseAltDesc: (Items, EventLog)-> Boolean = { _, _ -> false}) {

    fun roomDescription(items: Items, eventlog: EventLog): String =
        when(predicateToUseAltDesc.invoke(items, eventlog)){
            false -> description
            true -> altDescription
        }
}

open class Event(val gameText: String)
open class EndEvent(gameEndText:String):Event(gameEndText)

open class RoomEvent(gameText: String, val newRoom: Room) : Event("${gameText}\n${newRoom.description}")
class NewRoomEvent(gameText: String, room: Room): RoomEvent(gameText, room)
class SameRoomEvent(gameText: String, room: Room): RoomEvent(gameText, room)

typealias RoomConnectionsMap =  Map<Room, List<Pair<Guard, Room>>>


class Game(val connections: RoomConnectionsMap,
           itemsPlacementMap: ItemsPlacementMap = emptyMap(),
           val actionMap: Map<CommandType, (Input, Room, Room, Items) -> Event> = emptyMap(),
           itemUsageRoomMap: Map<ItemType, Room> = emptyMap(),
           val eventlog: EventLog = EventLog(),
           val startRoom: Room,
           val startState:Room
){

    val allItems: Items = Items(itemsPlacementMap, itemUsageRoomMap)


    fun playerDo(input: Input, currentRoom: Room, currentState: Room): Event {
        return actionMap.getOrElse(input.command) {
            throw Exception("Mama Mia! Undefined command in input ${input.command}")
        }.invoke(input, currentRoom, currentState, allItems)
    }

}

