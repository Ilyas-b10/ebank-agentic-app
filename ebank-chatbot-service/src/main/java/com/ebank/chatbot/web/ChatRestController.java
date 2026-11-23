package com.ebank.chatbot.web;

import com.ebank.chatbot.agents.EbankAiAgent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
public class ChatRestController {

    private static final String DEFAULT_CONVERSATION_ID = "rest-default";

    private final EbankAiAgent ebankAiAgent;

    public ChatRestController(EbankAiAgent ebankAiAgent) {
        this.ebankAiAgent = ebankAiAgent;
    }

    @GetMapping
    public ChatResponse chat(@RequestParam("query") String query,
                             @RequestParam(value = "conversationId", defaultValue = DEFAULT_CONVERSATION_ID)
                             String conversationId) {
        return new ChatResponse(conversationId, ebankAiAgent.chat(query, conversationId));
    }

    @PostMapping
    public ChatResponse chat(@RequestBody ChatRequest request) {
        String conversationId = (request.getConversationId() == null || request.getConversationId().isBlank())
                ? DEFAULT_CONVERSATION_ID
                : request.getConversationId();
        return new ChatResponse(conversationId, ebankAiAgent.chat(request.getQuery(), conversationId));
    }
}
