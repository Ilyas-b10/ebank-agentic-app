package com.ebank.chatbot.utils;

import java.util.ArrayList;
import java.util.List;

public final class MessageUtils {

    private MessageUtils() {
    }

    /**
     * Decoupe un long texte en morceaux d'au plus maxLength caracteres
     * (Telegram : 4096, Discord : 2000), en privilegiant les retours a la ligne.
     */
    public static List<String> split(String text, int maxLength) {
        List<String> parts = new ArrayList<>();
        if (text == null || text.isBlank()) {
            parts.add("Je n'ai pas pu générer de réponse.");
            return parts;
        }
        String remaining = text.strip();
        while (remaining.length() > maxLength) {
            int cut = remaining.lastIndexOf('\n', maxLength);
            if (cut <= 0) {
                cut = maxLength;
            }
            parts.add(remaining.substring(0, cut));
            remaining = remaining.substring(cut).stripLeading();
        }
        if (!remaining.isEmpty()) {
            parts.add(remaining);
        }
        return parts;
    }
}
