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
@DisplayName("Given a world with rooms, states and connections, the player:")
class RoomsAndConnectionTest {

    val alwaysPass = { _: Input, _: Room -> true}
    val alwaysStop = { _: Input, _: Room -> false}

    val stateA = State("a")
    val stateB = State("b")
    val stateC = State("c")
    val stateD1 = State("d1")
    val stateD2 = State("d2")
    val roomA = Room(listOf(Pair(alwaysPass, stateA)))
    val roomB = Room(listOf(Pair(alwaysPass, stateB)))
    val roomC = Room(listOf(Pair(alwaysPass, stateC)))
    val roomD = Room(listOf(
        Pair(alwaysStop, stateD1),      // <- If in RoomD, never end up in room state D1
        Pair(alwaysPass, stateD2)))     // <- If in RoomD, always end up in room state D2

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


    val actionMap: Map<CommandType, (Input, EventLog, Items) -> Event> = mapOf(
        GoCommand.GoEast to actionForGo(connectionsMap),
        GoCommand.GoWest to actionForGo(connectionsMap),
        GoCommand.GoNorth to actionForGo(connectionsMap),
        GoCommand.GoSouth to actionForGo(connectionsMap),
    )

    @Test
    fun `starts in a room`() {
        val eventLog = EventLog.fromList(listOf(NewRoomEvent("", Pair(roomA, stateA), Player))) // <- simple eventlog with only the start room/state
        val game = Game(connectionsMap, eventlog = eventLog)
        expectThat(game.eventlog.getCurrentRoom()).isEqualTo(roomA)
    }

    @Test
    fun `can go to connected rooms`() {
        val eventLog = EventLog.fromList(listOf(NewRoomEvent("", Pair(roomA, stateA), Player))) // <- simple eventlog with only the start room/state
        val game = Game(connectionsMap, actionMap = actionMap, eventlog = eventLog)


        val goEastEvent = game.playerDo(Input(GoCommand.GoEast), game.eventlog)
        expectThat(goEastEvent).isA<NewRoomEvent>()
        expectThat((goEastEvent as NewRoomEvent).roomAndState.first).isEqualTo(roomB)

        val goSouthEvent = game.playerDo(Input(GoCommand.GoSouth), game.eventlog)
        expectThat(goSouthEvent).isA<NewRoomEvent>()
        expectThat((goSouthEvent as NewRoomEvent).roomAndState.first).isEqualTo(roomC)

        val goWestEvent = game.playerDo(Input(GoCommand.GoWest), game.eventlog)
        expectThat(goWestEvent).isA<NewRoomEvent>()
        expectThat((goWestEvent as NewRoomEvent).roomAndState.first).isEqualTo(roomC)
    }

    @Test
    fun `will end up in the first matching state in a room`() {
        val eventLog = EventLog.fromList(listOf(NewRoomEvent("", Pair(roomB, stateB), Player))) // <- simple eventlog with only the start room/state
        val game = Game(connectionsMap, actionMap = actionMap, eventlog = eventLog)

        val goNorthEvent = game.playerDo(Input(GoCommand.GoNorth), game.eventlog)
        expectThat(goNorthEvent).isA<NewRoomEvent>()
        expectThat((goNorthEvent as NewRoomEvent).roomAndState.first ).isEqualTo(roomD)
        expectThat((goNorthEvent).roomAndState.second ).isEqualTo(stateD2) // <- End up in D2, not D1

    }



    @Test
    fun `can not go to not connected rooms`() {
        val eventLog = EventLog.fromList(listOf(NewRoomEvent("", Pair(roomA, stateA), Player))) // <- simple eventlog with only the start room/state
        val game = Game(connectionsMap, actionMap = actionMap, eventlog = eventLog)

        val goNorthEvent = game.playerDo(Input(GoCommand.GoNorth), game.eventlog)
        expectThat(goNorthEvent).isA<SameRoomEvent>()
        expectThat((goNorthEvent as SameRoomEvent).roomAndState.first ).isEqualTo(roomA)
    }
}