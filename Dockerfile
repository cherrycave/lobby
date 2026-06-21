#
# Set a variable that can be used in all stages.
#
ARG BUILD_HOME=/cherrycave-lobby

#
# Gradle image for the build stage.
#
FROM gradle:jdk25 as build-image

#
# Set the working directory.
#
ARG BUILD_HOME
ENV APP_HOME=$BUILD_HOME
WORKDIR $APP_HOME

#
# Copy the Gradle config, source code, and static analysis config
# into the build container.
#
COPY --chown=gradle:gradle build.gradle settings.gradle $APP_HOME/
COPY --chown=gradle:gradle src $APP_HOME/src
COPY --chown=gradle:gradle worlds $APP_HOME/worlds

#
# Build the application.
#
RUN gradle --no-daemon build

#
# Java image for the application to run in.
#
FROM openjdk:25-alpine

#
# Copy the jar file in and name it app.jar.
#
ARG BUILD_HOME
ENV APP_HOME=$BUILD_HOME
COPY --from=build-image $APP_HOME/build/libs/*.jar minestom-server.jar
COPY --from=build-image $APP_HOME/worlds/ worlds/

#
# The command to run when the container starts.
#
ENTRYPOINT exec java -jar minestom-server.jar