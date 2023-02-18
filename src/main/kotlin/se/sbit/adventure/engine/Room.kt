package se.sbit.adventure.engine

data class Room(val states: List<Pair<RoomStateGuard, RoomState>>)
data class RoomState (val description: String)


open class RoomEvent(gameText: String, roomAndState: Pair<Room, RoomState>, character: Character) : Event(gameText, roomAndState, character)
class NewRoomEvent(gameText: String, newRoomAndState: Pair<Room, RoomState>, character: Character): RoomEvent(gameText, newRoomAndState, character)
class SameRoomEvent(gameText: String, newRoomAndState: Pair<Room, RoomState>, character: Character): RoomEvent(gameText, newRoomAndState, character)

typealias RoomGuard = (Input) -> Boolean
typealias RoomStateGuard = (EventLog) -> Boolean

val north: RoomGuard = { input -> (input.command == GoCommand.GoNorth)}
val east: RoomGuard = { input -> (input.command == GoCommand.GoEast)}
val south: RoomGuard = { input -> (input.command == GoCommand.GoSouth)}
val west: RoomGuard = { input -> (input.command == GoCommand.GoWest)}

// This jdk signature clashes with the one for RoomState below, but I guess it would never be used anyway cause that would mean "inputA and inputB"
//infix fun RoomGuard.and(g2: RoomGuard): RoomGuard {
//    return {input -> this.invoke(input) && g2(input)}
//}

infix fun RoomGuard.or(g2: RoomGuard): RoomGuard {
    return {input -> this.invoke(input) || g2(input)}
}


infix fun RoomStateGuard.and(g2: RoomStateGuard): RoomStateGuard {
    return {eventLog -> this.invoke(eventLog) && g2(eventLog)}
}

// This jdk signature clashes with the one for Room above, but I guess you could live without and add two different guards leading to the same state instead
// of the two staed with "or"
//infix fun RoomStateGuard.or(g2: RoomStateGuard): RoomStateGuard {
//    return {eventLog -> this.invoke(eventLog) || g2(eventLog)}
//}

fun actionForGo(command: CommandType, connectionsMap: Map<Room, List<Pair<RoomGuard, Room>>>,
                sameRoomEventText: String = "That didn't work!"): (EventLog) -> Event
{
    return fun(eventLog): Event {
        val currentRoomAndState = eventLog.getCurrentRoomAndState(Player)
        val currentRoom  = currentRoomAndState.first

        // find new room
        val roomConnections = connectionsMap.getOrElse(currentRoom) {
            // Should neeeeeever happen.The room has no connections!
            return SameRoomEvent(sameRoomEventText, currentRoomAndState, Player)
        }

        val roomIndex = roomConnections.indexOfFirst { it.first.invoke(Input(command)) }
        if (roomIndex == -1) {
            // Trying to walk in an unconnected direction
            return SameRoomEvent(sameRoomEventText, currentRoomAndState, Player)
        }
        val newRoom = roomConnections[roomIndex].second

        val stateIndex = newRoom.states.indexOfFirst { it.first.invoke(eventLog) }
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
        val stateIndex = room.states.indexOfFirst { state -> state.first.invoke(eventLog)}
        if(stateIndex != -1) {
            return NewRoomEvent(enterRoomGameText, Pair(room, room.states[stateIndex].second), character)
        }
    }
    return SameRoomEvent("No possible state to enter", currentRoomAndState, character)
}
