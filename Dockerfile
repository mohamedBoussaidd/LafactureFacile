# Utiliser l'image Maven avec JDK pour le développement
FROM maven:3.9.9-eclipse-temurin-17-alpine AS build

# Définir le répertoire de travail
WORKDIR /app

# Copier uniquement le pom.xml pour installer les dépendances
COPY pom.xml ./
RUN mvn dependency:go-offline -B && mvn clean

# Copier les sources (seront remplacées par le volume dans Docker Compose)
COPY src ./src

# Compiler et packager l'application
RUN ./mvnw clean package spring-boot:build-image -Dmaven.test.skip=true

# Étape finale (image pour l'exécution)
FROM eclipse-temurin:17-alpine

# Créer un utilisateur et un groupe
RUN groupadd -r lffusergroup && useradd -r -g lffusergroup -m lffappuser

# Créer le répertoire où les PDF seront enregistrés
RUN mkdir -p /app/pdfs

# Donner les permissions nécessaires sur le répertoire
RUN chown -R lffappuser:lffusergroup /app/pdfs
RUN chmod -R 750 /app/pdfs

# Passer à l'utilisateur myuser
USER lffappuser

# Copier l'artefact généré par Maven
COPY --from=build /app/target/lafacturefacile-0.0.1-SNAPSHOT.jar /app/lafacturefacile-0.0.1-SNAPSHOT.jar

# Exposer le port sur lequel l'application va tourner
EXPOSE 8080

# Commande pour lancer Spring Boot
ENTRYPOINT ["java", "-jar", "/app/lafacturefacile-0.0.1-SNAPSHOT.jar"]