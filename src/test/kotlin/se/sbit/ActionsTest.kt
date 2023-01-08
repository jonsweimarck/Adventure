package se.sbit

import org.junit.jupiter.api.DisplayName
import strikt.assertions.*

/**
 * Note: To see the test names when running in IntelliJ IDE and not only in Gradle HTML report,
 * go to IntelliJ settings -> Build, Execution, Deployment -> Build Tools -> Gradle, and under "Run tests using" select "IntelliJ IDEA"
 */
@DisplayName("Given a world with items in rooms, the player:")
class ActionsTest {

    private val roomA = Room("a")
    private val roomB = Room("b")

    private val connectedRooms = mapOf(
        roomA to listOf(Pair(southGuard, roomB)),
        roomB to listOf(Pair(northGuard, roomA))
    )

    enum class TestItems(override val description: String) : ItemType {
        Sword("ett svärd"),
        Key("en nyckel"),
        Bottle("en flaska")
    }

    private var itemMap: Map<ItemType, Placement> = mapOf(
        TestItems.Sword to InRoom(roomA),
        TestItems.Key to InRoom(roomB),
        TestItems.Bottle to Carried
    )


    // Flyttas in i engine
//    interface ActionType

    class Actions {
        fun inRoom(currentRoom: Room): List<ActionItem> {
            return listOf(NotUsedKey)
        }
    }

    sealed class ActionItem

    // STOPP Flyttas in i engine
//
//    enum class TestAction : ActionType {
//        Key
//    }



    sealed class Key: ActionItem()
    object NotUsedKey: Key()
    object UsedKey: Key()

    sealed class Event
    data class KeyUsedSuccessfully(val newKey: Key): Event()
    data class KeyAlreadyUsed(val newKey: Key): Event()
    data class KeyCouldNotBeUsed(val newKey: Key): Event()
    data class NoKeyNotBeUsed(val newKey: Key): Event()

    fun useKey(currentKey: Key, currentRoom: Room, allItems: Items, allActions: Actions): Event {
        if(! allItems.carriedItems().contains(TestItems.Key)){
            return NoKeyNotBeUsed(currentKey);
        }
        if(allActions.inRoom(currentRoom).filterIsInstance<Key>().isEmpty()){
            return KeyCouldNotBeUsed(currentKey);
        }
        if(currentKey == UsedKey) {
            return KeyAlreadyUsed(currentKey)
        }
        return KeyUsedSuccessfully(UsedKey);
    }





/*
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