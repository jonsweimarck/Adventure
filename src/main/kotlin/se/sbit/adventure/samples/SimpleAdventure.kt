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

private val gardenWithSawnDownHedge = Room(
    """
        |Du står på en gräsmatta i en villaträdgård. 
        |Trädgården har osågningsbara höga häckar åt väster och öster, och åt söder ligger villan.
        |Åt norr har du sågat ner hela häcken.
    """.trimMargin())

private val endRoom = Room(
    """
        |Du står på allmänningen i villaområdet!
        |Att komma hit var tydligen meningen med spelet. 
        |Livet ligger framför dig!
    """.trimMargin())

val startRoom = garden

private val connectedRooms = mapOf(
    garden to listOf(
        Pair(south and ::doorIsClosed, inFrontOfClosedDoor),
        Pair(south and ::doorIsOpened, inFrontOfOpenDoor)),
    gardenWithSawnDownHedge to listOf(
        Pair(south and ::doorIsClosed, inFrontOfClosedDoor),
        Pair(south and ::doorIsOpened, inFrontOfOpenDoor),
        Pair(north, endRoom)),
    inFrontOfClosedDoor to listOf(Pair(north, garden)),
    inFrontOfOpenDoor to listOf(
        Pair(north and ::hedgeIsNotSawnDown, garden),
        Pair(north and ::hedgeIsSawnDown, gardenWithSawnDownHedge),
        Pair(south  and ::lightIsOff, insideDarkRoom),
        Pair(south  and ::lightIsOn, insideLitRoom),
        Pair(::enterRoom and ::lightIsOff, insideDarkRoom),
        Pair(::enterRoom and ::lightIsOn, insideLitRoom)),
    insideDarkRoom to listOf(
        Pair(north or ::exitRoom, inFrontOfOpenDoor),
        Pair(::lightIsOn, insideLitRoom)),
    insideLitRoom to listOf(
        Pair(::lightIsOff, insideDarkRoom),
        Pair(north or ::exitRoom, inFrontOfOpenDoor)),
)

fun doorIsOpened(input: Input, room: Room): Boolean = ! doorIsClosed(input, room)
fun doorIsClosed(input: Input, room: Room): Boolean = EventLog.log().filterIsInstance<KeyUsedSuccessfully>().isEmpty()
fun enterRoom(input: Input, room: Room): Boolean = input.command == ActionCommand.EnterHouse
fun exitRoom(input: Input, room: Room): Boolean = input.command == ActionCommand.ExitHouse
fun lightIsOn(input: Input, room: Room): Boolean = EventLog.log().filterIsInstance<SwitchedLightOnEvent>().size > EventLog.log().filterIsInstance<SwitchedLightOffEvent>().size
fun lightIsOff(input: Input, room: Room): Boolean = ! lightIsOn(input, room)
fun hedgeIsNotSawnDown(input: Input, room: Room): Boolean = EventLog.log().filterIsInstance<HedgeSawnDownEvent>().isEmpty()
fun hedgeIsSawnDown(input: Input, room: Room): Boolean = ! hedgeIsNotSawnDown(input, room)


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
//    Bottle to InRoom(garden),
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
    TakeLamp,
    TakeChainsaw,
    DropChainsaw,
    SawDownHedge,
    LookAround,
    Inventory,
    GibberishInput,
    EndGame,
    Dance,
    Smash,
}


val stringinput2Command: Map<String, CommandType> = mapOf (
    "((gå (åt )?)?s(öder)?|gå söderut)" to GoCommand.GoSouth,
    "((gå (åt )?)?n(orr)?|gå söderut)" to GoCommand.GoNorth,
    "(exit( game)?|(av)?sluta|bye|hej( då|då))" to ActionCommand.EndGame,
    "(ta |plocka )(upp )?svärd(et)?" to ActionCommand.TakeSword,
    "släpp svärd(et)?" to ActionCommand.DropSword,
    "(ta |plocka )(upp )?nyckel(n)?" to ActionCommand.TakeKey,
    "släpp nyckel(n)?" to ActionCommand.DropKey,
    "(öppna dörr(en)?|använd nyckel(n)?|lås upp( dörren?)?)" to ActionCommand.UseKey,
    "(gå in.*|gå till villan)" to ActionCommand.EnterHouse,
    "gå ut.*" to ActionCommand.ExitHouse,
    "tänd lampa(n)?" to ActionCommand.SwitchOnLight,
    "släck lampa(n)?" to ActionCommand.SwitchOffLight,
    "(ta |plocka )(upp )?(golv)?lampa(n)?" to ActionCommand.TakeLamp,
    "(ta |plocka )(upp )?motorsåg(en)?" to ActionCommand.TakeChainsaw,
    "släpp motorsåg(en)?" to ActionCommand.DropChainsaw,
    "(såga (ner )?häck(en)?|använd motorsåg(en)?)" to ActionCommand.SawDownHedge,
    "titta( omkring| runt)?" to ActionCommand.LookAround,
    "i(nventory)?" to ActionCommand.Inventory,
    "dansa" to ActionCommand.Dance,
    "(krossa |slå sönder |mosa ).*" to ActionCommand.Smash,
)

val input2Command = stringinput2Command.entries.associate { Pair(it.key.toRegex(RegexOption.IGNORE_CASE), it.value) }

// Mapping user inputs to what event-returning function to run
class KeyUsedSuccessfully(newRoom: Room) : RoomEvent("Du låser upp och öppnar dörren.", newRoom)
object KeyAlreadyUsed : Event("Du har redan låst upp dörren.")
object NoUsageOfKey : Event("Du kan inte använda en nyckel här.")
object NoKeyToBeUsed : Event("Du har väl ingen nyckel?")

class SwitchedLightOnEvent(newRoom: Room): RoomEvent("Nu blev det ljust!", newRoom)
class SwitchedLightOffEvent(currentRoom: Room): RoomEvent("Nu blev det mörkt igen!", currentRoom)

class HedgeSawnDownEvent:RoomEvent("Vroooooom! Du sågar ner häcken som värsta trädgårdsmästaren!", gardenWithSawnDownHedge)


val actionMap: Map<CommandType, (Input, Room, Items) -> Event> = mapOf(
    GoCommand.GoEast to goActionFromRoomConnectionsMap(connectedRooms, "Du kan inte gå dit."),
    GoCommand.GoWest to goActionFromRoomConnectionsMap(connectedRooms,"Du kan inte gå dit.!"),
    GoCommand.GoNorth to goActionFromRoomConnectionsMap(connectedRooms,"Du kan inte gå dit."),
    GoCommand.GoSouth to goActionFromRoomConnectionsMap(connectedRooms,"Du kan inte gå dit."),
    ActionCommand.UseKey to ::useKey,
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
    ActionCommand.TakeLamp to ::takeLamp,
    ActionCommand.TakeChainsaw to ::takeChainsawOrDie,
    ActionCommand.DropChainsaw to goActionForDropItem(Chainsaw, "Du har ingen sådan att släppa!", "Du släpper"),
    ActionCommand.SawDownHedge to ::sawDownHedge,
    ActionCommand.LookAround to { _, currentRoom, _  -> SameRoomEvent("Du tittar dig omkring.", currentRoom)},
    ActionCommand.Inventory to goActionForInventory("Du bär inte på något.", "Du bär på"),
    ActionCommand.Dance to { _, _, _  -> Event("Dance, dance, dance!")},
    ActionCommand.Smash to { _, _, _  -> Event("Så där gör man bara inte! Det kan räknas som skadegörelse och vara straffbart med böter eller fängelse enligt Brottbalken 12 kap. 1 §!")},
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

fun sawDownHedge(input: Input, currentRoom: Room, items: Items): Event =
    if(items.carriedItems().contains(Chainsaw)){
        when(currentRoom){
            gardenWithSawnDownHedge -> SameRoomEvent("Nä, de kvarvarande häckarna går inte att såga ner av någon mystisk anledning", currentRoom)
            garden -> HedgeSawnDownEvent()
            else -> SameRoomEvent("Du sätter igång motorsågen och viftar med den i luften. Wrooom, wroom! Du känner inte för att såga i något av det du ser, så du stänger av den igen.", currentRoom)
        }
    }else{
        Event("Nu går du väl ändå händelserna i förväg? Du har ju inget att såga med!")
    }

fun takeLamp(input: Input, currentRoom: Room, items: Items): Event =
    when(currentRoom){
        insideLitRoom, insideDarkRoom -> Event("Du rycker och sliter, men lampan verkar fastsatt i golvet. Eller så är du bara väldigt svag!")
        else -> SameRoomEvent("Var ser du en lampa att ta?", currentRoom)
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

