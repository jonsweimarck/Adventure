package se.sbit.adventure.engine


typealias ItemsPlacementMap = Map<ItemType, Placement>
typealias ItemsPlacementMap2 = Map<Item, Placement>

@Deprecated("")
interface ItemType {
    val description: String
}

data class Itemstate(val description: String)

interface Item {
    fun description(eventLog: EventLog): String
    fun state(eventLog: EventLog): Itemstate

}

abstract class SinglestateItem(val state: Itemstate):Item {
    override fun state(eventLog: EventLog): Itemstate = state
    override fun description(eventLog: EventLog): String = state.description
}

open class MultistateItem (val states: List<Pair<(EventLog)-> Boolean,  Itemstate> >): Item {

    override fun description(eventLog: EventLog): String =
        state(eventLog).description

    override fun state(eventLog: EventLog): Itemstate =
        when(val index = states.indexOfFirst { it.first.invoke(eventLog) } ) {
            -1 -> throw Exception("No matching itemstate was found for item")
            else -> states[index].second
        }
}



sealed class Placement
object Carried : Placement()
data class InRoom(val room: Room): Placement()

abstract class ItemPickedOrDropped(gameText: String, roomAndState: Pair<Room, State>, character: Character, val item: ItemType ): Event(gameText, roomAndState, character)
abstract class ItemPickedOrDropped2(gameText: String, roomAndState: Pair<Room, State>, character: Character, val item: Item ): Event(gameText, roomAndState, character)

//
//@Deprecated("") class PickedUpItemEvent(gameText: String, roomAndState: Pair<Room, State>, character: Character,  item: ItemType):ItemPickedOrDropped(gameText, roomAndState, character, item)
//@Deprecated("") class DroppedItemEvent(gameText: String,  roomAndState: Pair<Room, State>, character: Character,  item: ItemType):ItemPickedOrDropped(gameText,roomAndState, character, item)

class DroppedItemEvent2(gameText: String,  roomAndState: Pair<Room, State>, character: Character,  item: Item):ItemPickedOrDropped2(gameText,roomAndState, character, item)
class PickedUpItemEvent2(gameText: String, roomAndState: Pair<Room, State>, character: Character,  item: Item):ItemPickedOrDropped2(gameText, roomAndState, character, item)
class NoSuchItemHereEvent(gameText: String, roomAndState: Pair<Room, State>):Event(gameText, roomAndState)
class NoSuchItemToDropItemEvent(gameText: String, roomAndState: Pair<Room, State>):Event(gameText, roomAndState)
class InventoryEvent(gameText: String, roomAndState: Pair<Room, State>):Event(gameText, roomAndState)

//@Deprecated("")
//fun actionForPickUpItem(itemToPickUp:ItemType, noSuchItemHereEventText: String = "That didn't work!", pickedUpEventText: String = "Picked up"): (Input, EventLog, Items) -> Event
//{
//    return fun(input, eventLog, items): Event {
//        val currentRoomAndState = eventLog.getCurrentRoomAndState(Player)
//        val currentRoom = currentRoomAndState.first
//        if (itemsIn(currentRoom, eventLog).none { it == itemToPickUp }){
//            return NoSuchItemHereEvent(noSuchItemHereEventText, currentRoomAndState)
//        }
//        return PickedUpItemEvent("$pickedUpEventText ${itemToPickUp.description}.", currentRoomAndState, Player, itemToPickUp)
//    }
//}

fun actionForPickUpItem2(itemToPickUp:Item, noSuchItemHereEventText: String = "That didn't work!", pickedUpEventText: String = "Picked up"): (Input, EventLog, Items2) -> Event
{
    return fun(input, eventLog, items): Event {
        val currentRoomAndState = eventLog.getCurrentRoomAndState(Player)
        val currentRoom = currentRoomAndState.first
        if (itemsIn2(currentRoom, eventLog).none { it == itemToPickUp }){
            return NoSuchItemHereEvent(noSuchItemHereEventText, currentRoomAndState)
        }
        return PickedUpItemEvent2("$pickedUpEventText ${itemToPickUp.description(eventLog)}.", currentRoomAndState, Player, itemToPickUp)
    }
}

//@Deprecated("")
//fun actionForExamineItem(itemToExam: ItemType, successGameText: String= "You don't see anything special", failureGameText:String = "You don't carry that" ): (Input, EventLog, Items) -> Event =
//    fun(_, eventLog, items): Event =
//        when(carriedItems(eventLog).contains(itemToExam)) {
//            true -> Event(successGameText, eventLog.getCurrentRoomAndState(Player))
//            false -> Event(failureGameText, eventLog.getCurrentRoomAndState(Player))
//        }

fun actionForExamineItem2(itemToExam: Item, successGameText: String= "You don't see anything special", failureGameText:String = "You don't carry that" ): (Input, EventLog, Items2) -> Event =
    fun(_, eventLog, _): Event =
        when(carriedItems2(eventLog).contains(itemToExam)) {
            true -> Event(successGameText, eventLog.getCurrentRoomAndState(Player))
            false -> Event(failureGameText, eventLog.getCurrentRoomAndState(Player))
        }

//@Deprecated("")
//fun actionForDropItem(itemToDrop:ItemType, noSuchItemToDropEventText: String = "That didn't work!", droppedItemEventText: String = "Dropped"): (Input, EventLog, Items) -> Event
//{
//    return fun(_, eventLog, _): Event {
//        if (carriedItems(eventLog).none { it == itemToDrop }){
//            return NoSuchItemToDropItemEvent(noSuchItemToDropEventText, eventLog.getCurrentRoomAndState(Player))
//        }
//        return DroppedItemEvent("${droppedItemEventText} ${itemToDrop.description}.", eventLog.getCurrentRoomAndState(Player), Player, itemToDrop)
//    }
//
//}

fun actionForDropItem2(itemToDrop:Item, noSuchItemToDropEventText: String = "That didn't work!", droppedItemEventText: String = "Dropped"): (Input, EventLog, Items2) -> Event
{
    return fun(_, eventLog, _): Event {
        if (carriedItems2(eventLog).none { it == itemToDrop }){
            return NoSuchItemToDropItemEvent(noSuchItemToDropEventText, eventLog.getCurrentRoomAndState(Player))
        }
        return DroppedItemEvent2("$droppedItemEventText ${itemToDrop.description(eventLog)}.", eventLog.getCurrentRoomAndState(Player), Player, itemToDrop)
    }

}
//@Deprecated("")
//fun goActionForInventory(notCarryingAnythingEventText: String = "You don't carry anything!", carryingEventText: String = "You carry"): (Input, EventLog, Items) -> Event
//{
//    return fun(_, eventLog, items): Event {
//        if (carriedItems(eventLog).isEmpty()) {
//            return InventoryEvent(notCarryingAnythingEventText,eventLog.getCurrentRoomAndState(Player))
//        }
//        return InventoryEvent( "${carryingEventText} ${carriedItems(eventLog).joinToString { it.description }}", eventLog.getCurrentRoomAndState(Player))
//    }
//}

fun goActionForInventory2(notCarryingAnythingEventText: String = "You don't carry anything!", carryingEventText: String = "You carry"): (Input, EventLog, Items2) -> Event
{
    return fun(_, eventLog, items): Event {
        if (carriedItems2(eventLog).isEmpty()) {
            return InventoryEvent(notCarryingAnythingEventText,eventLog.getCurrentRoomAndState(Player))
        }
        return InventoryEvent( "${carryingEventText} ${carriedItems2(eventLog).joinToString { it.description(eventLog) }}", eventLog.getCurrentRoomAndState(Player))
    }
}


fun itemsIn2(room: Room, eventLog: EventLog): List<Item> {
    var itemSet = emptySet<Item>().toMutableSet()
    eventLog.log().forEach{
        if(it.roomAndState.first == room) {
            if (it is DroppedItemEvent2) {
                itemSet.add(it.item)
            } else if (it is PickedUpItemEvent2) {
                itemSet.remove(it.item)
            }
        }
    }
    return itemSet.toList()
}

//@Deprecated("")
//fun itemsIn(room: Room, eventLog: EventLog): List<ItemType> {
//    var itemSet = emptySet<ItemType>().toMutableSet()
//    eventLog.log().forEach{
//        if(it.roomAndState.first == room) {
//            if (it is DroppedItemEvent) {
//                itemSet.add(it.item)
//            } else if (it is PickedUpItemEvent) {
//                itemSet.remove(it.item)
//            }
//        }
//    }
//    return itemSet.toList()
//}


fun carriedItems2(eventLog: EventLog): List<Item> {

    var itemSet = emptySet<Item>().toMutableSet()
    eventLog.log().forEach {
        if (it is PickedUpItemEvent2) {
            itemSet.add(it.item)
        } else if (it is DroppedItemEvent2) {
            itemSet.remove(it.item)
        }
    }
    return itemSet.toList()
}


class Items2(initialItemMap: ItemsPlacementMap2) {

    private val itemMap: MutableMap<Item, Placement> = initialItemMap.toMutableMap()


}

