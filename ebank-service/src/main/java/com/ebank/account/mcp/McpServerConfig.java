package com.ebank.account.mcp;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class McpServerConfig {

    /**
     * Enregistre les methodes @Tool de EbankMcpTools comme outils du serveur MCP.
     */
    @Bean
    public ToolCallbackProvider ebankTools(EbankMcpTools ebankMcpTools) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(ebankMcpTools)
                .build();
    }
}
