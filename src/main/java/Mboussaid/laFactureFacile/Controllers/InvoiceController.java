package Mboussaid.laFactureFacile.Controllers;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InvoiceController {
    

    public InvoiceController() {
    }

    @PostMapping("/addInvoice")
    public String addInvoice() {
        return "Invoice added";
    }
}
