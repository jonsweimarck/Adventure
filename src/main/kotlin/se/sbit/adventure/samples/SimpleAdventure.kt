package se.sbit.adventure.samples

import se.sbit.adventure.engine.*


// *********************  Rooms and what States each room can be in ***********************
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

val eventLog = EventLog()

fun doorIsOpened(input: Input, room: Room): Boolean = ! doorIsClosed(input, room)
fun doorIsClosed(input: Input, room: Room): Boolean = eventLog.log().filterIsInstance<KeyUsedSuccessfully>().isEmpty()
fun lightIsOn(input: Input, room: Room): Boolean = eventLog.log().filterIsInstance<SwitchedLightOnEvent>().size > eventLog.log().filterIsInstance<SwitchedLightOffEvent>().size
fun lightIsOff(input: Input, room: Room): Boolean = ! lightIsOn(input, room)
fun hedgeIsNotSawnDown(input: Input, room: Room): Boolean = eventLog.log().filterIsInstance<HedgeSawnDownEvent>().isEmpty()
fun hedgeIsSawnDown(input: Input, room: Room): Boolean = ! hedgeIsNotSawnDown(input, room)

private val garden = Room(listOf(
    Pair(::hedgeIsNotSawnDown, gardenWithHedge),
    Pair(::hedgeIsSawnDown, gardenWithSawnDownHedge)))

private val inFrontOfDooor = Room(listOf(
    Pair(::doorIsClosed, inFrontOfClosedDoor),
    Pair(::doorIsOpened, inFrontOfOpenDoor)))

private val inside = Room(listOf(
    Pair(::doorIsOpened and ::lightIsOff, darkRoom),
    Pair(::doorIsOpened and ::lightIsOn, litRoom)))

private val endRoom = Room(listOf(
    Pair(::hedgeIsSawnDown, endState)))

val startRoom = garden
val startState = gardenWithHedge

// **************** How the Room are connected ******************
private val connectedRooms = mapOf(
    garden to listOf(
        Pair(south, inFrontOfDooor),
        Pair(north, endRoom)),
    inFrontOfDooor to listOf(
        Pair(north, garden),
        Pair(south or ::enterRoom, inside)),
    inside to listOf(
        Pair(north or ::exitRoom, inFrontOfDooor)),
)
fun enterRoom(input: Input): Boolean = input.command == ActionCommand.EnterHouse
fun exitRoom(input: Input): Boolean = input.command == ActionCommand.ExitHouse



// All Items, as well as where they are placed
class MiscItems(description: String): SinglestateItem(Itemstate(description))
val receipt = MiscItems("ett kvitto")
val chainsaw = MiscItems("en motorsåg")


private fun keyIsNotUsed(eventLog: EventLog): Boolean = eventLog.log().none { it is KeyUsedSuccessfully }
private fun keyIsUsed(eventLog: EventLog): Boolean = ! keyIsNotUsed(eventLog)
private val unusedKeyState = Itemstate("en nyckel")
private val usedKeyState = Itemstate("en typiskt använd nyckel")
val keyStates = listOf(
    Pair(::keyIsNotUsed, unusedKeyState),
    Pair(::keyIsUsed, usedKeyState))

val key = MultistateItem(keyStates)


private var placementMap: Map<Item, Placement> = mapOf(
    key to InRoom(garden),
    receipt to Carried,
    chainsaw to InRoom(inside),
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
class KeyUsedSuccessfully(currentRoom: Room, newState: State) : RoomEvent("Du låser upp och öppnar dörren.", Pair(currentRoom, newState), Player)
object KeyAlreadyUsed : Event("Du har redan låst upp dörren.", eventLog.getCurrentRoomAndState(Player))
object NoUsageOfKey : Event("Du kan inte använda en nyckel här.", eventLog.getCurrentRoomAndState(Player))
object NoKeyToBeUsed : Event("Du har väl ingen nyckel?", eventLog.getCurrentRoomAndState(Player))

class SwitchedLightOnEvent(currentRoom: Room, newState: State): RoomEvent("Nu blev det ljust!", Pair(currentRoom, newState), Player)
class SwitchedLightOffEvent(currentRoom: Room, newState: State): RoomEvent("Nu blev det mörkt igen!", Pair(currentRoom, newState), Player)

class HedgeSawnDownEvent:RoomEvent("Vroooooom! Du sågar ner häcken som värsta trädgårdsmästaren!", Pair(garden, gardenWithSawnDownHedge), Player)


val actionMap: Map<CommandType, (Input, EventLog) -> Event> = mapOf(
    GoCommand.GoEast to actionForGo(connectedRooms, "Du kan inte gå dit."),
    GoCommand.GoWest to actionForGo(connectedRooms,"Du kan inte gå dit.!"),
    GoCommand.GoNorth to actionForGo(connectedRooms,"Du kan inte gå dit."),
    GoCommand.GoSouth to actionForGo(connectedRooms,"Du kan inte gå dit."),

    ActionCommand.ExamineReceipt to actionForExamineItem(receipt, "Oh! Du har tydligen handlat mjölk, ost, yoghurt och skivbar leverpastej för några veckor sedan.", "Då får du först plocka upp det igen!"),
    ActionCommand.ExamineKey to ::examineKey,
    ActionCommand.ExamineChainsaw to actionForExamineItem(chainsaw, "Wow! En 13 tums Husqvarna 550 XPG Mark II! Orangeröd! ", "Då får du först plocka upp den igen!"),

    ActionCommand.UseKey to ::useKey,
    ActionCommand.GibberishInput to { _, _-> Event("Hmmm, det där förstod jag inte!", eventLog.getCurrentRoomAndState(Player)) },
    ActionCommand.EndGame to { _, eventLog -> EndEvent ("Slutspelat!\nSlut för idag, tack för idag!", eventLog.getCurrentRoomAndState(Player)) },
    ActionCommand.TakeReceipt to actionForPickUpItem(receipt, "Går inte att ta upp en sådan här!", "Du tar upp"),
    ActionCommand.DropReceipt to actionForDropItem(receipt, "Du har ingen sådan att släppa!", "Du släpper"),
    ActionCommand.TakeKey to ::takeAnyKey,
    ActionCommand.DropKey to ::dropAnyKey,
    ActionCommand.LookIn to ::lookIn2,
    ActionCommand.EnterHouse to actionForGo(connectedRooms, "Du kan inte gå dit."),
    ActionCommand.ExitHouse to actionForGo(connectedRooms, "Du kan inte gå dit."),
    ActionCommand.SwitchOnLight to ::switchOnLight,
    ActionCommand.SwitchOffLight to ::switchOffLight,
    ActionCommand.TakeLamp to ::takeLamp,
    ActionCommand.TakeChainsaw to ::takeChainsawOrDie,
    ActionCommand.DropChainsaw to actionForDropItem(chainsaw,"Du har ingen sådan att släppa!", "Du släpper"),
    ActionCommand.SawDownHedge to ::sawDownHedge,
    ActionCommand.LookAround to { _, eventLog  -> LookAroundEvent("Du tittar dig omkring.", eventLog.getCurrentRoomAndState(Player), Player)},
    ActionCommand.Inventory to goActionForInventory("Du bär inte på något.", "Du bär på"),
    ActionCommand.Dance to { _, eventLog -> Event("Dance, dance, dance!", eventLog.getCurrentRoomAndState(Player)) },
    ActionCommand.Smash to {  _, eventLog -> Event("Så där gör man bara inte! Det kan räknas som skadegörelse och vara straffbart med böter eller fängelse enligt Brottbalken 12 kap. 1 §!", eventLog.getCurrentRoomAndState(Player)) },
)



val roomConnections = connectedRooms.entries.associate { it.key to it.value.map { pair -> pair.second } }
val oldMan: NPC = object: NPC("en gubbe") {

    override fun doAction(evlog: EventLog): Event {
        val currentRoomAndState = evlog.getCurrentRoomAndState(this)
        if(evlog.getNumberOfTurnsSinceEnteredCurrentRoom(this) >= 2) {
            return goWherePossible(roomConnections, evlog, this, "En gammal gubbe vandrar långsamt förbi.")
        } else {
            return SameRoomEvent("Gubben fortsätter att gå långsam.", currentRoomAndState, this)
        }
    }

    override fun getGameText(evLog: EventLog): String =
        when(eventLog.getNumberOfOfTurnsStillInSameRoom(Player, this)) {
            0 -> ""
            1 -> "En gammal gubbe vandrar långsamt förbi."
            else -> "Den gamla gubben är fortfarande inom synhåll."

        }
}

// List of NPCs and where they start.
val npcs = listOf(Pair(oldMan, Pair(startRoom, startState)))


fun useKey(input: Input, eventLog: EventLog): Event {
    if(! carriedItems(eventLog).contains(key)){
        return NoKeyToBeUsed
    }
    if(eventLog.getCurrentState(Player) != inFrontOfClosedDoor){
        return NoUsageOfKey
    }

    val currentKey = carriedItems(eventLog).first{it == key}
    if(currentKey.state(eventLog) == usedKeyState) {
        return KeyAlreadyUsed
    }
    return KeyUsedSuccessfully(eventLog.getCurrentRoom(Player), inFrontOfOpenDoor)
}

fun takeAnyKey(input: Input, eventLog: EventLog): Event {
    val currentRoom = eventLog.getCurrentRoom(Player)
    if (! itemsIn(currentRoom, eventLog).contains(key)) {
        return NoSuchItemHereEvent("Går inte att ta upp en sådan här!", eventLog.getCurrentRoomAndState(Player))
    } else {
        //val keyToTake = itemsIn2(currentRoom, eventLog).find { it == key }
        actionForPickUpItem(key).invoke(input, eventLog)
        return PickedUpItemEvent("Du tar upp en nyckel", eventLog.getCurrentRoomAndState(Player), Player, key)
    }
}

fun dropAnyKey(input: Input, eventLog: EventLog): Event =
    if (! carriedItems(eventLog).contains(key)) {
        NoSuchItemToDropItemEvent("Du har ingen sådan att släppa!", eventLog.getCurrentRoomAndState(Player))
    } else {
        actionForDropItem(key).invoke(input, eventLog)
        DroppedItemEvent("Du släpper en nyckel", eventLog.getCurrentRoomAndState(Player), Player, key)
    }
fun examineKey(input: Input, eventLog: EventLog): Event =
    if (carriedItems(eventLog).contains(key)) {
        actionForExamineItem(key,"En helt vanlig nyckel", "Då får du först plocka upp det igen!").invoke(input,eventLog)
    } else {
        actionForExamineItem(key, "En helt vanlig nyckel", "Då får du först plocka upp det igen!").invoke(input,eventLog)
    }

fun lookIn2(input: Input, eventLog: EventLog): Event =
    when(eventLog.getCurrentState(Player)){
        inFrontOfOpenDoor  -> if (lightIsOn(input,  eventLog.getCurrentRoom(Player))) {
            SameRoomEvent("Du ser knappt något eftersom det enda ljuset kommer från en liten golvlampa.", eventLog.getCurrentRoomAndState(Player), Player)
        } else {
            SameRoomEvent("Det ser helt mörkt ut där inne.", eventLog.getCurrentRoomAndState(Player), Player)
        }
        inFrontOfClosedDoor -> SameRoomEvent("Duh! Det är en stängd dörr i vägen!", eventLog.getCurrentRoomAndState(Player), Player)
        else -> SameRoomEvent("Här? Hur då?", eventLog.getCurrentRoomAndState(Player), Player)
    }


fun switchOnLight(input: Input, eventLog: EventLog): Event =
    when(eventLog.getCurrentState(Player)){
        litRoom -> SameRoomEvent("Det är redan tänt, dumhuvve!", eventLog.getCurrentRoomAndState(Player),  Player)
        darkRoom -> SwitchedLightOnEvent(eventLog.getCurrentRoom(Player), litRoom)
        else -> SameRoomEvent("Här? Hur då?", eventLog.getCurrentRoomAndState(Player), Player)
    }

fun switchOffLight(input: Input, eventLog: EventLog): Event =
    when(eventLog.getCurrentState(Player)){
        darkRoom -> SameRoomEvent("Den är redan släckt, men det kanske du inte ser eftersom det är så mörkt, haha!", eventLog.getCurrentRoomAndState(Player), Player)
        litRoom -> SwitchedLightOffEvent(eventLog.getCurrentRoom(Player), darkRoom)
        else -> SameRoomEvent("Här? Hur då?", eventLog.getCurrentRoomAndState(Player), Player)
    }

fun takeChainsawOrDie(input: Input, eventLog: EventLog): Event =
    if (eventLog.getCurrentState(Player) == darkRoom && itemsIn(inside, eventLog).contains(chainsaw))
    {
        EndEvent("Du ser inte vad du gör i mörkret! Hoppsan, du råkar sätta på den! Oj! Aj! \nDu blev till en hög av blod!", eventLog.getCurrentRoomAndState(Player))
    } else {
        actionForPickUpItem(chainsaw, "Går inte att ta upp en sådan här!", "Du tar upp").invoke(input, eventLog)
    }

fun sawDownHedge(input: Input, eventLog: EventLog): Event =
    if(carriedItems(eventLog).contains(chainsaw)){
        when(eventLog.getCurrentState(Player)){
            gardenWithSawnDownHedge -> SameRoomEvent("De kvarvarande häckarna går inte att såga ner av någon mystisk anledning.", eventLog.getCurrentRoomAndState(Player), Player)
            gardenWithHedge -> HedgeSawnDownEvent()
            else -> SameRoomEvent("Du sätter igång motorsågen och viftar med den i luften. Wrooom, wroom! Du känner inte för att såga i något av det du ser, så du stänger av den igen.", eventLog.getCurrentRoomAndState(Player), Player)
        }
    }else{
        Event("Nu går du väl ändå händelserna i förväg? Du har ju inget att såga med!", eventLog.getCurrentRoomAndState(Player))
    }

fun takeLamp(input: Input, eventLog: EventLog): Event =
    when(eventLog.getCurrentState(Player)){
        litRoom, darkRoom -> Event("Du rycker och sliter, men lampan verkar fastsatt i golvet. Eller så är du bara väldigt svag!", eventLog.getCurrentRoomAndState(Player))
        else -> SameRoomEvent("Var ser du en lampa att ta?", eventLog.getCurrentRoomAndState(Player), Player)
    }


fun main() {
    println("**************  Simple Adventure ****************")

    // Must have starting NewRoomEvent for the player so the game can figure out where the player starts
    var playerEvent: Event = NewRoomEvent("Welcome!\n", Pair(startRoom, startState), Player)
    eventLog.add(playerEvent)

    val game = Game(connectedRooms, placementMap, actionMap, eventLog,  nonPlayerCharactersWithStartRooms = npcs)

    print("${eventLog.log()}\n")

    while (playerEvent !is EndEvent){
        val currentRoomAndState = game.eventlog.getCurrentRoomAndState(Player)
        val currentRoom = currentRoomAndState.first

        StandardInOut.showText(
            when(playerEvent){
                is NewRoomEvent, is LookAroundEvent -> formatGameTextAndItems("${playerEvent.gameText}\n${(playerEvent as RoomEvent).roomAndState.second.description}", itemsIn(currentRoom, eventLog), eventLog)
                is SameRoomEvent ->formatGameTextAndItems(playerEvent.gameText, itemsIn(currentRoom, eventLog), eventLog)
                else -> playerEvent.gameText
            }
        )
        val npcTotalGameTexts = game.nonPlayerCharacters.joinToString("\n") { it.getGameText(game.eventlog) }
        if(npcTotalGameTexts.isNotEmpty()){
            StandardInOut.showText(npcTotalGameTexts)
        }


        val input:String = StandardInOut.waitForInput()
        playerEvent = game.playerDo(Input(Interpreter.interpret(input, input2Command, ActionCommand.GibberishInput)), game.eventlog)
        eventLog.add(playerEvent)

         game.nonPlayerCharacters.forEach{ eventLog.add(it.doAction(game.eventlog)) }
    }

    StandardInOut.showText(playerEvent.gameText)
}

fun formatGameTextAndItems(gameText: String, items: List<Item>, eventLog: EventLog): String =
    if (items.isEmpty()) {
        gameText +"\n"
    } else{
        gameText +"\n" + "Du ser " + items.joinToString { it.description(eventLog) }
    }

