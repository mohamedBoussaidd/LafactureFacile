package Mboussaid.laFactureFacile.Services;

import org.springframework.stereotype.Service;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import Mboussaid.laFactureFacile.Models.Validation;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class NotificationService {

    private JavaMailSender javaMailSender;

    public NotificationService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }
    

    public NotificationService() {
    }


    public void sendNotificationActivation(Validation validation) {
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setFrom("mohamedboussaid69700@hotmail.fr");
        mail.setTo(validation.getUser().getEmail());
        mail.setSubject("Votre code d'activation");
        String lien = "http://localhost:4200/activation";
        String msg = String.format(
                "Bonjour %s,\n\nVotre code d'activation est : %s\n\n pour activer votre compte cliquez sur le lien " + lien +"\n\n Si le lien ne fonctionne pas il suffit de le copier et le coller dans votre navigateur \n\nCordialement,\nL'équipe Calendrier",
                validation.getUser().getName(), validation.getCode());

        mail.setText(msg);

        javaMailSender.send(mail);

        log.info("Email sent to {}", validation.getUser().getEmail());
    }

    public void sendNotificationCodeNewPassword(Validation validation) {
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setFrom("mohamedboussaid69700@hotmail.fr");
        mail.setTo(validation.getUser().getEmail());
        mail.setSubject("Votre code de Récupération de mot de passe");
        String msg = String.format(
                "Bonjour %s,\n\nVotre code de récuperation de mot de passe est : %s\n\nCordialement,\nL'équipe Calendrier",
                validation.getUser().getName(), validation.getCode());

        mail.setText(msg);

        javaMailSender.send(mail);

        log.info("Email sent to {}", validation.getUser().getEmail());
    }
}
