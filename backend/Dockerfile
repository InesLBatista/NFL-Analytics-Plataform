FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

#copy dependency manifests first to leverage Docker layer cache
#only re-downloads dependencies if pom.xml changes
COPY pom.xml .
RUN mvn dependency:go-offline -q

COPY src ./src
RUN mvn package -DskipTests -q


FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
