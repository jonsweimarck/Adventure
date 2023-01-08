package se.sbit

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import strikt.api.expectCatching
import strikt.api.expectThat
import strikt.assertions.*

/**
 * Note: To see the test names when running in IntelliJ IDE and not only in Gradle HTML report,
 * go to IntelliJ settings -> Build, Execution, Deployment -> Build Tools -> Gradle, and under "Run tests using" select "IntelliJ IDEA"
 */
@DisplayName("Given a world with items in rooms, the player:")
class ActionsTest {
/*
    private val roomA = Room("a")
    private val roomB = Room("b")

    private val connectedRooms = mapOf(
        roomA to listOf(Pair(southGuard, roomB)),
        roomB to listOf(Pair(northGuard, roomA))
    )

    enum class WordItems(override val description: String) : ItemType {
        Sword("ett svärd"),
        Key("en nyckel"),
        Bottle("en flaska")
    }

    private var itemMap: Map<ItemType, Placement<ItemType, Room>> = mapOf(
        WordItems.Sword to InRoom(WordItems.Sword, roomA),
        WordItems.Key to InRoom(WordItems.Key, roomB),
        WordItems.Bottle to Carried(WordItems.Bottle)
    )

    @Test
    fun `can do action without using item`() {
        val currentRoom = roomA
        val game = Game(connectedRooms, itemMap, currentRoom)

        game.playeractions.do(Action.PressButton)

        expectThat(game.allItems.carriedItems()).containsExactly(WordItems.Bottle)
    }

    @Test
    fun `can use item in an action`() {
        val currentRoom = roomA
        val game = Game(connectedRooms, itemMap, currentRoom)

        expectThat(game.allItems.carriedItems()).containsExactly(WordItems.Bottle)
        game.allItems.pickUp(WordItems.Sword, currentRoom)
        expectThat(game.allItems.carriedItems()).containsExactlyInAnyOrder(WordItems.Bottle, WordItems.Sword)
    }

    @Test
    fun `can get lits of all possible actions in current room`() {}

    @Test
    fun `cannot do action not listed in the current room`() {}
*/
}