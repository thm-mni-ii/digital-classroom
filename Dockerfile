FROM gradle:6.8.3-jdk11 AS cache-gradle
RUN mkdir /cache-gradle
RUN mkdir /code
ENV GRADLE_USER_HOME /cache-gradle
COPY build.gradle.kts /code
WORKDIR /code
RUN gradle clean build -i -x bootJar

FROM node:14.17.3-alpine AS build-node
COPY ./web-gui /web-gui
WORKDIR web-gui
RUN npm ci
RUN npm run build

FROM gradle:6.8.3-jdk11 AS build-gradle
COPY . /build/
COPY --from=cache-gradle /cache-gradle /home/gradle/.gradle
COPY --from=build-node /web-gui/dist/web-gui/ /build/src/main/resources/static/
WORKDIR /build/
RUN gradle dist -i -x npm_run_clean -x npm_run_build

FROM openjdk:11-jdk-slim
COPY --from=build-gradle /build/build/libs/*.jar /app/digital-classroom.jar
EXPOSE 8085
ENTRYPOINT ["java","-jar","/app/digital-classroom.jar"]
