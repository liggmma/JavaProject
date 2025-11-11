package luxdine.example.luxdine.common.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import luxdine.example.luxdine.domain.payment.entity.Payments;
import luxdine.example.luxdine.domain.payment.enums.PaymentMethod;
import luxdine.example.luxdine.domain.payment.enums.PaymentStatus;
import luxdine.example.luxdine.service.payment.PaymentService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentExpiryScheduler {
    private final PaymentService paymentService;

    // TTL = 10 phút
    private static final Duration TTL = Duration.ofMinutes(5);

    // Quét mỗi 3 giây sau khi job trước kết thúc (fixedDelay)
    @Scheduled(fixedRate = 3000)
    public void sweepTimeouts() {
        Instant threshold = Instant.now().minus(TTL);
        List<Payments> expired = paymentService
                .findByMethodAndStatusAndCreatedDateBefore(PaymentMethod.QR,
                        PaymentStatus.PENDING, threshold);
        for (Payments p : expired) {
            paymentService.markPaymentFailed(p, "TIMEOUT");
        }
        log.info("Successfully mark " + expired.size() + " payments expiry");
    }
}