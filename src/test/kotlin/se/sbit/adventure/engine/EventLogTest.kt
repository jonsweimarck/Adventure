package se.sbit.adventure.engine

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isA
import strikt.assertions.isEqualTo

@DisplayName("using a log of game event: ")
class EventLogTest {

    @Test
    fun `an event can be added`(){
        val eventLog = EventLog()
        eventLog.add(EndEvent("Game Over"))

        expectThat(eventLog.log().size).isEqualTo(1)
        expectThat(eventLog.log()[0]).isA<EndEvent>()
    }
}