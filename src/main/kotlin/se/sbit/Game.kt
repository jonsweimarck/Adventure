package se.sbit


enum class Command {
    goNorth, goEast, goSouth, goWest,
}

typealias RoomConnectionsMap =  Map<Room, List<Pair<Guard, Room>>>


class Game(val connections: RoomConnectionsMap, itemsMap: ItemsPlacementMap = emptyMap(), val startRoom: Room){

    val allItems: Items = Items(itemsMap)

    fun playerGo(input: Input, currentRoom: Room): Room {
        val roomConnections = connections.getOrElse(currentRoom) {
            return currentRoom // Should neeeeeever happen
        }

        val index = roomConnections.indexOfFirst { it.first.invoke(input, currentRoom)}
        if(index == -1) {
            return currentRoom
        }
        return roomConnections.get(index).second;
    }

}

data class Input(val command: Command) // includes player, inventory, entered Command, ...
data class Room(val name: String) // Room with it's description, possible commands, ...
typealias Guard = (Input, Room) -> Boolean

val northGuard: Guard = { input, _ -> (input.command == Command.goNorth)}
val eastGuard: Guard = { input, _ -> (input.command == Command.goEast)}
val southGuard: Guard = { input, _ -> (input.command == Command.goSouth)}
val westGuard: Guard = { input, _ -> (input.command == Command.goWest)}


infix fun Guard.and(g2: Guard): Guard  {
    return {input, room -> this.invoke(input, room) && g2(input, room)}
}

infix fun Guard.or(g2: Guard): Guard  {
    return {input, room -> this.invoke(input, room) || g2(input, room)}
}


fun main() {
    println("Nothing here, run tests")
}

