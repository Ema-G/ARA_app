package com.example.ara.controller;

import com.example.ara.dto.SubscribeRequest;
import com.example.ara.model.PushSubscription;
import com.example.ara.model.User;
import com.example.ara.repository.PushSubscriptionRepository;
import com.example.ara.service.PushNotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/push")
public class PushController {

    private final PushNotificationService pushService;
    private final PushSubscriptionRepository subscriptionRepo;

    public PushController(PushNotificationService pushService,
                          PushSubscriptionRepository subscriptionRepo) {
        this.pushService = pushService;
        this.subscriptionRepo = subscriptionRepo;
    }

    /** Returns the VAPID public key so the browser can subscribe. 404 if push is disabled. */
    @GetMapping("/vapid-key")
    public ResponseEntity<Map<String, String>> getVapidKey() {
        String key = pushService.getPublicKey();
        if (key == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Push notifications not configured on this server."));
        }
        return ResponseEntity.ok(Map.of("publicKey", key));
    }

    /** Save a push subscription for the authenticated user. */
    @PostMapping("/subscribe")
    @ResponseStatus(HttpStatus.CREATED)
    public void subscribe(@RequestBody SubscribeRequest request, Authentication auth) {
        if (!pushService.isEnabled()) return;

        User user = (User) auth.getPrincipal();
        String p256dh = request.keys() != null ? request.keys().get("p256dh") : null;
        String authKey = request.keys() != null ? request.keys().get("auth") : null;

        if (request.endpoint() == null || p256dh == null || authKey == null) return;

        // Avoid duplicate subscriptions for the same endpoint
        subscriptionRepo.deleteByEndpoint(request.endpoint());
        subscriptionRepo.save(new PushSubscription(user, request.endpoint(), p256dh, authKey));
    }

    /** Remove a push subscription (user opted out). */
    @PostMapping("/unsubscribe")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unsubscribe(@RequestBody Map<String, String> body) {
        String endpoint = body.get("endpoint");
        if (endpoint != null) subscriptionRepo.deleteByEndpoint(endpoint);
    }
}
