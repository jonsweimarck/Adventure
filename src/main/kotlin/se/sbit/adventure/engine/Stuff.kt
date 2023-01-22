package se.sbit.adventure.engine

//fun playerGo(input: Input, currentRoom: Room, connections: RoomConnectionsMap): Event {
//    val roomConnections = connections.getOrElse(currentRoom) {
//        return SameRoomEvent("", currentRoom) // Should neeeeeever happen
//    }
//
//    val index = roomConnections.indexOfFirst { it.first.invoke(input, currentRoom)}
//    if(index == -1) {
//        return SameRoomEvent("", currentRoom)
//    }
//    return NewRoomEvent("", roomConnections[index].second)
//}
//
//val y = fun (input: Input, currentRoom: Room, connections: RoomConnectionsMap): Event {
//    val roomConnections = connections.getOrElse(currentRoom) {
//        return SameRoomEvent("", currentRoom) // Should neeeeeever happen
//    }
//
//    val index = roomConnections.indexOfFirst { it.first.invoke(input, currentRoom)}
//    if(index == -1) {
//        return SameRoomEvent("", currentRoom)
//    }
//    return NewRoomEvent("", roomConnections[index].second)
//}
//
//fun goActionFrom1(connections: RoomConnectionsMap): (Input, Room, Items) -> Event =
//    {input, room, items -> y(input, room, connections) }
//
//
//fun goActionFrom2(connections: RoomConnectionsMap): (Input, Room, Items) -> Event =
//    {input, room, items -> playerGo(input, room, connections) }
//
//
//fun goActionFrom3(connections: RoomConnectionsMap): (Input, Room, Items) -> Event = {
//        input, room, items ->
//    println("")
//    SameRoomEvent("", room)
//}