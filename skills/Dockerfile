FROM adoptopenjdk/openjdk11-openj9:alpine-slim
COPY build/libs/skills-*-all.jar skills.jar
EXPOSE 8080
CMD java -Dcom.sun.management.jmxremote -noverify ${JAVA_OPTS} -jar skills.jar