package Mboussaid.laFactureFacile.Controllers;

import java.io.IOException;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import Mboussaid.laFactureFacile.DTO.CustomResponseEntity;
import Mboussaid.laFactureFacile.DTO.Request.InvoiceForSendEmailRequest;
import Mboussaid.laFactureFacile.DTO.Request.InvoiceRequest;
import Mboussaid.laFactureFacile.Services.InvoiceService;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

@RestController
public class InvoiceController {

    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @PostMapping("/addInvoice")
    public CustomResponseEntity<?> addInvoice(@RequestBody InvoiceRequest invoiceRequest) throws IOException {
        return this.invoiceService.createInvoice(invoiceRequest);
    }

    @GetMapping("getInvoice/{IndexOfInvoice}")
    @ResponseBody
    public ResponseEntity<Resource> displayInvoice(@PathVariable Integer IndexOfInvoice) {
        Resource file = this.invoiceService.displayInvoice(IndexOfInvoice);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
                .body(file);
    }

    @GetMapping("getInvoiceInfoById/{id}")
    @ResponseBody
    public CustomResponseEntity<?> getInvoiceByUser(@PathVariable Integer id) {
        return this.invoiceService.getInvoiceByUser(id);
    }

    @PostMapping("updateInvoice")
    public CustomResponseEntity<?> updateInvoiceStatus(@RequestBody InvoiceRequest invoice) {
        return this.invoiceService.updateInvoice(invoice);
    }
    @PostMapping("sendInvoice")
    public CustomResponseEntity<?> sendInvoice(@RequestBody InvoiceForSendEmailRequest invoice) throws IOException {
        return this.invoiceService.sendInvoice(invoice);
    }
    @PostMapping("relaunchCustomer")
    public CustomResponseEntity<?> relaunchCustomer(@RequestBody InvoiceForSendEmailRequest invoice) {
        return this.invoiceService.relaunchCustomer(invoice);
    }
    @DeleteMapping("deleteInvoice/{id}")
    public CustomResponseEntity<?> deleteInvoice(@PathVariable Integer id) {
        return this.invoiceService.deleteInvoice(id);
    }

}
