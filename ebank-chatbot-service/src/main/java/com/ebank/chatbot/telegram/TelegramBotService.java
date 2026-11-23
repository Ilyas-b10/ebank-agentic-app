package com.ebank.chatbot.telegram;

import com.ebank.chatbot.agents.EbankAiAgent;
import com.ebank.chatbot.utils.MessageUtils;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Bot Telegram en long polling (API Bot officielle, sans bibliotheque tierce).
 * Active avec telegram.bot.enabled=true et telegram.bot.token=<token BotFather>.
 */
@Component
@ConditionalOnProperty(prefix = "telegram.bot", name = "enabled", havingValue = "true")
public class TelegramBotService {

    private static final Logger log = LoggerFactory.getLogger(TelegramBotService.class);
    private static final int TELEGRAM_MAX_LENGTH = 4000;

    private final EbankAiAgent ebankAiAgent;
    private final RestClient restClient;
    private final ExecutorService executor = Executors.newFixedThreadPool(8);

    private volatile boolean running = false;
    private Thread pollingThread;

    public TelegramBotService(EbankAiAgent ebankAiAgent, @Value("${telegram.bot.token}") String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalStateException("telegram.bot.enabled=true mais TELEGRAM_BOT_TOKEN est vide");
        }
        this.ebankAiAgent = ebankAiAgent;

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(10));
        // Le long polling dure 30 s : le timeout de lecture doit etre superieur
        requestFactory.setReadTimeout(Duration.ofSeconds(45));

        this.restClient = RestClient.builder()
                .baseUrl("https://api.telegram.org/bot" + token)
                .requestFactory(requestFactory)
                .build();
    }

    @EventListener(ApplicationReadyEvent.class)
    public void start() {
        running = true;
        pollingThread = new Thread(this::pollLoop, "telegram-polling");
        pollingThread.setDaemon(true);
        pollingThread.start();
        log.info("Telegram bot started (long polling)");
    }

    @PreDestroy
    public void stop() {
        running = false;
        if (pollingThread != null) {
            pollingThread.interrupt();
        }
        executor.shutdownNow();
    }

    private void pollLoop() {
        long offset = 0;
        while (running) {
            try {
                final long currentOffset = offset;
                JsonNode response = restClient.get()
                        .uri(uriBuilder -> uriBuilder.path("/getUpdates")
                                .queryParam("timeout", 30)
                                .queryParam("offset", currentOffset)
                                .build())
                        .retrieve()
                        .body(JsonNode.class);

                if (response != null && response.path("ok").asBoolean(false)) {
                    for (JsonNode update : response.path("result")) {
                        offset = update.path("update_id").asLong() + 1;
                        handleUpdate(update);
                    }
                }
            } catch (Exception e) {
                if (!running) {
                    break;
                }
                log.warn("Telegram polling error : {}", e.getMessage());
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    private void handleUpdate(JsonNode update) {
        JsonNode message = update.path("message");
        String text = message.path("text").asText("");
        if (text.isBlank() || !message.path("chat").has("id")) {
            return;
        }
        long chatId = message.path("chat").path("id").asLong();

        executor.submit(() -> {
            try {
                if (text.equals("/start")) {
                    sendMessage(chatId, "Bonjour ! Je suis votre assistant bancaire. "
                            + "Posez-moi vos questions sur les clients, les comptes et les opérations.");
                    return;
                }
                sendTyping(chatId);
                String answer = ebankAiAgent.chat(text, "telegram-" + chatId);
                sendMessage(chatId, answer);
            } catch (Exception e) {
                log.error("Error while handling Telegram message", e);
                sendMessage(chatId, "Une erreur est survenue lors du traitement de votre demande.");
            }
        });
    }

    private void sendTyping(long chatId) {
        try {
            restClient.post()
                    .uri("/sendChatAction")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("chat_id", chatId, "action", "typing"))
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.debug("sendChatAction failed : {}", e.getMessage());
        }
    }

    private void sendMessage(long chatId, String text) {
        for (String part : MessageUtils.split(text, TELEGRAM_MAX_LENGTH)) {
            try {
                restClient.post()
                        .uri("/sendMessage")
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(Map.of("chat_id", chatId, "text", part))
                        .retrieve()
                        .toBodilessEntity();
            } catch (Exception e) {
                log.error("Telegram sendMessage failed : {}", e.getMessage());
            }
        }
    }
}
