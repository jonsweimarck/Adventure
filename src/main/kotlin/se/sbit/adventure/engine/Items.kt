package se.sbit.adventure.engine

interface Item {
    fun description(eventLog: EventLog): String
    fun state(eventLog: EventLog): ItemState
}

data class ItemState(val description: String)

abstract class SinglestateItem(private val state: ItemState):Item {
    override fun state(eventLog: EventLog): ItemState = state
    override fun description(eventLog: EventLog): String = state.description
}

open class MultistateItem (private val states: List<Pair<(EventLog)-> Boolean,  ItemState> >): Item {

    override fun description(eventLog: EventLog): String =
        state(eventLog).description

    override fun state(eventLog: EventLog): ItemState =
        when(val index = states.indexOfFirst { it.first.invoke(eventLog) } ) {
            -1 -> throw Exception("No matching itemstate was found for item")
            else -> states[index].second
        }
}

typealias ItemsPlacementMap = Map<Item, Placement>

sealed class Placement
object Carried : Placement()
data class InRoom(val room: Room): Placement()

abstract class ItemPickedOrDropped(gameText: String, roomAndState: Pair<Room, RoomState>, character: Character, val item: Item ): Event(gameText, roomAndState, character)


class DroppedItemEvent(gameText: String, roomAndState: Pair<Room, RoomState>, character: Character, item: Item):ItemPickedOrDropped(gameText,roomAndState, character, item)
class PickedUpItemEvent(gameText: String, roomAndState: Pair<Room, RoomState>, character: Character, item: Item):ItemPickedOrDropped(gameText, roomAndState, character, item)
class NoSuchItemHereEvent(gameText: String, roomAndState: Pair<Room, RoomState>):Event(gameText, roomAndState)
class NoSuchItemToDropItemEvent(gameText: String, roomAndState: Pair<Room, RoomState>):Event(gameText, roomAndState)
class InventoryEvent(gameText: String, roomAndState: Pair<Room, RoomState>):Event(gameText, roomAndState)


fun actionForPickUpItem(itemToPickUp:Item, noSuchItemHereEventText: String = "That didn't work!", pickedUpEventText: String = "Picked up"): (Input, EventLog) -> Event
{
    return fun(_ , eventLog): Event {
        val currentRoomAndState = eventLog.getCurrentRoomAndState(Player)
        val currentRoom = currentRoomAndState.first
        if (itemsIn(currentRoom, eventLog).none { it == itemToPickUp }){
            return NoSuchItemHereEvent(noSuchItemHereEventText, currentRoomAndState)
        }
        return PickedUpItemEvent("$pickedUpEventText ${itemToPickUp.description(eventLog)}.", currentRoomAndState, Player, itemToPickUp)
    }
}


fun actionForExamineItem(itemToExam: Item, successGameText: String= "You don't see anything special", failureGameText:String = "You don't carry that" ): (Input, EventLog) -> Event =
    fun(_, eventLog): Event =
        when(carriedItems(eventLog).contains(itemToExam)) {
            true -> Event(successGameText, eventLog.getCurrentRoomAndState(Player))
            false -> Event(failureGameText, eventLog.getCurrentRoomAndState(Player))
        }


fun actionForDropItem(itemToDrop:Item, noSuchItemToDropEventText: String = "That didn't work!", droppedItemEventText: String = "Dropped"): (Input, EventLog) -> Event
{
    return fun(_, eventLog): Event {
        if (carriedItems(eventLog).none { it == itemToDrop }){
            return NoSuchItemToDropItemEvent(noSuchItemToDropEventText, eventLog.getCurrentRoomAndState(Player))
        }
        return DroppedItemEvent("$droppedItemEventText ${itemToDrop.description(eventLog)}.", eventLog.getCurrentRoomAndState(Player), Player, itemToDrop)
    }

}

fun goActionForInventory(notCarryingAnythingEventText: String = "You don't carry anything!", carryingEventText: String = "You carry"): (Input, EventLog) -> Event
{
    return fun(_, eventLog): Event {
        if (carriedItems(eventLog).isEmpty()) {
            return InventoryEvent(notCarryingAnythingEventText,eventLog.getCurrentRoomAndState(Player))
        }
        return InventoryEvent( "$carryingEventText ${carriedItems(eventLog).joinToString { it.description(eventLog) }}", eventLog.getCurrentRoomAndState(Player))
    }
}


fun itemsIn(room: Room, eventLog: EventLog): List<Item> {
    val itemSet = emptySet<Item>().toMutableSet()
    eventLog.log().forEach{
        if(it.roomAndState.first == room) {
            if (it is DroppedItemEvent) {
                itemSet.add(it.item)
            } else if (it is PickedUpItemEvent) {
                itemSet.remove(it.item)
            }
        }
    }
    return itemSet.toList()
}



fun carriedItems(eventLog: EventLog): List<Item> {

    val itemSet = emptySet<Item>().toMutableSet()
    eventLog.log().forEach {
        if (it is PickedUpItemEvent) {
            itemSet.add(it.item)
        } else if (it is DroppedItemEvent) {
            itemSet.remove(it.item)
        }
    }
    return itemSet.toList()
}

