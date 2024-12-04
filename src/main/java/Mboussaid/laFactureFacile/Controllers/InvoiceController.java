package Mboussaid.laFactureFacile.Controllers;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import Mboussaid.laFactureFacile.DTO.CustomResponseEntity;
import Mboussaid.laFactureFacile.DTO.Request.InvoiceForSendEmailRequest;
import Mboussaid.laFactureFacile.DTO.Request.InvoiceInfoRequest;
import Mboussaid.laFactureFacile.DTO.Request.InvoiceRequest;
import Mboussaid.laFactureFacile.Models.FileInfo;
import Mboussaid.laFactureFacile.Models.GetDate;
import Mboussaid.laFactureFacile.Models.Invoice;
import Mboussaid.laFactureFacile.Models.User;
import Mboussaid.laFactureFacile.Repository.FileInfoRepository;
import Mboussaid.laFactureFacile.Repository.InvoiceRepository;
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
    private final FileInfoRepository fileInfoRepository;
    private final InvoiceRepository invoiceRepository;

    public InvoiceController(InvoiceService invoiceService, FileStorageService fileStorageService,
            PdfService pdfService , FileInfoRepository fileInfoRepository, InvoiceRepository invoiceRepository) {
        this.invoiceService = invoiceService;
        this.fileStorageService = fileStorageService;
        this.pdfService = pdfService;
        this.fileInfoRepository = fileInfoRepository;
        this.invoiceRepository = invoiceRepository;
    }

    @PostMapping("/addInvoice")
    public CustomResponseEntity<?> addInvoice(@RequestBody InvoiceRequest invoiceRequest) throws IOException {
        Map<String, Object> mapresult = this.invoiceService.createInvoice(invoiceRequest);

        Invoice invoice = (Invoice) mapresult.get("invoice");
        User user = (User) mapresult.get("user");

        File pdfFile = this.pdfService.createPdf(invoice, user);

        fileStorageService.storeFile(pdfFile);

        FileInfo fileInfo = new FileInfo();
        fileInfo.setName(pdfFile.getName());
        fileInfo.setType(fileInfo.getExtension(pdfFile.getName()));
        fileInfo.setCreationDate(GetDate.getNow());
        if (fileInfo.getCreationDate() == null) {
            return CustomResponseEntity.error(HttpStatus.BAD_REQUEST.value(),
                    "La date de création du fichier est invalid");
        }
        fileInfo.setExpirationDate(fileInfo.getCreationDate().plus(10, java.time.temporal.ChronoUnit.DAYS));
        FileInfo fileInfobdd = this.fileInfoRepository.save(fileInfo);

        invoice.setFile(fileInfobdd);
        this.invoiceRepository.save(invoice);

        return CustomResponseEntity.successWithoutDataHidden(HttpStatus.CREATED.value(), "La facture a été créée avec succès !!");
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
    public CustomResponseEntity<?> getInvoiceInfoByUser(@PathVariable Integer id) {
        return this.invoiceService.getInvoiceInfoByUser(id);
    }

    @PostMapping("updateInvoice")
    public CustomResponseEntity<?> updateInvoiceStatus(@RequestBody InvoiceInfoRequest invoiceInfo) {
        return this.invoiceService.updateInvoice(invoiceInfo);
    }
    @PostMapping("sendInvoice")
    public CustomResponseEntity<?> sendInvoice(@RequestBody InvoiceForSendEmailRequest invoice) throws IOException {
        return this.invoiceService.sendInvoice(invoice);
    }
    @PostMapping("relaunchCustomer")
    public CustomResponseEntity<?> relaunchCustomer(@RequestBody InvoiceForSendEmailRequest invoice) {
        return this.invoiceService.relaunchCustomer(invoice);
    }

}
