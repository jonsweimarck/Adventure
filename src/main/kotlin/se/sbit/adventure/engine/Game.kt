package se.sbit.adventure.engine


interface CommandType

enum class GoCommand: CommandType {
    GoNorth, GoEast, GoSouth, GoWest,
}

object NPCinput: CommandType


data class Input(val command: CommandType)

data class Room(val states: List<Pair<(Input, Room) ->Boolean, State>>)
data class State (val description: String)

open class Event(val gameText: String, val character: Character = Player)
open class EndEvent(gameEndText:String):Event(gameEndText)

open class RoomEvent(gameText: String, val roomAndState: Pair<Room, State>, character: Character, ) : Event("${gameText}\n${roomAndState.second.description}", character)
class NewRoomEvent(gameText: String, newRoomAndState: Pair<Room, State>, character: Character, ): RoomEvent(gameText, newRoomAndState, character)
class SameRoomEvent(gameText: String, newRoomAndState: Pair<Room, State>, character: Character, ): RoomEvent(gameText, newRoomAndState, character)

typealias RoomConnectionsMap =  Map<Room, List<Pair<RoomGuard, Room>>>


class Game(val connections: RoomConnectionsMap,
           itemsPlacementMap: ItemsPlacementMap = emptyMap(),
           val actionMap: Map<CommandType, (Input, EventLog, Items) -> Event> = emptyMap(),
           val eventlog: EventLog = EventLog(),
           val nonPlayerCharacters: Map<NPC, (EventLog)-> Event > = emptyMap()
){

    val allItems: Items = Items(itemsPlacementMap)


    fun playerDo(input: Input, eventLog: EventLog): Event {
        return actionMap.getOrElse(input.command) {
            throw Exception("Mama Mia! Undefined command in input ${input.command}")
        }.invoke(input, eventLog, allItems)
    }

}

