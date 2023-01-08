package se.sbit


typealias ItemsPlacementMap = Map<ItemType, Placement>

interface ItemType {
    val description: String
}

sealed class Placement
object Carried : Placement()
data class InRoom(val room: Room): Placement()

class Items(initialItemMap: ItemsPlacementMap) {

    private val itemMap: MutableMap<ItemType, Placement> = initialItemMap.toMutableMap()


    fun carriedItems(): List<ItemType> = itemMap.keys.filter{itemMap[it] is Carried}

    fun itemsIn(room: Room): List<ItemType> = itemMap.entries
        .filter{it.value is InRoom}
        .filter{(it.value as InRoom).room == room}
        .map{it.key}


    fun pickUp(item: ItemType, currentRoom:Room): ItemType {
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

