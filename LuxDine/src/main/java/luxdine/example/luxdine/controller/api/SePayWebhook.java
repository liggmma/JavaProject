package luxdine.example.luxdine.controller.api;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import luxdine.example.luxdine.domain.payment.dto.request.SepayWebhookPayload;
import luxdine.example.luxdine.domain.payment.entity.Payments;
import luxdine.example.luxdine.domain.payment.enums.PaymentMethod;
import luxdine.example.luxdine.domain.payment.enums.PaymentStatus;
import luxdine.example.luxdine.service.payment.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequestMapping("/webhook")
public class SePayWebhook {
    final PaymentService paymentService;

    @PostMapping("/sepay")
    public ResponseEntity<String> handle(@RequestBody SepayWebhookPayload body) {
        if (!"in".equalsIgnoreCase(body.getTransferType())) {
            return ResponseEntity.ok("ignored: not money-in");
        }

        String content = body.getContent() == null ? "" : body.getContent();
        Matcher m = Pattern.compile("\\bPAY[12][-_A-Za-z0-9]{6,}\\b").matcher(content);
        if (!m.find()) return ResponseEntity.ok("no ref token in content");

        // chuẩn hoá
        String norm = m.group().replaceAll("[^A-Za-z0-9]", "");

        // >>> Truy vấn duy nhất theo mã & amount & status, có PESSIMISTIC_WRITE <<<
        Payments p = paymentService.findPendingQrByRefAndAmountForUpdate(norm, body.getTransferAmount());
        if (p == null) return ResponseEntity.ok("no match");

        int type = norm.startsWith("PAY2") ? 2 : 1;
        paymentService.markPaymentCompleted(p, String.valueOf(body.getId()), type);
        return ResponseEntity.ok("ok");
    }

}
