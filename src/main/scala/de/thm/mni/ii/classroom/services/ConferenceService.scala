package de.thm.mni.ii.classroom.services

import de.thm.mni.ii.classroom.services.conference.Conference

/**
 * A Service offering Web Conferences for example BigBlueButton or Jitsi
 */
trait ConferenceService {
  /**
   * Creates a new Conference for the Conference Service
   *
   * @param id the id for the new conference
   * @return the newly created conference
   */
  def createConference(id: Int): Conference
}
