package de.thm.mni.ii.classroom.services

import de.thm.mni.ii.classroom.services.conference.Conference

/**
 * A Service offering Web Conferences for example BigBlueButton
 */
trait ConferenceService {
  /**
   * Creates a new Conference for the Conference Service
   *
   * @param courseId the courseId
   * @return the newly created conference
   */
  def createConference(courseId: Int): Conference
}
