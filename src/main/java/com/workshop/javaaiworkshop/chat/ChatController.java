package com.workshop.javaaiworkshop.chat;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
public class ChatController {

    private final ChatClient chatClient;

    public ChatController(ChatClient.Builder builder){
        this.chatClient = builder.build();
    }


    /**
     * el chatCLient.prompt().user().call().content permite que el usurio
     * de su prompt y se lo pasa, como respuesta dara una respuesta el LLM brusca por asi decirlo
     * */

    @GetMapping("/chat")
    public String chat(){
        return chatClient.prompt()
                .user("cuentame un chiste sobre java")
                .call()
                .content();
    }


    /**
     * Streaming the response: no queremos que nos retorne una respuesta larga y seca sino como hace
     * geminiy o chatgpt que te van respondiendo mientras van  escribiendo, por eso es un straming
     * y a su vez que este retorna un Flux<String>
     * */

    @GetMapping("/chatStream")
    public Flux<String> stream(){
        return chatClient.prompt()
                .user("Estoy visitando colonia hoy dia, me puedes recomendar 10 lugares que puedo visitar?")
                .stream()
                .content();
    }

    @GetMapping("chatJoke")
    public ChatResponse joke(){
        return chatClient.prompt()
                .user("cuentame un chiste malo sobre perros")
                .call()
                .chatResponse();
    }

}
