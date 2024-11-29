package Mboussaid.laFactureFacile.DTO.Request;

import java.util.List;

import Mboussaid.laFactureFacile.Models.ENUM.EStatusInvoice;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class InvoiceRequest {
    
    private String customerName;
    private String customerEmail;
    private String customerAddress;
    private String customerPhone;
    private String creationDate;
    private String expirationDate;
    private String invoiceNumber;
    private EStatusInvoice status;
    private List<ItemsRequest> items;
    private Integer amount;
}
