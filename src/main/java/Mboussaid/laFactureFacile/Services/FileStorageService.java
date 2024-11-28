package Mboussaid.laFactureFacile.Services;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import Mboussaid.laFactureFacile.DTO.CustomResponseEntity;
import Mboussaid.laFactureFacile.Models.Interface.FileStorage;

@Service
public class FileStorageService implements FileStorage {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    public CustomResponseEntity<?> storeFile(File file) {
        try {
            // Créer le répertoire si nécessaire
            File directory = new File(uploadDir);
            if (!directory.exists()) {
                directory.mkdirs(); // Crée tous les répertoires nécessaires
            }

            // Construire le chemin du fichier de destination
            Path filePath = Paths.get(uploadDir, file.getName());

            // Vérifier si le fichier de destination existe déjà
            if (Files.exists(filePath)) {
                return CustomResponseEntity.error(HttpStatus.CONFLICT.value(), "Le fichier existe déjà !!");
            }

            // Copier le fichier
            Files.copy(file.toPath(), filePath);
            return CustomResponseEntity.success(HttpStatus.ACCEPTED.value(), "Fichier enregistre !!");
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de l'enregistrement du fichier: " + e.getMessage());
        }
    }

    @Override
    public Resource readFile(String fileName) {
        try {
            Path filePath = Paths.get(uploadDir, fileName);
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Could not read the file");
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error: " + e.getMessage());
        }
    }

    @Override
    public void deleteFile(String fileName) {
        try {
            Path filePath = Paths.get(uploadDir, fileName);
            Files.delete(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de la suppression du fichier: " + e.getMessage());
        }
    }

}
