package se.sbit.adventure.engine

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import strikt.api.expectCatching
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isFailure

@DisplayName("using a log of game event: ")
class EventLogTest {

    @Test
    fun `the current Room and State can be found from single NewRoomEvent`(){
        val stateA = RoomState("a")
        val stateB = RoomState("b")
        val roomA = Room(listOf(Pair({ _, _ -> true}, stateA)))

        val log = EventLog.fromList(listOf(NewRoomEvent("", Pair(roomA, stateA), Player)))
        expectThat(log.getCurrentRoom(Player)).isEqualTo(roomA)
        expectThat(log.getCurrentRoomState(Player)).isEqualTo(stateA)
    }

    @Test
    fun `the current Room and State can be found from multiple NewRoomEvent from multiple Characters`(){

        val miscNPC = object: NPC("my NPC"){
            override fun doAction(eventlog: EventLog): Event {
                TODO("Not yet implemented")
            }

            override fun getGameText(eventlog: EventLog): String {
                TODO("Not yet implemented")
            }

        }

        val stateA = RoomState("a")
        val stateB = RoomState("b")
        val roomA = Room(listOf(Pair({ _, _ -> true}, stateA)))
        val roomB = Room(listOf(Pair({ _, _ -> true}, stateB)))
        val newRoomAplayer = NewRoomEvent("", Pair(roomA, stateA), Player)
        val newRoomBnpc = NewRoomEvent("", Pair(roomB, stateB), miscNPC)
        val sameRoomAplayer = SameRoomEvent("", Pair(roomA, stateA), Player)
        val newRoomBplayer = NewRoomEvent("", Pair(roomB, stateB), Player)
        val sameRoomBplayer = SameRoomEvent("", Pair(roomB, stateB), Player)

        val log = EventLog.fromList(listOf(newRoomBplayer, sameRoomBplayer,newRoomBnpc, newRoomAplayer, sameRoomAplayer))

        expectThat(log.getCurrentRoom(Player)).isEqualTo(roomA)
        expectThat(log.getCurrentRoomState(Player)).isEqualTo(stateA)

        expectThat(log.getCurrentRoom(miscNPC)).isEqualTo(roomB)
        expectThat(log.getCurrentRoomState(miscNPC)).isEqualTo(stateB)
    }

    @Test
    fun `the number of turns since entered the current room can be found `(){
        val stateA = RoomState("a")
        val stateB = RoomState("b")
        val roomA = Room(listOf(Pair({ _, _ -> true}, stateA)))
        val roomB = Room(listOf(Pair({ _, _ -> true}, stateB)))

        val newRoomBplayer = NewRoomEvent("", Pair(roomB, stateB), Player)
        val newRoomAplayer = NewRoomEvent("", Pair(roomA, stateA), Player)
        val newRoomAnpc = NewRoomEvent("", Pair(roomA, stateA), getDummyNpc())
        val sameRoomAplayer = SameRoomEvent("", Pair(roomA, stateA), Player)
        val notARoomEvent = Event("something", Pair(roomA, stateA), Player)

        val log = EventLog.fromList(listOf(newRoomBplayer, newRoomAplayer, notARoomEvent, newRoomAnpc, sameRoomAplayer))
        expectThat(log.getNumberOfTurnsSinceEnteredCurrentRoom(Player)).isEqualTo(3)
    }


    @Test
    fun `an exception is thrown if not current room can be found`(){
        expectCatching { EventLog().getCurrentRoom(Player)}.isFailure()
    }

    @Test
    fun `the number of concurrent turns in same room is zero if not in same room anymore`(){

        val miscNPC = getDummyNpc()
        val stateA = RoomState("a")
        val stateB = RoomState("b")
        val roomA = Room(listOf(Pair({ _, _ -> true}, stateA)))
        val roomB = Room(listOf(Pair({ _, _ -> true}, stateB)))

        val newRoomAplayer = NewRoomEvent("", Pair(roomA, stateA), Player)
        val newRoomBnpc = NewRoomEvent("", Pair(roomB, stateB), miscNPC)
        val newRoomBplayer = NewRoomEvent("", Pair(roomB, stateB), Player)

        val log = EventLog.fromList(listOf(newRoomBplayer, newRoomBnpc,newRoomAplayer))

        expectThat(log.getNumberOfOfTurnsStillInSameRoom(Player, miscNPC)).isEqualTo(0)

    }

    @Test
    fun `the number of concurrent turns in same room is 1 if only just entered the same room`(){

        val miscNPC = getDummyNpc()
        val stateA = RoomState("a")
        val stateB = RoomState("b")
        val roomA = Room(listOf(Pair({ _, _ -> true}, stateA)))
        val roomB = Room(listOf(Pair({ _, _ -> true}, stateB)))

        val newRoomAplayer = NewRoomEvent("", Pair(roomA, stateA), Player)
        val newRoomAnpc = NewRoomEvent("", Pair(roomA, stateA), miscNPC)
        val newRoomBnpc = NewRoomEvent("", Pair(roomB, stateB), miscNPC)
        val newRoomBplayer = NewRoomEvent("", Pair(roomB, stateB), Player)

        val log = EventLog.fromList(listOf(newRoomBplayer, newRoomBnpc, newRoomAplayer, newRoomAnpc))

        expectThat(log.getNumberOfOfTurnsStillInSameRoom(Player, miscNPC)).isEqualTo(1)
    }

    @Test
    fun `the number of concurrent turns in same room is correct if even when room not entered the same turn`(){

        val miscNPC = getDummyNpc()
        val stateA = RoomState("a")
        val stateB = RoomState("b")
        val roomA = Room(listOf(Pair({ _, _ -> true}, stateA)))
        val roomB = Room(listOf(Pair({ _, _ -> true}, stateB)))

        val newRoomAplayer = NewRoomEvent("", Pair(roomA, stateA), Player)
        val newRoomAnpc = NewRoomEvent("", Pair(roomA, stateA), miscNPC)
        val sameRoomAplayer = SameRoomEvent("", Pair(roomA, stateA), Player)
        val sameRoomAnpc = SameRoomEvent("", Pair(roomA, stateA), miscNPC)
        val newRoomBnpc = NewRoomEvent("", Pair(roomB, stateB), miscNPC)

        val log = EventLog.fromList(listOf(newRoomAplayer, newRoomBnpc, sameRoomAplayer, newRoomAnpc, sameRoomAplayer, sameRoomAnpc))

        expectThat(log.getNumberOfOfTurnsStillInSameRoom(Player, miscNPC)).isEqualTo(2)
    }

    private fun getDummyNpc(): Character =
        object: NPC("my NPC"){
            override fun doAction(eventlog: EventLog): Event {
                TODO("Not yet implemented")
            }

            override fun getGameText(eventlog: EventLog): String {
                TODO("Not yet implemented")
            }

    }

}