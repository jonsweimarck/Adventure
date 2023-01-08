package se.sbit


typealias ItemsPlacementMap = Map<ItemType, Placement<Room>>

interface ItemType {
    val description: String
}

sealed class Placement<out Room>
object Carried : Placement<Nothing>()
data class InRoom<out Room>(val room: Room): Placement<Room>()

class Items(initialItemMap: ItemsPlacementMap) {

    private val itemMap: MutableMap<ItemType, Placement<Room>> = initialItemMap.toMutableMap()


    fun carriedItems(): List<ItemType> = itemMap.keys.filter{itemMap[it] is Carried}

    fun itemsIn(room: Room): List<ItemType> = itemMap.entries
        .filter{it.value is InRoom<Room>}
        .filter{(it.value as InRoom).room == room}
        .map{it.key}


    fun pickUp(item: ItemType, currentRoom:Room): ItemType {
        // Sanity checks
        if(itemMap.containsKey(item)) {
            if(itemMap[item] is Carried) {
                throw Exception("Tried to carry already carried item")
            }
            if((itemMap[item] as InRoom<Room>).room != currentRoom){
                throw Exception("Tried to pick up something from another room")
            }
        }

        itemMap[item] = Carried
        return item;
    }

    fun drop(item: ItemType, room: Room): ItemType {
        // Sanity checks
        if(itemMap.containsKey(item) && (itemMap[item]!! is InRoom<Room>)) {
            throw Exception("Tried to drop not carried item")
        }

        itemMap[item] = InRoom(room)
        return item;
    }

}

