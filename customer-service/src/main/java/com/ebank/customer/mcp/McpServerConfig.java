package com.ebank.customer.mcp;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class McpServerConfig {

    /**
     * Enregistre les methodes @Tool de CustomerMcpTools comme outils du serveur MCP.
     */
    @Bean
    public ToolCallbackProvider customerTools(CustomerMcpTools customerMcpTools) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(customerMcpTools)
                .build();
    }
}
