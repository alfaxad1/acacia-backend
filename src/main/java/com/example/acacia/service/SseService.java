package com.example.acacia.service;

import com.example.acacia.enums.TransactionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SseService {
    private static final Logger logger = LoggerFactory.getLogger(SseService.class);
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(String checkoutId) {
        SseEmitter emitter = new SseEmitter(60000L); // 60 seconds timeout
        emitters.put(checkoutId, emitter);

        emitter.onCompletion(() -> emitters.remove(checkoutId));
        emitter.onTimeout(() -> {
            emitter.complete();
            emitters.remove(checkoutId);
        });
        emitter.onError((e) -> emitters.remove(checkoutId));

        return emitter;
    }

    public void notifyClient(String checkoutId, TransactionStatus status) {
        SseEmitter emitter = emitters.get(checkoutId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event().name("payment-status").data(status.name()));
                emitter.complete();
            } catch (IOException e) {
                logger.error("Error sending SSE event for checkoutId: {}", checkoutId, e);
                emitters.remove(checkoutId);
            }
        }
    }
}
