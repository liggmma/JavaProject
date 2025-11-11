package luxdine.example.luxdine.service.auth;

import com.vonage.client.VonageClient;
import com.vonage.client.sms.MessageStatus;
import com.vonage.client.sms.SmsSubmissionResponse;
import com.vonage.client.sms.messages.TextMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VonageSmsGateway implements SmsGateway {
    private final VonageClient client;
    @Value("${vonage.senderId}") private String senderId;

    @Override
    public void send(String phoneE164, String message) {
        TextMessage sms = new TextMessage(senderId, phoneE164, message);
        SmsSubmissionResponse res = client.getSmsClient().submitMessage(sms);
        var m = res.getMessages().get(0);
        if (m.getStatus() != MessageStatus.OK) {
            throw new IllegalStateException("Vonage SMS error: " + m.getErrorText());
        }
    }
}
