package de.thm.mni.ii.classroom.exception.classroom

import de.thm.mni.ii.classroom.event.InvitationEvent
import de.thm.mni.ii.classroom.model.classroom.UserCredentials

class InvitationException(userCredentials: UserCredentials, invitationEvent: InvitationEvent) :
    ClassroomException("Sender ${userCredentials.userId} does not match inviter ${invitationEvent.inviter.userId}!")
