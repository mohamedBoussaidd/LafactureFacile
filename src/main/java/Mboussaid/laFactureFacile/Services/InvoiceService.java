package Mboussaid.laFactureFacile.Services;

import org.apache.tomcat.util.digester.ArrayStack;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import Mboussaid.laFactureFacile.DTO.CustomResponseEntity;
import Mboussaid.laFactureFacile.DTO.Request.InvoiceForSendEmailRequest;
import Mboussaid.laFactureFacile.DTO.Request.InvoiceRequest;
import Mboussaid.laFactureFacile.DTO.Response.InvoiceDTO;
import Mboussaid.laFactureFacile.Models.FileInfo;
import Mboussaid.laFactureFacile.Models.GetDate;
import Mboussaid.laFactureFacile.Models.Invoice;
import Mboussaid.laFactureFacile.Models.Items;
import Mboussaid.laFactureFacile.Models.User;
import Mboussaid.laFactureFacile.Models.ENUM.EStatusInvoice;
import Mboussaid.laFactureFacile.Models.Interface.CheckOwnerForData;
import Mboussaid.laFactureFacile.Repository.FileInfoRepository;
import Mboussaid.laFactureFacile.Repository.InvoiceRepository;
import Mboussaid.laFactureFacile.Repository.UserRepository;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;

import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

import java.util.Optional;
import java.util.UUID;

@Service
public class InvoiceService {
        private final UserRepository userRepository;
        private final FileStorageService fileStorageService;
        private final NotificationService notificationService;
        private final InvoiceRepository invoiceRepository;
        private final PdfService pdfService;
        private final FileInfoRepository fileInfoRepository;
        private BigDecimal amountHT = new BigDecimal(0);
        private BigDecimal amountTTC = new BigDecimal(0);

        public InvoiceService(UserRepository userRepository,
                        FileStorageService fileStorageService, NotificationService notificationService,
                        InvoiceRepository invoiceRepository, PdfService pdfService,
                        FileInfoRepository fileInfoRepository) {
                this.userRepository = userRepository;
                this.fileStorageService = fileStorageService;
                this.notificationService = notificationService;
                this.invoiceRepository = invoiceRepository;
                this.pdfService = pdfService;
                this.fileInfoRepository = fileInfoRepository;
        }

        public User getCurrentUser() {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                User userImpl = (User) auth.getPrincipal();
                Optional<User> user = userRepository.findById(userImpl.getId());

                if (user.isEmpty()) {
                        throw new RuntimeException("Un probleme est survenu lors de la récupération de l'utilisateur");
                }
                return user.get();
        }

        public CustomResponseEntity<?> createInvoice(InvoiceRequest invoiceRequest) throws IOException {

                User currentUser = getCurrentUser();
                if(currentUser.getFirstname() == null || currentUser.getName() == null || currentUser.getAdresse() == null || currentUser.getCity() == null || currentUser.getPostalcode() == null || currentUser.getSiret() == null || currentUser.getTelephone() == null){
                        return CustomResponseEntity.error(HttpStatus.BAD_REQUEST.value(),
                                        "Veuillez completer votre profil avant de créer une facture");
                }

                Invoice invoice = preparInvoice(currentUser, invoiceRequest);
                File pdfFile = this.pdfService.createPdf(invoice, currentUser);
                fileStorageService.storeFile(pdfFile);

                FileInfo fileInfo = preparFileInfo(pdfFile);

                FileInfo fileInfobdd = this.fileInfoRepository.save(fileInfo);

                invoice.setFile(fileInfobdd);
                this.invoiceRepository.save(invoice);

                return CustomResponseEntity.successWithoutDataDisplayed(HttpStatus.CREATED.value(),
                                "La facture a été créée avec succès !!");
        }

        @CheckOwnerForData(entity = Invoice.class, idField = "id")
        public CustomResponseEntity<?> deleteInvoice(Integer id) {
                Optional<Invoice> optionalInvoice = this.invoiceRepository.findById(id);
                if (optionalInvoice.isEmpty()) {
                throw new RuntimeException("Un probleme est survenu lors de la suppression de la facture");
                }
                Invoice invoice = optionalInvoice.get();

                String filename = invoice.getFile().getName();
                this.fileStorageService.deleteFile(filename);
                this.invoiceRepository.delete(invoice);
                return CustomResponseEntity.successWithoutDataDisplayed(HttpStatus.CREATED.value(),
                                "La facture a été supprimer avec succès !!");
        }

        @CheckOwnerForData(entity = Invoice.class, idField = "id")
        public CustomResponseEntity<?> updateInvoice(InvoiceRequest invoiceRequest) {
              
                Optional<Invoice> optionalInvoice = this.invoiceRepository
                                .findById(invoiceRequest.getId());
                if (optionalInvoice.isEmpty()) {
                        return CustomResponseEntity.error(HttpStatus.FORBIDDEN.value(),
                                        "un probleme est survenu lors de la mise à jour de la facture");
                }

                Invoice invoice = optionalInvoice.get();
                if (invoice.getStatus() == EStatusInvoice.PAYEE) {
                        return CustomResponseEntity.error(HttpStatus.FORBIDDEN.value(),
                                        "La facture est déjà payer , vous ne pouvez pas la modifier");
                }

                // Validation des dates avec une méthode dédiée
                if (isInvalidExpirationDate(invoiceRequest.getExpirationDate(), invoiceRequest.getCreationDate(),
                                invoice)) {
                        return CustomResponseEntity.error(
                                        HttpStatus.FORBIDDEN.value(),
                                        "La date d'expiration de la facture doit être supérieure à la date de création");
                }
                // Appliquer les changements nécessaires
                boolean isModified = applyValidationsAndChanges(invoice, invoiceRequest);
                if (isModified) {
                        this.invoiceRepository.save(invoice);
                        return CustomResponseEntity.successWithoutDataDisplayed(HttpStatus.OK.value(),
                                        "La mise à jour de votre facture a ete effectué");
                } else {
                        return CustomResponseEntity.successWithoutDataDisplayed(HttpStatus.OK.value(),
                                        "Aucune modification n'a été effectuée");
                }
        }

        public CustomResponseEntity<?> getInvoiceByUser(Integer id) {
                Optional<User> user = this.userRepository.findById(id);
                if (user.isEmpty()) {
                        return CustomResponseEntity.error(HttpStatus.BAD_REQUEST.value(),
                                        "Un probleme est survenu lors de la récupération des factures");
                }
                User principalUser = user.get();
                List<Invoice> listInvoice = this.invoiceRepository.findByUser(principalUser);
                List<InvoiceDTO> result = new ArrayStack<>();
                listInvoice.forEach(invoice -> {
                        result.add(InvoiceDTO.builder()
                                        .id(invoice.getId())
                                        .invoiceNumber(invoice.getInvoiceNumber() != null ? invoice.getInvoiceNumber()
                                                        : invoice.getTmpInvoiceNumber())
                                        .invoiceCustomer(invoice.getCustomerName())
                                        .invoiceCustomerEmail(invoice.getCustomerEmail())
                                        .invoiceDate(invoice.getCreationDate())
                                        .invoiceExpirDate(invoice.getExpirationDate())
                                        .invoiceAmount(invoice.getAmountTTC().toString())
                                        .invoiceTax(invoice.getAmountTTC().subtract(invoice.getAmountHT()).toString())
                                        .status(invoice.getStatus())
                                        .build());
                });

                return CustomResponseEntity.successWithDataHidden(HttpStatus.OK.value(),
                                "La recuperation des factures a réussi",
                                result);
        }

        public Resource displayInvoice(Integer IndexOfInvoice) {
                Optional<Invoice> OptionalInvoice = this.invoiceRepository.findById(IndexOfInvoice);
                if (OptionalInvoice.isEmpty()) {
                        throw new RuntimeException("Un probleme est survenu lors de la récupération de la facture");
                }
                Invoice invoice = OptionalInvoice.get();
                String filename = invoice.getFile().getName();
                return this.fileStorageService.readFile(filename);
        }

        @CheckOwnerForData(entity = Invoice.class, idField = "id")
        public CustomResponseEntity<?> sendInvoice(InvoiceForSendEmailRequest invoiceForSendEmailRequest) throws IOException {
                Optional<Invoice> optionalInvoice = this.invoiceRepository.findById(invoiceForSendEmailRequest.id());
                if (optionalInvoice.isEmpty()) {
                        return CustomResponseEntity.error(HttpStatus.BAD_REQUEST.value(),
                                        "Un probleme est survenu lors de l'envoi de la facture");
                }
                Invoice invoice = optionalInvoice.get();
                if (invoice.getStatus() != EStatusInvoice.CREER) {
                        String filename = invoice.getFile().getName();

                        Resource file = this.fileStorageService.readFile(filename);
                        if (file == null) {
                                return CustomResponseEntity.error(HttpStatus.BAD_REQUEST.value(),
                                                "Un probleme est survenu lors de l'envoi de la facture");
                        }
                        String email = invoiceForSendEmailRequest.email();
                        this.notificationService.sendNotificationPdfInvoice(email, file, invoice);
                        return CustomResponseEntity.successWithoutDataDisplayed(HttpStatus.OK.value(),
                                        "La facture a été envoyée avec succès");
                }
                int numberOfInvoice = this.invoiceRepository
                                .findInvoiceWithoutThisStatus(EStatusInvoice.CREER, invoice.getUser().getId()).size();
                invoice.setInvoiceNumber(invoice.getInvoiceNumber() != null ? invoice.getInvoiceNumber()
                                : getNumberInvoice(invoice, numberOfInvoice));
                invoice.setStatus(EStatusInvoice.ATTENTE);
                Invoice invoiceBdd = this.invoiceRepository.save(invoice);

                File pdfFile = this.pdfService.createPdf(invoiceBdd, invoiceBdd.getUser());
                fileStorageService.storeFile(pdfFile);
                fileStorageService.deleteFile(invoiceBdd.getFile().getName());

                FileInfo fileInfo = new FileInfo();
                fileInfo.setName(pdfFile.getName());
                fileInfo.setType(fileInfo.getExtension(pdfFile.getName()));
                fileInfo.setCreationDate(GetDate.getNow());
                if (fileInfo.getCreationDate() == null) {
                        return CustomResponseEntity.error(HttpStatus.BAD_REQUEST.value(),
                                        "La date de création du fichier est invalid");
                }
                fileInfo.setExpirationDate(fileInfo.getCreationDate().plus(10, java.time.temporal.ChronoUnit.DAYS));
                FileInfo fileInfobdd = this.fileInfoRepository.save(fileInfo);

                invoiceBdd.setFile(fileInfobdd);
                this.invoiceRepository.save(invoiceBdd);
                String filename = invoiceBdd.getFile().getName();
                Resource file = this.fileStorageService.readFile(filename);
                this.notificationService.sendNotificationPdfInvoice(invoiceBdd.getCustomerEmail(), file, invoice);

                return CustomResponseEntity.successWithoutDataDisplayed(HttpStatus.OK.value(),
                                "La facture a été envoyée avec succès");
        }

        @CheckOwnerForData(entity = Invoice.class, idField = "id")
        public CustomResponseEntity<?> relaunchCustomer(InvoiceForSendEmailRequest invoiceRequest) {
                Optional<Invoice> optionalInvoice = this.invoiceRepository.findById(invoiceRequest.id());
                if (optionalInvoice.isEmpty()) {
                        return CustomResponseEntity.error(HttpStatus.BAD_REQUEST.value(),
                                        "Un probleme est survenu lors de l'envoi de la facture");
                }
                Invoice invoice = optionalInvoice.get();
                String filename = invoice.getFile().getName();
                Resource file = this.fileStorageService.readFile(filename);
                if (file == null) {
                        return CustomResponseEntity.error(HttpStatus.BAD_REQUEST.value(),
                                        "Un probleme est survenu lors de l'envoi de la facture");
                }
                String email = invoiceRequest.email();
                this.notificationService.sendNotificationRelanceInvoice(email, file, invoice);
                invoice.setStatus(EStatusInvoice.RELANCER);
                this.invoiceRepository.save(invoice);
                return CustomResponseEntity.successWithoutDataDisplayed(HttpStatus.OK.value(),
                                "La relance a été envoyée avec succès");
        }
        /*
         * -------------------------------------private---------------------------------
         * ----------------
         */

        /**
         * Applique les règles métiers spécifiques pour valider les changements de
         * facture.
         */
        private boolean applyValidationsAndChanges(Invoice invoice, InvoiceRequest invoiceRequest) {
                ZonedDateTime newExpirationDate = GetDate
                                .getZonedDateTimeFromString(invoiceRequest.getExpirationDate());
                ZonedDateTime newCreationDate = GetDate.getZonedDateTimeFromString(invoiceRequest.getCreationDate());

                if (invoice.getStatus() == EStatusInvoice.CREER
                                && !invoice.getCreationDate().isEqual(newCreationDate)) {
                        invoice.setCreationDate(newCreationDate);
                        return true;
                }

                if (invoice.getStatus() != EStatusInvoice.PAYEE &&
                                !invoice.getExpirationDate().isEqual(newExpirationDate) &&
                                newExpirationDate.isAfter(GetDate.getNow())) {
                        invoice.setExpirationDate(newExpirationDate);
                        return true;
                }

                if (invoice.getStatus() == EStatusInvoice.ATTENTE &&
                                (invoiceRequest.getStatus() == EStatusInvoice.PAYEE
                                                || invoiceRequest.getStatus() == EStatusInvoice.RETARD)) {
                        invoice.setStatus(invoiceRequest.getStatus());
                        return true;
                }
                return false;
        }
        /**
         * Vérifie si la nouvelle date d'expiration est valide.
         */
        private boolean isInvalidExpirationDate(String expirationDateStr, String creationDateStr, Invoice invoice) {
                ZonedDateTime newExpirationDate = GetDate.getZonedDateTimeFromString(expirationDateStr);
                ZonedDateTime newCreationDate = GetDate.getZonedDateTimeFromString(creationDateStr);
                return newExpirationDate.isBefore(invoice.getCreationDate()) ||
                                newExpirationDate.isBefore(GetDate.getNow()) ||
                                newExpirationDate.isBefore(newCreationDate);
        }
        /*
         * @param user
         * 
         * @param invoiceRequest
         * 
         * @return Invoice
         * Fonction pour preparer une facture apartir d'une invoiceRequest et un user
         * crée pour la lisibilité du code
         */
        private Invoice preparInvoice(User user, InvoiceRequest invoiceRequest) {
                Invoice invoice = Invoice.builder()
                                .sellerName(user.getName() + " " + user.getFirstname())
                                .sellerEmail(user.getEmail())
                                .sellerAddress(user.getAdresse() + "\n " + user.getCity() + "\n "
                                                + user.getPostalcode())
                                .sellerSiret(user.getSiret())
                                .sellerPhone(user.getTelephone())
                                .customerName(invoiceRequest.getCustomerName())
                                .customerEmail(invoiceRequest.getCustomerEmail())
                                .customerAddress(invoiceRequest.getCustomerAddress())
                                .customerPhone(invoiceRequest.getCustomerPhone())
                                .creationDate(GetDate.getZonedDateTimeFromString(invoiceRequest.getCreationDate()))
                                .expirationDate(invoiceRequest.getExpirationDate() != "" && GetDate
                                                .getZonedDateTimeFromString(invoiceRequest.getExpirationDate())
                                                .isAfter(GetDate.getNow())
                                                                ? GetDate.getZonedDateTimeFromString(
                                                                                invoiceRequest.getExpirationDate())
                                                                : GetDate.getZonedDateTimeFromString(
                                                                                invoiceRequest.getCreationDate())
                                                                                .plus(20, java.time.temporal.ChronoUnit.DAYS))// 20
                                                                                                                              // jours
                                .user(user)
                                .status(EStatusInvoice.CREER)
                                .build();
                if (invoiceRequest.getInvoiceNumber().equals("")) {
                        invoice.setTmpInvoiceNumber(getTmpInvoiceNumber(invoice));
                } else {
                        // A VOIR CAR POSE PEUT ETRE PROBLEME LOGIQUE A REVOIR SI IL INDIQUE UN NUMERO
                        // DE FACTURE SI IL FAUT L AJOUTER DIRECT AU NUMERO DE FACTURE OU PAS
                        invoice.setTmpInvoiceNumber(invoiceRequest.getInvoiceNumber());
                        invoice.setInvoiceNumber(invoiceRequest.getInvoiceNumber());
                }
                List<Items> items = new ArrayStack<>();
                invoiceRequest.getItems().forEach(item -> {
                        BigDecimal taxMultiplier = BigDecimal.valueOf(item.getTax()).divide(BigDecimal.valueOf(100)).add(BigDecimal.ONE);
                        BigDecimal priceTTC = BigDecimal.valueOf(item.getUnitPrice()).multiply(BigDecimal.valueOf(item.getQuantity())).multiply(taxMultiplier);
                        items.add(Items.builder()
                                        .productName(item.getProductName())
                                        .unitPrice(BigDecimal.valueOf(item.getUnitPrice()))
                                        .priceTTC(priceTTC)
                                        .amountOfTaxe(priceTTC.subtract(BigDecimal.valueOf(item.getUnitPrice()).multiply(BigDecimal.valueOf(item.getQuantity()))))
                                        .tax(BigDecimal.valueOf(item.getTax()))
                                        .quantity(item.getQuantity())
                                        .invoice(invoice)
                                        .build());
                        amountTTC = amountTTC.add(new BigDecimal(priceTTC.toString()));
                        amountHT = amountHT.add(new BigDecimal(item.getUnitPrice().toString()).multiply(new BigDecimal(item.getQuantity().toString())));
                });
                invoice.setItems(items);
                invoice.setAmountHT(amountHT);
                invoice.setAmountTTC(amountTTC);
                return invoice;
        }
        private FileInfo preparFileInfo(File pdfFile) {
                FileInfo fileInfo = new FileInfo();
                fileInfo.setName(pdfFile.getName());
                fileInfo.setType(fileInfo.getExtension(pdfFile.getName()));
                fileInfo.setCreationDate(GetDate.getNow());
                if (fileInfo.getCreationDate() == null) {
                        throw new RuntimeException("La date de création du fichier est invalid");
                }
                fileInfo.setExpirationDate(fileInfo.getCreationDate().plus(10, java.time.temporal.ChronoUnit.DAYS));
                return fileInfo;
        }
        private String getNumberInvoice(Invoice invoice, Integer numberInvoice) {
                Integer realNumber = numberInvoice + 1;
                String actualDate = new SimpleDateFormat("yyMMdd").format(new Date());
                String base = invoice.getSellerName().substring(0, 3) + invoice.getCustomerName().substring(0, 3)
                                + actualDate;
                System.out.println(actualDate + invoice.getCustomerName().substring(0, 3));
                return base + "--" + realNumber;
        }
        private String getTmpInvoiceNumber(Invoice invoice) {
                String actualDate = new SimpleDateFormat("yyMMdd").format(new Date());
                String base = invoice.getSellerName().substring(0, 3) + invoice.getCustomerName().substring(0, 3)
                                + actualDate;
                String uniquePart = UUID.randomUUID().toString().substring(0, 8);
                String finalTmpInvoiceNumber = base + "-" + uniquePart;
                System.out.println(finalTmpInvoiceNumber);
                return finalTmpInvoiceNumber;
        }
}
