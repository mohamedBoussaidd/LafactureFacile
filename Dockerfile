# Utiliser l'image Maven avec JDK pour le développement
FROM maven:3.9.9-eclipse-temurin-17-alpine AS build

# Définir le répertoire de travail
WORKDIR /app

# Copier uniquement le pom.xml pour installer les dépendances
COPY pom.xml ./
RUN mvn dependency:go-offline -B && mvn clean

# Copier les sources (seront remplacées par le volume dans Docker Compose)
COPY src ./src

# Installer les utilitaires nécessaires pour ajouter un utilisateur et un groupe
RUN apk update && apk add --no-cache

# Compiler et packager l'application
RUN mvn clean package -Dmaven.test.skip=true

# Étape finale (image pour l'exécution)
FROM eclipse-temurin:17-alpine

# Utiliser addgroup et adduser au lieu de groupadd et useradd
RUN addgroup -S lffusergroup && adduser -S lffappuser -G lffusergroup
RUN addgroup -g 998 docker && addgroup lffappuser docker


# Copier l'artefact généré par Maven
COPY --from=build /app/target/lafacturefacile-0.0.1-SNAPSHOT.jar /app/lafacturefacile-0.0.1-SNAPSHOT.jar

# Créer le répertoire où les PDF definitf et temporaire seront enregistrés et donner les permissions à l'utilisateur lffappuser
RUN mkdir -p /app/pdfs && \
    mkdir -p /app/pdfs/tmp && \
    chown -R lffappuser:lffusergroup /app/pdfs && \
    chmod -R 750 /app/pdfs && \
    chown -R lffappuser:lffusergroup /app/pdfs/tmp && \
    chmod -R 750 /app/pdfs/tmp

    # Assurer les permissions sur l'artefact JAR généré
RUN chown lffappuser:lffusergroup /app/lafacturefacile-0.0.1-SNAPSHOT.jar && \
    chmod 755 /app/lafacturefacile-0.0.1-SNAPSHOT.jar

# Passer à l'utilisateur myuser
USER lffappuser

# Exposer le port sur lequel l'application va tourner et je met une variable d'environnement pour le port
ENV APP_PORT=8081
EXPOSE $APP_PORT

# Commande pour lancer Spring Boot
ENTRYPOINT ["java", "-jar", "/app/lafacturefacile-0.0.1-SNAPSHOT.jar", "--server.port=${APP_PORT}"]
