package se.sbit.adventure.engine


typealias RoomGuard = (Input) -> Boolean
typealias StateGuard = (Input, Room) -> Boolean

val north: RoomGuard = { input -> (input.command == GoCommand.GoNorth)}
val east: RoomGuard = { input -> (input.command == GoCommand.GoEast)}
val south: RoomGuard = { input -> (input.command == GoCommand.GoSouth)}
val west: RoomGuard = { input -> (input.command == GoCommand.GoWest)}

infix fun RoomGuard.and(g2: RoomGuard): RoomGuard {
    return {input -> this.invoke(input) && g2(input)}
}

infix fun RoomGuard.or(g2: RoomGuard): RoomGuard {
    return {input -> this.invoke(input) || g2(input)}
}


infix fun StateGuard.and(g2: StateGuard): StateGuard {
    return {input, room -> this.invoke(input, room) && g2(input, room)}
}

infix fun StateGuard.or(g2: StateGuard): StateGuard {
    return {input, room -> this.invoke(input, room) || g2(input, room)}
}

fun actionForGo(connectionsMap: RoomConnectionsMap,
                sameRoomEventText: String = "That didn't work!"): (Input, EventLog) -> Event
{
    return fun(input, eventLog): Event {
        val currentRoomAndState = eventLog.getCurrentRoomAndState(Player)
        val currentRoom  = currentRoomAndState.first

        // find new room
        val roomConnections = connectionsMap.getOrElse(currentRoom) {
            // Should neeeeeever happen.The room has no connections!
            return SameRoomEvent(sameRoomEventText, currentRoomAndState, Player)
        }

        val roomIndex = roomConnections.indexOfFirst { it.first.invoke(input) }
        if (roomIndex == -1) {
            // Trying to walk in an unconnected direction
            return SameRoomEvent(sameRoomEventText, currentRoomAndState, Player)
        }
        val newRoom = roomConnections[roomIndex].second

        val stateIndex = newRoom.states.indexOfFirst { it.first.invoke(input, currentRoom) }
        if (stateIndex == -1) {
            // No state matches!
            return SameRoomEvent(sameRoomEventText, currentRoomAndState, Player)
        }
        val newState = newRoom.states[stateIndex].second

        return NewRoomEvent("", Pair(newRoom, newState), Player)
    }
}

fun goWherePossible(roomConnections: Map<Room, List<Room>>, eventLog: EventLog, character: Character, enterRoomGameText: String): Event {
    val currentRoomAndState = eventLog.getCurrentRoomAndState(character)
    val currentRoom  = currentRoomAndState.first

    // get a shuffled list of possible rooms.
    val rooms = roomConnections.getOrElse(currentRoom) {
        return SameRoomEvent("No possible rooms to enter", currentRoomAndState, character)
    }.shuffled()

    // Try to find a state in any room
    for(room in rooms) {
        val stateIndex = room.states.indexOfFirst { state -> state.first.invoke(Input(NPCinput), currentRoom)}
        if(stateIndex != -1) {
            return NewRoomEvent(enterRoomGameText, Pair(room, room.states[stateIndex].second), character)
        }
    }
    return SameRoomEvent("No possible state to enter", currentRoomAndState, character)
}


