package de.thm.mni.ii.classroom.services

import de.thm.mni.ii.classroom.event.ClassroomEvent
import de.thm.mni.ii.classroom.model.classroom.DigitalClassroom
import org.slf4j.LoggerFactory
import org.springframework.messaging.rsocket.RSocketRequester
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class ClassroomEventSenderService {

    private val logger = LoggerFactory.getLogger(this::class.java)

    fun sendToAll(classroom: DigitalClassroom, event: ClassroomEvent): Mono<Void> {
        return classroom.getSockets().doOnNext { (user, requester) ->
            if (requester != null) {
                logger.info("sending to ${user.fullName}")
                fireAndForget(event, requester)
            }
        }.then()
    }

    fun fireAndForget(event: ClassroomEvent, requester: RSocketRequester) {
        requester.route("").data(event).send().subscribe().dispose()
    }
}
