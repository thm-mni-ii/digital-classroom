package de.thm.mni.ii.classroom

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling
import reactor.core.publisher.Hooks

@SpringBootApplication
@EnableScheduling
@ConfigurationPropertiesScan("de.thm.mni.ii.classroom.properties")
class DigitalClassroomApplication

fun main(args: Array<String>) {
    Hooks.onErrorDropped {}
    runApplication<DigitalClassroomApplication>(*args)
}
