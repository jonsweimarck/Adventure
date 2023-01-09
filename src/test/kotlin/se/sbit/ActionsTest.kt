package se.sbit

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.*

/**
 * Note: To see the test names when running in IntelliJ IDE and not only in Gradle HTML report,
 * go to IntelliJ settings -> Build, Execution, Deployment -> Build Tools -> Gradle, and under "Run tests using" select "IntelliJ IDEA"
 */
@DisplayName("The world can be configured so the player:")
class ActionsTest {

    // Setting up two rooms, connected North-South
    private val roomA = Room("a")
    private val roomB = Room("b")

    private val connectedRooms = mapOf(
        roomA to listOf(Pair(southGuard, roomB)),
        roomB to listOf(Pair(northGuard, roomA))
    )


    // All Items, as well as where they are placed, and in what rooms they can be used
    sealed class MiscItems(override val description: String): ItemType
    object Sword: MiscItems("ett svärd")
    object Bottle: MiscItems("en flaska")

    sealed class Key(override val description: String): ItemType
    object UnusedKey: Key("Oanvänd nyckel")
    object UsedKey: Key("Använd nyckel")

    private var placementMap: Map<ItemType, Placement> = mapOf(
        Sword to InRoom(roomA),
        UnusedKey to Carried,
        Bottle to Carried
    )

    val itemUsageRoomMap: Map<ItemType, Room> = mapOf(
        UnusedKey to roomA,
        Bottle to roomB)

    // Possible user input
    enum class ActionCommand: CommandType {
        UseKey,
        Dance
    }

    // Mapping user inputs to what event-returning function to run
    data class KeyUsedSuccessfully(val newKey: Key) : Event()
    data class KeyAlreadyUsed(val newKey: Key) : Event()
    object NoUsageOfKey : Event()
    object NoKeyToBeUsed : Event()

    object DancingEvent: Event()

    val actionMap: Map<CommandType, (Input, Room, Items) -> Event> = mapOf(
        ActionCommand.UseKey to ::useKey,
        ActionCommand.Dance to { _, _, _  -> DancingEvent })


    fun useKey(input:Input, currentRoom: Room, items: Items): Event {
        if(items.carriedItems().filterIsInstance<Key>().isEmpty()){
            return NoKeyToBeUsed;
        }

        if(items.usableItemsInRoom(currentRoom).filterIsInstance<Key>().isEmpty()){
            return NoUsageOfKey;
        }
        val currentKey = items.usableItemsInRoom(currentRoom).filterIsInstance<Key>().first()

        if(currentKey == UsedKey) {
            return KeyAlreadyUsed(currentKey)
        }

        return KeyUsedSuccessfully(UsedKey);
    }




    @Test
    fun `can do an action in any room without carrying any item`() {
        val currentRoom = roomA
        val game = Game(connectedRooms, placementMap, actionMap, itemUsageRoomMap = emptyMap(), currentRoom)

        val event = game.playerDo(Input(ActionCommand.Dance), currentRoom)
        expectThat(event).isA<DancingEvent>()
    }


    @Test
    fun `must carry an item to do an action`() {
        val currentRoom = roomA
        val game = Game(connectedRooms, placementMap, actionMap, itemUsageRoomMap, currentRoom)

        expectThat(game.allItems.carriedItems()).contains(UnusedKey)
        val event = game.playerDo(Input(ActionCommand.UseKey), currentRoom)
        expectThat(event).isA<KeyUsedSuccessfully>()
        expectThat((event as  KeyUsedSuccessfully).newKey).isEqualTo(UsedKey)
    }

    @Test
    fun `cannot do action if not carrying correct item`() {
        val currentRoom = roomA
        val game = Game(connectedRooms, placementMap, actionMap, itemUsageRoomMap, currentRoom)

        game.allItems.drop(UnusedKey, currentRoom)
        expectThat(game.allItems.carriedItems()).doesNotContain(UnusedKey)
        val event = game.playerDo(Input(ActionCommand.UseKey), currentRoom)
        expectThat(event).isA<NoKeyToBeUsed>()
    }

    @Test
    fun `cannot do action specific for another room`() {
        val currentRoom = roomB     // <- starts in roomB where the key can't be used
        val game = Game(connectedRooms, placementMap, actionMap, itemUsageRoomMap, currentRoom)

        expectThat(game.allItems.carriedItems()).contains(UnusedKey)
        val event = game.playerDo(Input(ActionCommand.UseKey), currentRoom)
        expectThat(event).isA<NoUsageOfKey>()
    }


}