package se.sbit.adventure.engine

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import se.sbit.adventure.engine.*
import strikt.api.expectCatching
import strikt.api.expectThat
import strikt.assertions.*

/**
 * Note: To see the test names when running in IntelliJ IDE and not only in Gradle HTML report,
 * go to IntelliJ settings -> Build, Execution, Deployment -> Build Tools -> Gradle, and under "Run tests using" select "IntelliJ IDEA"
 */
@DisplayName("The world can be configured so the player:")
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


    // All Items, as well as where they are placed
    sealed class MiscItems(override val description: String): ItemType
    object Sword: MiscItems("ett svärd")
    object Bottle: MiscItems("en flaska")
    object UnusedThing: MiscItems("Oanvänd grej")
    object UsedThing: MiscItems("Använd grej")

    sealed class Key(override val description: String): ItemType
    object UnusedKey: Key("Oanvänd nyckel")
    object UsedKey: Key("Använd nyckel")

    private var placementMap: Map<ItemType, Placement> = mapOf(
        Sword to InRoom(roomA),
        UnusedKey to Carried,
        UnusedThing to Carried,
        Bottle to Carried
    )

    // Possible user input
    enum class ActionCommand: CommandType {
        UseKey,
        UseThing,
        UseNotCarriedThing,
        Dance
    }

    // Mapping user inputs to what event-returning function to run
    data class KeyUsedSuccessfully(val newKey: Key) : Event("The was used successfully!")
    data class KeyAlreadyUsed(val newKey: Key) : Event("You have already used the key")
    object NoUsageOfKey : Event("You cannot use the key here!")
    object NoKeyToBeUsed : Event("You havn't got a key, have you?")

    object ThingUsedSuccessfully : Event("The Thing was used successfully!")

    object DancingEvent: Event("Dance, dance, dance!")

    val actionMap: Map<CommandType, (Input, EventLog, Items) -> Event> = mapOf(
        GoCommand.GoEast to actionForGo(connectedRooms),
        GoCommand.GoWest to actionForGo(connectedRooms),
        GoCommand.GoNorth to actionForGo(connectedRooms),
        GoCommand.GoSouth to actionForGo(connectedRooms),
        ActionCommand.UseKey to ::useKey,
        ActionCommand.UseThing to ::useThing,
        ActionCommand.UseNotCarriedThing to ::useUncarriedThing,
        ActionCommand.Dance to { _, _, _  -> DancingEvent }
    )


    fun useKey(input: Input, eventLog: EventLog, items: Items): Event {
        if(items.carriedItems().filterIsInstance<Key>().isEmpty()){
            return NoKeyToBeUsed;
        }

        if(eventLog.getCurrentRoom() != roomA){
            return NoUsageOfKey;
        }
        val currentKey = items.carriedItems().filterIsInstance<Key>().first()

        if(currentKey == UsedKey) {
            return KeyAlreadyUsed(currentKey)
        }

        return KeyUsedSuccessfully(UsedKey);
    }

    fun useThing(input: Input, eventLog: EventLog, items: Items): Event {
        items.replaceCarried(UnusedThing, UsedThing)
        return ThingUsedSuccessfully;
    }

    fun useUncarriedThing(input: Input, eventLog: EventLog, items: Items): Event {
        items.replaceCarried(Sword, UsedThing) // <- Should throw as we are not carrying a Receipt
        return ThingUsedSuccessfully;
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


        expectThat(game.allItems.carriedItems()).contains(UnusedKey)
        val event = game.playerDo(Input(ActionCommand.UseKey), game.eventlog)
        expectThat(event).isA<KeyUsedSuccessfully>()
        expectThat((event as KeyUsedSuccessfully).newKey).isEqualTo(UsedKey)
    }

    @Test
    fun `can do action that replaces carried item`() {
        val eventLog = EventLog.fromList(listOf(NewRoomEvent("", Pair(roomA, stateA), Player))) // <- simple eventlog with only the start room/state
        val game = Game(connectedRooms, placementMap, actionMap, eventlog = eventLog)

        expectThat(game.allItems.carriedItems()).contains(UnusedThing)
        expectThat(game.allItems.carriedItems()).doesNotContain(UsedThing)

        game.playerDo(Input(ActionCommand.UseThing), game.eventlog)

        expectThat(game.allItems.carriedItems()).contains(UsedThing)
        expectThat(game.allItems.carriedItems()).doesNotContain(UnusedThing)
    }

    @Test
    fun `cannot do action that replaces carried item when item is not carried`() {
        val eventLog = EventLog.fromList(listOf(NewRoomEvent("", Pair(roomA, stateA), Player))) // <- simple eventlog with only the start room/state
        val game = Game(connectedRooms, placementMap, actionMap, eventlog = eventLog)

        expectThat(game.allItems.carriedItems()).doesNotContain(Sword)

        expectCatching {game.playerDo(Input(ActionCommand.UseNotCarriedThing), game.eventlog)}.isFailure()
    }

    @Test
    fun `cannot do action if not carrying correct item`() {
        val currentRoom = roomA
        val currentState = stateA
        val eventLog = EventLog.fromList(listOf(NewRoomEvent("", Pair(currentRoom, currentState), Player))) // <- simple eventlog with only the start room/state
        val game = Game(connectedRooms, placementMap, actionMap, eventlog = eventLog)

        game.allItems.drop(UnusedKey, currentRoom)
        expectThat(game.allItems.carriedItems()).doesNotContain(UnusedKey)
        val event = game.playerDo(Input(ActionCommand.UseKey), game.eventlog)
        expectThat(event).isA<NoKeyToBeUsed>()
    }

    @Test
    fun `cannot do action specific for another room`() {
        val currentRoom = roomB     // <- starts in roomB where the key can't be used
        val currentState = stateB
        val eventLog = EventLog.fromList(listOf(NewRoomEvent("", Pair(currentRoom, currentState), Player))) // <- simple eventlog with only the start room/state
        val game = Game(connectedRooms, placementMap, actionMap, eventlog = eventLog)

        expectThat(game.allItems.carriedItems()).contains(UnusedKey)
        val event = game.playerDo(Input(ActionCommand.UseKey), game.eventlog)
        expectThat(event).isA<NoUsageOfKey>()
    }


}