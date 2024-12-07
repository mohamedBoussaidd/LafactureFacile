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
    @Value("${tmpfile.upload-dir}")
    private String tmpUploadDir;

    @Override
    public CustomResponseEntity<?> storeFile(File file) {
        if (file == null || file.getName() == null || file.getName().isEmpty()) {
            return CustomResponseEntity.error(HttpStatus.BAD_REQUEST.value(), "Fichier invalide ou non spécifié");
        }
        // Déterminer le répertoire de destination selon le nom du fichier si il est temporaire ou definitif
        String targetDir = file.getName().contains("TMP") ? tmpUploadDir : uploadDir;
 
        Path filePath = Paths.get(targetDir, file.getName());
        try {
            // Créer le répertoire si nécessaire
            Files.createDirectories(Paths.get(targetDir));
            // Vérifier si le fichier de destination existe déjà
            if (Files.exists(filePath)) {
                return CustomResponseEntity.error(HttpStatus.CONFLICT.value(), "Le fichier existe déjà !!");
            }
            // Copier le fichier
            Files.copy(file.toPath(), filePath);
            return CustomResponseEntity.successWithoutDataHidden(HttpStatus.ACCEPTED.value(), "Fichier enregistre !!");
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de l'enregistrement du fichier: " + e.getMessage());
        }
    }

    @Override
    public Resource readFile(String fileName) {
        String targetDir = fileName.contains("TMP") ? tmpUploadDir : uploadDir;
        try {
            Path filePath = Paths.get(targetDir, fileName);
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
        String targetDir = fileName.contains("TMP") ? tmpUploadDir : uploadDir;
        try {
            Path filePath = Paths.get(targetDir, fileName);
            Files.delete(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de la suppression du fichier: " + e.getMessage());
        }
    }

}
