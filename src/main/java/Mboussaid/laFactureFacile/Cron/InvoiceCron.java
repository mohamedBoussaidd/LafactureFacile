package Mboussaid.laFactureFacile.Cron;

import java.time.LocalDateTime;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import Mboussaid.laFactureFacile.Models.ENUM.EStatusInvoice;
import Mboussaid.laFactureFacile.Repository.InvoiceRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class InvoiceCron {

    private final InvoiceRepository invoiceRepository;

    public InvoiceCron(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    /*
     * Met à jour le statut des factures en attente a en retard
     * tous les jours
     */
    @Scheduled(cron = "0 0 0/3 * * *")
    @Transactional
    public void updateAttenteInvoicesStatus() {
        log.info("Démarrage de la mise à jour des factures à : " + LocalDateTime.now());
        int updatedRows = this.invoiceRepository.updateStatusInvoiceForAttenteAtRetard(EStatusInvoice.RETARD);
        log.info("Nombre de factures mises à jour : " + updatedRows);
    }
}
