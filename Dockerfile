# Utiliser l'image Maven avec JDK pour le développement
FROM maven:3.9.9-eclipse-temurin-17-alpine AS build

# Définir le répertoire de travail
WORKDIR /app

# Copier uniquement le pom.xml pour installer les dépendances
COPY pom.xml ./
RUN mvn dependency:go-offline -B && mvn clean

# Copier les sources (seront remplacées par le volume dans Docker Compose)
COPY src ./src

# Utiliser addgroup et adduser au lieu de groupadd et useradd
RUN addgroup -S lffusergroup && adduser -S lffappuser -G lffusergroup

RUN chown -R lffappuser:lffusergroup /app \
    chmod -R 755 /app

# Installer les utilitaires nécessaires pour ajouter un utilisateur et un groupe (shadow)
RUN apk update && apk add --no-cache bash

# Assurez-vous que l'utilisateur peut utiliser le répertoire Maven (.m2)
RUN mkdir -p /home/lffappuser/.m2
RUN chown -R lffappuser:lffusergroup /home/lffappuser/.m2

# Compiler et packager l'application
RUN mvn clean package spring-boot:build-image -Dmaven.test.skip=true

# Étape finale (image pour l'exécution)
FROM eclipse-temurin:17-alpine

# Utiliser addgroup et adduser au lieu de groupadd et useradd
RUN addgroup -S lffusergroup && adduser -S lffappuser -G lffusergroup

# Créer le répertoire où les PDF seront enregistrés et donner les permissions à l'utilisateur lffappuser
RUN mkdir -p /app/pdfs && \
    chown -R lffappuser:lffusergroup /app/pdfs && \
    chmod -R 750 /app/pdfs

# Copier l'artefact généré par Maven
COPY --from=build /app/target/lafacturefacile-0.0.1-SNAPSHOT.jar /app/lafacturefacile-0.0.1-SNAPSHOT.jar

# Passer à l'utilisateur myuser
USER lffappuser


# Assurer les permissions sur l'artefact JAR généré
RUN chown lffappuser:lffusergroup /app/lafacturefacile-0.0.1-SNAPSHOT.jar
RUN chmod 755 /app/lafacturefacile-0.0.1-SNAPSHOT.jar

# Exposer le port sur lequel l'application va tourner
EXPOSE 8080

# Commande pour lancer Spring Boot
ENTRYPOINT ["java", "-jar", "/app/lafacturefacile-0.0.1-SNAPSHOT.jar"]
