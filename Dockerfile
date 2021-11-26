FROM gradle:6.8.3-jdk11 AS cache-gradle
RUN mkdir /cache-gradle
RUN mkdir /code
ENV GRADLE_USER_HOME /cache-gradle
COPY build.gradle.kts /code
WORKDIR /code
RUN gradle clean build -i -x bootJar

FROM gradle:6.8.3-jdk11 AS build-node
COPY ./ /build
WORKDIR /build/web-gui
RUN rm -rf node_modules
RUN gradle installDist

FROM gradle:6.8.3-jdk11 AS build-gradle
COPY . /build/
COPY --from=cache-gradle /cache-gradle /home/gradle/.gradle
COPY --from=build-node /build/web-gui/dist/web-gui/ /build/src/main/resources/static/
WORKDIR /build/
RUN gradle dist -i -x npm_run_clean -x npm_run_build

FROM openjdk:11-jdk-slim
COPY --from=build-gradle /build/build/libs/digital-classroom-0.0.1-SNAPSHOT.jar /app/digital-classroom.jar
EXPOSE 8085
ENTRYPOINT ["java","-jar","/app/digital-classroom.jar"]
