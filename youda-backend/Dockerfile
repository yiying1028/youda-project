ARG MAVEN_IMAGE=maven:3.9.9-eclipse-temurin-11
ARG JRE_IMAGE=eclipse-temurin:11-jre-jammy

FROM ${MAVEN_IMAGE} AS build

WORKDIR /app
ENV MAVEN_OPTS="-Djava.net.preferIPv4Stack=true -Djava.net.preferIPv6Addresses=false"

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests -B

FROM ${JRE_IMAGE}

WORKDIR /app

RUN mkdir -p /app/uploads

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
