package se.sbit.adventure.engine


data class NonPlayerCharacter(val description: String, val doAction: (Room, Room, State, EventLog)-> Event) {
}