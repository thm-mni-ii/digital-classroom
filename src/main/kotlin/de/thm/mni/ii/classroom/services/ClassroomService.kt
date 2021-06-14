package de.thm.mni.ii.classroom.services

import de.thm.mni.ii.classroom.exception.MissingMeetingIDException
import de.thm.mni.ii.classroom.model.DigitalClassroomInstance
import de.thm.mni.ii.classroom.model.dto.*
import de.thm.mni.ii.classroom.util.getMeetingID
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.util.MultiValueMap
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@Component
class ClassroomService {

    private val logger = LoggerFactory.getLogger(this::class.java)

    private val classrooms = HashMap<String, DigitalClassroomInstance>()

    fun createClassroom(param: MultiValueMap<String, String>): Mono<ReturnCodeBBB> {
        return Mono.create {
            val meetingID = getMeetingID(param)
            val classroomInstance = classrooms.computeIfAbsent(meetingID) { DigitalClassroomInstance(param) }
            it.success(CreateRoomBBB(classroomInstance))
        }
    }

    fun joinClassroom(param: MultiValueMap<String, String>): Mono<ReturnCodeBBB> = JoinRoomBBB().toMono()

    fun isMeetingRunning(param: MultiValueMap<String, String>): Mono<ReturnCodeBBB> {
        return Mono.create {
            val meetingID = getMeetingID(param)
            it.success(IsMeetingRunningBBB(classrooms.containsKey(meetingID)))
        }
    }

    fun getClassroomInstance(meetingID: String): DigitalClassroomInstance? = classrooms[meetingID]

}
