package de.thm.mni.ii.classroom

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import reactor.core.publisher.Hooks

@SpringBootApplication
@ConfigurationPropertiesScan("de.thm.mni.ii.classroom.properties")
class DigitalClassroomApplication

fun main(args: Array<String>) {
    Hooks.onErrorDropped {}
    runApplication<DigitalClassroomApplication>(*args)
}
