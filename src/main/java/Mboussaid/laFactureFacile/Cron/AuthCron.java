package Mboussaid.laFactureFacile.Cron;

import java.time.Instant;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import Mboussaid.laFactureFacile.Models.GetDate;
import Mboussaid.laFactureFacile.Repository.JwtRepository;
import Mboussaid.laFactureFacile.Repository.ValidationRepository;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class AuthCron {

    private final ValidationRepository validationRepository;
    private final JwtRepository jwtRepository;

    public AuthCron(ValidationRepository validationRepository, JwtRepository jwtRepository) {
        this.validationRepository = validationRepository;
        this.jwtRepository = jwtRepository;
    }

    /* 
     * Supprime les validations expirées
     * Tous les jours à minuit et midi
     */
    @Scheduled(cron = "0 0 0,12 * * *")
    public void deleteExpired() {
        log.info("suppression des validations expirées {}", GetDate.getNow());
        int deleteRow = this.validationRepository.deleteAllByExpiredBefore(GetDate.getNow());
        log.info("Nombre de validations supprimées : " + deleteRow);
    }
    /* 
     * Supprime les tokens inutiles
     * Tous les jours
     */
        @Scheduled(cron = "@daily")
    public void removeUselessToken() {
        log.info("removeUselessToken {}", Instant.now());
        this.jwtRepository.deleteAllByIsExpiredAndIsBlackListed(true, true);
    }
}
