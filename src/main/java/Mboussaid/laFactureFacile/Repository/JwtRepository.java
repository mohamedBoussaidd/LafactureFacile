package Mboussaid.laFactureFacile.Repository;

import java.util.Optional;
import java.util.stream.Stream;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import Mboussaid.laFactureFacile.Models.Jwt;
@Repository
public interface JwtRepository extends JpaRepository<Jwt, Integer> {
    Optional<Jwt> findByValueAndIsExpiredAndIsBlackListed(String value, boolean isExpired, boolean isBlackListed);

    @Query("FROM Jwt j WHERE j.user.email = :email AND j.isExpired = :isExpired AND j.isBlackListed = :isBlackListed")
    Optional<Jwt> findUserValidToken(String email, boolean isExpired, boolean isBlackListed);

    @Query("FROM Jwt j WHERE j.user.email = :email")
    Stream<Jwt> findUserToken(String email);

    Optional<Jwt> findByValue(String value);

    void deleteAllByIsExpiredAndIsBlackListed(boolean isExpired, boolean isBlackListed);
}