package se.sbit.adventure.engine


typealias ItemsPlacementMap = Map<ItemType, Placement>

interface ItemType {
    val description: String
}

sealed class Placement
object Carried : Placement()
data class InRoom(val room: Room): Placement()

abstract class ItemPickedOrDropped(gameText: String, roomAndState: Pair<Room, State>, character: Character, val item: ItemType ): Event(gameText, roomAndState, character)
class PickedUpItemEvent(gameText: String, roomAndState: Pair<Room, State>, character: Character,  item: ItemType):ItemPickedOrDropped(gameText, roomAndState, character, item)
class DroppedItemEvent(gameText: String,  roomAndState: Pair<Room, State>, character: Character,  item: ItemType):ItemPickedOrDropped(gameText,roomAndState, character, item)

class NoSuchItemHereEvent(gameText: String, roomAndState: Pair<Room, State>):Event(gameText, roomAndState)
class NoSuchItemToDropItemEvent(gameText: String, roomAndState: Pair<Room, State>):Event(gameText, roomAndState)
class InventoryEvent(gameText: String, roomAndState: Pair<Room, State>):Event(gameText, roomAndState)

fun actionForPickUpItem(itemToPickUp:ItemType, noSuchItemHereEventText: String = "That didn't work!", pickedUpEventText: String = "Picked up"): (Input, EventLog, Items) -> Event
{
    return fun(input, eventLog, items): Event {
        val currentRoomAndState = eventLog.getCurrentRoomAndState(Player)
        val currentRoom = currentRoomAndState.first
        if (items.itemsIn(currentRoom).none { it == itemToPickUp }){
            return NoSuchItemHereEvent(noSuchItemHereEventText, currentRoomAndState)
        }
        return PickedUpItemEvent("$pickedUpEventText ${itemToPickUp.description}.", currentRoomAndState, Player, itemToPickUp)
    }
}


fun actionForExamineItem(itemToExam: ItemType, successGameText: String= "You don't see anything special", failureGameText:String = "You don't carry that" ): (Input, EventLog, Items) -> Event =
    fun(_, eventLog, items): Event =
        when(carriedItems(eventLog).contains(itemToExam)) {
            true -> Event(successGameText, eventLog.getCurrentRoomAndState(Player))
            false -> Event(failureGameText, eventLog.getCurrentRoomAndState(Player))
        }


fun actionForDropItem(itemToDrop:ItemType, noSuchItemToDropEventText: String = "That didn't work!", droppedItemEventText: String = "Dropped"): (Input, EventLog, Items) -> Event
{
    return fun(_, eventLog, _): Event {
        if (carriedItems(eventLog).none { it == itemToDrop }){
            return NoSuchItemToDropItemEvent(noSuchItemToDropEventText, eventLog.getCurrentRoomAndState(Player))
        }
        return DroppedItemEvent("${droppedItemEventText} ${itemToDrop.description}.", eventLog.getCurrentRoomAndState(Player), Player, itemToDrop)
    }

}

fun goActionForInventory(notCarryingAnythingEventText: String = "You don't carry anything!", carryingEventText: String = "You carry"): (Input, EventLog, Items) -> Event
{
    return fun(_, eventLog, items): Event {
        if (carriedItems(eventLog).isEmpty()) {
            return InventoryEvent(notCarryingAnythingEventText,eventLog.getCurrentRoomAndState(Player))
        }
        return InventoryEvent( "${carryingEventText} ${carriedItems(eventLog).joinToString { it.description }}", eventLog.getCurrentRoomAndState(Player))
    }
}

fun itemsIn(room: Room, eventLog: EventLog): List<ItemType> {

    var itemMap = emptyMap<ItemType, Int>().toMutableMap()
    eventLog.log().forEach{
        if(it.roomAndState.first == room){
            if(it is PickedUpItemEvent){
                var pickedAndDrop: Int = itemMap[it.item] ?: 0
                itemMap[it.item] = pickedAndDrop +1
            } else if(it is DroppedItemEvent){
                var pickedAndDrop: Int = itemMap[it.item] ?: 0
                itemMap[it.item] = pickedAndDrop -1
            }
        }
    }
    //sanity check
    if(! itemMap.filterValues { it > 1 || it < 0 }.isEmpty()){
        throw Exception("The room has more than one or less than zero of something")
    }

    return itemMap.filter { it.value == 1 }.keys.toList()
}




fun carriedItems(eventLog: EventLog): List<ItemType> {
    var itemMap = emptyMap<ItemType, Int>().toMutableMap()
    eventLog.log().forEach{
        if(it.character == Player){
            if(it is PickedUpItemEvent){
                var pickedAndDrop: Int = itemMap[it.item] ?: 0
                itemMap[it.item] = pickedAndDrop +1
            } else if(it is DroppedItemEvent){
                var pickedAndDrop: Int = itemMap[it.item] ?: 0
                itemMap[it.item] = pickedAndDrop -1
            }
        }
    }
    //sanity check
    if(! itemMap.filterValues { it > 1 || it < 0 }.isEmpty()){
        throw Exception("carrying more than one or less than zero of something ")
    }

    return itemMap.filter { it.value == 1 }.keys.toList()
}



class Items(initialItemMap: ItemsPlacementMap) {

    private val itemMap: MutableMap<ItemType, Placement> = initialItemMap.toMutableMap()


    @Deprecated("Use caller fu above instead")
    fun itemsIn(room: Room): List<ItemType> = itemMap.entries
        .filter{it.value is InRoom }
        .filter{(it.value as InRoom).room == room}
        .map{it.key}


    @Deprecated("Use caller fun above instead")
    // Kan man göra denna eller hela itemMap unmutable?
    fun replaceCarried(carriedItem: ItemType, replaceWith: ItemType, eventLog: EventLog): ItemType {
        if(! carriedItems(eventLog).contains(carriedItem)){
            throw Exception("Tried to replace not carried item")
        }

        itemMap.put(replaceWith, Carried)
        itemMap.remove(carriedItem)
        return replaceWith
    }

}

