package se.sbit.adventure.engine


typealias ItemsPlacementMap = Map<ItemType, Placement>

interface ItemType {
    val description: String
}

sealed class Placement
object Carried : Placement()
data class InRoom(val room: Room): Placement()

abstract class ItemPickedOrDropped(gameText: String, character: Character, val item: ItemType ): Event(gameText, character)
class PickedUpItemEvent(gameText: String,  character: Character,  item: ItemType):ItemPickedOrDropped(gameText, character, item)
class DroppedItemEvent(gameText: String,  character: Character,  item: ItemType):ItemPickedOrDropped(gameText, character, item)

class NoSuchItemHereEvent(gameText: String):Event(gameText)
class NoSuchItemToDropItemEvent(gameText: String):Event(gameText)
class InventoryEvent(gameText: String):Event(gameText)

fun actionForPickUpItem(itemToPickUp:ItemType, noSuchItemHereEventText: String = "That didn't work!", pickedUpEventText: String = "Picked up"): (Input, EventLog, Items) -> Event
{
    return fun(input, eventLog, items): Event {
        val currentRoom = eventLog.getCurrentRoom(Player)
        if (items.itemsIn(currentRoom).none { it == itemToPickUp }){
            return NoSuchItemHereEvent(noSuchItemHereEventText)
        }
        items.pickUp(itemToPickUp, currentRoom)
        return PickedUpItemEvent("$pickedUpEventText ${itemToPickUp.description}.", Player, itemToPickUp)
    }
}

fun actionForExamineItem(itemToExam: ItemType, successGameText: String= "You don't see anything special", failureGameText:String = "You don't carry that" ): (Input, EventLog, Items) -> Event =
    fun(_, _, items): Event =
        when(items.carriedItems().contains(itemToExam)) {
            true -> Event(successGameText)
            false -> Event(failureGameText)
        }


fun actionForDropItem(itemToDrop:ItemType, noSuchItemToDropEventText: String = "That didn't work!", droppedItemEventText: String = "Dropped"): (Input, EventLog, Items) -> Event
{
    return fun(input, eventLog, items): Event {
        if (items.carriedItems().none { it == itemToDrop }){
            return NoSuchItemToDropItemEvent(noSuchItemToDropEventText)
        }
        items.drop(itemToDrop, eventLog.getCurrentRoom(Player))
        return DroppedItemEvent("${droppedItemEventText} ${itemToDrop.description}.", Player, itemToDrop)
    }

}

fun goActionForInventory(notCarryingAnythingEventText: String = "You don't carry anything!", carryingEventText: String = "You carry"): (Input, EventLog, Items) -> Event
{
    return fun(_, _, items): Event {
        if (items.carriedItems().isEmpty()) {
            return InventoryEvent(notCarryingAnythingEventText)
        }
        return InventoryEvent( "${carryingEventText} ${items.carriedItems().joinToString { it.description }}")
    }
}

// *****************

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

// *****************


class Items(initialItemMap: ItemsPlacementMap) {

    private val itemMap: MutableMap<ItemType, Placement> = initialItemMap.toMutableMap()


    fun carriedItems(): List<ItemType> = itemMap.keys.filter{itemMap[it] is Carried }

    fun itemsIn(room: Room): List<ItemType> = itemMap.entries
        .filter{it.value is InRoom }
        .filter{(it.value as InRoom).room == room}
        .map{it.key}


    fun pickUp(item: ItemType, currentRoom: Room): ItemType {
        // Sanity checks
        if(itemMap.containsKey(item)) {
            if(itemMap[item] is Carried) {
                throw Exception("Tried to carry already carried item")
            }
            if((itemMap[item] as InRoom).room != currentRoom){
                throw Exception("Tried to pick up something from another room")
            }
        }

        itemMap[item] = Carried
        return item;
    }

    fun drop(item: ItemType, room: Room): ItemType {
        // Sanity checks
        if(itemMap.containsKey(item) && (itemMap[item]!! is InRoom)) {
            throw Exception("Tried to drop not carried item")
        }

        itemMap[item] = InRoom(room)
        return item;
    }

    // Kan man göra denna eller hela itemMap unmutable?
    fun replaceCarried(carriedItem: ItemType, replaceWith: ItemType): ItemType {
        if(! carriedItems().contains(carriedItem)){
            throw Exception("Tried to replace not carried item")
        }

        itemMap.put(replaceWith, Carried)
        itemMap.remove(carriedItem)
        return replaceWith
    }

}

