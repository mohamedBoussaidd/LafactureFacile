package Mboussaid.laFactureFacile.Controllers;

import java.io.IOException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import Mboussaid.laFactureFacile.DTO.Request.InvoiceRequest;
import Mboussaid.laFactureFacile.Services.FileStorageService;
import Mboussaid.laFactureFacile.Services.InvoiceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;



@RestController
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final FileStorageService fileStorageService;

    public InvoiceController(InvoiceService invoiceService, FileStorageService fileStorageService) {
        this.invoiceService = invoiceService;
        this.fileStorageService = fileStorageService;
    }

    @PostMapping("/addInvoice")
    public ResponseEntity<?> addInvoice(@RequestBody InvoiceRequest invoice) throws IOException {
        return this.invoiceService.createInvoice(invoice);
    }
    @GetMapping("getInvoice")
    @ResponseBody
    public  ResponseEntity<Resource> displayInvoice() {
        Resource file = this.invoiceService.displayInvoice();
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
                .body(file);
    }
}
