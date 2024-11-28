package Mboussaid.laFactureFacile.Models.Interface;

import java.io.File;

import org.springframework.core.io.Resource;

import Mboussaid.laFactureFacile.DTO.CustomResponseEntity;

public interface FileStorage {
    CustomResponseEntity<?> storeFile (File file);
    Resource readFile(String fileName);
    void deleteFile(String fileName);
}
