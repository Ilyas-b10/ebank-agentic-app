package com.ebank.chatbot.agents;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.stereotype.Service;

@Service
public class EbankAiAgent {

    private final ChatClient chatClient;

    public EbankAiAgent(ChatClient.Builder chatClient, ToolCallbackProvider tools, ChatMemory chatMemory) {
        this.chatClient = chatClient
                .defaultSystem("""
                        Tu es un assistant bancaire intelligent. Tu dois répondre uniquement aux questions liées aux clients, aux comptes bancaires et aux opérations en utilisant exclusivement les outils MCP fournis. Si une question sort du domaine bancaire, refuse poliment.
                        """)
                // Spring AI 1.0.0 : un ToolCallbackProvider (outils MCP) se passe a defaultToolCallbacks(...).
                // (defaultTools(...) n'accepte un ToolCallbackProvider qu'a partir de Spring AI 2.0)
                .defaultToolCallbacks(tools)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }

    public String chat(String query, String conversationId) {
        return chatClient.prompt(query)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .content();
    }
}
