package se.sbit


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

fun goActionFromRoomConnectionsMap(connectionsMap: RoomConnectionsMap): (Input, Room, Items) -> Event
{
    return fun(input, currentRoom, items): Event {
        val roomConnections = connectionsMap.getOrElse(currentRoom) {
            return SameRoomEvent(currentRoom) // Should neeeeeever happen
        }

        val index = roomConnections.indexOfFirst { it.first.invoke(input, currentRoom) }
        if (index == -1) {
            return SameRoomEvent(currentRoom)
        }
        return NewRoomEvent(roomConnections[index].second)
    }
}
