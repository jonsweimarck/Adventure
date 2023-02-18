package se.sbit.adventure.engine

import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.contains
import strikt.assertions.doesNotContain
import strikt.assertions.isA
import strikt.assertions.isEqualTo

class ActionsTest {

    // Setting up two rooms, connected North-South
    // Each room has just a single state
    private val alwaysPass = { _: Input, _: Room -> true}
    private val stateA = RoomState("a")
    private val stateB = RoomState("b")
    private val roomA = Room(listOf(Pair(alwaysPass, stateA)))
    private val roomB = Room(listOf(Pair(alwaysPass, stateB)))

    private val connectedRooms = mapOf(
        roomA to listOf(Pair(south, roomB)),
        roomB to listOf(Pair(north, roomA))
    )


    //    // All Items, as well as where they are placed
    class MiscItems(description: String): SingleStateItem(ItemState(description))
    private val sword = MiscItems("ett svärd")
    private val bottle = MiscItems("en flaska")

    private fun keyIsNotUsed(eventLog: EventLog): Boolean = eventLog.log().none { it is KeyUsedSuccessfully }
    private fun keyIsUsed(eventLog: EventLog): Boolean = ! keyIsNotUsed(eventLog)
    private val unusedKeyState = ItemState("Oanvänd nyckel")
    private val usedKeyState = ItemState("Använd nyckel")
    private val unusedKeyPredicate:Pair<(EventLog)-> Boolean,  ItemState> = Pair(::keyIsNotUsed, unusedKeyState)
    private val usedKeyPredicate:Pair<(EventLog)-> Boolean,  ItemState> = Pair(::keyIsUsed, usedKeyState)
    val keyStates = listOf(unusedKeyPredicate, usedKeyPredicate)

    val key = MultiStateItem(keyStates)


    private var placementMap: Map<Item, Placement> = mapOf(
        sword to InRoom(roomA),
        key to Carried,
        bottle to Carried
    )

    // Possible user input
    enum class ActionCommand: CommandType {
        UseKey,
        Dance
    }

    // Mapping user inputs to what event-returning function to run
    // ** Events **
    class KeyUsedSuccessfully(roomAndState: Pair<Room, RoomState>) : Event("The key was used successfully!", roomAndState)
    class KeyAlreadyUsed(roomAndState: Pair<Room, RoomState>) : Event("You have already used the key", roomAndState)
    class NoUsageOfKey(roomAndState: Pair<Room, RoomState>) : Event("You cannot use the key here!", roomAndState)
    class NoKeyToBeUsed(roomAndState: Pair<Room, RoomState>) : Event("You havn't got a key, have you?", roomAndState)

    class ThingUsedSuccessfully(roomAndState: Pair<Room, RoomState>) : Event("The Thing was used successfully!", roomAndState)

    class DancingEvent(roomAndState: Pair<Room, RoomState>): Event("Dance, dance, dance!", roomAndState)

    val actionMap: Map<CommandType, (Input, EventLog) -> Event> = mapOf(
        GoCommand.GoEast to actionForGo(connectedRooms),
        GoCommand.GoWest to actionForGo(connectedRooms),
        GoCommand.GoNorth to actionForGo(connectedRooms),
        GoCommand.GoSouth to actionForGo(connectedRooms),
        ActionCommand.UseKey to ::useKey,
        ActionCommand.Dance to { _, eventlog  -> DancingEvent(eventlog.getCurrentRoomAndState(Player)) }
    )


    fun useKey(input: Input, eventLog: EventLog): Event {
        if(!carriedItems(eventLog).contains(key)){
            return NoKeyToBeUsed(eventLog.getCurrentRoomAndState(Player))
        }

        if(eventLog.getCurrentRoom(Player) != roomA){
            return NoUsageOfKey(eventLog.getCurrentRoomAndState(Player))
        }
        val currentKey = carriedItems(eventLog).first{it == key}

        if(currentKey.state(eventLog) == usedKeyState) {
            return KeyAlreadyUsed(eventLog.getCurrentRoomAndState(Player))
        }

        return KeyUsedSuccessfully(eventLog.getCurrentRoomAndState(Player));
    }


    @Test
    fun `can do an action in any room without carrying any item`() {
        val eventLog = EventLog.fromList(listOf(NewRoomEvent("", Pair(roomA, stateA), Player))) // <- simple eventlog with only the start room/state
        val game = Game(connectedRooms, placementMap, actionMap, eventlog = eventLog)

        val event = game.playerDo(Input(ActionCommand.Dance), game.eventlog)
        expectThat(event).isA<DancingEvent>()
    }

    @Test
    fun `must carry an item to do an action`() {
        val eventLog = EventLog.fromList(listOf(NewRoomEvent("", Pair(roomA, stateA), Player))) // <- simple eventlog with only the start room/state
        val game = Game(connectedRooms, placementMap, actionMap, eventlog = eventLog)


        expectThat(carriedItems(game.eventlog)).contains(key)
        val event = game.playerDo(Input(ActionCommand.UseKey), game.eventlog)
        expectThat(event).isA<KeyUsedSuccessfully>()
    }

    @Test
    fun `can do action that changes state of carried item`() {
        val eventLog = EventLog.fromList(listOf(NewRoomEvent("", Pair(roomA, stateA), Player))) // <- simple eventlog with only the start room/state
        val game = Game(connectedRooms, placementMap, actionMap, eventlog = eventLog)

        expectThat(carriedItems(game.eventlog)).contains(key)
        expectThat(key.state(game.eventlog)).isEqualTo(unusedKeyState)

        val resultingEvent = game.playerDo(Input(ActionCommand.UseKey), game.eventlog)
        game.eventlog.add(resultingEvent)

        expectThat(carriedItems(game.eventlog)).contains(key)
        expectThat(key.state(game.eventlog)).isEqualTo(usedKeyState)
    }


    @Test
    fun `cannot do action if not carrying correct item`() {
        val currentRoom = roomA
        val currentState = stateA
        val eventLog = EventLog.fromList(listOf(NewRoomEvent("", Pair(currentRoom, currentState), Player))) // <- simple eventlog with only the start room/state
        val game = Game(connectedRooms, placementMap, actionMap, eventlog = eventLog)

        // Drop the key
        val resultingEvent = actionForDropItem(key).invoke(Input(object: CommandType{}), game.eventlog )
        game.eventlog.add(resultingEvent)
        expectThat(carriedItems(game.eventlog)).doesNotContain(key)
        // Try to use it
        val event = game.playerDo(Input(ActionCommand.UseKey), game.eventlog)
        expectThat(event).isA<NoKeyToBeUsed>()
    }

    @Test
    fun `cannot do action specific for another room`() {
        val currentRoom = roomB     // <- starts in roomB where the key can't be used
        val currentState = stateB
        val eventLog = EventLog.fromList(listOf(NewRoomEvent("", Pair(currentRoom, currentState), Player))) // <- simple eventlog with only the start room/state
        val game = Game(connectedRooms, placementMap, actionMap, eventlog = eventLog)

        expectThat(carriedItems(game.eventlog)).contains(key)
        val event = game.playerDo(Input(ActionCommand.UseKey), game.eventlog)
        expectThat(event).isA<NoUsageOfKey>()
    }
}