package Mboussaid.laFactureFacile.Repository;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import Mboussaid.laFactureFacile.Models.Invoice;
import Mboussaid.laFactureFacile.Models.User;
import Mboussaid.laFactureFacile.Models.ENUM.EStatusInvoice;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Integer> {
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    Optional<Invoice> findByCustomerName(String invoiceCustomer);

    Optional<Invoice> findByCreationDate(ZonedDateTime invoiceDate);

    Optional<Invoice> findByAmountTTC(BigDecimal invoiceAmount);

    List<Invoice> findByUser(User user);

    void deleteAllByInvoiceNumber(String invoiceNumber);

    void deleteByTmpInvoiceNumber(String tmpInvoiceNumber);

    @Modifying
    @Query("UPDATE Invoice i SET i.status = :newStatus WHERE i.status = 'CREER'")
    int updateStatusForCreatedInvoices(EStatusInvoice newStatus);

    @Modifying
    @Query("UPDATE Invoice i SET i.status = :newStatus WHERE i.status = 'ATTENTE' AND i.expirationDate < CURRENT_TIMESTAMP")
    int updateStatusInvoiceForAttenteAtRetard(EStatusInvoice newStatus);

    @Modifying
    @Query("SELECT i FROM Invoice i WHERE i.status != :status AND i.user.id = :id")
    List<Invoice> findInvoiceWithoutThisStatus(@Param("status") EStatusInvoice statusInvoice, Integer id);
    
    @Modifying
    @Query("DELETE FROM Invoice i WHERE i.status = :status AND i.expirationDate < CURRENT_TIMESTAMP")
    int deleteInvoicewhitStatusAndExpir(EStatusInvoice status);

    @Modifying
    @Query("SELECT i FROM Invoice i WHERE i.status = :status AND i.expirationDate < CURRENT_TIMESTAMP")
    List<Invoice> findAllInvoiceCreerExpir(EStatusInvoice status);
}
