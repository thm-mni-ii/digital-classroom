package de.thm.mni.ii.classroom.exception.classroom

import de.thm.mni.ii.classroom.event.InvitationEvent
import de.thm.mni.ii.classroom.model.classroom.User

class InvitationException(user: User, invitationEvent: InvitationEvent) :
    ClassroomException("Sender ${user.userId} does not match inviter ${invitationEvent.inviter.userId}!")
