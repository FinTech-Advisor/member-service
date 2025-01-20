FROM propheter/ubuntu
ARG JAR_FILE=build/libs/member-service-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar
ENV SPRING_PROFILES_ACTIVE=default,jwt
ENV DB_HOST=localhost:1521
ENV DDL_AUTO=update
ENV JWT_VALID_TIME=900

ENTRYPOINT ["java", "-jar", "-Dconfig.server=${CONFIG_SERVER}", "-Ddb.host=${DB_HOST}", "-Ddb.username=${DB_USERNAME}", "-Ddb.password=${DB_PASSWORD}", "-Dddl.auto=${DDL_AUTO}", "-DjwtSecret=${JWT_SECRET}", "-DjwtValidTime=${JWT_VALID_TIME}", "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE}", "app.jar"]

EXPOSE 3332
