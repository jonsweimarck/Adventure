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
    private val stateA = State("a")
    private val stateB = State("b")
    private val roomA = Room(listOf(Pair(alwaysPass, stateA)))
    private val roomB = Room(listOf(Pair(alwaysPass, stateB)))

    private val connectedRooms = mapOf(
        roomA to listOf(Pair(south, roomB)),
        roomB to listOf(Pair(north, roomA))
    )


    //    // All Items, as well as where they are placed
    class MiscItems(description: String): SinglestateItem(Itemstate(description))
    private val sword = MiscItems("ett svärd")
    private val bottle = MiscItems("en flaska")

    private fun keyIsNotUsed(eventLog: EventLog): Boolean = eventLog.log().none { it is KeyUsedSuccessfully }
    private fun keyIsUsed(eventLog: EventLog): Boolean = ! keyIsNotUsed(eventLog)
    private val unusedKeyState = Itemstate("Oanvänd nyckel")
    private val usedKeyState = Itemstate("Använd nyckel")
    private val unusedKeyPredicate:Pair<(EventLog)-> Boolean,  Itemstate> = Pair(::keyIsNotUsed, unusedKeyState)
    private val usedKeyPredicate:Pair<(EventLog)-> Boolean,  Itemstate> = Pair(::keyIsUsed, usedKeyState)
    val keyStates = listOf(unusedKeyPredicate, usedKeyPredicate)

    val key = MultistateItem(keyStates)


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
    class KeyUsedSuccessfully(roomAndState: Pair<Room, State>) : Event("The key was used successfully!", roomAndState)
    class KeyAlreadyUsed(roomAndState: Pair<Room, State>) : Event("You have already used the key", roomAndState)
    class NoUsageOfKey(roomAndState: Pair<Room, State>) : Event("You cannot use the key here!", roomAndState)
    class NoKeyToBeUsed(roomAndState: Pair<Room, State>) : Event("You havn't got a key, have you?", roomAndState)

    class ThingUsedSuccessfully(roomAndState: Pair<Room, State>) : Event("The Thing was used successfully!", roomAndState)

    class DancingEvent(roomAndState: Pair<Room, State>): Event("Dance, dance, dance!", roomAndState)

    val actionMap: Map<CommandType, (Input, EventLog, Items2) -> Event> = mapOf(
        GoCommand.GoEast to actionForGo2(connectedRooms),
        GoCommand.GoWest to actionForGo2(connectedRooms),
        GoCommand.GoNorth to actionForGo2(connectedRooms),
        GoCommand.GoSouth to actionForGo2(connectedRooms),
        ActionCommand.UseKey to ::useKey,
        ActionCommand.Dance to { _, eventlog, _  -> DancingEvent(eventlog.getCurrentRoomAndState(Player)) }
    )


    fun useKey(input: Input, eventLog: EventLog, items: Items2): Event {
        if(!carriedItems2(eventLog).contains(key)){
            return NoKeyToBeUsed(eventLog.getCurrentRoomAndState(Player))
        }

        if(eventLog.getCurrentRoom(Player) != roomA){
            return NoUsageOfKey(eventLog.getCurrentRoomAndState(Player))
        }
        val currentKey = carriedItems2(eventLog).first{it == key}

        if(currentKey.state(eventLog) == usedKeyState) {
            return KeyAlreadyUsed(eventLog.getCurrentRoomAndState(Player))
        }

        return KeyUsedSuccessfully(eventLog.getCurrentRoomAndState(Player));
    }


    @Test
    fun `can do an action in any room without carrying any item`() {
        val eventLog = EventLog.fromList(listOf(NewRoomEvent("", Pair(roomA, stateA), Player))) // <- simple eventlog with only the start room/state
        val game = Game2(connectedRooms, placementMap, actionMap, eventlog = eventLog)

        val event = game.playerDo(Input(ActionCommand.Dance), game.eventlog)
        expectThat(event).isA<DancingEvent>()
    }

    @Test
    fun `must carry an item to do an action`() {
        val eventLog = EventLog.fromList(listOf(NewRoomEvent("", Pair(roomA, stateA), Player))) // <- simple eventlog with only the start room/state
        val game = Game2(connectedRooms, placementMap, actionMap, eventlog = eventLog)


        expectThat(carriedItems2(game.eventlog)).contains(key)
        val event = game.playerDo(Input(ActionCommand.UseKey), game.eventlog)
        expectThat(event).isA<KeyUsedSuccessfully>()
    }

    @Test
    fun `can do action that changes state of carried item`() {
        val eventLog = EventLog.fromList(listOf(NewRoomEvent("", Pair(roomA, stateA), Player))) // <- simple eventlog with only the start room/state
        val game = Game2(connectedRooms, placementMap, actionMap, eventlog = eventLog)

        expectThat(carriedItems2(game.eventlog)).contains(key)
        expectThat(key.state(game.eventlog)).isEqualTo(unusedKeyState)

        val resultingEvent = game.playerDo(Input(ActionCommand.UseKey), game.eventlog)
        game.eventlog.add(resultingEvent)

        expectThat(carriedItems2(game.eventlog)).contains(key)
        expectThat(key.state(game.eventlog)).isEqualTo(usedKeyState)
    }


    @Test
    fun `cannot do action if not carrying correct item`() {
        val currentRoom = roomA
        val currentState = stateA
        val eventLog = EventLog.fromList(listOf(NewRoomEvent("", Pair(currentRoom, currentState), Player))) // <- simple eventlog with only the start room/state
        val game = Game2(connectedRooms, placementMap, actionMap, eventlog = eventLog)

        // Drop the key
        val resultingEvent = actionForDropItem2(key).invoke(Input(object: CommandType{}), game.eventlog, game.allItems )
        game.eventlog.add(resultingEvent)
        expectThat(carriedItems2(game.eventlog)).doesNotContain(key)
        // Try to use it
        val event = game.playerDo(Input(ActionCommand.UseKey), game.eventlog)
        expectThat(event).isA<NoKeyToBeUsed>()
    }

    @Test
    fun `cannot do action specific for another room`() {
        val currentRoom = roomB     // <- starts in roomB where the key can't be used
        val currentState = stateB
        val eventLog = EventLog.fromList(listOf(NewRoomEvent("", Pair(currentRoom, currentState), Player))) // <- simple eventlog with only the start room/state
        val game = Game2(connectedRooms, placementMap, actionMap, eventlog = eventLog)

        expectThat(carriedItems2(game.eventlog)).contains(key)
        val event = game.playerDo(Input(ActionCommand.UseKey), game.eventlog)
        expectThat(event).isA<NoUsageOfKey>()
    }
}