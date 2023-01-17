package se.sbit.adventure.engine


typealias Guard = (Input, Room) -> Boolean

val northGuard: Guard = { input, _ -> (input.command == GoCommand.GoNorth)}
val eastGuard: Guard = { input, _ -> (input.command == GoCommand.GoEast)}
val southGuard: Guard = { input, _ -> (input.command == GoCommand.GoSouth)}
val westGuard: Guard = { input, _ -> (input.command == GoCommand.GoWest)}


infix fun Guard.and(g2: Guard): Guard {
    return {input, room -> this.invoke(input, room) && g2(input, room)}
}

infix fun Guard.or(g2: Guard): Guard {
    return {input, room -> this.invoke(input, room) || g2(input, room)}
}

fun goActionFromRoomConnectionsMap(connectionsMap: RoomConnectionsMap, sameRoomEventText: String = "That didn't work!"): (Input, Room, Items) -> Event
{
    return fun(input, currentRoom, items): Event {
        val roomConnections = connectionsMap.getOrElse(currentRoom) {
            // Should neeeeeever happen.The room has no connections!
            return SameRoomEvent(sameRoomEventText +"1\n", currentRoom)
        }

        val index = roomConnections.indexOfFirst { it.first.invoke(input, currentRoom) }
        if (index == -1) {
            // Trying to walk in an unconnected direction
            return SameRoomEvent(sameRoomEventText +"2\n", currentRoom)
        }
        return NewRoomEvent("", roomConnections[index].second)
    }
}
