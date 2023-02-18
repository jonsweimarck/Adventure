package se.sbit.adventure.engine


open class Event(val gameText: String, val roomAndState: Pair<Room, RoomState>, val character: Character = Player)
class LookAroundEvent(gameText: String, newRoomAndState: Pair<Room, RoomState>, character: Character): RoomEvent(gameText, newRoomAndState, character)

class EventLog {
    private var events: List<Event> = emptyList()

    fun log() = events.toList()

    fun add(event: Event): List<Event> {
        events = events.plus(event)
        return events
    }



    fun getCurrentRoomAndState(character: Character): Pair<Room, RoomState> {
        val lastNewRoom = events.filterIsInstance<RoomEvent>().last { it.character == character }
        return lastNewRoom.roomAndState
    }
    fun getCurrentRoom(character: Character): Room  = getCurrentRoomAndState(character).first
    fun getCurrentRoomState(character: Character): RoomState  = getCurrentRoomAndState(character).second

    fun getNumberOfTurnsSinceEnteredCurrentRoom(character: Character): Int =
        events.filter { it.character == character }.takeLastWhile { it !is NewRoomEvent}.size +1


    fun getNumberOfOfTurnsStillInSameRoom(character1: Character, character2: Character): Int =
        when(isInSameRoom(character1, character2)){
            true -> minOf(getNumberOfTurnsSinceEnteredCurrentRoom(character1), getNumberOfTurnsSinceEnteredCurrentRoom(character2))
            false -> 0
        }



    fun isInSameRoom(character1: Character, character2: Character): Boolean =
        getCurrentRoom(character1) ==  getCurrentRoom(character2)


    companion object {
        fun fromList(initialEvents: List<Event>): EventLog {
            val e = EventLog()
            e.events = initialEvents
            return e
        }
    }
}