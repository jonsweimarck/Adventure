package se.sbit.adventure.engine

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import se.sbit.adventure.engine.*
import strikt.api.expectThat
import strikt.assertions.*

/**
 * Note: To see the test names when running in IntelliJ IDE and not only in Gradle HTML report,
 * go to IntelliJ settings -> Build, Execution, Deployment -> Build Tools -> Gradle, and under "Run tests using" select "IntelliJ IDEA"
 */
@DisplayName("Given a world with items in rooms, the player:")
class PickUpAndDropTest {

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


    sealed class TestItems(override val description: String): ItemType
    object Sword: TestItems("ett svärd")
    object Key: TestItems("en nyckel")
    object Bottle: TestItems("en flaska")

    private var itemMap: Map<ItemType, Placement> = mapOf(
        Sword to InRoom(roomA),
        Key to InRoom(roomB),
        Bottle to Carried
    )

    @Test
    fun `can carry item from start`() {
        val game = Game(connectedRooms, itemMap)

        expectThat(carriedItems(game.eventlog)).containsExactly(Bottle)
    }

    @Test
    fun `can pick up item in current room`() {
        val eventLog = EventLog.fromList(listOf(NewRoomEvent("start room", Pair(roomA, stateA), Player)))
        val game = Game(connectedRooms, itemMap, eventlog = eventLog)

        expectThat(carriedItems(game.eventlog)).containsExactly(Bottle)

        val resultingEvent = actionForPickUpItem(Sword).invoke(Input(object: CommandType{}), game.eventlog, game.allItems)
        game.eventlog.add(resultingEvent)

        expectThat(carriedItems(game.eventlog)).containsExactlyInAnyOrder(Bottle, Sword)
    }


    @Test
    fun `cannot pick up item from another room`() {

        val eventLog = EventLog.fromList(listOf(NewRoomEvent("start room", Pair(roomA, stateA), Player)))
        val game = Game(connectedRooms, itemMap, eventlog = eventLog)

        val result = actionForPickUpItem(Key).invoke(Input(object: CommandType{}), game.eventlog, game.allItems)
        expectThat(result).isA<NoSuchItemHereEvent>()
    }

    @Test
    fun `cannot pick up item already carried`() {
        val eventLog = EventLog.fromList(listOf(NewRoomEvent("start room", Pair(roomA, stateA), Player)))
        val game = Game(connectedRooms, itemMap, eventlog = eventLog)

        val result = actionForPickUpItem(Bottle).invoke(Input(object: CommandType{}), game.eventlog, game.allItems)
        expectThat(result).isA<NoSuchItemHereEvent>()
    }

    @Test
    fun `can drop carried item`() {
        val eventLog = EventLog.fromList(listOf(NewRoomEvent("start room", Pair(roomA, stateA), Player)))

        val game = Game(connectedRooms, itemMap, eventlog = eventLog)

        expectThat(carriedItems(game.eventlog)).containsExactly(Bottle)

        val resultingEvent = actionForDropItem(Bottle).invoke(Input(object: CommandType{}), game.eventlog, game.allItems)
        game.eventlog.add(resultingEvent)

        expectThat(carriedItems(game.eventlog)).isEmpty()
    }

    @Test
    fun `cannot drop item not carried`() {

        val eventLog = EventLog.fromList(listOf(NewRoomEvent("start room", Pair(roomA, stateA), Player)))
        val game = Game(connectedRooms, itemMap, eventlog = eventLog)

        val result = actionForDropItem(Key).invoke(Input(object: CommandType{}), game.eventlog, game.allItems)
        expectThat(result).isA<NoSuchItemToDropItemEvent>()
    }

}