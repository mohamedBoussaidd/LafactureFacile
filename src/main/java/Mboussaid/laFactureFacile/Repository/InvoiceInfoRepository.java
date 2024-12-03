package Mboussaid.laFactureFacile.Repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import Mboussaid.laFactureFacile.Models.InvoiceInfo;
import Mboussaid.laFactureFacile.Models.User;
import Mboussaid.laFactureFacile.Models.ENUM.EStatusInvoice;

@Repository
public interface InvoiceInfoRepository extends JpaRepository<InvoiceInfo, Integer> {
    Optional<InvoiceInfo> findByInvoiceNumber(String invoiceNumber);
    Optional<InvoiceInfo> findByInvoiceCustomer(String invoiceCustomer);
    Optional<InvoiceInfo> findByInvoiceDate(ZonedDateTime invoiceDate);
    Optional<InvoiceInfo> findByInvoiceAmount(String invoiceAmount);
    List<InvoiceInfo> findByUser(User user);
    void deleteAllByInvoiceNumber(String invoiceNumber);
    void deleteAllByInvoiceCustomer(String invoiceCustomer);
    void deleteAllByInvoiceDate(ZonedDateTime invoiceDate);

    @Modifying
    @Query("UPDATE InvoiceInfo i SET i.status = :newStatus WHERE i.status = 'CREER'")
    int updateStatusForCreatedInvoices(EStatusInvoice newStatus);

    @Modifying
    @Query("UPDATE InvoiceInfo i SET i.status = :newStatus WHERE i.status = 'ATTENTE' AND i.invoiceExpirDate < CURRENT_TIMESTAMP")
    int updateStatusInvoiceForAttenteAtRetard(EStatusInvoice newStatus);
}
