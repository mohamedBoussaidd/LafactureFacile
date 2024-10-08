package Mboussaid.laFactureFacile.Security;

import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import Mboussaid.laFactureFacile.Models.Jwt;
import Mboussaid.laFactureFacile.Models.User;
import Mboussaid.laFactureFacile.Repository.JwtRepository;
import Mboussaid.laFactureFacile.Services.UserService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class JwtService {

    private static final String BEARER = "bearer";
    private final String ENCRIPTION_key = "8bd3ef239b58e5d372958eda64b775a1458da6335f6c35855e4c7ace48141f7e";
    private final UserService userService;
    private final JwtRepository jwtRepository;

    public JwtService(UserService userService, JwtRepository jwtRepository) {
        this.userService = userService;
        this.jwtRepository = jwtRepository;
    }

    public Map<String, String> generate(String username) {
        User user = (User) this.userService.loadUserByUsername(username);
        this.disableToken(user);
        final Map<String, String> jwtMap = this.generateJwt(user);
        final Jwt jwt = Jwt.builder()
                .user(user)
                .value(jwtMap.get(BEARER))
                .isBlackListed(false)
                .isExpired(false)
                .build();
        this.jwtRepository.save(jwt);
        return jwtMap;
    }

    public String extractUsername(String token) {
        return this.getClaim(token, Claims::getSubject);
    }

    public Jwt tokenByValue(String value) {
        return this.jwtRepository.findByValueAndIsExpiredAndIsBlackListed(value, false, false)
                .orElseThrow(() -> new RuntimeException("token invalid ou inconnue"));
    }

    public boolean isTokenExpired(String token) {
        Date expirationDate = this.getClaim(token, Claims::getExpiration);
        return expirationDate.before(new Date());
    }

    private void disableToken(User user) {
        final List<Jwt> jwtList = this.jwtRepository.findUserToken(user.getEmail()).peek(jwt -> {
            jwt.setExpired(true);
            jwt.setBlackListed(true);
        }).collect(Collectors.toList());
        this.jwtRepository.saveAll(jwtList);
    }

    private Map<String, String> generateJwt(User user) {

        final long currentTime = System.currentTimeMillis();
        final long expirationTime = currentTime + 1440 * 60 * 1000;

        final Map<String, Object> claims = Map.of(
                "name", user.getName(),
                "id", user.getId(),
                Claims.EXPIRATION, new Date(expirationTime),
                Claims.SUBJECT, user.getEmail(), 
                "role", user.getRoles().stream().map(role -> role.getName()).collect(Collectors.toList())
                );

        final String bearer = Jwts.builder()
                .claims(claims)
                .issuedAt(new Date(currentTime))
                .expiration(new Date(expirationTime))
                .subject(user.getEmail())
                .signWith(getKey())
                .compact();
        return Map.of(BEARER, bearer, "message", "Connexion réussie");
    }

    private Key getKey() {
        final byte[] decoder = Decoders.BASE64.decode(ENCRIPTION_key);
        return Keys.hmacShaKeyFor(decoder);
    }

    private <T> T getClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = this.getAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims getAllClaims(String token) {
        return Jwts.parser()
                .verifyWith((SecretKey) getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public void disconnection() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Jwt jwt = this.jwtRepository.findUserValidToken(user.getEmail(), false, false)
                .orElseThrow(() -> new RuntimeException("Token invalide"));

        jwt.setExpired(true);
        jwt.setBlackListed(true);

        this.jwtRepository.save(jwt);
    }

    @Scheduled(cron = "@daily")
    public void removeUselessToken() {
        log.info("removeUselessToken {}", Instant.now());
        this.jwtRepository.deleteAllByIsExpiredAndIsBlackListed(true, true);
    }
}