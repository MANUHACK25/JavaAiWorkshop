package com.workshop.javaaiworkshop.prompt;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

@RestController
public class ArticleController {
    private final ChatClient chatClient;
    public ArticleController(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    /**
     * Este metodo nos permite hace un controller el cual nosotros le damos
     * un tema para que haga un articulo y este lo hara en base al SystemPrompt que tiene
     * */

    @GetMapping(path = "/posts/new", produces = "text/plain; charset=UTF-8")
    public Flux<String> newPost(@RequestParam(value = "topic", defaultValue = "Fubtol Peruano") String topic){
        var systemPrompt = """           
                Pautas del generador de publicaciones de blog:
                Longitud y propósito: Generar publicaciones de blog de 300 palabras que informen e involucren a audiencias generales.
                               
                Estructura:
                    Introducción: Atrapa a los lectores y establece la relevancia del tema      
                    Cuerpo: Desarrolla 3 puntos principales con evidencia y ejemplos de apoyo.
                    Conclusión: Resume los puntos clave e incluye una llamada a la acción.
                               
                Requisitos de contenido:
                    Incluye aplicaciones del mundo real o estudios de caso.
                                   
                    Incorpora estadísticas relevantes o puntos de datos cuando sea apropiado.
                                   
                    Explica claramente los beneficios/implicaciones para los no expertos.
                               
                Tono y estilo:
                    Escribe con una voz informativa pero conversacional.
                         
                    Usa un lenguaje accesible manteniendo la autoridad.
                                   
                    Divide el texto con subtítulos y párrafos cortos.
                               
                Formato de respuesta: Entrega publicaciones completas, listas para publicar, con un título sugerido.
                """;

        return chatClient
                .prompt()
                .user(promptUserSpec -> {
                    promptUserSpec.text("escribeme un blog sobre {topic}");
                    promptUserSpec.param("topic", topic);
                })
                .system(systemPrompt)
                .stream()
                .content();
    }
}
