package com.example.ara.service;

import com.example.ara.model.PushSubscription;
import com.example.ara.model.User;
import com.example.ara.repository.PushSubscriptionRepository;
import jakarta.annotation.PostConstruct;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.interfaces.ECPrivateKey;
import org.bouncycastle.jce.interfaces.ECPublicKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Security;
import java.util.Base64;
import java.util.List;

@Service
public class PushNotificationService {

    private static final Logger log = LoggerFactory.getLogger(PushNotificationService.class);

    private final PushSubscriptionRepository subscriptionRepo;

    @Value("${ara.vapid.public-key:}")
    private String publicKey;

    @Value("${ara.vapid.private-key:}")
    private String privateKey;

    @Value("${ara.vapid.subject:mailto:ema.georgiev@gmail.com}")
    private String subject;

    private PushService pushService;
    private boolean enabled = false;

    public PushNotificationService(PushSubscriptionRepository subscriptionRepo) {
        this.subscriptionRepo = subscriptionRepo;
    }

    @PostConstruct
    void init() {
        Security.addProvider(new BouncyCastleProvider());

        if (publicKey.isBlank() || privateKey.isBlank()) {
            logGeneratedKeys();
            return;
        }

        try {
            pushService = new PushService(publicKey, privateKey, subject);
            enabled = true;
            log.info("Push notifications enabled");
        } catch (Exception e) {
            log.error("Failed to initialize push service — push notifications disabled", e);
        }
    }

    public boolean isEnabled() { return enabled; }

    public String getPublicKey() { return enabled ? publicKey : null; }

    @Async
    public void sendToUser(User user, String title, String body, String urlPath) {
        if (!enabled) return;

        List<PushSubscription> subs = subscriptionRepo.findByUserId(user.getId());
        String payload = """
            {"title":"%s","body":"%s","icon":"/icons/icon-192.svg","url":"%s"}
            """.formatted(
                title.replace("\"", "\\\""),
                body.replace("\"", "\\\""),
                urlPath
            ).strip();

        for (PushSubscription sub : subs) {
            try {
                Notification notification = new Notification(
                    sub.getEndpoint(),
                    sub.getP256dh(),
                    sub.getAuth(),
                    payload.getBytes()
                );
                pushService.send(notification);
            } catch (Exception e) {
                log.warn("Push delivery failed for subscription {}: {}", sub.getId(), e.getMessage());
                // Remove stale subscriptions (410 Gone means browser unsubscribed)
                if (e.getMessage() != null && e.getMessage().contains("410")) {
                    subscriptionRepo.deleteByEndpoint(sub.getEndpoint());
                }
            }
        }
    }

    private void logGeneratedKeys() {
        try {
            ECNamedCurveParameterSpec spec = ECNamedCurveTable.getParameterSpec("prime256v1");
            KeyPairGenerator gen = KeyPairGenerator.getInstance("ECDH", "BC");
            gen.initialize(spec);
            KeyPair pair = gen.generateKeyPair();

            byte[] pubBytes = ((ECPublicKey) pair.getPublic()).getQ().getEncoded(false);
            String genPublic = Base64.getUrlEncoder().withoutPadding().encodeToString(pubBytes);

            byte[] privRaw = ((ECPrivateKey) pair.getPrivate()).getD().toByteArray();
            byte[] privBytes = new byte[32];
            if (privRaw.length >= 32) {
                System.arraycopy(privRaw, privRaw.length - 32, privBytes, 0, 32);
            } else {
                System.arraycopy(privRaw, 0, privBytes, 32 - privRaw.length, privRaw.length);
            }
            String genPrivate = Base64.getUrlEncoder().withoutPadding().encodeToString(privBytes);

            log.warn("══════════════════════════════════════════════════════════");
            log.warn("VAPID keys not set — push notifications are DISABLED");
            log.warn("Add these to your .env file and restart:");
            log.warn("  VAPID_PUBLIC_KEY={}", genPublic);
            log.warn("  VAPID_PRIVATE_KEY={}", genPrivate);
            log.warn("  VAPID_SUBJECT=mailto:ema.georgiev@gmail.com");
            log.warn("══════════════════════════════════════════════════════════");
        } catch (Exception e) {
            log.error("Could not generate VAPID keys for hint", e);
        }
    }
}
