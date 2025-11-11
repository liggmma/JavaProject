package luxdine.example.luxdine.service.auth;

public interface SmsGateway {
    void send(String phoneE164, String message) throws Exception;
}
