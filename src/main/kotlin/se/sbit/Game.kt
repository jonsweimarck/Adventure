package se.sbit


interface CommandType

enum class GoCommand: CommandType {
    GoNorth, GoEast, GoSouth, GoWest,
}

open class Event

open class RoomEvent(val newRoom: Room)
class NewRoomEvent(room: Room): RoomEvent(room)
class SameRoomEvent(room: Room): RoomEvent(room)

typealias RoomConnectionsMap =  Map<Room, List<Pair<Guard, Room>>>


class Game(val connections: RoomConnectionsMap,
           itemsPlacementMap: ItemsPlacementMap = emptyMap(),
           val actionMap: Map<CommandType, (Input, Room, Items) -> Event> = emptyMap(),
           itemUsageRoomMap: Map<ItemType, Room> = emptyMap(),
           val startRoom: Room){

    val allItems: Items = Items(itemsPlacementMap, itemUsageRoomMap)

    fun playerGo(input: Input, currentRoom: Room): RoomEvent {
        val roomConnections = connections.getOrElse(currentRoom) {
            return SameRoomEvent(currentRoom) // Should neeeeeever happen
        }

        val index = roomConnections.indexOfFirst { it.first.invoke(input, currentRoom)}
        if(index == -1) {
            return SameRoomEvent(currentRoom)
        }
        return NewRoomEvent(roomConnections.get(index).second)
    }


    fun playerDo(input: Input, currentRoom: Room): Event {
        return actionMap.getOrElse(input.command) {
            throw Exception("Mama Mia! Undefined command in input$input.command")
        }.invoke(input, currentRoom, allItems)
    }

}



data class Input(val command: CommandType) // includes player, inventory, entered GoCommand, ...
data class Room(val name: String) // Room with it's description, possible commands, ...
typealias Guard = (Input, Room) -> Boolean

val northGuard: Guard = { input, _ -> (input.command == GoCommand.GoNorth)}
val eastGuard: Guard = { input, _ -> (input.command == GoCommand.GoEast)}
val southGuard: Guard = { input, _ -> (input.command == GoCommand.GoSouth)}
val westGuard: Guard = { input, _ -> (input.command == GoCommand.GoWest)}


infix fun Guard.and(g2: Guard): Guard  {
    return {input, room -> this.invoke(input, room) && g2(input, room)}
}

infix fun Guard.or(g2: Guard): Guard  {
    return {input, room -> this.invoke(input, room) || g2(input, room)}
}


fun main() {
    println("Nothing here, run tests")
}

