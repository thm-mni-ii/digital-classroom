package de.thm.mni.ii.classroom.services

import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.stereotype.Service

/**
  * Factory that creates ConferenceServices
  */
@Service
class ConferenceServiceFactoryService() {
  @Autowired
  private val templateBuilder: RestTemplateBuilder = null

  @Value("${services.bbb.service-url}")
  private val apiUrl: String = null
  @Value("${services.bbb.shared-secret}")
  private val secret: String = null
  @Value("${services.bbb.origin-name}")
  private val originName: String = null
  @Value("${services.bbb.origin-version}")
  private val originVersion: String = null

  /**
    * Runs the factory
    * @param service The name of the service that should be constructed
    * @return A new de.thm.mni.ii.classroom.services.conferences.ConferenceService
    */
  def apply(service: String): ConferenceService = {
    service match {
      // case "jitsi" => new JitsiService(templateBuilder)
      case "bigbluebutton" => new BBBService(templateBuilder, apiUrl, secret, originName, originVersion)
      case name: String => throw new IllegalArgumentException(s"unknown conference service: ${name}")
    }
  }
}
