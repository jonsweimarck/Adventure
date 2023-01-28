package se.sbit.adventure.engine

class EventLog {
    private var events: List<Event> = emptyList()

    fun log() = events.toList()

    fun add(event: Event): List<Event> {
        events = events.plus(event)
        return events
    }



    fun getCurrentRoomAndState(): Pair<Room, State> {
        val lastNewRoom = events.filterIsInstance<NewRoomEvent>().last()
        return Pair (lastNewRoom.newRoom, lastNewRoom.newState)
    }

    fun getCurrentRoom(): Room  = getCurrentRoomAndState().first
    fun getCurrentState(): State  = getCurrentRoomAndState().second


    companion object {
        fun fromList(initialEvents: List<Event>): EventLog {
            var e = EventLog()
            e.events = initialEvents
            return e
        }
    }
}