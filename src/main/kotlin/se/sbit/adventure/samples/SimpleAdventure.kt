package se.sbit.adventure.samples

import se.sbit.adventure.engine.*

// Setting up two rooms, connected North-South
private val roomA = Room(
    """
        |Du står på en gräsmatta i en villaträdgård. 
        |Trädgården har höga häckar åt tre väderstreck, och åt söder ligger villan.
    """.trimMargin())
private val roomB = Room(
    """
        |Du står på ett trädäck framför en villa. 
        |En dörr leder in i huset.
    """.trimMargin())

val startRoom = roomA

private val connectedRooms = mapOf(
    roomA to listOf(Pair(southGuard, roomB)),
    roomB to listOf(Pair(northGuard, roomA))
)

// All Items, as well as where they are placed, and in what rooms they can be used
sealed class MiscItems(override val description: String): ItemType
object Sword: MiscItems("ett svärd")
object Bottle: MiscItems("en flaska")

sealed class Key(override val description: String): ItemType
object UnusedKey: Key("en nyckel")
object UsedKey: Key("en nyckel")

private var placementMap: Map<ItemType, Placement> = mapOf(
    UnusedKey to InRoom(roomA),
    Sword to Carried,
    Bottle to InRoom(roomA)
)

val itemUsageRoomMap: Map<ItemType, Room> = mapOf(
    UnusedKey to roomA,
    Bottle to roomB)

// Possible user input
enum class ActionCommand: CommandType {
    TakeSword,
    DropSword,
    TakeKey,
    DropKey,
    UseKey,
    LookAround,
    Inventory,
    Dance,
    GibberishInput,
    EndGame
}


val input2Command: Map<String, CommandType> = mapOf (
    "gå söder" to GoCommand.GoSouth,
    "söder" to GoCommand.GoSouth,
    "gå norr" to GoCommand.GoNorth,
    "norr" to GoCommand.GoNorth,
    "exit" to ActionCommand.EndGame,
    "dansa" to ActionCommand.Dance,
    "ta svärd" to ActionCommand.TakeSword,
    "ta upp svärd" to ActionCommand.TakeSword,
    "släpp svärd" to ActionCommand.DropSword,
    "ta nyckel" to ActionCommand.TakeKey,
    "ta upp nyckel" to ActionCommand.TakeKey,
    "släpp nyckel" to ActionCommand.DropKey,
    "titta" to ActionCommand.LookAround,
    "inventory" to ActionCommand.Inventory,
    "i" to ActionCommand.Inventory,
)

// Mapping user inputs to what event-returning function to run
data class KeyUsedSuccessfully(val newKey: Key) : Event("The was used successfully!")
data class KeyAlreadyUsed(val newKey: Key) : Event("You have already used the key")
object NoUsageOfKey : Event("You cannot use the key here!")
object NoKeyToBeUsed : Event("You havn't got a key, have you?")


val actionMap: Map<CommandType, (Input, Room, Items) -> Event> = mapOf(
    GoCommand.GoEast to goActionFromRoomConnectionsMap(connectedRooms, "Du kan inte gå dit."),
    GoCommand.GoWest to goActionFromRoomConnectionsMap(connectedRooms,"Du kan inte gå dit.!"),
    GoCommand.GoNorth to goActionFromRoomConnectionsMap(connectedRooms,"Du kan inte gå dit."),
    GoCommand.GoSouth to goActionFromRoomConnectionsMap(connectedRooms,"Du kan inte gå dit."),
    ActionCommand.UseKey to ::useKey,
    ActionCommand.Dance to { _, _, _  -> Event("Dance, dance, dance!")},
    ActionCommand.GibberishInput to { _, _, _  -> Event("Hmmm, det där förstod jag inte!") },
    ActionCommand.EndGame to { _, _, _  -> EndEvent },
    ActionCommand.TakeSword to goActionForPickUpItem(Sword, "Går inte att ta upp en sådan här!", "Du tar upp"),
    ActionCommand.DropSword to goActionForDropItem(Sword, "Du har ingen sådan att släppa!", "Du släpper"),
    ActionCommand.TakeKey to goActionForPickUpItem(UnusedKey, "Går inte att ta upp en sådan här!", "Du tar upp"),
    ActionCommand.DropKey to goActionForDropItem(UnusedKey, "Du har ingen sådan att släppa!", "Du släpper"),
    ActionCommand.LookAround to { _, currentRoom, _  -> SameRoomEvent("Du tittar dig omkring.", currentRoom)},
    ActionCommand.Inventory to goActionForInventory("Du bär inte på något.", "Du bär på")
)


fun useKey(input: Input, currentRoom: Room, items: Items): Event {
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


fun main() {
    println("**************  Simple Adventure ****************")


    val game = Game(connectedRooms, placementMap, actionMap, itemUsageRoomMap, startRoom)
    var event: Event = NewRoomEvent("Welcome!\n", startRoom)
    var currentRoom = startRoom
    while (event !is EndEvent){

        StandardInOut.showText(
            if(event is RoomEvent) {
                formatGameTextAndItems(event.gameText, game.allItems.itemsIn(currentRoom))
            } else {
                event.gameText
            })

        val input:String = StandardInOut.waitForInput()
        event = game.playerDo(Input(Interpreter.interpret(input, input2Command, ActionCommand.GibberishInput)), currentRoom)
        if(event is RoomEvent) {
            currentRoom = event.newRoom
        }
    }
}

fun formatGameTextAndItems(gameText: String, items: List<ItemType>): String =
    if (items.isEmpty()) {
        gameText +"\n"
    } else{
        gameText +"\n" + "Du ser " + items.joinToString { it.description }
    }

