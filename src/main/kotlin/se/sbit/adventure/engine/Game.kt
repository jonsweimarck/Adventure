package se.sbit.adventure.engine


interface CommandType

enum class GoCommand: CommandType {
    GoNorth, GoEast, GoSouth, GoWest,
}

data class Input(val command: CommandType) // includes player, inventory, entered GoCommand, ...
data class Room(val name: String) // Room with it's description, possible commands, ...

open class Event(val gameText: String)
open class EndEvent(gameEndText:String):Event(gameEndText)

open class RoomEvent(gameText: String, val newRoom: Room) : Event("${gameText}\n${newRoom.name}")
class NewRoomEvent(gameText: String, room: Room): RoomEvent(gameText, room)
class SameRoomEvent(gameText: String, room: Room): RoomEvent(gameText, room)

typealias RoomConnectionsMap =  Map<Room, List<Pair<Guard, Room>>>


class Game(val connections: RoomConnectionsMap,
           itemsPlacementMap: ItemsPlacementMap = emptyMap(),
           val actionMap: Map<CommandType, (Input, Room, Items) -> Event> = emptyMap(),
           itemUsageRoomMap: Map<ItemType, Room> = emptyMap(),
           val startRoom: Room
){

    val allItems: Items = Items(itemsPlacementMap, itemUsageRoomMap)


    fun playerDo(input: Input, currentRoom: Room): Event {
        return actionMap.getOrElse(input.command) {
            throw Exception("Mama Mia! Undefined command in input ${input.command}")
        }.invoke(input, currentRoom, allItems)
    }

}

