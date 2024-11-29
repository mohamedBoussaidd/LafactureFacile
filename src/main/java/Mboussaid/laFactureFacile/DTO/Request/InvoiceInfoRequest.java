package Mboussaid.laFactureFacile.DTO.Request;

import Mboussaid.laFactureFacile.Models.ENUM.EStatusInvoice;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class InvoiceInfoRequest {
    private Integer id;
    private String invoiceNumber;
    private String invoiceCustomer;
    private String invoiceDate;
    private String invoiceExpirDate;
    private String invoiceAmount;
    private EStatusInvoice status;
}
