package com.ebank.chatbot.discord;

import com.ebank.chatbot.agents.EbankAiAgent;
import com.ebank.chatbot.utils.MessageUtils;
import jakarta.annotation.PreDestroy;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Bot Discord (JDA). Repond aux messages prives et aux messages d'un serveur ou le bot est mentionne.
 * Active avec discord.bot.enabled=true et discord.bot.token=<token>.
 * Le "MESSAGE CONTENT INTENT" doit etre active dans le portail developpeur Discord.
 */
@Component
@ConditionalOnProperty(prefix = "discord.bot", name = "enabled", havingValue = "true")
public class DiscordBotService extends ListenerAdapter {

    private static final Logger log = LoggerFactory.getLogger(DiscordBotService.class);
    private static final int DISCORD_MAX_LENGTH = 1900;

    private final EbankAiAgent ebankAiAgent;
    private final String token;
    private final ExecutorService executor = Executors.newFixedThreadPool(8);
    private JDA jda;

    public DiscordBotService(EbankAiAgent ebankAiAgent, @Value("${discord.bot.token}") String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalStateException("discord.bot.enabled=true mais DISCORD_BOT_TOKEN est vide");
        }
        this.ebankAiAgent = ebankAiAgent;
        this.token = token;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void start() {
        jda = JDABuilder.createLight(token, List.of(
                        GatewayIntent.GUILD_MESSAGES,
                        GatewayIntent.DIRECT_MESSAGES,
                        GatewayIntent.MESSAGE_CONTENT))
                .addEventListeners(this)
                .build();
        log.info("Discord bot started");
    }

    @PreDestroy
    public void stop() {
        if (jda != null) {
            jda.shutdown();
        }
        executor.shutdownNow();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) {
            return;
        }
        User self = event.getJDA().getSelfUser();
        boolean isPrivate = event.isFromType(ChannelType.PRIVATE);
        boolean mentioned = event.getMessage().getMentions().getUsers().contains(self);
        if (!isPrivate && !mentioned) {
            return;
        }

        String content = event.getMessage().getContentRaw()
                .replace("<@" + self.getId() + ">", "")
                .replace("<@!" + self.getId() + ">", "")
                .trim();
        if (content.isEmpty()) {
            return;
        }

        String conversationId = "discord-" + event.getAuthor().getId();
        event.getChannel().sendTyping().queue();

        executor.submit(() -> {
            try {
                String answer = ebankAiAgent.chat(content, conversationId);
                for (String part : MessageUtils.split(answer, DISCORD_MAX_LENGTH)) {
                    event.getChannel().sendMessage(part).queue();
                }
            } catch (Exception e) {
                log.error("Error while handling Discord message", e);
                event.getChannel().sendMessage("Une erreur est survenue lors du traitement de votre demande.").queue();
            }
        });
    }
}
