package Mboussaid.laFactureFacile.DTO.Response;

import java.time.ZonedDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import Mboussaid.laFactureFacile.Models.ENUM.EStatusInvoice;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class InvoiceDTO {
    
    private Integer id;
    private String invoiceNumber;
    private String invoiceCustomer;
    private String invoiceCustomerEmail;
    private ZonedDateTime invoiceDate;
    private ZonedDateTime invoiceExpirDate;
    private String invoiceAmount;
    private EStatusInvoice status;
}

