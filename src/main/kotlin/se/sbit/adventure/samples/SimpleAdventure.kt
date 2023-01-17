package se.sbit.adventure.samples

import se.sbit.adventure.engine.*

// Setting up two rooms, connected North-South
private val roomA = Room(
    """
        |Du står på en gräsmatta i en villaträdgård. 
        |Trädgården har höga häckan åt tre väderstreck, och åt söder ligger villan.
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
    TakeKey,
    DropKey,
    UseKey,
    Dance,
    GibberishInput,
    EndGame
}

// Mapping user inputs to what event-returning function to run
data class KeyUsedSuccessfully(val newKey: Key) : Event("The was used successfully!")
data class KeyAlreadyUsed(val newKey: Key) : Event("You have already used the key")
object NoUsageOfKey : Event("You cannot use the key here!")
object NoKeyToBeUsed : Event("You havn't got a key, have you?")


val actionMap: Map<CommandType, (Input, Room, Items) -> Event> = mapOf(
    GoCommand.GoEast to goActionFromRoomConnectionsMap(connectedRooms, "Så kan du inte gå!"),
    GoCommand.GoWest to goActionFromRoomConnectionsMap(connectedRooms,"Så kan du inte gå!"),
    GoCommand.GoNorth to goActionFromRoomConnectionsMap(connectedRooms,"Så kan du inte gå!"),
    GoCommand.GoSouth to goActionFromRoomConnectionsMap(connectedRooms,"Så kan du inte gå!"),
    ActionCommand.UseKey to ::useKey,
    ActionCommand.Dance to { _, _, _  -> Event("Dance, dance, dance!")},
    ActionCommand.GibberishInput to { _, _, _  -> Event("Sorry, I don't understand that!") },
    ActionCommand.EndGame to { _, _, _  -> EndEvent },)


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

val input2Command: Map<String, CommandType> = mapOf (
    "go south" to GoCommand.GoSouth,
    "south" to GoCommand.GoSouth,
    "go north" to GoCommand.GoNorth,
    "north" to GoCommand.GoNorth,
    "exit" to ActionCommand.EndGame,
    "dance" to ActionCommand.Dance,
    "ta nyckel" to ActionCommand.TakeKey


)


fun main() {
    println("**************  Simple Adventure ****************")


    val game = Game(connectedRooms, placementMap, actionMap, itemUsageRoomMap, startRoom)
    var event: Event = NewRoomEvent("Welcome!\n", startRoom)
    var currentRoom = startRoom
    while (event !is EndEvent){
        StandardInOut.showText(formatGameTextAndItems(event.gameText, game.allItems.itemsIn(currentRoom)))

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

