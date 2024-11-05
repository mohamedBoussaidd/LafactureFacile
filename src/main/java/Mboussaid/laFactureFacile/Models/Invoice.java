package Mboussaid.laFactureFacile.Models;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Setter;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
// import java.time.Instant;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String invoiceNumber;
    private String sellerName;
    private String sellerEmail;
    private String sellerAddress;
    private String sellerSiret;
    private String sellerPhone;
    private String customerName;
    private String customerEmail;
    private String customerAddress;
    private String customerPhone;
    private String creationDate;
    private String expirationDate;
    private List<Items> items;
    private BigDecimal amountHT;
    private BigDecimal amountTTC;
}
