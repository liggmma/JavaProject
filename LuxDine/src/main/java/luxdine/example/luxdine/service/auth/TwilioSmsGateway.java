package luxdine.example.luxdine.service.auth;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
public class TwilioSmsGateway implements SmsGateway {

    public TwilioSmsGateway(
            @Value("${twilio.accountSid}") String sid,
            @Value("${twilio.authToken}")  String token
    ){
        Twilio.init(sid, token);
    }

    @Override
    public void send(String toE164, String message) {
        Message.creator(
                        new com.twilio.type.PhoneNumber("whatsapp:" + toE164),
                        new com.twilio.type.PhoneNumber("whatsapp:+14155238886"),
                        message
                )
                .create();

    }

}
