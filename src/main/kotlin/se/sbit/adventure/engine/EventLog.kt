package se.sbit.adventure.engine

class EventLog {
    private var events: List<Event> = emptyList()

    fun log() = events.toList()

    fun add(event: Event): List<Event> {
        events = events.plus(event)
        return events
    }



    fun getCurrentRoomAndState(character: Character): Pair<Room, State> {
        val lastNewRoom = events.filterIsInstance<NewRoomEvent>().last { it.character == character }
        return lastNewRoom.roomAndState
    }

    fun getCurrentRoom(character: Character): Room  = getCurrentRoomAndState(character).first
    fun getCurrentState(character: Character): State  = getCurrentRoomAndState(character).second


    companion object {
        fun fromList(initialEvents: List<Event>): EventLog {
            var e = EventLog()
            e.events = initialEvents
            return e
        }
    }
}