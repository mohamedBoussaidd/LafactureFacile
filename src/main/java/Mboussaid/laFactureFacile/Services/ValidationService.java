package Mboussaid.laFactureFacile.Services;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import Mboussaid.laFactureFacile.Models.GetDate;
import Mboussaid.laFactureFacile.Models.User;
import Mboussaid.laFactureFacile.Models.Validation;
import Mboussaid.laFactureFacile.Repository.ValidationRepository;
import Mboussaid.laFactureFacile.DTO.CustomResponseEntity;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Transactional
@Service
@Slf4j
public class ValidationService {
    private final ValidationRepository validationRepository;
    private final NotificationService notificationService;

    public ValidationService(ValidationRepository validationRepository, NotificationService notificationService) {
        this.validationRepository = validationRepository;
        this.notificationService = notificationService;
    }

    public CustomResponseEntity<?> addValidation(User user) {
        Optional<Validation> validationOptional = this.validationRepository.findByUser(user);
        if (validationOptional.isPresent()) {
            Validation validation = validationOptional.get();
            validation.setCreation(GetDate.getNow());
            validation.setExpired(GetDate.getNow().plus(10, java.time.temporal.ChronoUnit.MINUTES));
            Random random = new Random();
            int randomInteger = random.nextInt(999999);
            String code = String.format("%06d", randomInteger);
            validation.setCode(code);
            String uid = UUID.randomUUID().toString();
            validation.setUid(uid);
            this.validationRepository.save(validation);
            notificationService.sendNotificationActivation(validation);
            return CustomResponseEntity.success(HttpStatus.CREATED.value(),
                    "Un email vous a été envoyé. Veuillez vérifier votre boite de réception. Attention ce code est valable 10 minutes !");
        }
        Validation validation = new Validation();
        validation.setUser(user);
        ZonedDateTime creation = GetDate.getNow();
        validation.setCreation(creation);
        ZonedDateTime expiration = creation.plus(10, java.time.temporal.ChronoUnit.MINUTES);
        validation.setExpired(expiration);

        Random random = new Random();
        int randomInteger = random.nextInt(999999);
        String code = String.format("%06d", randomInteger);

        validation.setCode(code);
        String uid = UUID.randomUUID().toString();
        validation.setUid(uid);
        validationRepository.save(validation);
        notificationService.sendNotificationActivation(validation);
        return CustomResponseEntity.success(HttpStatus.CREATED.value(),
                "Un email vous a été envoyé. Veuillez vérifier votre boite de réception.  Attention ce code est valable 10 minutes !");
    }

    public Validation getValidationByCode(String code) {
        return validationRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Veuillez verifier votre code de validation !!"));
    }

    public void deleteValidation(Validation validation) {
        validationRepository.delete(validation);
    }

    @Scheduled(cron = "0 0/10 * * * *")
    public void deleteExpired() {
        log.info("suppression des validations expirées {}", GetDate.getNow());
        this.validationRepository.deleteAllByExpiredBefore(GetDate.getNow());
    }
}
