package luxdine.example.luxdine.domain.payment.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SepayWebhookPayload {
    private Long id;                 // 26876129
    private String gateway;          // "MBBank"
    private String transactionDate;  // "2025-10-19 12:32:00"
    private String accountNumber;    // "0000139713489"
    private String subAccount;       // null
    private String code;             // null
    private String content;          // "...PAY1835CE...sh20"
    private String transferType;     // "in"
    private String description;      // full SMS
    private Long transferAmount;     // 10000
    private String referenceCode;    // "FT25293147844105"
    private Long accumulated;        // 0
}