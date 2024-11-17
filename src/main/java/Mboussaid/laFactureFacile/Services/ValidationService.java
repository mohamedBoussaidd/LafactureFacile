package Mboussaid.laFactureFacile.Services;

import java.time.Instant;
import java.util.Random;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import Mboussaid.laFactureFacile.Models.User;
import Mboussaid.laFactureFacile.Models.Validation;
import Mboussaid.laFactureFacile.Repository.ValidationRepository;
import Mboussaid.laFactureFacile.DTO.MessageEntity;


import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Transactional
@Service
@Slf4j
public class ValidationService {

    private static final Instant NOW = Instant.now();
    private final ValidationRepository validationRepository;
    private final NotificationService notificationService;

    public ValidationService(ValidationRepository validationRepository, NotificationService notificationService) {
        this.validationRepository = validationRepository;
        this.notificationService = notificationService;
    }

    public ResponseEntity<?> addValidation(User user) {
        if (this.validationRepository.findByUser(user).isPresent()) {
            Validation validation = this.validationRepository.findByUser(user).get();
            validation.setCreation(NOW);
            validation.setExpired(NOW.plus(10, java.time.temporal.ChronoUnit.MINUTES));
            Random random = new Random();
            int randomInteger = random.nextInt(999999);
            String code = String.format("%06d", randomInteger);
            validation.setCode(code);
            String uid = UUID.randomUUID().toString();
            validation.setUid(uid);
            this.validationRepository.save(validation);
            notificationService.sendNotificationCodeNewPassword(validation);
            return ResponseEntity.ok(new MessageEntity(HttpStatus.CREATED.value(),"Un email vous a été envoyé. Veuillez vérifier votre boite de réception. Attention ce code est valable 10 minutes !"));
        }
        Validation validation = new Validation();
        validation.setUser(user);
        Instant creation = NOW;
        validation.setCreation(creation);
        Instant expiration = creation.plus(10, java.time.temporal.ChronoUnit.MINUTES);
        validation.setExpired(expiration);

        Random random = new Random();
        int randomInteger = random.nextInt(999999);
        String code = String.format("%06d", randomInteger);

        validation.setCode(code);
        validationRepository.save(validation);
        notificationService.sendNotificationActivation(validation);
        return ResponseEntity.ok(new MessageEntity(HttpStatus.CREATED.value(),"Un email vous a été envoyé. Veuillez vérifier votre boite de réception."));
    }

    public Validation getValidationByCode(String code) {
        return validationRepository.findByCode(code).orElseThrow(() -> new RuntimeException("Error: Validation not found."));
    }

    public void deleteValidation(Validation validation) {
        validationRepository.delete(validation);
    }
    @Scheduled(cron = "0 0/10 * * * *")
    public void deleteExpired() {
        log.info( "suppression des validations expirées {}", NOW);
        this.validationRepository.deleteAllByExpiredBefore(NOW);
    }
}
