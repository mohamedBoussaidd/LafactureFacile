package Mboussaid.laFactureFacile.Services;

import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

import org.flywaydb.core.internal.util.StringUtils;
import org.springframework.core.io.Resource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import Mboussaid.laFactureFacile.Models.InvoiceInfo;
import Mboussaid.laFactureFacile.Models.Validation;
import jakarta.mail.internet.MimeMessage;
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
                "Bonjour %s,\n\nMerci de vous être inscrit sur MaFacture!\n\n Code de validation : %s\n\n Voici les étapes à suivre pour valider votre compte :\n\n1. Connecter vous à votre compte sur "
                        + lien
                        + "\n\n 2.Entrez le code de validation ci-dessus dans l'interface de verification.\n\n3. Cliquer sur \'Valider\'. \n\nSi vous n'avez pas demandé cette inscription, veuillez ignorer cet e-mail.\n\nNous vous remercions de faire partie de notre communauté et restons à votre disposition pour toute question ou assistance.\n\nCordialement,\nL'équipe MaFacture",
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

    public void sendNotificationRelanceInvoice( String email,Resource file,InvoiceInfo invoiceInfo) {
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setFrom("mohamedboussaid69700@hotmail.fr");
        // mail.setTo(email);
        mail.setTo("mohamedboussaid69700@hotmail.fr");
        mail.setSubject("Votre facture");
        String msg = String.format(
            "Bonjour [Nom du client],\n\n"
            + "Nous souhaitons vous rappeler que votre facture numéro ** " + invoiceInfo.getInvoiceNumber()+" ** reste impayée à ce jour.\n\n"
            + "Voici un récapitulatif :\n"
            + "- **Date d'échéance** : "+ invoiceInfo.getInvoiceExpirDate() + "\n"
            + "- **Montant dû** : "+ invoiceInfo.getInvoiceAmount() + "\n\n"
            + "Nous vous prions de bien vouloir effectuer le règlement avant cette date pour éviter tout frais de retard. Vous pouvez effectuer le paiement via le lien ci-dessous :\n"
            + "👉 [Lien de paiement sécurisé]\n\n"
            + "Si vous avez déjà procédé au paiement, veuillez ignorer ce message. Dans le cas contraire, n’hésitez pas à nous contacter pour toute question.\n\n"
            + "Merci pour votre coopération !\n\n"
            + "Cordialement,\n"
            + "[Votre prénom et nom]\n"
            + "[Votre fonction]\n"
            + "[Nom de l'entreprise]\n"
            + "[Email de contact] | [Numéro de téléphone]"
        );
    }
    public void sendNotificationPdfInvoice( String email,Resource file,InvoiceInfo invoiceInfo) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        try{
            MimeMessage message = this.javaMailSender.createMimeMessage();
            MimeMessageHelper mail = new MimeMessageHelper(message, true);
            mail.setTo(email);
            mail.setSubject("Votre facture");
            String msg = String.format(
                "Bonjour "+StringUtils.capitalizeFirstLetter(invoiceInfo.getInvoiceCustomer())+",\n\n"
                + "Nous espérons que vous allez bien.\n\n"
                + "Nous vous écrivons pour vous informer que votre facture numéro ** " + invoiceInfo.getInvoiceNumber()+" ** est maintenant disponible. Voici les détails :\n\n"
                + "- **Client** : "+StringUtils.capitalizeFirstLetter(invoiceInfo.getInvoiceCustomer())+"\n"
                + "- **Date de la facture** : "+ invoiceInfo.getInvoiceDate().format(formatter)+ "\n"
                + "- **Montant dû** : "+ invoiceInfo.getInvoiceAmount() +" € \n\n"
                + "Vous pouvez consulter ou télécharger votre facture en cliquant sur la piece-jointe :\n"
                + "Si vous avez des questions ou si vous avez besoin d’une assistance supplémentaire, n’hésitez pas à nous contacter à tout moment.\n\n"
                + "Merci de votre confiance et au plaisir de vous servir !\n\n"
                + "Cordialement,\n"
                + invoiceInfo.getUser().getName()+" "+ StringUtils.capitalizeFirstLetter(invoiceInfo.getUser().getFirstname()) +"\n"
                + invoiceInfo.getUser().getEmail()+"\n"
                + invoiceInfo.getUser().getTelephone()+"\n\n\n\n"
                + "- **Facture faite par** : LAFACTUREFACILE\n"
            );
            mail.setText(msg, false);
            mail.addAttachment(file.getFile().getName(), file.getFile());
            this.javaMailSender.send(message);
            System.out.println("Email sent to "+email);
        }
        catch(Exception e){
            log.error("Error sending email: ", e);
        }

    }
}
