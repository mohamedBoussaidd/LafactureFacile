package Mboussaid.laFactureFacile.Repository;

import java.util.Optional;
import java.util.stream.Stream;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import Mboussaid.laFactureFacile.Models.Jwt;

public interface JwtRepository extends JpaRepository<Jwt, Long> {
    Optional<Jwt> findByValueAndIsExpiredAndIsBlackListed(String value, boolean isExpired, boolean isBlackListed);

    @Query("FROM Jwt j WHERE j.user.email = :email AND j.isExpired = :isExpired AND j.isBlackListed = :isBlackListed")
    Optional<Jwt> findUserValidToken(String email, boolean isExpired, boolean isBlackListed);

    @Query("FROM Jwt j WHERE j.user.email = :email")
    Stream<Jwt> findUserToken(String email);

    void deleteAllByIsExpiredAndIsBlackListed(boolean isExpired, boolean isBlackListed);
}