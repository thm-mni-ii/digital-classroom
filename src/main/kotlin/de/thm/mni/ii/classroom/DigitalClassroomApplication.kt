package de.thm.mni.ii.classroom

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication


@SpringBootApplication
@ConfigurationPropertiesScan("de.thm.mni.ii.classroom.properties")
class DigitalClassroomApplication

fun main(args: Array<String>) {
    runApplication<DigitalClassroomApplication>(*args)
}
