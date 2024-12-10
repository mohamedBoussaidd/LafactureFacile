package Mboussaid.laFactureFacile.Models;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "items")
public class Items {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "product_name", nullable = false)
    private String productName;
    @Column(name = "unit_price", nullable = false)
    private BigDecimal unitPrice;
    @Column(name = "amount_of_tax", nullable = false)
    private BigDecimal amountOfTaxe;
    @Column(name = "price_ttc", nullable = false)
    private BigDecimal priceTTC;
    @Column(name = "tax", nullable = false)
    private BigDecimal tax;
    @Column(name = "quantity", nullable = false)
    private Integer quantity;
    @Column(name = "description")
    private String description;
    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;
}
