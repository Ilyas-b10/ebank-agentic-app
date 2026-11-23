package com.ebank.chatbot.web;

public class ChatRequest {

    private String query;
    private String conversationId;

    public ChatRequest() {
    }

    public ChatRequest(String query, String conversationId) {
        this.query = query;
        this.conversationId = conversationId;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }
}
