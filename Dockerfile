# Etapa 1: build del JAR con Maven
FROM maven:3.9-eclipse-temurin-23 AS build

WORKDIR /app

# Copiamos descriptor primero (aprovecha cache de dependencias)
COPY pom.xml .
RUN mvn -q -e -DskipTests dependency:go-offline

# Ahora copiamos el código fuente
COPY src ./src

# Build del proyecto (sin tests para acelerar)
RUN mvn -q -DskipTests package

# Etapa 2: imagen liviana solo con el JAR
FROM eclipse-temurin:23-jre

WORKDIR /app

# Copiamos el JAR generado
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
