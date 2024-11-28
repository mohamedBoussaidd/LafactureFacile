package Mboussaid.laFactureFacile.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import Mboussaid.laFactureFacile.Models.FileInfo;

public interface FileInfoRepository extends JpaRepository<FileInfo, Integer> {
    
}
