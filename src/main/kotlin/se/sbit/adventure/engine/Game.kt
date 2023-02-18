package se.sbit.adventure.engine

sealed class Placement
object Carried : Placement()
data class InRoom(val room: Room): Placement()

open class EndEvent(gameEndText:String, roomAndState: Pair<Room, RoomState>):Event(gameEndText, roomAndState)


class Game(val connections: Map<Room, List<Pair<RoomGuard, Room>>>,
           itemsPlacementMap: Map<Item, Placement> = emptyMap(),
           val actionMap: Map<CommandType, (Input, EventLog) -> Event> = emptyMap(),
           val eventlog: EventLog = EventLog(),
           nonPlayerCharactersWithStartRooms: List<Pair<NPC, Pair<Room, RoomState>>> = emptyList()
) {
    val nonPlayerCharacters: List<NPC> = nonPlayerCharactersWithStartRooms.map { it.first }

    init {
        itemsPlacementMap.filter { it.value == Carried }.keys
            .map { PickedUpItemEvent("Carried from start", eventlog.getCurrentRoomAndState(Player), Player, it) }
            .forEach { eventlog.add(it) }

        itemsPlacementMap.filter { it.value is InRoom }
            .map { DroppedItemEvent("Dropped from start", roomAndStateFor(it), Player, it.key) }
            .forEach { eventlog.add(it) }

        // Add a NewRoomEvent for each NPC
        nonPlayerCharactersWithStartRooms.forEach { eventlog.add(NewRoomEvent("NPC: ${it.first.description}", Pair(it.second.first, it.second.second), it.first)) }
    }



    private fun roomAndStateFor(entry: Map.Entry<Item, Placement>): Pair<Room, RoomState> {
        val room = (entry.value as InRoom).room
        val state = RoomState("No real RoomState as droped from start")
        return Pair(room, state)
    }


    fun playerDo(input: Input, eventLog: EventLog): Event {
        return actionMap.getOrElse(input.command) {
            throw Exception("Mama Mia! Undefined command in input ${input.command}")
        }.invoke(input, eventLog)
    }
}

