package Mboussaid.laFactureFacile.Cron;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import Mboussaid.laFactureFacile.Models.Invoice;
import Mboussaid.laFactureFacile.Models.ENUM.EStatusInvoice;
import Mboussaid.laFactureFacile.Repository.InvoiceRepository;
import Mboussaid.laFactureFacile.Services.FileStorageService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class InvoiceCron {

    private final InvoiceRepository invoiceRepository;
    private final FileStorageService fileStorageService;

    public InvoiceCron(InvoiceRepository invoiceRepository, FileStorageService fileStorageService) {
        this.invoiceRepository = invoiceRepository;
        this.fileStorageService = fileStorageService;
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
    /* 
     * effacer les facture avec le status cree  et avec la date d'expiration depasser
     * 2 fois par jour
     */
    @Scheduled(cron = "0 0 0/6 * * *")
    @Transactional
    public void deleteInvoiceCreerAndExpir(){
        log.info("XXXXXXXXXXXXXXXXXXXXXXXX-----Démarrage du netoyage des factures creer------XXXXXXXXXXXXXXXXXXXXXXXXXX");
        List<Invoice> listInvoiceForDelete = this.invoiceRepository.findAllInvoiceCreerExpir(EStatusInvoice.CREER);
        log.info("XXXXXXXXXXXXXXXXXXXX-------- Nombre de facture trouver : "+ listInvoiceForDelete.size() + "-------XXXXXXXXXXXXXXXXXXXXXXXXXXXX");
        for (Invoice invoice : listInvoiceForDelete) {
            this.fileStorageService.deleteFile(invoice.getFile().getName());
            this.invoiceRepository.deleteById(invoice.getId());
            log.info("XXXXXXXXXXXXXXXXXXXX-------- Facture supprimer : "+ invoice.getId() + "-------XXXXXXXXXXXXXXXXXXXXXXXXXXXX");
        }
    }
}
