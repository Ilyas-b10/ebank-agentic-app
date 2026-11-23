# ebank-agentic-app

## Partie 1 - Microservices
discovery-service (8761), config-service (9999), gateway-service (8888),
customer-service (8081), ebank-service (8082)

## Partie 2 - IA agentique (Spring AI 1.0.0, MCP, Telegram, Discord)
- customer-service et ebank-service sont des serveurs MCP (SSE : /sse)
- ebank-chatbot-service (8083) est client MCP + agent IA (OpenAI) + bots Telegram / Discord

Variables d'environnement : OPENAI_API_KEY (obligatoire),
TELEGRAM_BOT_ENABLED / TELEGRAM_BOT_TOKEN, DISCORD_BOT_ENABLED / DISCORD_BOT_TOKEN (optionnelles)

Ordre de demarrage :
1. discovery-service  2. config-service  3. customer-service  4. ebank-service
5. ebank-chatbot-service  6. gateway-service

Test : http://localhost:8888/ebank-chatbot-service/api/chat?query=Liste%20des%20clients
