package se.sbit.adventure.engine

open class Character(val description: String)
object Player: Character("A player")
abstract class NPC(description: String): Character(description)
