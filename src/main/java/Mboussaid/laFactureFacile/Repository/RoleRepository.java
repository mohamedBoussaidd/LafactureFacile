package Mboussaid.laFactureFacile.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import Mboussaid.laFactureFacile.Models.ERole;
import Mboussaid.laFactureFacile.Models.Role;

@Repository
public interface RoleRepository  extends JpaRepository<Role, Integer>{
    Optional<Role> findByName(ERole name);
}
