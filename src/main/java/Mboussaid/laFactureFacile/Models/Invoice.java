package Mboussaid.laFactureFacile.Models;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Setter;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;

import Mboussaid.laFactureFacile.Models.ENUM.EStatusInvoice;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "invoice")
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "invoice_number")
    private String invoiceNumber;
    @Column(name = "tmp_invoice_number", nullable = false)
    private String tmpInvoiceNumber;
    @Column(name = "seller_name", nullable = false)
    private String sellerName;
    @Column(name = "seller_email", nullable = false)
    private String sellerEmail;
    @Column(name = "seller_address", nullable = false)
    private String sellerAddress;
    @Column(name = "seller_siret", nullable = false)
    private String sellerSiret;
    @Column(name = "seller_phone", nullable = false)
    private String sellerPhone;
    @Column(name = "customer_name", nullable = false)
    private String customerName;
    @Column(name = "customer_email", nullable = false)
    private String customerEmail;
    @Column(name = "customer_address", nullable = false)
    private String customerAddress;
    @Column(name = "customer_phone", nullable = false)
    private String customerPhone;
    @Column(name = "creation_date", nullable = false)
    private ZonedDateTime creationDate;
    @Column(name = "expiration_date", nullable = false)
    private ZonedDateTime expirationDate;
    @Column(name = "amount_ht", nullable = false)
    private BigDecimal amountHT;
    @Column(name = "amount_ttc", nullable = false)
    private BigDecimal amountTTC;
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private EStatusInvoice status;
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "file_id", referencedColumnName = "id", nullable = false)
    private FileInfo file;
    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Items> items;

}
