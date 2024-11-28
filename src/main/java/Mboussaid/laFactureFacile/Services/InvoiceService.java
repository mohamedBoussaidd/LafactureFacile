package Mboussaid.laFactureFacile.Services;

import org.apache.tomcat.util.digester.ArrayStack;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import Mboussaid.laFactureFacile.DTO.CustomResponseEntity;
import Mboussaid.laFactureFacile.DTO.Request.InvoiceInfoRequest;
import Mboussaid.laFactureFacile.DTO.Request.InvoiceRequest;
import Mboussaid.laFactureFacile.Models.GetDate;
import Mboussaid.laFactureFacile.Models.Invoice;
import Mboussaid.laFactureFacile.Models.InvoiceInfo;
import Mboussaid.laFactureFacile.Models.Items;
import Mboussaid.laFactureFacile.Models.User;
import Mboussaid.laFactureFacile.Repository.InvoiceInfoRepository;
import Mboussaid.laFactureFacile.Repository.UserRepository;

import java.io.IOException;
import java.math.BigDecimal;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import java.util.Map;
import java.util.Optional;

@Service
public class InvoiceService {
        private final UserRepository userRepository;
        private final InvoiceInfoRepository invoiceInfoRepository;
        private final FileStorageService fileStorageService;
        private BigDecimal amountHT = new BigDecimal(0);
        private BigDecimal amountTTC = new BigDecimal(0);

        public InvoiceService(UserRepository userRepository, InvoiceInfoRepository invoiceInfoRepository,
                        FileStorageService fileStorageService) {
                this.userRepository = userRepository;
                this.invoiceInfoRepository = invoiceInfoRepository;
                this.fileStorageService = fileStorageService;
        }

        public Map<String, Object> createInvoice(InvoiceRequest invoiceRequest) throws IOException {

                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                User userImpl = (User) auth.getPrincipal();
                Optional<User> user = userRepository.findById(userImpl.getId());

                if (user.isEmpty()) {
                        return null;
                }

                List<Items> items = new ArrayStack<>();
                invoiceRequest.getItems().forEach(item -> {
                        items.add(Items.builder()
                                        .name(item.getDescription())
                                        .priceHT(BigDecimal.valueOf(item.getPriceHT()))
                                        .priceTTC(BigDecimal.valueOf(item.getPriceHT() * item.getQuantity()
                                                        * ((double) item.getTax() / 100 + 1)))
                                        .tax(BigDecimal.valueOf(item.getTax()))
                                        .quantity(item.getQuantity())
                                        .build());
                        amountTTC = amountTTC.add(new BigDecimal(item.getPriceTTC()));
                        amountHT = amountHT.add(new BigDecimal(item.getPriceHT()));
                });
                Invoice invoice = Invoice.builder()
                                .sellerName(user.get().getName() + " " + user.get().getFirstname())
                                .sellerEmail(user.get().getEmail())
                                .sellerAddress(user.get().getAdresse() + "\n " + user.get().getCity() + "\n "
                                                + user.get().getPostalcode())
                                .sellerSiret(user.get().getSiret())
                                .sellerPhone(user.get().getTelephone())
                                .customerEmail(invoiceRequest.getCustomerEmail())
                                .customerName(invoiceRequest.getCustomerName())
                                .customerAddress(invoiceRequest.getCustomerAddress())
                                .customerPhone(invoiceRequest.getCustomerPhone())
                                .creationDate(GetDate.getZonedDateTimeFromString(invoiceRequest.getCreationDate()))
                                .expirationDate(invoiceRequest.getExpirationDate() != ""
                                                ? GetDate.getZonedDateTimeFromString(invoiceRequest.getCreationDate())
                                                : GetDate.getZonedDateTimeFromString(invoiceRequest.getCreationDate())
                                                                .plus(20, java.time.temporal.ChronoUnit.DAYS))// 20
                                                                                                              // jours
                                .items(items)
                                .amountHT(amountHT)
                                .amountTTC(amountTTC)
                                .build();
                User principalUser = user.get();
                Integer numberOfInvoiceInfo = principalUser.getInvoicesInfo().size();
                if (invoiceRequest.getInvoiceNumber().equals("")) {
                        invoice.setInvoiceNumber(getNumberInvoice(invoice, numberOfInvoiceInfo));
                } else {
                        invoice.setInvoiceNumber(invoiceRequest.getInvoiceNumber());
                }

                InvoiceInfo invoiceInfo = new InvoiceInfo();
                invoiceInfo.setInvoiceCustomer(invoice.getCustomerName());
                invoiceInfo.setInvoiceNumber(invoice.getInvoiceNumber());
                invoiceInfo.setInvoiceDate(invoice.getCreationDate());
                invoiceInfo.setInvoiceExpirDate(invoice.getExpirationDate());
                invoiceInfo.setStatus(invoiceRequest.getStatus());
                invoiceInfo.setInvoiceAmount(invoice.getAmountTTC().toString());
                invoiceInfo.setUser(principalUser);

                Map<String, Object> result = new HashMap<>();
                result.put("user", principalUser);
                result.put("invoice", invoice);
                result.put("invoiceInfo", invoiceInfo);

                return result;
        }

        public String getNumberInvoice(Invoice invoice, Integer numberOfInvoiceInfo) {
                Integer realNumber = numberOfInvoiceInfo + 1;
                String actualDate = new SimpleDateFormat("yyMMdd").format(new Date());
                System.out.println(actualDate + invoice.getCustomerName().substring(0, 3));
                return actualDate + invoice.getSellerName().substring(0, 3) + invoice.getCustomerName().substring(0, 3)
                                + realNumber;
        }

        public void deleteInvoice() {
        }

        public CustomResponseEntity<?> updateInvoice(InvoiceInfoRequest invoiceInfoRequest) {
                Optional<InvoiceInfo> optionalInvoiceInfo = this.invoiceInfoRepository
                                .findById(invoiceInfoRequest.getId());
                if (optionalInvoiceInfo.isEmpty()) {
                        return CustomResponseEntity.error(HttpStatus.FORBIDDEN.value(),
                                        "un probleme est survenu lors de la mise à jour de la facture");
                }
                InvoiceInfo invoiceInfo = optionalInvoiceInfo.get();
                invoiceInfo.setInvoiceExpirDate(
                                GetDate.getZonedDateTimeFromString(invoiceInfoRequest.getInvoiceExpirDate()));
                invoiceInfo.setStatus(invoiceInfoRequest.getStatus());
                InvoiceInfo invoiceUpdate = this.invoiceInfoRepository.save(invoiceInfo);
                return CustomResponseEntity.success(HttpStatus.OK.value(),
                                "La mise à jour de votre facture a ete effectué", invoiceUpdate);
        }

        public CustomResponseEntity<?> getInvoiceInfoByUser(Integer id) {
                Optional<User> user = this.userRepository.findById(id);
                if (user.isEmpty()) {
                        return CustomResponseEntity.error(HttpStatus.BAD_REQUEST.value(),
                                        "Un probleme est survenu lors de la récupération des factures");
                }
                User principalUser = user.get();
                List<InvoiceInfo> listInvoiceinfo = this.invoiceInfoRepository.findByUser(principalUser);
                return CustomResponseEntity.success(HttpStatus.OK.value(), "La recuperation des factures a réussi",
                                listInvoiceinfo);
        }

        public Resource displayInvoice(Integer IndexOfInvoice) {
                Optional<InvoiceInfo> OptionalInvoiceInfo = this.invoiceInfoRepository.findById(IndexOfInvoice);
                if (OptionalInvoiceInfo.isEmpty()) {
                        throw new RuntimeException("Un probleme est survenu lors de la récupération de la facture");
                }
                InvoiceInfo invoiceInfo = OptionalInvoiceInfo.get();
                String filename = invoiceInfo.getFile().getName();
                return this.fileStorageService.readFile(filename);
        }
}
