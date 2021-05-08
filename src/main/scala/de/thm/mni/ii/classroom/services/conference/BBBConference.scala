package de.thm.mni.ii.classroom.services.conference

import de.thm.mni.ii.classroom.model.User
import de.thm.mni.ii.classroom.services.BBBService

import java.net.URI

/**
  * A BBB de.thm.mni.ii.classroom.services.conferences.conference.Conference
  * @param id The id of the conference
  * @param courseId The courseid of the de.thm.mni.ii.classroom.services.conferences.conference.Conference
  * @param meetingPassword The meeting password of the conference
  * @param moderatorPassword The moderator password of the conference
  * @param bbbService The de.thm.mni.ii.classroom.services.conferences.BBBService used internaly to create the bbb URLs
  */
class BBBConference(override val id: String, override val courseId: Int,
                    val meetingPassword: String, val moderatorPassword: String,
                    private val bbbService: BBBService) extends Conference {
  /**
    * The name of the de.thm.mni.ii.classroom.services.conferences.ConferenceService used to create the de.thm.mni.ii.classroom.services.conferences.conference.Conference
    */
  override val serviceName: String = BBBService.name
  /**
    * The visibility of the de.thm.mni.ii.classroom.services.conferences.conference.Conference
    */
  override var isVisible: Boolean = true

  /**
    * Gets the url to the de.thm.mni.ii.classroom.services.conferences.conference.Conference
    * @param user the user for which to generate the URL
    * @param moderator the type of url to generate
    * @return the conference url
    */
  override def getURL(user: User, moderator: Boolean): URI =
    bbbService.getBBBConferenceLink(user, id, if (moderator) moderatorPassword else meetingPassword)

  /**
    * Ends the Conferences
    */
  override def end(): Unit =
    bbbService.endBBBConference(id, moderatorPassword)

  /**
    * Creates a map containing information about the de.thm.mni.ii.classroom.services.conferences.conference.Conference
    *  @return the map
    */
  override def toMap: Map[String, String] = Map(
    "meetingId" -> id,
    "meetingPassword" -> meetingPassword,
    "moderatorPassword" -> moderatorPassword,
    "service" -> serviceName
  )
}
