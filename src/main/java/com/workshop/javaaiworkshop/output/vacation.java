package com.workshop.javaaiworkshop.output;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class vacation {
    private final ChatClient chatClient;

    public vacation(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @GetMapping("/vacation/unstructured")

    public Flux<String> unstructured(){
        return chatClient.prompt()
                .user("quiero hacer un viaje a cusco por 3 dias, dame una lista de cosas que hacer")
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
        return (Itinerary) chatClient.prompt()
                .user("quiero hacer un viaje a cusco por 4 dias, dame una lista de cosas que hacer")
                .call()
                .entity(new BeanOutputConverter(Itinerary.class){
                    @Override
                    public Object convert(String text){
                        String clean = text.replace("(?i)```json", "")
                                .replaceAll("```", "")
                                .replaceAll("(?i)^json\\s*", "")
                                .trim();
                        return super.convert(clean);
                    }
                });

    }

    /***
     * this method tries to improve structured Method (/vacation/structured)
     * instead of doing just a raw calling could call a Flux<Object>
     *     still at testing though
     * */
    @GetMapping("/vacation/structuredMono")
    public Mono<Itinerary> monoStructured(){
        // Se define el conversor, igual que en tu código original.
        BeanOutputConverter<Itinerary> outputConverter = new BeanOutputConverter<>(Itinerary.class);

        // Se usa .stream() para recibir fragmentos de la respuesta.
        // Se usan operadores de Reactor para procesar el stream.
        return chatClient.prompt()
                .user("quiero hacer un viaje a cusco por 4 dias, dame una lista de cosas que hacer")
                .stream()
                .content()
                // Se acumulan todos los fragmentos del stream en un solo StringBuilder.
                .collectList()
                .map(list -> String.join("", list))
                // Una vez que se tiene el String completo, se limpia y se convierte.
                .map(text -> {
                    String cleanText = text.replace("(?i)```json", "")
                            .replaceAll("```", "")
                            .replaceAll("(?i)^json\\s*", "")
                            .trim();
                    return outputConverter.convert(cleanText);
                });
    }
}
