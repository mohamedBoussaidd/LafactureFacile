package Mboussaid.laFactureFacile.Repository;

import java.time.ZonedDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import Mboussaid.laFactureFacile.Models.User;
import Mboussaid.laFactureFacile.Models.Validation;

@Repository
public interface ValidationRepository extends JpaRepository<Validation, Integer> {
    Optional<Validation> findByCode(String code);
    Optional<Validation> findByUser(User user);
    Optional<Validation> findByUid(String uid);

    int deleteAllByExpiredBefore(ZonedDateTime now);
}
