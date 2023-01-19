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
        EventLog.add(EndEvent)

        expectThat(EventLog.log().size).isEqualTo(1)
        expectThat(EventLog.log()[0]).isA<EndEvent>()
    }
}