package Mboussaid.laFactureFacile.Services;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import Mboussaid.laFactureFacile.Models.Interface.FileStorage;

@Service
public class FileStorageService implements FileStorage {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    public String storeFile(File file) {
        try {
            // Créer le répertoire si nécessaire
            File directory = new File(uploadDir);
            System.out.println( "directory : " + directory);
            System.out.println( "uploadDir : " + uploadDir);
            if (!directory.exists()) {
                directory.mkdirs(); // Crée tous les répertoires nécessaires
            }
    
            // Construire le chemin du fichier de destination
            Path filePath = Paths.get(uploadDir, file.getName());
    
            // Vérifier si le fichier de destination existe déjà
            if (Files.exists(filePath)) {
                System.out.println("Le fichier existe déjà : " + filePath);
                // Vous pouvez choisir de renommer le fichier ou de le supprimer, par exemple
                // return file.getName(); // Pour retourner le nom d'origine si le fichier existe déjà
            }
    
            // Copier le fichier
            Files.copy(file.toPath(), filePath);
            return file.getName(); // Retourner le nom du fichier enregistré
    
        } catch (IOException e) {
            throw new RuntimeException("Could not store the file. Error: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("An unexpected error occurred. Error: " + e.getMessage(), e);
        }
    }

    @Override
    public byte[] readFile(String fileName) {
        try {
            Path filePath = Paths.get(uploadDir, fileName);
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de la lecture du fichier: " + e.getMessage());
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
