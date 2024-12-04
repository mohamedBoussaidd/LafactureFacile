package Mboussaid.laFactureFacile.Services;

import org.apache.tomcat.util.digester.ArrayStack;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import Mboussaid.laFactureFacile.DTO.CustomResponseEntity;
import Mboussaid.laFactureFacile.DTO.Request.InvoiceForSendEmailRequest;
import Mboussaid.laFactureFacile.DTO.Request.InvoiceInfoRequest;
import Mboussaid.laFactureFacile.DTO.Request.InvoiceRequest;
import Mboussaid.laFactureFacile.DTO.Response.InvoiceDTO;
import Mboussaid.laFactureFacile.Models.FileInfo;
import Mboussaid.laFactureFacile.Models.GetDate;
import Mboussaid.laFactureFacile.Models.Invoice;
import Mboussaid.laFactureFacile.Models.InvoiceInfo;
import Mboussaid.laFactureFacile.Models.Items;
import Mboussaid.laFactureFacile.Models.User;
import Mboussaid.laFactureFacile.Models.ENUM.EStatusInvoice;
import Mboussaid.laFactureFacile.Repository.FileInfoRepository;
import Mboussaid.laFactureFacile.Repository.InvoiceInfoRepository;
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
import java.util.HashMap;
import java.util.List;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class InvoiceService {
        private final UserRepository userRepository;
        private final InvoiceInfoRepository invoiceInfoRepository;
        private final FileStorageService fileStorageService;
        private final NotificationService notificationService;
        private final InvoiceRepository invoiceRepository;
        private final PdfService pdfService;
        private final FileInfoRepository fileInfoRepository;
        private BigDecimal amountHT = new BigDecimal(0);
        private BigDecimal amountTTC = new BigDecimal(0);

        public InvoiceService(UserRepository userRepository, InvoiceInfoRepository invoiceInfoRepository,
                        FileStorageService fileStorageService, NotificationService notificationService,
                        InvoiceRepository invoiceRepository, PdfService pdfService, FileInfoRepository fileInfoRepository) {
                this.userRepository = userRepository;
                this.invoiceInfoRepository = invoiceInfoRepository;
                this.fileStorageService = fileStorageService;
                this.notificationService = notificationService;
                this.invoiceRepository = invoiceRepository;
                this.pdfService = pdfService;
                this.fileInfoRepository = fileInfoRepository;
        }

        public Map<String, Object> createInvoice(InvoiceRequest invoiceRequest) throws IOException {

                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                User userImpl = (User) auth.getPrincipal();
                Optional<User> user = userRepository.findById(userImpl.getId());

                if (user.isEmpty()) {
                        return null;
                }
                User principalUser = user.get();
                Invoice invoice = Invoice.builder()
                                .sellerName(principalUser.getName() + " " + principalUser.getFirstname())
                                .sellerEmail(principalUser.getEmail())
                                .sellerAddress(principalUser.getAdresse() + "\n " + principalUser.getCity() + "\n "
                                                + principalUser.getPostalcode())
                                .sellerSiret(principalUser.getSiret())
                                .sellerPhone(principalUser.getTelephone())
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
                                .user(principalUser)
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
                        items.add(Items.builder()
                                        .productName(item.getDescription())
                                        .priceHT(BigDecimal.valueOf(item.getPriceHT()))
                                        .priceTTC(BigDecimal.valueOf(item.getPriceHT() * item.getQuantity()
                                                        * ((double) item.getTax() / 100 + 1)))
                                        .tax(BigDecimal.valueOf(item.getTax()))
                                        .quantity(item.getQuantity())
                                        .totalHT(BigDecimal.valueOf(item.getPriceHT().doubleValue())
                                                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                                        .totalTTC(BigDecimal.valueOf(item.getPriceTTC().doubleValue())
                                                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                                        .invoice(invoice)
                                        .build());
                        amountTTC = amountTTC.add(new BigDecimal(item.getPriceTTC()));
                        amountHT = amountHT.add(new BigDecimal(item.getPriceHT()));
                });

                invoice.setItems(items);
                invoice.setAmountHT(amountHT);
                invoice.setAmountTTC(amountTTC);

                Map<String, Object> result = new HashMap<>();
                result.put("user", principalUser);
                result.put("invoice", invoice);

                return result;
        }

        /* A FAIRE */
        public Map<String, Object> preparInvoice(User user, Invoice invoice) {
                return null;
        }

        public String getNumberInvoice(Invoice invoice, Integer numberOfInvoiceInfo) {
                Integer realNumber = numberOfInvoiceInfo + 1;
                String actualDate = new SimpleDateFormat("yyMMdd").format(new Date());
                String base = invoice.getSellerName().substring(0, 3) + invoice.getCustomerName().substring(0, 3)
                                + actualDate;
                System.out.println(actualDate + invoice.getCustomerName().substring(0, 3));
                return base + "--" + realNumber;
        }

        public String getTmpInvoiceNumber(Invoice invoice) {
                String actualDate = new SimpleDateFormat("yyMMdd").format(new Date());
                String base = invoice.getSellerName().substring(0, 3) + invoice.getCustomerName().substring(0, 3)
                                + actualDate;
                String uniquePart = UUID.randomUUID().toString().substring(0, 8);
                String finalTmpInvoiceNumber = base + "-" + uniquePart;
                System.out.println(finalTmpInvoiceNumber);
                return finalTmpInvoiceNumber;
        }

        public void deleteInvoice() {
        }

        public CustomResponseEntity<?> updateInvoice(InvoiceInfoRequest invoiceRequest) {
                Optional<Invoice> optionalInvoice = this.invoiceRepository
                                .findById(invoiceRequest.getId());
                if (optionalInvoice.isEmpty()) {
                        return CustomResponseEntity.error(HttpStatus.FORBIDDEN.value(),
                                        "un probleme est survenu lors de la mise à jour de la facture");
                }
                Invoice invoice = optionalInvoice.get();
                ZonedDateTime newExpirationDate = GetDate.getZonedDateTimeFromString(invoiceRequest.getInvoiceExpirDate());
                if(newExpirationDate.isBefore(invoice.getCreationDate())){
                        return CustomResponseEntity.error(HttpStatus.FORBIDDEN.value(),
                                        "La date d'expiration de la facture doit être supérieure à la date de création");

                }
                invoice.setExpirationDate(newExpirationDate);
                Invoice invoiceUpdate = this.invoiceRepository.save(invoice);
                return CustomResponseEntity.successWithDataDisplayed(HttpStatus.OK.value(),
                                "La mise à jour de votre facture a ete effectué", invoiceUpdate);
        }

        public CustomResponseEntity<?> getInvoiceInfoByUser(Integer id) {
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
                                        .status(invoice.getStatus())
                                        .build());
                });

                return CustomResponseEntity.successWithDataHidden(HttpStatus.OK.value(), "La recuperation des factures a réussi",
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

        public CustomResponseEntity<?> sendInvoice(InvoiceForSendEmailRequest invoiceRequest) throws IOException {
                Optional<Invoice> optionalInvoice = this.invoiceRepository.findById(invoiceRequest.id());
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
                        String email = invoiceRequest.email();
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

                invoice.setFile(fileInfobdd);
                this.invoiceRepository.save(invoice);

                return CustomResponseEntity.successWithoutDataDisplayed(HttpStatus.OK.value(), "La facture a été envoyée avec succès");
        }

        public CustomResponseEntity<?> relaunchCustomer(InvoiceForSendEmailRequest invoice) {
                Optional<InvoiceInfo> optionalInvoiceInfo = this.invoiceInfoRepository.findById(invoice.id());
                if (optionalInvoiceInfo.isEmpty()) {
                        return CustomResponseEntity.error(HttpStatus.BAD_REQUEST.value(),
                                        "Un probleme est survenu lors de l'envoi de la facture");
                }
                InvoiceInfo invoiceInfo = optionalInvoiceInfo.get();
                String filename = invoiceInfo.getFile().getName();
                Resource file = this.fileStorageService.readFile(filename);
                if (file == null) {
                        return CustomResponseEntity.error(HttpStatus.BAD_REQUEST.value(),
                                        "Un probleme est survenu lors de l'envoi de la facture");
                }
                String email = invoice.email();
                this.notificationService.sendNotificationRelanceInvoice(email, file, invoiceInfo);
                invoiceInfo.setStatus(EStatusInvoice.RELANCER);
                this.invoiceInfoRepository.save(invoiceInfo);
                return CustomResponseEntity.successWithoutDataDisplayed(HttpStatus.OK.value(), "La relance a été envoyée avec succès");
        }
}
