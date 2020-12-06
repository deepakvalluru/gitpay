#
# Build stage
#
FROM maven:3.6.0-jdk-11-slim AS build
COPY src /home/app/src
COPY pom.xml /home/app
RUN mvn -f /home/app/pom.xml clean package -DskipTests

#
# Package stage
#
FROM openjdk:8-jre-alpine
COPY --from=build /home/app/target/gitpay-0.0.1-SNAPSHOT.jar /usr/local/lib/app.jar
#EXPOSE 8080
ENTRYPOINT ["java","-jar","/usr/local/lib/app.jar"]
