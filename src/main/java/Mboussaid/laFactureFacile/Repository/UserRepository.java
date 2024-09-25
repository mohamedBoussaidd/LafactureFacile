package Mboussaid.laFactureFacile.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import Mboussaid.laFactureFacile.Models.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByName(String username);

    Optional<User> findByEmail(String email);
    
}
