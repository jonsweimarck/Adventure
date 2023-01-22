package se.sbit.adventure.engine

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import se.sbit.adventure.engine.*
import strikt.api.expectThat
import strikt.assertions.isA
import strikt.assertions.isEqualTo

/**
 * Note: To see the test names when running in IDE and not only in Gradle HTML report,
 * go to IntelliJ settings -> Build, Execution, Deployment -> Build Tools -> Gradle and under "Run tests using" select "IntelliJ IDEA"
 */
@DisplayName("Given a world with rooms and connections, the player:")
class RoomsAndConnectionTest {

    val roomA = Room("a")
    val roomB = Room("b")
    val roomC = Room("c")
    val roomD = Room("d")

    val connectionsMap = mapOf (
        roomA to listOf(
            Pair(east, roomB),
            Pair(west or south , roomC)),
        roomB to listOf(
            Pair(west, roomA),
            Pair(north, roomD)),
        roomC to listOf(Pair(west or north, roomA)),
        roomD to listOf(Pair(south, roomB)),
    )

    // No states in the rooms
    val alwaysPass = { _: Input, _: Room -> true}
    private val roomStates = mapOf(
        roomA to listOf(Pair(alwaysPass, roomA)),
        roomB to listOf(Pair(alwaysPass, roomB)),
        roomC to listOf(Pair(alwaysPass, roomC)),
        roomD to listOf(Pair(alwaysPass, roomD))
    )


    val actionMap: Map<CommandType, (Input, Room, Room, Items) -> Event> = mapOf(
        GoCommand.GoEast to goActionFromRoomConnectionsMap(connectionsMap, roomStates),
        GoCommand.GoWest to goActionFromRoomConnectionsMap(connectionsMap, roomStates),
        GoCommand.GoNorth to goActionFromRoomConnectionsMap(connectionsMap, roomStates),
        GoCommand.GoSouth to goActionFromRoomConnectionsMap(connectionsMap, roomStates),
    )

    @Test
    fun `starts in a room`() {
        val game = Game(connectionsMap, startRoom = roomA, startState = roomA)
        expectThat(game.startRoom).isEqualTo(roomA)
    }

    @Test
    fun `can go to connected rooms`() {
        val game = Game(connectionsMap, actionMap = actionMap, startRoom = roomA, startState = roomA)

        val goEastEvent = game.playerDo(Input(GoCommand.GoEast), roomA, roomA)
        expectThat(goEastEvent).isA<NewRoomEvent>()
        expectThat((goEastEvent as NewRoomEvent).newRoom ).isEqualTo(roomB)

        val goSouthEvent = game.playerDo(Input(GoCommand.GoSouth), roomA, roomA)
        expectThat(goSouthEvent).isA<NewRoomEvent>()
        expectThat((goSouthEvent as NewRoomEvent).newRoom).isEqualTo(roomC)

        val goWestEvent = game.playerDo(Input(GoCommand.GoWest), roomA, roomA)
        expectThat(goWestEvent).isA<NewRoomEvent>()
        expectThat((goWestEvent as NewRoomEvent).newRoom).isEqualTo(roomC)
    }



    @Test
    fun `can not go to not connected rooms`() {
        val game = Game(connectionsMap, actionMap = actionMap, startRoom = roomA, startState = roomA)

        val goNorthEvent = game.playerDo(Input(GoCommand.GoNorth), roomA, roomA)
        expectThat(goNorthEvent).isA<SameRoomEvent>()
        expectThat((goNorthEvent as SameRoomEvent).newRoom ).isEqualTo(roomA)
    }
}