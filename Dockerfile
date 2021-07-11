FROM openjdk:14.0.1-slim
LABEL org.opencontainers.image.authors="Dominik Kröll <Dominik.Kroell@mni.thm.de>"
LABEL org.opencontainers.image.title="Digital Classroom"
LABEL org.opencontainers.image.vendor="Technische Hochschule Mittelhessen"

ADD build/libs /classroom
WORKDIR /classroom
RUN mv digital-classroom-*.jar digital-classroom.jar
EXPOSE 8085
ENTRYPOINT ["java","-jar","digital-classroom.jar"]
