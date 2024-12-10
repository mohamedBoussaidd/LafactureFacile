package Mboussaid.laFactureFacile.Services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

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

import Mboussaid.laFactureFacile.Models.Invoice;
import Mboussaid.laFactureFacile.Models.Items;
import Mboussaid.laFactureFacile.Models.User;
import Mboussaid.laFactureFacile.Models.ENUM.EStatusInvoice;
import Mboussaid.laFactureFacile.Models.GetDate;

@Service
public class PdfService {
        @Value("${file.upload-dir}")
        private String uploadDir;
        @Value("${tmpfile.upload-dir}")
        private String tmpUploadDir;

        private final String SUFFIXPDF = ".pdf";

        public File createPdf(Invoice invoice, User user) throws IOException {
                // String fileName = null;
                if (invoice == null || user == null) {
                        throw new IllegalArgumentException("Invoice and User must not be null");
                }
                String fileName = invoice.getStatus() == EStatusInvoice.CREER
                                ? "TMP" + user.getName() + user.getId() + invoice.getCustomerName()
                                                + invoice.getTmpInvoiceNumber() + this.SUFFIXPDF
                                : user.getName() + user.getId() + invoice.getCustomerName()
                                                + invoice.getInvoiceNumber() + this.SUFFIXPDF;
                File tempFile = invoice.getStatus() == EStatusInvoice.CREER
                                ? new File(this.tmpUploadDir, fileName)
                                : new File(this.uploadDir, fileName);

                try (FileOutputStream fos = new FileOutputStream(tempFile);
                                PdfWriter writer = new PdfWriter(fos);
                                PdfDocument pdf = new PdfDocument(writer);
                                Document document = new Document(pdf)) {
                        document.setFontSize(9);
                        Table headerInvoice = headerOfInvoiceTable(invoice);
                        Table infoSender = infoSenderTable(invoice);
                        Table infoCustomer = infoCustomerTable(invoice);
                        Table infoConfigInvoice = infoConfigInvoiceTable(invoice);
                        Table infoProduct = infoProductTable(invoice);
                        Table infoAmount = infoAmountTable(invoice);
                        Table infoFooter = infoFootTable(user);

                        document.add(headerInvoice);
                        document.add(infoSender);
                        document.add(infoCustomer);
                        document.add(infoConfigInvoice);
                        document.add(infoProduct);
                        document.add(infoAmount);
                        document.add(infoFooter.setFixedPosition(33, 20,
                                        document.getPdfDocument().getDefaultPageSize().getWidth()));

                        return tempFile; // Retourner le fichier PDF temporaire
                } catch (Exception e) {
                        e.printStackTrace();
                        throw new RuntimeException("Failed to create PDF document: " + e.getMessage(), e);
                } finally {
                        System.out.println("Document created at: " + tempFile.getAbsolutePath());
                }
        }

        public Table headerOfInvoiceTable(Invoice invoice) {
                float col = 280f;
                float columnWidth[] = { col, col };
                Table table = new Table(columnWidth);
                table.setFontColor(new DeviceRgb(100, 149, 237))
                                .setBorder(Border.NO_BORDER);
                table.addCell(new Cell().add(new Paragraph("FACTURE"))
                                .setTextAlignment(TextAlignment.LEFT)
                                .setVerticalAlignment(VerticalAlignment.TOP)
                                .setMarginTop(30f)
                                .setMarginBottom(30f)
                                .setFontSize(30f)
                                .setBorder(Border.NO_BORDER)).setHeight(100f);
                return table;
        }

        public Table infoSenderTable(Invoice invoice) {
                float colWidht[] = {
                                100, 310
                };
                Table infoSeller = new Table(colWidht).setHeight(70f);
                infoSeller.addCell(new Cell(0, 1).add(new Paragraph("Vendeur :"))
                                .setBold()
                                .setBorder(Border.NO_BORDER));
                // Changement : Ajout du nom de l'expediteur
                infoSeller.addCell(new Cell(0, 1)
                                .add(new Paragraph(
                                                invoice.getSellerName() + "\n" + invoice.getSellerEmail() + "\n"
                                                                + invoice.getSellerPhone()))
                                .setBorder(Border.NO_BORDER));
                return infoSeller;
        }

        public Table infoCustomerTable(Invoice invoice) {
                // information du client
                float colWidht[] = {
                                100, 310
                };
                Table infoCustomer = new Table(colWidht).setHeight(70f);
                infoCustomer.addCell(new Cell(0, 1).add(new Paragraph("Client :"))
                                .setBold()
                                .setBorder(Border.NO_BORDER));
                // Changement : Ajout du nom de l'expediteur
                infoCustomer.addCell(new Cell(0, 1)
                                .add(new Paragraph(invoice.getCustomerName() + "\n" + invoice.getCustomerEmail() + "\n"
                                                + invoice.getCustomerPhone()))
                                .setBorder(Border.NO_BORDER));
                return infoCustomer;
        }

        public Table infoConfigInvoiceTable(Invoice invoice) {
                // variable de configuration de la facture
                float colWidht[] = {
                                140, 140, 140, 140
                };
                Table infoFacture = new Table(colWidht).setHeight(50f);
                DateTimeFormatter formatterDate = DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.FRANCE);
                String expirationDate = invoice.getExpirationDate() != null
                                ? invoice.getExpirationDate().format(formatterDate)
                                : "Date non définie";
                String creationDate = invoice.getCreationDate() != null
                                ? invoice.getCreationDate().format(formatterDate)
                                : GetDate.getNow().format(formatterDate);
                String invoiceNumber = invoice.getInvoiceNumber() != null ? invoice.getInvoiceNumber()
                                : invoice.getTmpInvoiceNumber();
                String reference = invoice.getId() != null ? invoice.getId().toString() : "Référence non définie";

                infoFacture.addCell(new Cell().add(new Paragraph("No. Facture")).setBorder(Border.NO_BORDER).setBold()
                                .setTextAlignment(TextAlignment.LEFT));
                infoFacture.addCell(new Cell().add(new Paragraph("Création")).setBorder(Border.NO_BORDER).setBold()
                                .setTextAlignment(TextAlignment.LEFT));
                infoFacture.addCell(new Cell().add(new Paragraph("Expiration")).setBorder(Border.NO_BORDER).setBold()
                                .setTextAlignment(TextAlignment.LEFT));
                infoFacture.addCell(new Cell().add(new Paragraph("Référence")).setBorder(Border.NO_BORDER).setBold()
                                .setTextAlignment(TextAlignment.LEFT));
                // Changement : Ajout du numero de facture
                infoFacture.addCell(new Cell()
                                .add(new Paragraph(invoiceNumber))
                                .setBorder(Border.NO_BORDER));
                // Changement : Ajout de la date d'emission
                infoFacture.addCell(new Cell()
                                .add(new Paragraph(creationDate))
                                .setBorder(Border.NO_BORDER));
                // Changement : Ajout de la date d'expiration
                infoFacture.addCell(new Cell()
                                .add(new Paragraph(expirationDate))
                                .setBorder(Border.NO_BORDER));
                // Changement : Ajout de la reference
                infoFacture.addCell(new Cell()
                                .add(new Paragraph(reference))
                                .setBorder(Border.NO_BORDER));
                return infoFacture;
        }

        public Table infoProductTable(Invoice invoice) {
                float corpWidth[] = { 112, 112, 112, 112, 112 };
                Table infoProduit = new Table(corpWidth)
                                .setMarginTop(25f)
                                .setBorder(Border.NO_BORDER)
                                .setMarginBottom(20f);
                infoProduit.addCell(new Cell().add(new Paragraph("SERVICE")).setBorderRight(Border.NO_BORDER)
                                .setTextAlignment(TextAlignment.CENTER)
                                .setBackgroundColor(new DeviceRgb(100, 149, 237))
                                .setBold()
                                .setFontColor(new DeviceRgb(255, 250, 240)));
                infoProduit.addCell(new Cell().add(new Paragraph("QUANTITÉ")).setBorderRight(Border.NO_BORDER)
                                .setBorderLeft(Border.NO_BORDER)
                                .setTextAlignment(TextAlignment.CENTER)
                                .setBackgroundColor(new DeviceRgb(100, 149, 237))
                                .setBold()
                                .setFontColor(new DeviceRgb(255, 250, 240)));
                infoProduit.addCell(new Cell().add(new Paragraph("PRIX/UNITAIRE"))
                                .setBorderRight(Border.NO_BORDER)
                                .setBorderLeft(Border.NO_BORDER)
                                .setTextAlignment(TextAlignment.CENTER)
                                .setBackgroundColor(new DeviceRgb(100, 149, 237))
                                .setBold()
                                .setFontColor(new DeviceRgb(255, 250, 240)));
                infoProduit.addCell(new Cell().add(new Paragraph("TVA"))
                                .setBorderRight(Border.NO_BORDER)
                                .setBorderLeft(Border.NO_BORDER)
                                .setBold()
                                .setBackgroundColor(new DeviceRgb(100, 149, 237))
                                .setTextAlignment(TextAlignment.CENTER).setBold()
                                .setFontColor(new DeviceRgb(255, 250, 240)));
                infoProduit.addCell(new Cell().add(new Paragraph("PRIX/TTC"))
                                .setBorderLeft(Border.NO_BORDER)
                                .setTextAlignment(TextAlignment.CENTER)
                                .setBackgroundColor(new DeviceRgb(100, 149, 237))
                                .setBold()
                                .setFontColor(new DeviceRgb(255, 250, 240)));
                NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.FRANCE);
                for (Items item : invoice.getItems()) {
                        infoProduit.addCell(new Cell().add(new Paragraph(item.getProductName()))
                                        .setBorder(Border.NO_BORDER)
                                        .setTextAlignment(TextAlignment.CENTER)
                                        .setVerticalAlignment(VerticalAlignment.MIDDLE));
                        infoProduit.addCell(new Cell().add(new Paragraph("" + item.getQuantity()))
                                        .setBorder(Border.NO_BORDER)
                                        .setTextAlignment(TextAlignment.CENTER)
                                        .setVerticalAlignment(VerticalAlignment.MIDDLE));
                        infoProduit.addCell(new Cell().add(new Paragraph("" + formatter.format(item.getUnitPrice())))
                                        .setBorder(Border.NO_BORDER)
                                        .setTextAlignment(TextAlignment.CENTER)
                                        .setVerticalAlignment(VerticalAlignment.MIDDLE));
                        infoProduit.addCell(new Cell().add(new Paragraph("" + item.getTax() + "%"))
                                        .setBorder(Border.NO_BORDER)
                                        .setTextAlignment(TextAlignment.CENTER)
                                        .setVerticalAlignment(VerticalAlignment.MIDDLE));
                        infoProduit.addCell(new Cell().add(new Paragraph("" + formatter.format(item.getPriceTTC())))
                                        .setBorder(Border.NO_BORDER)
                                        .setTextAlignment(TextAlignment.CENTER)
                                        .setVerticalAlignment(VerticalAlignment.MIDDLE));
                }
                return infoProduit;
        }

        public Table infoAmountTable(Invoice invoice) {
                /*
                 * information total
                 */
                float largeurInfoTotal[] = {
                                500, 60
                };
                NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.FRANCE);
                Table infoTotal = new Table(largeurInfoTotal);
                infoTotal.addCell(new Cell().add(new Paragraph(
                                "Total HT: "))
                                .setBorder(Border.NO_BORDER)
                                .setBold()
                                .setTextAlignment(TextAlignment.RIGHT));
                infoTotal.addCell(new Cell().add(new Paragraph(
                                formatter.format(invoice.getAmountHT())))
                                .setBorder(Border.NO_BORDER)
                                .setTextAlignment(TextAlignment.RIGHT));
                infoTotal.addCell(new Cell().add(new Paragraph("Total TVA: "))
                                .setBorder(Border.NO_BORDER)
                                .setBold()
                                .setTextAlignment(TextAlignment.RIGHT));
                infoTotal.addCell(new Cell().add(new Paragraph(
                                formatter.format(invoice.getAmountTTC().intValue() -
                                                invoice.getAmountHT().intValue())))
                                .setBorder(Border.NO_BORDER)
                                .setTextAlignment(TextAlignment.RIGHT));
                infoTotal.addCell(new Cell().add(new Paragraph(
                                "Total TTC: "))
                                .setBorder(Border.NO_BORDER)
                                .setBold()
                                .setTextAlignment(TextAlignment.RIGHT));
                infoTotal.addCell(new Cell().add(new Paragraph(
                                formatter.format(invoice.getAmountTTC())))
                                .setBorder(Border.NO_BORDER)
                                .setTextAlignment(TextAlignment.RIGHT));
                return infoTotal;
        }

        public Table infoFootTable(User user) {
                float colWidht[] = {
                                262, 262
                };
                Table infoFooter = new Table(colWidht)
                                .setMaxWidth(525)
                                .setBackgroundColor(new DeviceRgb(100, 149, 237))
                                .setFontColor(new DeviceRgb(255, 250, 240));
                infoFooter.addCell(new Cell(0, 1).add(new Paragraph("Mon entreprise"))
                                .setBold()
                                .setBorder(Border.NO_BORDER));
                infoFooter.addCell(new Cell(0, 1)
                                .add(new Paragraph("Coordonnées"))
                                .setBold()
                                .setBorder(Border.NO_BORDER));
                infoFooter.addCell(new Cell(0, 1)
                                .add(new Paragraph(
                                                user.getAdresse() + "\n" + user.getPostalcode() + " " + user.getCity()
                                                                + "\n"
                                                                + "siret: " + user.getSiret()))
                                .setBorder(Border.NO_BORDER));
                infoFooter.addCell(new Cell(0, 1)
                                .add(new Paragraph(
                                                user.getName() + " " + user.getFirstname()
                                                                + "\n"
                                                                + user.getTelephone() + "\n" + user.getEmail()))
                                .setBorder(Border.NO_BORDER));
                return infoFooter;
        }

}
