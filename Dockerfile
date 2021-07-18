FROM node:14.17.3-alpine AS build-node
COPY ./web-gui /web-gui
WORKDIR web-gui
RUN npm ci
RUN npm run build

FROM gradle:6.8.3-jdk11 AS build-gradle
COPY . /build/
COPY --from=build-node /web-gui/dist/web-gui/* /build/src/main/resources/static/
WORKDIR /build/
RUN gradle bootJar

FROM openjdk:11-jdk-slim
COPY --from=build-gradle /build/build/libs/*.jar /app/digital-classroom.jar
EXPOSE 8085
ENTRYPOINT ["java","-jar","/app/digital-classroom.jar"]