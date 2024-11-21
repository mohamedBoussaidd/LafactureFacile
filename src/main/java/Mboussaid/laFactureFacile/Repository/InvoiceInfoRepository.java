package Mboussaid.laFactureFacile.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import Mboussaid.laFactureFacile.Models.InvoiceInfo;
import Mboussaid.laFactureFacile.Models.User;

@Repository
public interface InvoiceInfoRepository extends JpaRepository<InvoiceInfo, Integer> {
    Optional<InvoiceInfo> findByInvoiceNumber(String invoiceNumber);
    Optional<InvoiceInfo> findByInvoiceCustomer(String invoiceCustomer);
    Optional<InvoiceInfo> findByInvoiceDate(String invoiceDate);
    Optional<InvoiceInfo> findByInvoiceAmount(String invoiceAmount);
    List<InvoiceInfo> findByUser(User user);
    void deleteAllByInvoiceNumber(String invoiceNumber);
    void deleteAllByInvoiceCustomer(String invoiceCustomer);
    void deleteAllByInvoiceDate(String invoiceDate);
}
