package se.sbit

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
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
        roomA to listOf(Pair(eastGuard, roomB),
            Pair(westGuard or southGuard , roomC)),
        roomB to listOf(Pair(westGuard, roomA),
            Pair(northGuard, roomD)),
        roomC to listOf(Pair(westGuard or northGuard, roomA)),
        roomD to listOf(Pair(southGuard, roomB)),
    )


    val actionMap: Map<CommandType, (Input, Room, Items) -> Event> = mapOf(
        GoCommand.GoEast to goActionFromRoomConnectionsMap(connectionsMap),
        GoCommand.GoWest to goActionFromRoomConnectionsMap(connectionsMap),
        GoCommand.GoNorth to goActionFromRoomConnectionsMap(connectionsMap),
        GoCommand.GoSouth to goActionFromRoomConnectionsMap(connectionsMap))

    @Test
    fun `starts in a room`() {
        val game = Game(connectionsMap, startRoom = roomA)
        expectThat(game.startRoom).isEqualTo(roomA)
    }

    @Test
    fun `can go to connected rooms`() {
        val game = Game(connectionsMap, actionMap = actionMap, startRoom = roomA)

        val goEastEvent = game.playerDo(Input(GoCommand.GoEast), roomA)
        expectThat(goEastEvent).isA<NewRoomEvent>()
        expectThat((goEastEvent as NewRoomEvent).newRoom ).isEqualTo(roomB)

        val goSouthEvent = game.playerDo(Input(GoCommand.GoSouth), roomA)
        expectThat(goSouthEvent).isA<NewRoomEvent>()
        expectThat((goSouthEvent as NewRoomEvent).newRoom).isEqualTo(roomC)

        val goWestEvent = game.playerDo(Input(GoCommand.GoWest), roomA)
        expectThat(goWestEvent).isA<NewRoomEvent>()
        expectThat((goWestEvent as NewRoomEvent).newRoom).isEqualTo(roomC)
    }



    @Test
    fun `can not go to not connected rooms`() {
        val game = Game(connectionsMap, actionMap = actionMap, startRoom = roomA)

        val goNorthEvent = game.playerDo(Input(GoCommand.GoNorth), roomA)
        expectThat(goNorthEvent).isA<SameRoomEvent>()
        expectThat((goNorthEvent as SameRoomEvent).newRoom ).isEqualTo(roomA)
    }
}