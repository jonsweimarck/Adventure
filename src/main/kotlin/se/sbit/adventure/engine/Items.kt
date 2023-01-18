package se.sbit.adventure.engine


typealias ItemsPlacementMap = Map<ItemType, Placement>

interface ItemType {
    val description: String
}

sealed class Placement
object Carried : Placement()
data class InRoom(val room: Room): Placement()


class NoSuchItemHereEvent(gameText: String):Event(gameText)
class PickedUpItemEvent(gameText: String):Event(gameText)
class DroppedItemEvent(gameText: String):Event(gameText)
class NoSuchItemToDropItemEvent(gameText: String):Event(gameText)

fun goActionForPickUpItem(itemToPickUp:ItemType, noSuchItemHereEventText: String = "That didn't work!", pickedUpEventText: String = "Picked up"): (Input, Room, Items) -> Event
{
    return fun(input, currentRoom, items): Event {
        if (items.itemsIn(currentRoom).none { it == itemToPickUp }){
            return NoSuchItemHereEvent(noSuchItemHereEventText)
        }
        items.pickUp(itemToPickUp, currentRoom)
        return PickedUpItemEvent("${pickedUpEventText} ${itemToPickUp.description}.")
    }
}

fun goActionForDropItem(itemToDrop:ItemType, noSuchItemToDropEventText: String = "That didn't work!", droppedItemEventText: String = "Dropped"): (Input, Room, Items) -> Event
{
    return fun(input, currentRoom, items): Event {
        if (items.carriedItems().none { it == itemToDrop }){
            return NoSuchItemToDropItemEvent(noSuchItemToDropEventText)
        }
        items.drop(itemToDrop, currentRoom)
        return DroppedItemEvent("${droppedItemEventText} ${itemToDrop.description}.")
    }

}

class Items(initialItemMap: ItemsPlacementMap, val itemUsageRoomMap: Map<ItemType, Room>) {

    private val itemMap: MutableMap<ItemType, Placement> = initialItemMap.toMutableMap()


    fun carriedItems(): List<ItemType> = itemMap.keys.filter{itemMap[it] is Carried }

    fun itemsIn(room: Room): List<ItemType> = itemMap.entries
        .filter{it.value is InRoom }
        .filter{(it.value as InRoom).room == room}
        .map{it.key}

    fun usableItemsInRoom(room: Room): List<ItemType> = itemUsageRoomMap.entries.filter { it.value==room }.map { it.key }

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

}

