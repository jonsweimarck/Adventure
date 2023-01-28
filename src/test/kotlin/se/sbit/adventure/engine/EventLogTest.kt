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

        val log = EventLog.fromList(listOf(NewRoomEvent("", roomA, stateA, Player)))
        expectThat(log.getCurrentRoom()).isEqualTo(roomA)
        expectThat(log.getCurrentState()).isEqualTo(stateA)
    }

    @Test
    fun `the current Room and State can be found from multiple NewRoomEvent`(){
        val stateA = State("a")
        val stateB = State("b")
        val roomA = Room(listOf(Pair({ _, _ -> true}, stateA)))
        val roomB = Room(listOf(Pair({ _, _ -> true}, stateB)))
        val newRoomA = NewRoomEvent("", roomA, stateA, Player)
        val sameRoomA = SameRoomEvent("", roomA, stateA, Player)
        val newRoomB = NewRoomEvent("", roomB, stateB, Player)
        val sameRoomB = SameRoomEvent("", roomB, stateB, Player)

        val log = EventLog.fromList(listOf(newRoomB, sameRoomB, newRoomA, sameRoomA))
        expectThat(log.getCurrentRoom()).isEqualTo(roomA)
        expectThat(log.getCurrentState()).isEqualTo(stateA)
    }

    @Test
    fun `an exception is thrown if not current room can be found`(){
        expectCatching { EventLog().getCurrentRoom()}.isFailure()
    }
}