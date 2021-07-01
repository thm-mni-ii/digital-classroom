package de.thm.mni.ii.classroom.model

data class Ticket(val title: String,
                  val description: String,
                  val creator: User,
                  val assignee: User?)
