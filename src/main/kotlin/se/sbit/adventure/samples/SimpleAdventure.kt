package se.sbit.adventure.samples

import se.sbit.adventure.engine.*

// Setting up two rooms, connected North-South
private val roomA = Room("a")
private val roomB = Room("b")

val currentRoom = roomA

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
    GoCommand.GoEast to goActionFromRoomConnectionsMap(connectedRooms),
    GoCommand.GoWest to goActionFromRoomConnectionsMap(connectedRooms),
    GoCommand.GoNorth to goActionFromRoomConnectionsMap(connectedRooms),
    GoCommand.GoSouth to goActionFromRoomConnectionsMap(connectedRooms),
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
    "exit" to ActionCommand.EndGame


)


fun main() {
    println("**************  Simple Adventure ****************")


    val game = Game(connectedRooms, placementMap, actionMap, itemUsageRoomMap, currentRoom)
    var event: Event = NewRoomEvent("Welcome!\n", currentRoom)

    while (event !is EndEvent){
        StandardInOut.showText(event.gameText)
        val input:String = StandardInOut.waitForInput()
        event = game.playerDo(Input(Interpreter.interpret(input, input2Command, ActionCommand.GibberishInput)), currentRoom)
    }
}
