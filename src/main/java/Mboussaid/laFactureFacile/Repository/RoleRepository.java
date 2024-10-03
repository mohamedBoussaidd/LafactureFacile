package Mboussaid.laFactureFacile.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

import Mboussaid.laFactureFacile.Models.ERole;
import Mboussaid.laFactureFacile.Models.Role;


public interface RoleRepository  extends JpaRepository<Role, Integer>{
    Optional<Role> findByName(ERole name);
}
