package Mboussaid.laFactureFacile.Services;

import org.apache.tomcat.util.digester.ArrayStack;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import Mboussaid.laFactureFacile.DTO.Request.InvoiceRequest;
import Mboussaid.laFactureFacile.Models.Invoice;
import Mboussaid.laFactureFacile.Models.Items;
import Mboussaid.laFactureFacile.Models.User;
import Mboussaid.laFactureFacile.Repository.UserRepository;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import java.util.Map;
import java.util.Optional;

@Service
public class InvoiceService {
        private final Path root = Paths.get("src/main/resources/telechargements");
        // private final FileStorageService fileStorageService;
        private final UserRepository userRepository;
        // private final PdfService pdfService;
        private BigDecimal amountHT = new BigDecimal(0);
        private BigDecimal amountTTC = new BigDecimal(0);

        public InvoiceService(FileStorageService fileStorageService, UserRepository userRepository,
                        PdfService pdfService) {
                this.userRepository = userRepository;
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
                                .creationDate(invoiceRequest.getCreationDate())
                                .expirationDate(invoiceRequest.getExpirationDate())
                                .items(items)
                                .amountHT(amountHT)
                                .amountTTC(amountTTC)
                                .build();
                invoice.setInvoiceNumber(getNumberInvoice(invoice));
                User principalUser = user.get();
                Map<String, Object> result = new HashMap<>();
                result.put("utilisateur", principalUser);
                result.put("invoice", invoice);
                return result;
                // invoice.setInvoiceNumber(getNumberInvoice(invoice));
                // User principalUser = user.get();
                // File pdfFile = this.pdfService.createPdf(invoice, principalUser);
                // fileStorageService.storeFile(pdfFile);
                // Map<String, String> response = new HashMap<>();
                // response.put("message", "Facture créée avec succès");
                // response.put("filename", pdfFile.getName());
                // return new ResponseEntity<>(response, HttpStatus.CREATED);
        }

        public String getNumberInvoice(Invoice invoice) {
                String actualDate = new SimpleDateFormat("yyyyMMdd").format(new Date());
                System.out.println(actualDate + invoice.getCustomerName().substring(0, 3));
                return actualDate + invoice.getSellerName().substring(0, 3);
        }

        public void deleteInvoice() {
        }

        public void getInvoice() {
        }

        public void getInvoices() {
        }

        public void updateInvoice() {
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
