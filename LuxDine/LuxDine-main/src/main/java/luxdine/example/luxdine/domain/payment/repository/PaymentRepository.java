package luxdine.example.luxdine.domain.payment.repository;


import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import luxdine.example.luxdine.domain.payment.entity.Payments;
import luxdine.example.luxdine.domain.payment.enums.PaymentMethod;
import luxdine.example.luxdine.domain.payment.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payments,Long> {
    Optional<Payments> findById(Long id);
    List<Payments> findByStatusAndMethod(PaymentStatus status, PaymentMethod method);
    List<Payments> findByMethodAndStatusAndCreatedDateBefore(PaymentMethod method, PaymentStatus status, Instant createdDate);
    List<Payments> findByStatus(PaymentStatus status);
    List<Payments> findByStatusAndCreatedDateBetween(
            PaymentStatus status, Instant startDate, Instant endDate
    );
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({ @QueryHint(name = "javax.persistence.lock.timeout", value = "3000") }) // ms, optional
    @Query("""
        select p
        from Payments p
        where p.status = :status
          and p.method = :method
          and function('REPLACE', upper(p.referenceCode), '-', '') = :refNorm
          and function('ROUND', p.amount, 0) = :amount
    """)
    Optional<Payments> findByRefNormAndAmountForUpdate(
            @Param("refNorm") String refNorm,
            @Param("amount") double amount,
            @Param("method") PaymentMethod method,
            @Param("status") PaymentStatus status
    );
}
