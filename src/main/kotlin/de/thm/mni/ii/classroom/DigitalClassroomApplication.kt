package de.thm.mni.ii.classroom

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan("de.thm.mni.ii.classroom.properties")
class DigitalClassroomApplication

fun main(args: Array<String>) {
    runApplication<DigitalClassroomApplication>(*args)
}
