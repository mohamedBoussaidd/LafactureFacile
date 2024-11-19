package Mboussaid.laFactureFacile.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import Mboussaid.laFactureFacile.Models.InvoiceInfo;

@Repository
public interface InvoiceInfoRepository extends JpaRepository<InvoiceInfo, Integer> {
    Optional<InvoiceInfo> findByInvoiceNumber(String invoiceNumber);
    Optional<InvoiceInfo> findByInvoiceCustomer(String invoiceCustomer);
    Optional<InvoiceInfo> findByInvoiceDate(String invoiceDate);
    Optional<InvoiceInfo> findByInvoiceAmount(String invoiceAmount);
    void deleteAllByInvoiceNumber(String invoiceNumber);
    void deleteAllByInvoiceCustomer(String invoiceCustomer);
    void deleteAllByInvoiceDate(String invoiceDate);
}
