package Mboussaid.laFactureFacile.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import Mboussaid.laFactureFacile.Models.Role;
import Mboussaid.laFactureFacile.Models.ENUM.ERole;

@Repository
public interface RoleRepository  extends JpaRepository<Role, Integer>{
    Optional<Role> findByName(ERole name);
}
