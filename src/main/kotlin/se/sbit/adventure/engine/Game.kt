package se.sbit.adventure.engine


interface CommandType

enum class GoCommand: CommandType {
    GoNorth, GoEast, GoSouth, GoWest,
}

object NPCinput: CommandType


data class Input(val command: CommandType)

data class Room(val states: List<Pair<(Input, Room) ->Boolean, State>>)
data class State (val description: String)

open class Event(val gameText: String, val roomAndState: Pair<Room, State>, val character: Character = Player)
open class EndEvent(gameEndText:String, roomAndState: Pair<Room, State>):Event(gameEndText, roomAndState)

open class RoomEvent(gameText: String, roomAndState: Pair<Room, State>, character: Character, ) : Event("${gameText}", roomAndState, character)
class NewRoomEvent(gameText: String, newRoomAndState: Pair<Room, State>, character: Character, ): RoomEvent(gameText, newRoomAndState, character)
class SameRoomEvent(gameText: String, newRoomAndState: Pair<Room, State>, character: Character, ): RoomEvent(gameText, newRoomAndState, character)
class LookAroundEvent(gameText: String, newRoomAndState: Pair<Room, State>, character: Character, ): RoomEvent(gameText, newRoomAndState, character)


typealias RoomConnectionsMap =  Map<Room, List<Pair<RoomGuard, Room>>>



class Game2(val connections: RoomConnectionsMap,
            itemsPlacementMap: ItemsPlacementMap2 = emptyMap(),
            val actionMap: Map<CommandType, (Input, EventLog, Items2) -> Event> = emptyMap(),
            val eventlog: EventLog = EventLog(),
            val nonPlayerCharacters: List<NPC> = emptyList()
) {
    val allItems: Items2 = Items2(itemsPlacementMap)

    init {
        itemsPlacementMap.filter { it.value == Carried }.keys
            .map { PickedUpItemEvent2("Carried from start", eventlog.getCurrentRoomAndState(Player), Player, it) }
            .forEach { eventlog.add(it) }

        itemsPlacementMap.filter { it.value is InRoom }
            .map { DroppedItemEvent2("Dropped from start", roomAndStateFor(it), Player, it.key) }
            .forEach { eventlog.add(it) }
    }

    private fun roomAndStateFor(entry: Map.Entry<Item, Placement>): Pair<Room, State> {
        val room = (entry.value as InRoom).room
        val state = State("No real State as droped from start")
        return Pair(room, state)
    }


    fun playerDo(input: Input, eventLog: EventLog): Event {
        return actionMap.getOrElse(input.command) {
            throw Exception("Mama Mia! Undefined command in input ${input.command}")
        }.invoke(input, eventLog, allItems)
    }
}

