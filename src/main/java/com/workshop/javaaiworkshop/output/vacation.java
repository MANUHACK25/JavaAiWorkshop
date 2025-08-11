package com.workshop.javaaiworkshop.output;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
public class vacation {
    private final ChatClient chatClient;


    public vacation(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @GetMapping("/vacation/unstructured")

    public Flux<String> unstructured(){
        return chatClient.prompt()
                .user("quiero hacer un viaje a cusco, dame una lista de cosas que hacer")
                .stream()
                .content();

    }

    /***
     * la idea ahora es que no vamos a retornar una repuesta normal asi como quieiseramos sino es que
     * ahora vamos a devolver un tipo especifico de respuestra basado a una entidad que tengamos
     * puede ser un record, o un json un ejemplo en si que haya
     * */

    @GetMapping("/vacation/structured")
    public Itinerary structured(){
        var converter = new BeanOutputConverter(Itinerary.class);

        return chatClient.prompt()
                .user("quiero hacer un viaje a cusco, dame una lista de cosas que hacer")
                .call()
                .entity(Itinerary.class);
    }
}
