package Mboussaid.laFactureFacile.Services;


import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.border.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.VerticalAlignment;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import Mboussaid.laFactureFacile.Models.Invoice;

@Service
public class PdfService {

    public ResponseEntity<?> createPdf(Invoice invoice) {
        String content = // header de la facture
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
        table.addCell(new Cell().add(new Paragraph(invoice.getCustomerName +"\n"+"DESCRIPTION ENTREPRISE"+"\n"+"NUMERO D ENTREPRISE"))
                        .setTextAlignment(TextAlignment.RIGHT)
                        .setVerticalAlignment(VerticalAlignment.MIDDLE)
                        .setMarginTop(30f)
                        .setMarginBottom(30f)
                        .setBorder(Border.NO_BORDER)
                        .setFontSize(10f)
                        .setMarginRight(20f));
                        // information de l expediteur
        float colWidht[] = {
                        80, 300, 100, 80
        };
        Table infoFacture = new Table(colWidht);
        infoFacture.addCell(new Cell(0, 4).add(new Paragraph("Envoyer par : "))
                        .setBold()
                        .setBorder(Border.NO_BORDER));
        infoFacture.addCell(new Cell().add(new Paragraph("Nom : ")).setBorder(Border.NO_BORDER));
        infoFacture.addCell(new Cell().add(new Paragraph(invoice.getCustomerName()))
                        .setBorder(Border.NO_BORDER));
        infoFacture.addCell(
                        new Cell().add(new Paragraph("No. Facture : ")).setBorder(Border.NO_BORDER));
        infoFacture.addCell(
                        new Cell().add(new Paragraph("" + "facture.getNumeroFacture()"))
                                        .setBorder(Border.NO_BORDER));
        infoFacture.addCell(new Cell().add(new Paragraph("No. Siret : ")).setBorder(Border.NO_BORDER));
        infoFacture.addCell(
                        new Cell().add(new Paragraph("" + "invoice.getCustomerNumeroSiret()"))
                                        .setBorder(Border.NO_BORDER));
        DateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd", Locale.FRANCE);
        infoFacture.addCell(new Cell().add(new Paragraph("Date : ")).setBorder(Border.NO_BORDER));
        infoFacture
                        .addCell(new Cell()
                                        .add(new Paragraph(""
                                                        + dateFormat.format(facture.getDateCreation())))
                                        .setBorder(Border.NO_BORDER));
        infoFacture.addCell(new Cell().add(new Paragraph("E-mail : ")).setBorder(Border.NO_BORDER));
        infoFacture.addCell(new Cell().add(new Paragraph(invoice.getCustomerEmail()))
                        .setBorder(Border.NO_BORDER));
        infoFacture.addCell(new Cell().add(new Paragraph("No. Tel : ")).setBorder(Border.NO_BORDER));
        infoFacture.addCell(new Cell().add(new Paragraph("" + "invoice.getCustomerNumber()"))
                        .setBorder(Border.NO_BORDER));
        // corp de la facture
        float corpWidth[] = { 140, 140, 140, 140 };
        Table infoProduit = new Table(corpWidth).setMarginTop(25f);
        infoProduit.addCell(new Cell().add(new Paragraph("SERVICE")).setBorderRight(Border.NO_BORDER)
                        .setTextAlignment(TextAlignment.CENTER));
        infoProduit.addCell(new Cell().add(new Paragraph("QTÉ")).setBorderRight(Border.NO_BORDER)
                        .setBorderLeft(Border.NO_BORDER).setTextAlignment(TextAlignment.CENTER));
        infoProduit.addCell(new Cell().add(new Paragraph("PRIX")).setBorderRight(Border.NO_BORDER)
                        .setBorderLeft(Border.NO_BORDER).setTextAlignment(TextAlignment.CENTER));
        infoProduit.addCell(new Cell().add(new Paragraph("TOTAL")).setBorderLeft(Border.NO_BORDER)
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
                                new Cell().add(new Paragraph("" + formatter.format(totalLigne)))
                                                .setBorder(Border.NO_BORDER)
                                                .setTextAlignment(TextAlignment.CENTER)
                                                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                                                .setBackgroundColor(new DeviceRgb(219, 215, 214)));
        }
         /*
                         * information total
                         */
                        float largeurInfoTotal[] = {
                            560
            };
            Table infoTotal = new Table(largeurInfoTotal);
            Integer total = 1500;
            infoTotal.addCell(
                            new Cell().add(new Paragraph("TOTAL HT: " + formatter.format(totalFacture)))
                                            .setBorder(Border.NO_BORDER)
                                            .setTextAlignment(TextAlignment.RIGHT));
            infoTotal.addCell(new Cell().add(new Paragraph("TVA: " + formatter.format(totalFacture * 0.2)))
                            .setBorder(Border.NO_BORDER)
                            .setTextAlignment(TextAlignment.RIGHT));
            infoTotal.addCell(
                            new Cell().add(new Paragraph(
                                            "TOTAL TTC: " + formatter.format(invoice.getPriceTTC())))
                                            .setBorder(Border.NO_BORDER)
                                            .setTextAlignment(TextAlignment.RIGHT));
        /*
        * information destinataire
        */
       float largeurInfoDestinataire[] = {560};
       Table infoDestinataire = new Table(largeurInfoDestinataire);   
       infoDestinataire.addCell(new Cell(0, 4).add(new Paragraph("Facturer à : ")).setBold().setBorder(Border.NO_BORDER));
       infoDestinataire.addCell(new Cell(0, 4).add(new Paragraph(invoice.getCustomerName())).setBorder(Border.NO_BORDER));
       infoDestinataire.addCell(new Cell(0, 4).add(new Paragraph("ADRESSE DU CLIENT")).setBorder(Border.NO_BORDER));
       infoDestinataire.addCell(new Cell(0, 4).add(new Paragraph("COMPLEMENT D ADRESS")).setBorder(Border.NO_BORDER));
       infoDestinataire.addCell(new Cell(0, 4).add(new Paragraph(invoice.getCustomerEmail())).setBorder(Border.NO_BORDER));
       infoDestinataire.addCell(new Cell(0, 4).add(new Paragraph("NUMERO DU CLIENT")).setBorder(Border.NO_BORDER));


        Document document = new Document();
        String filePath = "//Users/momo/Desktop/appFactureSimple/backend/laFactureFacile/src/main/resources/telechargements/ " + invoice.getCustomerName() + ".pdf";
        try {
            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();
            document.add(new Paragraph(content));
        } catch (FileNotFoundException | DocumentException e) {
            e.printStackTrace();
        } finally {
            document.close();
        }
    }
}
