# Étape de build avec Maven
FROM maven:3.8.6 AS build

# Définir le répertoire de travail
WORKDIR /app

# Copier le fichier pom.xml et le dossier des sources
COPY pom.xml ./
COPY src ./src

# Construire le projet
RUN mvn clean package -DskipTests

# Étape de production
FROM eclipse-temurin:17-jre

# Définir le répertoire de travail
WORKDIR /app

# Copier le JAR généré depuis l'étape de build
COPY --from=build /app/target/laFactureFacile-0.0.1-SNAPSHOT.jar app.jar

# Exposer le port de l'application
EXPOSE 8080

# Commande pour exécuter l'application
ENTRYPOINT ["java", "-jar", "app.jar"]