package Mboussaid.laFactureFacile.Services;

import org.apache.tomcat.util.digester.ArrayStack;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import Mboussaid.laFactureFacile.DTO.Request.InvoiceRequest;
import Mboussaid.laFactureFacile.Models.Invoice;
import Mboussaid.laFactureFacile.Models.Items;
import Mboussaid.laFactureFacile.Models.User;
import Mboussaid.laFactureFacile.Repository.UserRepository;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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

    public InvoiceService(FileStorageService fileStorageService, UserRepository userRepository) {
        this.fileStorageService = fileStorageService;
        this.userRepository = userRepository;
    }

    public String getNumberInvoice(InvoiceRequest invoice) {
        String actualDate = new SimpleDateFormat("yyyyMMdd").format(new Date());
        System.out.println(actualDate + invoice.getCustomerName().substring(0, 3));
        return actualDate + invoice.getCustomerName().substring(0, 3);
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
                    .priceHT(item.getPriceHT())
                    .priceTTC(item.getPriceTTC())
                    .tax(item.getTax())
                    .quantity(item.getQuantity())
                    .build());
        });
        Invoice invoice = Invoice.builder()
                .sellerName(user.get().getUsername())
                .sellerEmail(user.get().getEmail())
                // .sellerAddress(user.get().getAddress())
                // .sellerSiret(user.get().getNumeroSiret())
                .sellerAddress("123 rue de la rue")
                .sellerSiret("123456789")
                .invoiceNumber(getNumberInvoice(invoiceRequest))
                .customerEmail(invoiceRequest.getCustomerEmail())
                .customerName(invoiceRequest.getCustomerName())
                .creationDate(invoiceRequest.getCreationDate())
                .expirationDate(invoiceRequest.getExpirationDate())
                .items(items)
                .amount(invoiceRequest.getAmount())
                .build();
        File pdfFile = createPdf(invoice);
        fileStorageService.storeFile(pdfFile);

        return ResponseEntity.ok().body("Facture créée: " + invoice.getCustomerName());
    }

    public File createPdf(Invoice invoice) throws IOException {
        // Crée un fichier temporaire pour le PDF
        File tempFile = File.createTempFile("momo", ".pdf");

        try (FileOutputStream fos = new FileOutputStream(tempFile);
                PdfWriter writer = new PdfWriter(fos);
                PdfDocument pdf = new PdfDocument(writer);
                Document document = new Document(pdf)) {

            Table infoInvoice = infoSenderTable(invoice);
            Table infoCustomer = infoCustomerTable(invoice);
            Table infoProduct = infoProductTable(invoice);
            Table infoAmount = infoAmountTable(invoice);

            document.add(infoInvoice); // Ajouter la table au document
            document.add(infoCustomer);
            document.add(infoProduct);
            document.add(infoAmount);
            // Changement : Utilisation du total de la facture depuis l'objet Invoice
            document.add(new Paragraph("Total: " + invoice.getAmount() + "€").setBold());
            return tempFile; // Retourner le fichier PDF temporaire
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to create PDF document: " + e.getMessage(), e);
        } finally {
            System.out.println("Document created at: " + tempFile.getAbsolutePath());
        }

    }

    public Table infoSenderTable(Invoice invoice) {
        float col = 280f;
        float columnWidth[] = { col, col };
        Table table = new Table(columnWidth);
        table.setBackgroundColor(new DeviceRgb(240, 238, 238)).setFontColor(new DeviceRgb(55, 53, 53))
                .setBorder(Border.NO_BORDER);
        table.addCell(new Cell().add(new Paragraph("FACTURE"))
                .setTextAlignment(TextAlignment.CENTER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setMarginTop(30f)
                .setMarginBottom(30f)
                .setFontSize(30f)
                .setBorder(Border.NO_BORDER)).setHeight(150f);
        table.addCell(new Cell()
                .add(new Paragraph(
                        invoice.getSellerName() + "\n" + invoice.getSellerEmail() + "\n" + invoice.getSellerSiret()))
                .setTextAlignment(TextAlignment.RIGHT)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setMarginTop(30f)
                .setMarginBottom(30f)
                .setBorder(Border.NO_BORDER)
                .setFontSize(10f)
                .setMarginRight(20f));
        return table;
    }

    public Table infoCustomerTable(Invoice invoice) {
        // information de l expediteur
        float colWidht[] = {
                80, 300, 100, 80
        };
        Table infoFacture = new Table(colWidht);
        infoFacture.addCell(new Cell(0, 4).add(new Paragraph("Envoyer par : "))
                .setBold()
                .setBorder(Border.NO_BORDER));
        infoFacture.addCell(new Cell().add(new Paragraph("Nom : ")).setBorder(Border.NO_BORDER));
        infoFacture.addCell(new Cell().add(new Paragraph(invoice.getSellerName()))
                .setBorder(Border.NO_BORDER));
        infoFacture.addCell(
                new Cell().add(new Paragraph("No. Facture : ")).setBorder(Border.NO_BORDER));
        infoFacture.addCell(
                new Cell().add(new Paragraph("" + invoice.getInvoiceNumber()))
                        .setBorder(Border.NO_BORDER));
        infoFacture.addCell(new Cell().add(new Paragraph("No. Siret : ")).setBorder(Border.NO_BORDER));
        infoFacture.addCell(
                new Cell().add(new Paragraph("" + invoice.getSellerSiret()))
                        .setBorder(Border.NO_BORDER));
        DateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd", Locale.FRANCE);
        infoFacture.addCell(new Cell().add(new Paragraph("Date : ")).setBorder(Border.NO_BORDER));
        infoFacture
                .addCell(new Cell()
                        .add(new Paragraph(""
                                + dateFormat.format(new Date())))
                        .setBorder(Border.NO_BORDER));
        infoFacture.addCell(new Cell().add(new Paragraph("E-mail : ")).setBorder(Border.NO_BORDER));
        infoFacture.addCell(new Cell().add(new Paragraph(invoice.getSellerEmail()))
                .setBorder(Border.NO_BORDER));
        infoFacture.addCell(new Cell().add(new Paragraph("No. Tel : ")).setBorder(Border.NO_BORDER));
        infoFacture.addCell(new Cell().add(new Paragraph("" + "invoice.getCustomerNumber()"))
                .setBorder(Border.NO_BORDER));

        return infoFacture;
    }

    public Table infoProductTable(Invoice invoice) {
        float corpWidth[] = { 140, 140, 140, 140 };
        Table infoProduit = new Table(corpWidth).setMarginTop(25f);
        infoProduit.addCell(new Cell().add(new Paragraph("SERVICE")).setBorderRight(Border.NO_BORDER)
                .setTextAlignment(TextAlignment.CENTER));
        infoProduit.addCell(new Cell().add(new Paragraph("QUANTITÉ")).setBorderRight(Border.NO_BORDER)
                .setBorderLeft(Border.NO_BORDER).setTextAlignment(TextAlignment.CENTER));
        infoProduit.addCell(new Cell().add(new Paragraph("PRIX/HT")).setBorderRight(Border.NO_BORDER)
                .setBorderLeft(Border.NO_BORDER).setTextAlignment(TextAlignment.CENTER));
        infoProduit.addCell(new Cell().add(new Paragraph("PRIX/TTC")).setBorderLeft(Border.NO_BORDER)
                .setTextAlignment(TextAlignment.CENTER));
        NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.FRANCE);
        double totalFacture = 0;
        for (Items item : invoice.getItems()) {
            infoProduit.addCell(new Cell().add(new Paragraph("item.getDesignation()"))
                    .setBorder(Border.NO_BORDER)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE)
                    .setBackgroundColor(new DeviceRgb(219, 215, 214)).setHeight(40f));
            infoProduit
                    .addCell(new Cell().add(new Paragraph("" + item.getQuantity()))
                            .setBorder(Border.NO_BORDER)
                            .setTextAlignment(TextAlignment.CENTER)
                            .setVerticalAlignment(VerticalAlignment.MIDDLE)
                            .setBackgroundColor(new DeviceRgb(219, 215, 214)));
            infoProduit.addCell(
                    new Cell().add(new Paragraph("" + formatter.format(item.getPriceHT())))
                            .setBorder(Border.NO_BORDER)
                            .setTextAlignment(TextAlignment.CENTER)
                            .setVerticalAlignment(VerticalAlignment.MIDDLE)
                            .setBackgroundColor(new DeviceRgb(219, 215, 214)));
            double totalLigne = item.getPriceHT() * item.getQuantity();
            totalFacture += totalLigne;
            infoProduit.addCell(
                    new Cell().add(new Paragraph("" + formatter.format(item.getPriceTTC())))
                            .setBorder(Border.NO_BORDER)
                            .setTextAlignment(TextAlignment.CENTER)
                            .setVerticalAlignment(VerticalAlignment.MIDDLE)
                            .setBackgroundColor(new DeviceRgb(219, 215, 214)));
        }
        return infoProduit;
    }

    public Table infoAmountTable(Invoice invoice) {
        /*
         * information total
         */
        float largeurInfoTotal[] = {
                560
        };
        NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.FRANCE);
        Table infoTotal = new Table(largeurInfoTotal);
        infoTotal.addCell(
                new Cell().add(new Paragraph("TOTAL HT: " + formatter.format(invoice.getAmount() / 1.2)))
                        .setBorder(Border.NO_BORDER)
                        .setTextAlignment(TextAlignment.RIGHT));
        infoTotal.addCell(new Cell().add(new Paragraph("TVA: " +
                formatter.format(invoice.getAmount() * 0.2)))
                .setBorder(Border.NO_BORDER)
                .setTextAlignment(TextAlignment.RIGHT));
        infoTotal.addCell(
                new Cell().add(new Paragraph(
                        "TOTAL TTC: " + formatter.format(invoice.getAmount())))
                        .setBorder(Border.NO_BORDER)
                        .setTextAlignment(TextAlignment.RIGHT));
        return infoTotal;
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
