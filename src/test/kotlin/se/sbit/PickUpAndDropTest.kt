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
class PickUpAndDropTest {

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

    private var itemMap: Map<ItemType, Placement> = mapOf(
        WordItems.Sword to InRoom(roomA),
        WordItems.Key to InRoom(roomB),
        WordItems.Bottle to Carried
    )

    @Test
    fun `can carry item from start`() {
        val currentRoom = roomA
        val game = Game(connectedRooms, itemMap, currentRoom)

        expectThat(game.allItems.carriedItems()).containsExactly(WordItems.Bottle)
    }

    @Test
    fun `can pick up item in current room`() {
        val currentRoom = roomA
        val game = Game(connectedRooms, itemMap, currentRoom)

        expectThat(game.allItems.carriedItems()).containsExactly(WordItems.Bottle)
        game.allItems.pickUp(WordItems.Sword, currentRoom)
        expectThat(game.allItems.carriedItems()).containsExactlyInAnyOrder(WordItems.Bottle, WordItems.Sword)
    }


    @Test
    fun `cannot pick up item from another room`() {
        val currentRoom = roomA
        val game = Game(connectedRooms, itemMap, currentRoom)

        expectCatching {game.allItems.pickUp(WordItems.Key, roomA)}.isFailure()
    }

    @Test
    fun `cannot pick up item already carried`() {
        val currentRoom = roomA
        val game = Game(connectedRooms, itemMap, currentRoom)

        expectCatching {game.allItems.pickUp(WordItems.Bottle, roomA)}.isFailure()
    }

    @Test
    fun `can drop carried item`() {
        val currentRoom = roomA
        val game = Game(connectedRooms, itemMap, currentRoom)

        expectThat(game.allItems.carriedItems()).containsExactly(WordItems.Bottle)
        game.allItems.drop(WordItems.Bottle, roomA)
        expectThat(game.allItems.carriedItems()).isEmpty()
    }

    @Test
    fun `cannot drop item not carried`() {
        val currentRoom = roomA
        val game = Game(connectedRooms, itemMap, currentRoom)

        expectCatching {game.allItems.drop(WordItems.Key, roomA)}.isFailure()
    }

}