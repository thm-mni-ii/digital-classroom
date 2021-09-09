package de.thm.mni.ii.classroom.exception.classroom

class ConferenceNotFoundException(conferenceId: String): Exception("Conference with id $conferenceId does not exist!")
