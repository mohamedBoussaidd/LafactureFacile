package Mboussaid.laFactureFacile.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import Mboussaid.laFactureFacile.Models.FileInfo;

@Repository
public interface FileInfoRepository extends JpaRepository<FileInfo, Integer> {
    
}
