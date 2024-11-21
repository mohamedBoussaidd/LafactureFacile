package Mboussaid.laFactureFacile.Controllers;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import Mboussaid.laFactureFacile.DTO.Request.InvoiceRequest;
import Mboussaid.laFactureFacile.Models.Invoice;
import Mboussaid.laFactureFacile.Models.InvoiceInfo;
import Mboussaid.laFactureFacile.Models.User;
import Mboussaid.laFactureFacile.Services.FileStorageService;
import Mboussaid.laFactureFacile.Services.InvoiceService;
import Mboussaid.laFactureFacile.Services.PdfService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

@RestController
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final FileStorageService fileStorageService;
    private final PdfService pdfService;

    public InvoiceController(InvoiceService invoiceService, FileStorageService fileStorageService,
            PdfService pdfService) {
        this.invoiceService = invoiceService;
        this.fileStorageService = fileStorageService;
        this.pdfService = pdfService;
    }

    @PostMapping("/addInvoice")
    public ResponseEntity<?> addInvoice(@RequestBody InvoiceRequest invoice) throws IOException {
        Map<String, Object> mapresult = this.invoiceService.createInvoice(invoice);

        Invoice invoiceResult = (Invoice) mapresult.get("invoice");
        User principalUserResulat = (User) mapresult.get("user");
        File pdfFile = this.pdfService.createPdf(invoiceResult, principalUserResulat);
        fileStorageService.storeFile(pdfFile);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Facture créée avec succès");
        response.put("filename", pdfFile.getName());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("getInvoice/{filename}")
    @ResponseBody
    public ResponseEntity<Resource> displayInvoice(@PathVariable String filename) {
        Resource file = this.invoiceService.displayInvoice(filename);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
                .body(file);
    }
    @GetMapping("getInvoiceInfoById/{id}")
    @ResponseBody
    public ResponseEntity<?> getInvoiceInfoById(@PathVariable Integer id) {
        ResponseEntity<?> invoiceInfo = this.invoiceService.getInvoiceInfoByUser(id);
        return new ResponseEntity<>(invoiceInfo, HttpStatus.OK);
    }

}
