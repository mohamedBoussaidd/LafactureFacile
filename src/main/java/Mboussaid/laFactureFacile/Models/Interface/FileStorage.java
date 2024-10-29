package Mboussaid.laFactureFacile.Models.Interface;

import java.io.File;

public interface FileStorage {
    String storeFile (File file);
    byte[] readFile(String fileName);
    void deleteFile(String fileName);
}
