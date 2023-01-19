package se.sbit.adventure.samples

import se.sbit.adventure.engine.*

// Setting up two rooms, connected North-South
private val garden = Room(
    """
        |Du står på en gräsmatta i en villaträdgård. 
        |Trädgården har höga häckar åt tre väderstreck, och åt söder ligger villan.
    """.trimMargin())
private val inFrontOfClosedDoor = Room(
    """
        |Du står på ett trädäck framför en villa. 
        |En stängd dörr leder in i huset.
    """.trimMargin())

private val inFrontOfOpenDoor = Room(
    """
        |Du står på ett trädäck framför en villa. 
        |En öppen dörr leder in i huset.
    """.trimMargin())

private val insideDarkRoom = Room(
    """
        |Du står i ett alldeles mörkt rum. Du kan ana en golvlampa vid din ena sida.
        |Åt norr leder den öppna dörren ut ur huset.
    """.trimMargin())

private val insideLitRoom = Room(
    """
        |Du står i ett rum endast upplyst av en golvlampa.
        |Åt norr leder den öppna dörren ut ur huset.
    """.trimMargin())

val startRoom = garden

private val connectedRooms = mapOf(
    garden to listOf(
        Pair(southGuard and ::doorIsClosed, inFrontOfClosedDoor),
        Pair(southGuard and ::doorIsOpened, inFrontOfOpenDoor)),
    inFrontOfClosedDoor to listOf(Pair(northGuard, garden)),
    inFrontOfOpenDoor to listOf(
        Pair(northGuard, garden),
        Pair(::enterRoom and ::lightIsOff, insideDarkRoom),
        Pair(::enterRoom and ::lightIsOn, insideLitRoom)),
    insideDarkRoom to listOf(
        Pair(northGuard or ::exitRoom, inFrontOfOpenDoor),
        Pair(::lightIsOn, insideLitRoom)),
    insideLitRoom to listOf(
        Pair(::lightIsOff, insideDarkRoom),
        Pair(northGuard or ::exitRoom, inFrontOfOpenDoor)),
)

fun doorIsOpened(input: Input, room: Room): Boolean = ! doorIsClosed(input, room)
fun doorIsClosed(input: Input, room: Room): Boolean = EventLog.log().filterIsInstance<KeyUsedSuccessfully>().isEmpty()
fun enterRoom(input: Input, room: Room): Boolean = input.command == ActionCommand.EnterHouse
fun exitRoom(input: Input, room: Room): Boolean = input.command == ActionCommand.ExitHouse
fun lightIsOn(input: Input, room: Room): Boolean = EventLog.log().filterIsInstance<SwitchedLightOnEvent>().size > EventLog.log().filterIsInstance<SwitchedLightOffEvent>().size
fun lightIsOff(input: Input, room: Room): Boolean = ! lightIsOn(input, room)


// All Items, as well as where they are placed, and in what rooms they can be used
sealed class MiscItems(override val description: String): ItemType
object Sword: MiscItems("ett svärd")
object Bottle: MiscItems("en flaska")

sealed class Key(override val description: String): ItemType
object UnusedKey: Key("en nyckel")
object UsedKey: Key("en nyckel")

object Chainsaw: MiscItems("en motorsåg")

private var placementMap: Map<ItemType, Placement> = mapOf(
    UnusedKey to InRoom(garden),
    Sword to Carried,
    Bottle to InRoom(garden),
    Chainsaw to InRoom(insideLitRoom),
)

val itemUsageRoomMap: Map<ItemType, Room> = mapOf(
    UnusedKey to inFrontOfClosedDoor,)

// Possible user input
enum class ActionCommand: CommandType {
    TakeSword,
    DropSword,
    TakeKey,
    DropKey,
    UseKey,
    EnterHouse,
    ExitHouse,
    SwitchOnLight,
    SwitchOffLight,
    TakeChainsaw,
    DropChainsaw,
    LookAround,
    Inventory,
    Dance,
    GibberishInput,
    EndGame
}


val input2Command: Map<String, CommandType> = mapOf (
    "gå söder" to GoCommand.GoSouth,
    "söder" to GoCommand.GoSouth,
    "s" to GoCommand.GoSouth,
    "gå norr" to GoCommand.GoNorth,
    "norr" to GoCommand.GoNorth,
    "n" to GoCommand.GoNorth,
    "exit" to ActionCommand.EndGame,
    "dansa" to ActionCommand.Dance,
    "ta svärd" to ActionCommand.TakeSword,
    "ta upp svärd" to ActionCommand.TakeSword,
    "släpp svärd" to ActionCommand.DropSword,
    "ta nyckel" to ActionCommand.TakeKey,
    "ta upp nyckel" to ActionCommand.TakeKey,
    "släpp nyckel" to ActionCommand.DropKey,
    "använd nyckel" to ActionCommand.UseKey,
    "öppna dörren" to ActionCommand.UseKey,
    "gå in" to ActionCommand.EnterHouse,
    "gå ut" to ActionCommand.ExitHouse,
    "tänd lampan" to ActionCommand.SwitchOnLight,
    "släck lampan" to ActionCommand.SwitchOffLight,
    "ta motorsåg" to ActionCommand.TakeChainsaw,
    "ta motorsågen" to ActionCommand.TakeChainsaw,
    "ta upp motorsåg" to ActionCommand.TakeChainsaw,
    "ta upp motorsågen" to ActionCommand.TakeChainsaw,
    "släpp motorsåg" to ActionCommand.DropChainsaw,
    "släpp motorsågen" to ActionCommand.DropChainsaw,
    "titta" to ActionCommand.LookAround,
    "inventory" to ActionCommand.Inventory,
    "i" to ActionCommand.Inventory,
)

// Mapping user inputs to what event-returning function to run
class KeyUsedSuccessfully(newRoom: Room) : RoomEvent("Du låser upp och öppnar dörren.", newRoom)
object KeyAlreadyUsed : Event("Du har redan låst upp dörren.")
object NoUsageOfKey : Event("Du kan inte använda en nyckel här.")
object NoKeyToBeUsed : Event("Du har väl ingen nyckel?")

class SwitchedLightOnEvent(newRoom: Room): RoomEvent("Nu blev det ljust!", newRoom)
class SwitchedLightOffEvent(currentRoom: Room): RoomEvent("Nu blev det mörkt igen!", currentRoom)


val actionMap: Map<CommandType, (Input, Room, Items) -> Event> = mapOf(
    GoCommand.GoEast to goActionFromRoomConnectionsMap(connectedRooms, "Du kan inte gå dit."),
    GoCommand.GoWest to goActionFromRoomConnectionsMap(connectedRooms,"Du kan inte gå dit.!"),
    GoCommand.GoNorth to goActionFromRoomConnectionsMap(connectedRooms,"Du kan inte gå dit."),
    GoCommand.GoSouth to goActionFromRoomConnectionsMap(connectedRooms,"Du kan inte gå dit."),
    ActionCommand.UseKey to ::useKey,
    ActionCommand.Dance to { _, _, _  -> Event("Dance, dance, dance!")},
    ActionCommand.GibberishInput to { _, _, _  -> Event("Hmmm, det där förstod jag inte!") },
    ActionCommand.EndGame to { _, _, _  -> EndEvent ("Slutspelat!") },
    ActionCommand.TakeSword to goActionForPickUpItem(Sword, "Går inte att ta upp en sådan här!", "Du tar upp"),
    ActionCommand.DropSword to goActionForDropItem(Sword, "Du har ingen sådan att släppa!", "Du släpper"),
    ActionCommand.TakeKey to goActionForPickUpItem(UnusedKey, "Går inte att ta upp en sådan här!", "Du tar upp"),
    ActionCommand.DropKey to goActionForDropItem(UnusedKey, "Du har ingen sådan att släppa!", "Du släpper"),
    ActionCommand.EnterHouse to goActionFromRoomConnectionsMap(connectedRooms, "Du kan inte gå dit."),
    ActionCommand.ExitHouse to goActionFromRoomConnectionsMap(connectedRooms, "Du kan inte gå dit."),
    ActionCommand.SwitchOnLight to ::switchOnLight,
    ActionCommand.SwitchOffLight to ::switchOffLight,
    ActionCommand.TakeChainsaw to ::takeChainsawOrDie,
    ActionCommand.DropChainsaw to goActionForDropItem(Chainsaw, "Du har ingen sådan att släppa!", "Du släpper"),
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
    val currentKey = items.carriedItems().filterIsInstance<Key>().first()

    if(currentKey is UsedKey) {
        return KeyAlreadyUsed
    }
    items.replaceCarried(currentKey, UsedKey)
    return KeyUsedSuccessfully(inFrontOfOpenDoor)
}

fun switchOnLight(input: Input, currentRoom: Room, items: Items): Event =
    when(currentRoom){
        insideLitRoom -> SameRoomEvent("Det är redan tänt, dumhuvve!", currentRoom)
        insideDarkRoom -> SwitchedLightOnEvent(insideLitRoom)
        else -> SameRoomEvent("Här? Hur då?", currentRoom)
    }

fun switchOffLight(input: Input, currentRoom: Room, items: Items): Event =
    when(currentRoom){
        insideDarkRoom -> SameRoomEvent("Den är redan släckt, men det kanske du inte ser eftersom det är så mörkt, haha!", currentRoom)
        insideLitRoom -> SwitchedLightOffEvent(insideDarkRoom)
        else -> SameRoomEvent("Här? Hur då?", currentRoom)
    }

fun takeChainsawOrDie(input: Input, currentRoom: Room, items: Items): Event =
    if (currentRoom == insideDarkRoom && items.itemsIn(insideLitRoom).contains(Chainsaw))
    {
        EndEvent("Du ser inte vad du gör! I mörkret råkar du sätta på den! Oj! Hoppsan! Aj! \nDu blev till en hög av blod!")
    } else {
        goActionForPickUpItem(Chainsaw, "Går inte att ta upp en sådan här!", "Du tar upp").invoke(input, currentRoom, items)
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
        EventLog.add(event)
        if(event is RoomEvent) {
            currentRoom = event.newRoom
        }
    }

    StandardInOut.showText(event.gameText)
}

fun formatGameTextAndItems(gameText: String, items: List<ItemType>): String =
    if (items.isEmpty()) {
        gameText +"\n"
    } else{
        gameText +"\n" + "Du ser " + items.joinToString { it.description }
    }

