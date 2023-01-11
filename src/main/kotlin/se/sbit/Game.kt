package se.sbit


interface CommandType

enum class GoCommand: CommandType {
    GoNorth, GoEast, GoSouth, GoWest,
}

data class Input(val command: CommandType) // includes player, inventory, entered GoCommand, ...
data class Room(val name: String) // Room with it's description, possible commands, ...

open class Event

open class RoomEvent(val newRoom: Room) : Event()
class NewRoomEvent(room: Room): RoomEvent(room)
class SameRoomEvent(room: Room): RoomEvent(room)

typealias RoomConnectionsMap =  Map<Room, List<Pair<Guard, Room>>>


class Game(val connections: RoomConnectionsMap,
           itemsPlacementMap: ItemsPlacementMap = emptyMap(),
           val actionMap: Map<CommandType, (Input, Room, Items) -> Event> = emptyMap(),
           itemUsageRoomMap: Map<ItemType, Room> = emptyMap(),
           val startRoom: Room){

    val allItems: Items = Items(itemsPlacementMap, itemUsageRoomMap)


    fun playerDo(input: Input, currentRoom: Room): Event {
        return actionMap.getOrElse(input.command) {
            throw Exception("Mama Mia! Undefined command in input$input.command")
        }.invoke(input, currentRoom, allItems)
    }
}



fun main() {
    println("Nothing here, run tests")
}

