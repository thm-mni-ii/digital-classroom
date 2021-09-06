import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.5.4"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    idea
    kotlin("jvm") version "1.4.32"
    kotlin("plugin.spring") version "1.4.32"
    kotlin("kapt") version "1.4.32"
}

group = "de.thm.mni.ii"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.google.guava:guava:30.1.1-jre")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.security:spring-security-rsocket")
    implementation("org.springframework.security:spring-security-messaging")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("org.springframework.boot:spring-boot-starter-rsocket")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    implementation("org.hibernate:hibernate-validator:6.0.16.Final")
    implementation("commons-codec:commons-codec:1.15")
    implementation("org.apache.commons:commons-lang3:3.12.0")
    kapt("org.springframework.boot:spring-boot-configuration-processor")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
    // Java 11 removed these Java EE modules
    implementation("javax.xml.bind:jaxb-api:2.3.1")
    implementation("com.sun.xml.bind:jaxb-core:2.3.0.1")
    implementation("com.sun.xml.bind:jaxb-impl:2.3.2")
    implementation("org.springdoc:springdoc-openapi-webflux-ui:1.5.10")
    implementation("org.springdoc:springdoc-openapi-kotlin:1.5.10")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "11"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.register("dist") {
    dependsOn("build", "web-gui:copyWebToBackend")
}

sourceSets {
    main {
        withConvention(org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet::class) {
            this.kotlin.srcDirs("src/main/kotlin")
        }
        java {
            setSrcDirs(emptyList<String>())
        }
    }
    test {
        withConvention(org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet::class) {
            this.kotlin.srcDirs("src/test/kotlin")
        }
        java {
            setSrcDirs(emptyList<String>())
        }
    }
}

idea {
    module {
        val kaptMain = file("${project.buildDir}/generated/source/kapt/main")
        sourceDirs.add(kaptMain)
        generatedSourceDirs.add(kaptMain)
        outputDir = file("${project.buildDir}/classes/main")
        testOutputDir = file("${project.buildDir}/classes/test")
    }
}
