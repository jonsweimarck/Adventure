package se.sbit.adventure.engine

open class Character(val description: String)
object Player: Character("The player")
abstract class NPC(description: String) : Character(description)
