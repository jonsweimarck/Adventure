package se.sbit.adventure.engine

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import strikt.api.expectCatching
import strikt.api.expectThat
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import strikt.assertions.isFailure

@DisplayName("using a log of game event: ")
class EventLogTest {

    @Test
    fun `an event can be added`(){
        val eventLog = EventLog()
        eventLog.add(EndEvent("Game Over"))

        expectThat(eventLog.log().size).isEqualTo(1)
        expectThat(eventLog.log()[0]).isA<EndEvent>()
    }

    @Test
    fun `the current Room and State can be found from single NewRoomEvent`(){
        val stateA = State("a")
        val stateB = State("b")
        val roomA = Room(listOf(Pair({ _, _ -> true}, stateA)))
        val roomB = Room(listOf(Pair({ _, _ -> true}, stateB)))

        val log = EventLog.fromList(listOf(NewRoomEvent("", Pair(roomA, stateA), Player)))
        expectThat(log.getCurrentRoom(Player)).isEqualTo(roomA)
        expectThat(log.getCurrentState(Player)).isEqualTo(stateA)
    }

    @Test
    fun `the current Room and State can be found from multiple NewRoomEvent from multiple Characters`(){

        val miscNPC = object: NPC("my NPC"){}

        val stateA = State("a")
        val stateB = State("b")
        val roomA = Room(listOf(Pair({ _, _ -> true}, stateA)))
        val roomB = Room(listOf(Pair({ _, _ -> true}, stateB)))
        val newRoomAplayer = NewRoomEvent("", Pair(roomA, stateA), Player)
        val newRoomBnpc = NewRoomEvent("", Pair(roomB, stateB), miscNPC)
        val sameRoomAplayer = SameRoomEvent("", Pair(roomA, stateA), Player)
        val newRoomBplayer = NewRoomEvent("", Pair(roomB, stateB), Player)
        val sameRoomBplayer = SameRoomEvent("", Pair(roomB, stateB), Player)

        val log = EventLog.fromList(listOf(newRoomBplayer, sameRoomBplayer,newRoomBnpc, newRoomAplayer, sameRoomAplayer))

        expectThat(log.getCurrentRoom(Player)).isEqualTo(roomA)
        expectThat(log.getCurrentState(Player)).isEqualTo(stateA)

        expectThat(log.getCurrentRoom(miscNPC)).isEqualTo(roomB)
        expectThat(log.getCurrentState(miscNPC)).isEqualTo(stateB)
    }

    @Test
    fun `an exception is thrown if not current room can be found`(){
        expectCatching { EventLog().getCurrentRoom(Player)}.isFailure()
    }
}