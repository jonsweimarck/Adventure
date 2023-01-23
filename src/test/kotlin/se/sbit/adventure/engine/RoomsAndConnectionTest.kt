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

    val alwaysPass = { _: Input, _: Room -> true}

    val stateA = State("a")
    val stateB = State("b")
    val stateC = State("c")
    val stateD = State("d")
    val roomA = Room(listOf(Pair(alwaysPass, stateA)))
    val roomB = Room(listOf(Pair(alwaysPass, stateB)))
    val roomC = Room(listOf(Pair(alwaysPass, stateC)))
    val roomD = Room(listOf(Pair(alwaysPass, stateD)))

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


    val actionMap: Map<CommandType, (Input, Room, State, Items) -> Event> = mapOf(
        GoCommand.GoEast to goActionFromRoomConnectionsMap(connectionsMap),
        GoCommand.GoWest to goActionFromRoomConnectionsMap(connectionsMap),
        GoCommand.GoNorth to goActionFromRoomConnectionsMap(connectionsMap),
        GoCommand.GoSouth to goActionFromRoomConnectionsMap(connectionsMap),
    )

    @Test
    fun `starts in a room`() {
        val game = Game(connectionsMap, startRoom = roomA, startState = stateA)
        expectThat(game.startRoom).isEqualTo(roomA)
    }

    @Test
    fun `can go to connected rooms`() {
        val game = Game(connectionsMap, actionMap = actionMap, startRoom = roomA, startState = stateA)

        val goEastEvent = game.playerDo(Input(GoCommand.GoEast), roomA, stateA)
        expectThat(goEastEvent).isA<NewRoomEvent>()
        expectThat((goEastEvent as NewRoomEvent).newRoom ).isEqualTo(roomB)

        val goSouthEvent = game.playerDo(Input(GoCommand.GoSouth), roomA, stateA)
        expectThat(goSouthEvent).isA<NewRoomEvent>()
        expectThat((goSouthEvent as NewRoomEvent).newRoom).isEqualTo(roomC)

        val goWestEvent = game.playerDo(Input(GoCommand.GoWest), roomA, stateA)
        expectThat(goWestEvent).isA<NewRoomEvent>()
        expectThat((goWestEvent as NewRoomEvent).newRoom).isEqualTo(roomC)
    }



    @Test
    fun `can not go to not connected rooms`() {
        val game = Game(connectionsMap, actionMap = actionMap, startRoom = roomA, startState = stateA)

        val goNorthEvent = game.playerDo(Input(GoCommand.GoNorth), roomA, stateA)
        expectThat(goNorthEvent).isA<SameRoomEvent>()
        expectThat((goNorthEvent as SameRoomEvent).newRoom ).isEqualTo(roomA)
    }
}