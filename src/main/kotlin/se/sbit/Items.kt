package se.sbit


typealias ItemsPlacementMap = Map<ItemType, Placement<ItemType, Room>>

interface ItemType {
    val description: String
}

sealed class Placement<out Item, out Room>
data class Carried<out Item>(val item: Item) : Placement<Item, Nothing>()
data class InRoom<out Item, out Room>(val item: Item, val room: Room): Placement<Item, Room>()

class Items(initialItemMap: ItemsPlacementMap) {

    private val itemMap: MutableMap<ItemType, Placement<ItemType, Room>> = initialItemMap.toMutableMap()


    fun carriedItems(): List<ItemType> = itemMap.values
        .filter{it is Carried<ItemType>}
        .map{it as Carried<ItemType>}
        .map{it.item}

    fun itemsIn(room: Room): List<ItemType> = itemMap.values
        .filter{it is InRoom<ItemType, Room>}
        .map{it as InRoom<ItemType, Room>}
        .filter{it.room == room}
        .map{it.item}


    fun pickUp(item: ItemType, currentRoom:Room): ItemType {
        // Sanity checks
        if(itemMap.containsKey(item)) {
            if(itemMap[item] is Carried<ItemType>) {
                throw Exception("Tried to carry already carried item")
            }
            if((itemMap[item] as InRoom<ItemType, Room>).room != currentRoom){
                throw Exception("Tried to pick up something from another room")
            }
        }

        itemMap[item] = Carried(item)
        return item;
    }

    fun drop(item: ItemType, room: Room): ItemType {
        // Sanity checks
        if(itemMap.containsKey(item) && (itemMap[item]!! is InRoom<ItemType, Room>)) {
            throw Exception("Tried to drop not carried item")
        }

        itemMap[item] = InRoom(item, room)
        return item;
    }

}

