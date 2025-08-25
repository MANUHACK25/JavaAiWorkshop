package com.workshop.javaaiworkshop.prompt;


import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/bank")
public class BankController {
    private final ChatClient chatClient;

    public BankController(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    /**
     * aca vemos como es que le damos un SystemInstruction para el sistema y se le indica cosas como
     * que responda solo en espaniol y un promp de sistema que reciba solo a referido futbol
     *
     * a parte de eso retornoa un Flux<String>
     * */
    @GetMapping("/bankChat")
    public Flux<String> chat(@RequestParam String message){
        var systemInstructions = """
                eres un asistente que solo sabe de futbol en general para la liga peruana que solo habla en espaniol
                
                solo puedes discutir:
                - Futbol Peruano
                - Resultados
                - Estadisiticas
                
                si alguien te pregunta sobre alguna otra cosa que no sea futbol y que no sea en espaniol,
                reponde con: "yo solo puedo responder con preguntas asociadas al futbol peruano". 
                
                si alguiente te pregunta en otros idiomas, respondele: "yo solo puedo responder en espaniol".
                """;
        return chatClient
                .prompt()
                .user(message)
                .system(systemInstructions)
                .stream()
                .content();
    }



}
