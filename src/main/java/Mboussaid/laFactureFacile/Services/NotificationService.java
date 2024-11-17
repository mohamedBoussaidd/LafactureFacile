package Mboussaid.laFactureFacile.Services;

import org.springframework.stereotype.Service;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import Mboussaid.laFactureFacile.Models.Validation;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class NotificationService {

    private final JavaMailSender javaMailSender;

    public NotificationService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    public void sendNotificationActivation(Validation validation) {
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setFrom("mohamedboussaid69700@hotmail.fr");
        mail.setTo(validation.getUser().getEmail());
        mail.setSubject("Votre code d'activation");
        String lien = "http://localhost:4200/activation/" + validation.getUid();
        String msg = String.format(
                "Bonjour %s,\n\nMerci de vous être inscrit sur MaFacture!\n\n Code de validation : %s\n\n Voici les étapes à suivre pour valider votre compte :\n\n1. Connecter vous à votre compte sur " + lien +"\n\n 2.Entrez le code de validation ci-dessus dans l'interface de verification.\n\n3. Cliquer sur \'Valider\'. \n\nSi vous n'avez pas demandé cette inscription, veuillez ignorer cet e-mail.\n\nNous vous remercions de faire partie de notre communauté et restons à votre disposition pour toute question ou assistance.\n\nCordialement,\nL'équipe MaFacture",
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
