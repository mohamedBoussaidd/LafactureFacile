package Mboussaid.laFactureFacile.Services;

import org.apache.tomcat.util.digester.ArrayStack;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import Mboussaid.laFactureFacile.DTO.Request.InvoiceInfoRequest;
import Mboussaid.laFactureFacile.DTO.Request.InvoiceRequest;
import Mboussaid.laFactureFacile.Models.Invoice;
import Mboussaid.laFactureFacile.Models.InvoiceInfo;
import Mboussaid.laFactureFacile.Models.Items;
import Mboussaid.laFactureFacile.Models.User;
import Mboussaid.laFactureFacile.Repository.InvoiceInfoRepository;
import Mboussaid.laFactureFacile.Repository.UserRepository;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import java.util.Map;
import java.util.Optional;

@Service
public class InvoiceService {
        private final Path root = Paths.get("src/main/resources/telechargements");
        private final UserRepository userRepository;
        private final InvoiceInfoRepository invoiceInfoRepository;
        private BigDecimal amountHT = new BigDecimal(0);
        private BigDecimal amountTTC = new BigDecimal(0);

        public InvoiceService(UserRepository userRepository, InvoiceInfoRepository invoiceInfoRepository) {
                this.userRepository = userRepository;
                this.invoiceInfoRepository = invoiceInfoRepository;
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
                                .creationDate(invoiceRequest.getCreationDate())
                                .expirationDate(invoiceRequest.getExpirationDate())
                                .items(items)
                                .amountHT(amountHT)
                                .amountTTC(amountTTC)
                                .build();
                User principalUser = user.get();
                Integer numberOfInvoiceInfo = principalUser.getInvoicesInfo().size();
                System.out.println(numberOfInvoiceInfo);
                if (invoiceRequest.getInvoiceNumber().equals("")) {
                        invoice.setInvoiceNumber(getNumberInvoice(invoice, numberOfInvoiceInfo));
                } else {
                        invoice.setInvoiceNumber(invoiceRequest.getInvoiceNumber());
                }
                System.out.println(invoice.getInvoiceNumber());
                Map<String, Object> result = new HashMap<>();
                result.put("user", principalUser);
                result.put("invoice", invoice);

                InvoiceInfo invoiceInfo = new InvoiceInfo();
                System.out.println(invoice.getAmountTTC().toString());
                invoiceInfo.setInvoiceAmount(invoice.getAmountTTC().toString());
                invoiceInfo.setInvoiceNumber(invoice.getInvoiceNumber());
                invoiceInfo.setInvoiceExpirDate(invoice.getExpirationDate());
                invoiceInfo.setStatus(invoiceRequest.getStatus());
                invoiceInfo.setInvoiceDate(invoice.getCreationDate());
                invoiceInfo.setInvoiceCustomer(invoice.getCustomerName());
                invoiceInfo.setUser(principalUser);
                this.invoiceInfoRepository.save(invoiceInfo);
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

        public void getInvoice() {
        }

        public void getInvoices() {
        }

        public ResponseEntity<?> updateInvoice(InvoiceInfoRequest invoiceInfoRequest) {
                Optional<InvoiceInfo> optionalInvoiceInfo = this.invoiceInfoRepository.findById(invoiceInfoRequest.getId());
                if (optionalInvoiceInfo.isEmpty()) {
                        return ResponseEntity.badRequest().body("Invoice not found");
                }
                InvoiceInfo invoiceInfo = optionalInvoiceInfo.get();
                invoiceInfo.setInvoiceExpirDate(invoiceInfoRequest.getInvoiceExpirDate());
                invoiceInfo.setStatus(invoiceInfoRequest.getStatus());
                InvoiceInfo invoiceUpdate = this.invoiceInfoRepository.save(invoiceInfo);
                return ResponseEntity.ok(invoiceUpdate);
        }

        public ResponseEntity<?> getInvoiceInfoByUser(Integer id) {
                Optional<User> user = this.userRepository.findById(id);
                if (user.isEmpty()) {
                        return ResponseEntity.badRequest().body("User not found");
                }
                User principalUser = user.get();
                List<InvoiceInfo> listInvoiceinfo = this.invoiceInfoRepository.findByUser(principalUser);
                return ResponseEntity.ok(listInvoiceinfo);
        }

        public Resource displayInvoice(String filename) {
                // String filename = "momo2719384209193558121.pdf";
                try {
                        Path filePath = root.resolve(filename);
                        Resource resource = new UrlResource(filePath.toUri());

                        if (resource.exists() || resource.isReadable()) {
                                return resource;
                        } else {
                                throw new RuntimeException("Could not read the file");
                        }
                } catch (MalformedURLException e) {
                        throw new RuntimeException("Error: " + e.getMessage());
                }
        }
}
