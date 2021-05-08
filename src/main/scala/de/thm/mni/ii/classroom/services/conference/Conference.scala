package de.thm.mni.ii.classroom.services.conference

import de.thm.mni.ii.classroom.model.User

import java.net.URI

/**
  * A de.thm.mni.ii.classroom.services.conferences.conference.Conference created by a de.thm.mni.ii.classroom.services.conferences.ConferenceService
  */
abstract class Conference {
  /**
    * The id of the conference
    */
  val id: String

  /**
    * The name of the de.thm.mni.ii.classroom.services.conferences.ConferenceService used to create the de.thm.mni.ii.classroom.services.conferences.conference.Conference
    */
  val serviceName: String

  /**
    * The courseid of the de.thm.mni.ii.classroom.services.conferences.conference.Conference
    */
  val courseId: Int

  /**
    * The visibility of the de.thm.mni.ii.classroom.services.conferences.conference.Conference
    */
  var isVisible: Boolean

  /**
    * Gets the http URL for the conference
    * @param user the user for which to generate the URL
    * @param moderator the type of url to generate
    * @return the conference url
    */
  def getURL(user: User, moderator: Boolean = false): URI

  /**
    * Ends the Conferences
    */
  def end(): Unit

  /**
    * Creates a map containing information about the de.thm.mni.ii.classroom.services.conferences.conference.Conference
    * @return the map
    */
  def toMap: Map[String, String]

}
