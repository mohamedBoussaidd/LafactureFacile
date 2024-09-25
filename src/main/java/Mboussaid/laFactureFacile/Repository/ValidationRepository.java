package Mboussaid.laFactureFacile.Repository;

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import Mboussaid.laFactureFacile.Models.User;
import Mboussaid.laFactureFacile.Models.Validation;

public interface ValidationRepository extends JpaRepository<Validation, Long> {
    Optional<Validation> findByCode(String code);
    Optional<Validation> findByUser(User user);

    void deleteAllByExpiredBefore(Instant now);
}
