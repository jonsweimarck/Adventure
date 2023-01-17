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
@DisplayName("Given a world with items in rooms, the player:")
class PickUpAndDropTest {

    private val roomA = Room("a")
    private val roomB = Room("b")

    private val connectedRooms = mapOf(
        roomA to listOf(Pair(southGuard, roomB)),
        roomB to listOf(Pair(northGuard, roomA))
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
        val currentRoom = roomA
        val game = Game(connectedRooms, itemMap, startRoom = currentRoom)

        expectThat(game.allItems.carriedItems()).containsExactly(Bottle)
    }

    @Test
    fun `can pick up item in current room`() {
        val currentRoom = roomA
        val game = Game(connectedRooms, itemMap, startRoom = currentRoom)

        expectThat(game.allItems.carriedItems()).containsExactly(Bottle)
        game.allItems.pickUp(Sword, currentRoom)
        expectThat(game.allItems.carriedItems()).containsExactlyInAnyOrder(Bottle, Sword)
    }


    @Test
    fun `cannot pick up item from another room`() {
        val currentRoom = roomA
        val game = Game(connectedRooms, itemMap, startRoom = currentRoom)

        expectCatching {game.allItems.pickUp(Key, roomA)}.isFailure()
    }

    @Test
    fun `cannot pick up item already carried`() {
        val currentRoom = roomA
        val game = Game(connectedRooms, itemMap, startRoom = currentRoom)

        expectCatching {game.allItems.pickUp(Bottle, roomA)}.isFailure()
    }

    @Test
    fun `can drop carried item`() {
        val currentRoom = roomA
        val game = Game(connectedRooms, itemMap, startRoom = currentRoom)

        expectThat(game.allItems.carriedItems()).containsExactly(Bottle)
        game.allItems.drop(Bottle, roomA)
        expectThat(game.allItems.carriedItems()).isEmpty()
    }

    @Test
    fun `cannot drop item not carried`() {
        val currentRoom = roomA
        val game = Game(connectedRooms, itemMap, startRoom = currentRoom)

        expectCatching {game.allItems.drop(Key, roomA)}.isFailure()
    }

}