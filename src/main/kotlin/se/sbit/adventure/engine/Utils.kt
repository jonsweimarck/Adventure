package se.sbit.adventure.engine


typealias Guard = (Input, Room) -> Boolean

val north: Guard = { input, _ -> (input.command == GoCommand.GoNorth)}
val east: Guard = { input, _ -> (input.command == GoCommand.GoEast)}
val south: Guard = { input, _ -> (input.command == GoCommand.GoSouth)}
val west: Guard = { input, _ -> (input.command == GoCommand.GoWest)}


infix fun Guard.and(g2: Guard): Guard {
    return {input, room -> this.invoke(input, room) && g2(input, room)}
}

infix fun Guard.or(g2: Guard): Guard {
    return {input, room -> this.invoke(input, room) || g2(input, room)}
}

fun actionForGo(connectionsMap: RoomConnectionsMap,
                sameRoomEventText: String = "That didn't work!"): (Input, EventLog, Items) -> Event
{
    return fun(input, eventLog, items): Event {
        val currentRoom  = eventLog.getCurrentRoom()
        val currentState = eventLog.getCurrentState()

        // find new room
        val roomConnections = connectionsMap.getOrElse(currentRoom) {
            // Should neeeeeever happen.The room has no connections!
            return SameRoomEvent(sameRoomEventText , currentRoom, currentState, Player)
        }

        val roomIndex = roomConnections.indexOfFirst { it.first.invoke(input, currentRoom) }
        if (roomIndex == -1) {
            // Trying to walk in an unconnected direction
            return SameRoomEvent(sameRoomEventText , currentRoom, currentState, Player)
        }
        val newRoom = roomConnections[roomIndex].second

        val stateIndex = newRoom.states.indexOfFirst { it.first.invoke(input, currentRoom) }
        if (stateIndex == -1) {
            // No state matches!
            return SameRoomEvent(sameRoomEventText , currentRoom, currentState, Player)
        }
        val newState = newRoom.states[stateIndex].second

        return NewRoomEvent("", newRoom, newState, Player)
    }
}



