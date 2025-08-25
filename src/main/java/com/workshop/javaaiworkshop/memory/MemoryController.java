package com.workshop.javaaiworkshop.memory;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
public class MemoryController {

    /**
     * este Feature permite a la ventana de contexto darle una memoria al chat
     *
     * que teiene un default max messages de 20
     *
     *
     *     private static final int DEFAULT_MAX_MESSAGES = 20;
     *     private final ChatMemoryRepository chatMemoryRepository;
     *     private final int maxMessages;
     *
     * */

    private final ChatClient chatClient;

    public MemoryController(ChatClient.Builder builder, ChatMemory chatMemory) {
        this.chatClient = builder
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }
    @GetMapping(value = "/memory", produces = "text/plain; charset=UTF-8")
    public Flux<String> memory(@RequestParam String message){
        return chatClient.prompt()
                .user(message)
                .stream()
                .content();
    }
}
