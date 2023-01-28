package se.sbit.adventure.samples

import se.sbit.adventure.engine.*


private val gardenWithHedge = State(
    """
        |Du står på en gräsmatta i en villaträdgård. 
        |Trädgården har höga häckar åt tre väderstreck, och åt söder ligger villan.
    """.trimMargin())
private val inFrontOfClosedDoor = State(
    """
        |Du står på ett trädäck framför en villa. 
        |En stängd dörr leder in i huset.
    """.trimMargin())

private val inFrontOfOpenDoor = State(
    """
        |Du står på ett trädäck framför en villa. 
        |En öppen dörr leder in i huset.
    """.trimMargin())

private val darkRoom = State(
    """
        |Du står i ett alldeles mörkt rum. Du kan ana en golvlampa vid din ena sida, men allt är verkligen svårt att se.
        |Åt norr leder den öppna dörren ut ur huset.
    """.trimMargin())

private val litRoom = State(
    """
        |Du står i ett rum endast upplyst av en golvlampa.
        |Åt norr leder den öppna dörren ut ur huset.
    """.trimMargin())

private val gardenWithSawnDownHedge = State(
    """
        |Du står på en gräsmatta i en villaträdgård. 
        |Trädgården har osågningsbara höga häckar åt väster och öster, och åt söder ligger villan.
        |Åt norr har du sågat ner hela häcken.
    """.trimMargin())

private val endState = State(
    """
        |Du står på allmänningen i villaområdet!
        |Att komma hit var tydligen meningen med spelet. 
        |Livet ligger framför dig!
    """.trimMargin())


private val garden = Room(listOf(
    Pair(::hedgeIsNotSawnDown, gardenWithHedge),
    Pair(::hedgeIsSawnDown, gardenWithSawnDownHedge)))

private val inFrontOfDooor = Room(listOf(
    Pair(::doorIsClosed, inFrontOfClosedDoor),
    Pair(::doorIsOpened, inFrontOfOpenDoor)))

private val inside = Room(listOf(
    Pair(::lightIsOff, darkRoom),
    Pair(::lightIsOn, litRoom)))

private val endRoom = Room(listOf(
    Pair({_,_ -> true}, endState)))  // <-- Always true

val startRoom = garden
val startState = gardenWithHedge


private val connectedRooms = mapOf(
    garden to listOf(
        Pair(south, inFrontOfDooor),
        Pair(north and ::hedgeIsSawnDown, endRoom)),
    inFrontOfDooor to listOf(
        Pair(north, garden),
        Pair(::enterRoom and ::doorIsOpened, inside),
        Pair(south and ::doorIsOpened, inside)),
    inside to listOf(
        Pair(::exitRoom, inFrontOfDooor),
        Pair(north, inFrontOfDooor)),
)



val eventLog = EventLog()

fun doorIsOpened(input: Input, room: Room): Boolean = ! doorIsClosed(input, room)
fun doorIsClosed(input: Input, room: Room): Boolean = eventLog.log().filterIsInstance<KeyUsedSuccessfully>().isEmpty()
fun enterRoom(input: Input, room: Room): Boolean = input.command == ActionCommand.EnterHouse
fun exitRoom(input: Input, room: Room): Boolean = input.command == ActionCommand.ExitHouse
fun lightIsOn(input: Input, room: Room): Boolean = eventLog.log().filterIsInstance<SwitchedLightOnEvent>().size > eventLog.log().filterIsInstance<SwitchedLightOffEvent>().size
fun lightIsOff(input: Input, room: Room): Boolean = ! lightIsOn(input, room)
fun hedgeIsNotSawnDown(input: Input, room: Room): Boolean = eventLog.log().filterIsInstance<HedgeSawnDownEvent>().isEmpty()
fun hedgeIsSawnDown(input: Input, room: Room): Boolean = ! hedgeIsNotSawnDown(input, room)


// All Items, as well as where they are placed, and in what rooms they can be used
sealed class MiscItems(override val description: String): ItemType
object Receipt: MiscItems("ett kvitto")

sealed class Key(override val description: String): ItemType
object UnusedKey: Key("en nyckel")
object UsedKey: Key("en nyckel")

object Chainsaw: MiscItems("en motorsåg")

private var placementMap: Map<ItemType, Placement> = mapOf(
    UnusedKey to InRoom(garden),
    Receipt to Carried,
    Chainsaw to InRoom(inside),
)

// Possible user input
enum class ActionCommand: CommandType {
    ExamineReceipt,
    ExamineKey,
    ExamineChainsaw,
    TakeReceipt,
    DropReceipt,
    TakeKey,
    DropKey,
    UseKey,
    LookIn,
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
    "(Läs |Undersök |titta (på )?)kvitto(t)?" to ActionCommand.ExamineReceipt,
    "(Undersök |titta (på )?)nyckel(n)?" to ActionCommand.ExamineKey,
    "(Undersök |titta (på )?)motorsåg(en)?" to ActionCommand.ExamineChainsaw,
    "(ta |plocka )(upp )?kvitto(t)?" to ActionCommand.TakeReceipt,
    "släpp kvitto(t)?" to ActionCommand.DropReceipt,
    "(ta |plocka )(upp )?nyckel(n)?" to ActionCommand.TakeKey,
    "släpp nyckel(n)?" to ActionCommand.DropKey,
    "(öppna|öppna dörr(en)?|använd nyckel(n)?|lås upp( dörren?)?)" to ActionCommand.UseKey,
    "titta in( .*)?" to ActionCommand.LookIn,
    "(gå in( .*)?|gå till villan)" to ActionCommand.EnterHouse,
    "gå ut.*" to ActionCommand.ExitHouse,
    "(tänd|tänd ((golv)?lampa(n)?|ljuset))" to ActionCommand.SwitchOnLight,
    "(släck|släck ((golv)?lampa(n)?|ljuset))" to ActionCommand.SwitchOffLight,
    "(ta |plocka )(upp )?(golv)?lampa(n)?" to ActionCommand.TakeLamp,
    "(ta |plocka )(upp )?motorsåg(en)?" to ActionCommand.TakeChainsaw,
    "släpp motorsåg(en)?" to ActionCommand.DropChainsaw,
    "(såga (ner )?häck(en)?|(använd |sätt på |starta )motorsåg(en)?).*" to ActionCommand.SawDownHedge,
    "titta( omkring.*| runt.*)?" to ActionCommand.LookAround,
    "i(nventory)?" to ActionCommand.Inventory,
    "dansa.*" to ActionCommand.Dance,
    "(krossa |slå sönder |mosa ).*" to ActionCommand.Smash,
)

val input2Command = stringinput2Command.entries.associate { Pair(it.key.toRegex(RegexOption.IGNORE_CASE), it.value) }

// Mapping user inputs to what event-returning function to run
class KeyUsedSuccessfully(currentRoom: Room, newState: State) : RoomEvent("Du låser upp och öppnar dörren.", currentRoom, newState, Player)
object KeyAlreadyUsed : Event("Du har redan låst upp dörren.")
object NoUsageOfKey : Event("Du kan inte använda en nyckel här.")
object NoKeyToBeUsed : Event("Du har väl ingen nyckel?")

class SwitchedLightOnEvent(currentRoom: Room, newState: State): RoomEvent("Nu blev det ljust!", currentRoom, newState, Player)
class SwitchedLightOffEvent(currentRoom: Room, newState: State): RoomEvent("Nu blev det mörkt igen!", currentRoom, newState, Player)

class HedgeSawnDownEvent:RoomEvent("Vroooooom! Du sågar ner häcken som värsta trädgårdsmästaren!", garden, gardenWithSawnDownHedge, Player)


val actionMap: Map<CommandType, (Input, EventLog,  Items) -> Event> = mapOf(
    GoCommand.GoEast to actionForGo(connectedRooms, "Du kan inte gå dit."),
    GoCommand.GoWest to actionForGo(connectedRooms,"Du kan inte gå dit.!"),
    GoCommand.GoNorth to actionForGo(connectedRooms,"Du kan inte gå dit."),
    GoCommand.GoSouth to actionForGo(connectedRooms,"Du kan inte gå dit."),

    ActionCommand.ExamineReceipt to actionForExamineItem(Receipt, "Oh! Du har tydligen handlat mjölk, ost, yoghurt och skivbar leverpastej för några veckor sedan.", "Då får du först plocka upp det igen!"),
    ActionCommand.ExamineKey to ::examineKey,
    ActionCommand.ExamineChainsaw to actionForExamineItem(Chainsaw, "Wow! En 13 tums Husqvarna 550 XPG Mark II! Orangeröd! ", "Då får du först plocka upp den igen!"),

    ActionCommand.UseKey to ::useKey,
    ActionCommand.GibberishInput to { _, _, _  -> Event("Hmmm, det där förstod jag inte!") },
    ActionCommand.EndGame to { _, _, _ -> EndEvent ("Slutspelat!\nSlut för idag, tack för idag!") },
    ActionCommand.TakeReceipt to actionForPickUpItem(Receipt, "Går inte att ta upp en sådan här!", "Du tar upp"),
    ActionCommand.DropReceipt to actionForDropItem(Receipt, "Du har ingen sådan att släppa!", "Du släpper"),
    ActionCommand.TakeKey to ::takeAnyKey,
    ActionCommand.DropKey to ::dropAnyKey,
    ActionCommand.LookIn to ::lookIn,
    ActionCommand.EnterHouse to actionForGo(connectedRooms, "Du kan inte gå dit."),
    ActionCommand.ExitHouse to actionForGo(connectedRooms, "Du kan inte gå dit."),
    ActionCommand.SwitchOnLight to ::switchOnLight,
    ActionCommand.SwitchOffLight to ::switchOffLight,
    ActionCommand.TakeLamp to ::takeLamp,
    ActionCommand.TakeChainsaw to ::takeChainsawOrDie,
    ActionCommand.DropChainsaw to actionForDropItem(Chainsaw, "Du har ingen sådan att släppa!", "Du släpper"),
    ActionCommand.SawDownHedge to ::sawDownHedge,
    ActionCommand.LookAround to { _, eventLog, _  -> SameRoomEvent("Du tittar dig omkring.", eventLog.getCurrentRoom(), eventLog.getCurrentState(), Player)},
    ActionCommand.Inventory to goActionForInventory("Du bär inte på något.", "Du bär på"),
    ActionCommand.Dance to { _, _, _  -> Event("Dance, dance, dance!")},
    ActionCommand.Smash to {  _, _, _  -> Event("Så där gör man bara inte! Det kan räknas som skadegörelse och vara straffbart med böter eller fängelse enligt Brottbalken 12 kap. 1 §!")},
)

//fun npcNextAction(npcCurrentRoom: Room, playerCurrentRoom: Room, playerCurrentState: State, playerItems: Items, eventLog: EventLog): Event {
//    when(npcCurrentRoom == playerCurrentRoom){
//        true -> return NpcInPlayerRoom"(En gubbe står och tittar på dig", )
//        false -> return npcRandomWalk(npcCurrentRoom: Room, playerCurrentRoom: Room, playerCurrentState: State, playerItems: Items, eventLog: EventLog)
//    }
//
//}

//val oldMan = NonPlayerCharacter("en gubbe") { npcRoom, playerRoom, playerState, eventlog ->
//    NewRoomEvent()
//}

class gubbe: NPC("En gubbe")


fun useKey(input: Input, eventLog: EventLog, items: Items): Event {
    if(items.carriedItems().filterIsInstance<Key>().isEmpty()){
        return NoKeyToBeUsed;
    }
    val currentKey = items.carriedItems().filterIsInstance<Key>().first()
    if(currentKey is UsedKey) {
        return KeyAlreadyUsed
    }
    if(eventLog.getCurrentState() != inFrontOfClosedDoor){
        return NoUsageOfKey;
    }
    items.replaceCarried(currentKey, UsedKey)
    return KeyUsedSuccessfully(eventLog.getCurrentRoom(), inFrontOfOpenDoor)
}

fun takeAnyKey(input: Input, eventLog: EventLog, items: Items): Event {
    val currentRoom = eventLog.getCurrentRoom()
    if (items.itemsIn(currentRoom).filterIsInstance<Key>().isEmpty()) {
        return NoSuchItemHereEvent("Går inte att ta upp en sådan här!")
    } else {
        val key = items.itemsIn(currentRoom).filterIsInstance<Key>().first()
        items.pickUp(key, currentRoom)
        return PickedUpItemEvent("Du tar upp en nyckel")
    }
}

fun dropAnyKey(input: Input, eventLog: EventLog, items: Items): Event =
    if (items.carriedItems().filterIsInstance<Key>().isEmpty()) {
        NoSuchItemToDropItemEvent("Du har ingen sådan att släppa!")
    } else {
        val key = items.carriedItems().filterIsInstance<Key>().first()
        items.drop(key, eventLog.getCurrentRoom())
        DroppedItemEvent("Du släpper en nyckel")
    }
fun examineKey(input: Input, eventLog: EventLog, items: Items): Event =
    if (items.carriedItems().contains(UnusedKey)) {
        actionForExamineItem(UnusedKey, "En helt vanlig nyckel", "Då får du först plocka upp det igen!").invoke(input,eventLog, items)
    } else {
        actionForExamineItem(UsedKey, "En helt vanlig nyckel", "Då får du först plocka upp det igen!").invoke(input,eventLog, items)
    }

fun lookIn(input: Input, eventLog: EventLog, items: Items): Event =
    when(eventLog.getCurrentState()){
        inFrontOfOpenDoor  -> if (lightIsOn(input,  eventLog.getCurrentRoom())) {
            SameRoomEvent("Du ser knappt något eftersom det enda ljuset kommer från en liten golvlampa.", eventLog.getCurrentRoom(), eventLog.getCurrentState(), Player)
        } else {
            SameRoomEvent("Det ser helt mörkt ut där inne.", eventLog.getCurrentRoom(), eventLog.getCurrentState(), Player)
        }
        inFrontOfClosedDoor -> SameRoomEvent("Duh! Det är en stängd dörr i vägen!", eventLog.getCurrentRoom(), eventLog.getCurrentState(), Player)
        else -> SameRoomEvent("Här? Hur då?", eventLog.getCurrentRoom(), eventLog.getCurrentState(), Player)
    }


fun switchOnLight(input: Input, eventLog: EventLog, items: Items): Event =
    when(eventLog.getCurrentState()){
        litRoom -> SameRoomEvent("Det är redan tänt, dumhuvve!", eventLog.getCurrentRoom(), eventLog.getCurrentState(),  Player)
        darkRoom -> SwitchedLightOnEvent(eventLog.getCurrentRoom(), litRoom)
        else -> SameRoomEvent("Här? Hur då?", eventLog.getCurrentRoom(), eventLog.getCurrentState(), Player)
    }

fun switchOffLight(input: Input, eventLog: EventLog, items: Items): Event =
    when(eventLog.getCurrentState()){
        darkRoom -> SameRoomEvent("Den är redan släckt, men det kanske du inte ser eftersom det är så mörkt, haha!", eventLog.getCurrentRoom(), eventLog.getCurrentState(), Player)
        litRoom -> SwitchedLightOffEvent(eventLog.getCurrentRoom(), darkRoom)
        else -> SameRoomEvent("Här? Hur då?", eventLog.getCurrentRoom(), eventLog.getCurrentState(), Player)
    }

fun takeChainsawOrDie(input: Input, eventLog: EventLog, items: Items): Event =
    if (eventLog.getCurrentState() == darkRoom && items.itemsIn(inside).contains(Chainsaw))
    {
        EndEvent("Du ser inte vad du gör i mörkret! Hoppsan, du råkar sätta på den! Oj! Aj! \nDu blev till en hög av blod!")
    } else {
        actionForPickUpItem(Chainsaw, "Går inte att ta upp en sådan här!", "Du tar upp").invoke(input, eventLog, items)
    }

fun sawDownHedge(input: Input, eventLog: EventLog, items: Items): Event =
    if(items.carriedItems().contains(Chainsaw)){
        when(eventLog.getCurrentState()){
            gardenWithSawnDownHedge -> SameRoomEvent("De kvarvarande häckarna går inte att såga ner av någon mystisk anledning.", eventLog.getCurrentRoom(), eventLog.getCurrentState(), Player)
            gardenWithHedge -> HedgeSawnDownEvent()
            else -> SameRoomEvent("Du sätter igång motorsågen och viftar med den i luften. Wrooom, wroom! Du känner inte för att såga i något av det du ser, så du stänger av den igen.", eventLog.getCurrentRoom(), eventLog.getCurrentState(), Player)
        }
    }else{
        Event("Nu går du väl ändå händelserna i förväg? Du har ju inget att såga med!")
    }

fun takeLamp(input: Input, eventLog: EventLog, items: Items): Event =
    when(eventLog.getCurrentState()){
        litRoom, darkRoom -> Event("Du rycker och sliter, men lampan verkar fastsatt i golvet. Eller så är du bara väldigt svag!")
        else -> SameRoomEvent("Var ser du en lampa att ta?", eventLog.getCurrentRoom(), eventLog.getCurrentState(), Player)
    }


fun main() {
    println("**************  Simple Adventure ****************")

    var event: Event = NewRoomEvent("Welcome!\n", startRoom, startState, Player)
    eventLog.add(event) //<-- Must have a starting NewRoomEvent so the game can figure out where the player starts

    val game = Game(connectedRooms, placementMap, actionMap, eventLog,  emptyList())

    while (event !is EndEvent){
        val currentRoom = game.eventlog.getCurrentRoom()
        val currentState = game.eventlog.getCurrentState()

        StandardInOut.showText(
            if(event is RoomEvent) {
                formatGameTextAndItems(event.gameText, game.allItems.itemsIn(currentRoom))
            } else {
                event.gameText
            })

        val input:String = StandardInOut.waitForInput()
        event = game.playerDo(Input(Interpreter.interpret(input, input2Command, ActionCommand.GibberishInput)), game.eventlog)
        eventLog.add(event)

        game.nonPlayerCharacters = game.nonPlayerCharacters.map {  Pair(it.first, EventLog.fromList(it.second.add(it.first.doAction(it.second.getCurrentRoom(), currentRoom, currentState, eventLog))))}
    }

    StandardInOut.showText(event.gameText)
}

fun formatGameTextAndItems(gameText: String, items: List<ItemType>): String =
    if (items.isEmpty()) {
        gameText +"\n"
    } else{
        gameText +"\n" + "Du ser " + items.joinToString { it.description }
    }

