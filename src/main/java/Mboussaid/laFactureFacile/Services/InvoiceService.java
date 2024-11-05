package Mboussaid.laFactureFacile.Services;

import org.apache.tomcat.util.digester.ArrayStack;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.method.P;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import Mboussaid.laFactureFacile.DTO.Request.InvoiceRequest;
import Mboussaid.laFactureFacile.Models.Invoice;
import Mboussaid.laFactureFacile.Models.Items;
import Mboussaid.laFactureFacile.Models.User;
import Mboussaid.laFactureFacile.Repository.UserRepository;
import Mboussaid.laFactureFacile.Services.PdfService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.VerticalAlignment;

@Service
public class InvoiceService {
        private final Path root = Paths.get("src/main/resources/telechargements");
        private final FileStorageService fileStorageService;
        private final UserRepository userRepository;
        private final PdfService pdfService;
        private BigDecimal amountHT = new BigDecimal(0);
        private BigDecimal amountTTC = new BigDecimal(0);

        public InvoiceService(FileStorageService fileStorageService, UserRepository userRepository,
                        PdfService pdfService) {
                this.fileStorageService = fileStorageService;
                this.userRepository = userRepository;
                this.pdfService = pdfService;
        }

        public ResponseEntity<String> createInvoice(InvoiceRequest invoiceRequest) throws IOException {

                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                User userImpl = (User) auth.getPrincipal();
                Optional<User> user = userRepository.findById(userImpl.getId());

                if (user.isEmpty()) {
                        return ResponseEntity.badRequest().body("Utilisateur non trouvé");
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
                                .sellerName(user.get().getUsername())
                                .sellerEmail(user.get().getEmail())
                                .sellerAddress(user.get().getAdresse())
                                .sellerSiret(user.get().getSiret())
                                .sellerPhone(user.get().getTelephone())
                                .invoiceNumber(getNumberInvoice(invoiceRequest))
                                .customerEmail(invoiceRequest.getCustomerEmail())
                                .customerName(invoiceRequest.getCustomerName())
                                .creationDate(invoiceRequest.getCreationDate())
                                .expirationDate(invoiceRequest.getExpirationDate())
                                .items(items)
                                .amountHT(amountHT)
                                .amountTTC(amountTTC)
                                .build();
                User principalUser = user.get();
                File pdfFile = this.pdfService.createPdf(invoice, principalUser);
                fileStorageService.storeFile(pdfFile);

                return ResponseEntity.ok().body("Facture créée: " + HttpStatus.OK);
        }

        public String getNumberInvoice(InvoiceRequest invoice) {
                String actualDate = new SimpleDateFormat("yyyyMMdd").format(new Date());
                System.out.println(actualDate + invoice.getCustomerName().substring(0, 3));
                return actualDate + invoice.getCustomerName().substring(0, 3);
        }

        public void deleteInvoice() {
        }

        public void getInvoice() {
        }

        public void getInvoices() {
        }

        public void updateInvoice() {
        }

        public Resource displayInvoice() {
                String filename = "momo.pdf";
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
