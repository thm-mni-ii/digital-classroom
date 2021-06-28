import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.4.5"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    java
    idea
    kotlin("jvm") version "1.4.32"
    kotlin("plugin.spring") version "1.4.32"
    kotlin("kapt") version "1.4.32"
    scala
}

group = "de.thm.mni.ii"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_14

repositories {
    mavenCentral()
}

dependencies {
    // implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
    implementation("com.google.guava:guava:30.1.1-jre")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("io.jsonwebtoken:jjwt-impl:0.11.2")
    implementation("io.jsonwebtoken:jjwt-jackson:0.11.2")
    //implementation("org.springframework.boot:spring-boot-starter-websocket")
    //implementation("org.springframework:spring-websocket")
    implementation("org.springframework:spring-messaging")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.fasterxml.jackson.module:jackson-module-scala_2.13:2.11.3")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("org.scala-lang:scala-library:2.13.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    implementation("javax.xml.bind:jaxb-api:2.3.1")
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
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "14"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.compileScala {
    classpath = sourceSets.main.get().compileClasspath
}

tasks.compileKotlin {
    classpath += files(sourceSets.main.get().withConvention(ScalaSourceSet::class) {scala}.classesDirectory)
}

sourceSets {
    main {
        withConvention(ScalaSourceSet::class) {
            scala {
                setSrcDirs(listOf("src/main/scala"))
            }
        }
        withConvention(org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet::class) {
            this.kotlin.srcDirs("src/main/kotlin")
        }
        java {
            setSrcDirs(emptyList<String>())
        }
    }
    test {
        withConvention(ScalaSourceSet::class) {
            scala {
                setSrcDirs(listOf("src/test/scala"))
            }
        }
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
