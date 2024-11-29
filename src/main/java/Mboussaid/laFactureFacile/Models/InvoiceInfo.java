package Mboussaid.laFactureFacile.Models;

import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "invoice_info")
public class InvoiceInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "invoice_number")
    private String invoiceNumber;
    @Column(name = "invoice_customer")
    private String invoiceCustomer;
    @Column(name = "invoice_customer_email") 
    private String invoiceCustomerEmail;
    @Column(name = "invoice_date")
    private ZonedDateTime invoiceDate;
    @Column(name = "invoice_expir_date")
    private ZonedDateTime invoiceExpirDate;
    @Column(name = "invoice_amount")
    private String invoiceAmount;
    @Column(name = "status")
    private String status;
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "file_id", referencedColumnName = "id")
    private FileInfo file;
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;
}
