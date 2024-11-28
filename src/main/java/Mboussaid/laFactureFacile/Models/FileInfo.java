package Mboussaid.laFactureFacile.Models;

import java.time.ZonedDateTime;
import java.util.Arrays;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "file_info")
public class FileInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "file_name")
    private String name;
    @Column(name = "file_type")
    private String type;
    @Column(name = "creation_date")
    private ZonedDateTime creationDate;
    @Column(name = "expiration_date")
    private ZonedDateTime expirationDate;

    private String[] getAllExtensions(String name) {
        // Vérifier si le nom contient un "."
        if (!name.contains(".") || name.startsWith(".") || name.endsWith(".")) {
            return new String[0]; // Pas d'extensions valides
        }
    
        // Extraire toutes les extensions en divisant par les "."
        String[] parts = name.split("\\.");
        if (parts.length <= 1) {
            return new String[0]; // Pas d'extensions
        }
    
        // Retourner toutes les parties sauf le nom de fichier avant le premier "."
        return Arrays.copyOfRange(parts, 1, parts.length);
    }
    
    public String getExtension(String name) {
        String[] extensions = getAllExtensions(name);
        return String.join(".", extensions); // Concaténer avec un "."
    }
}
