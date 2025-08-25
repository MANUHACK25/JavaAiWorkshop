package com.workshop.javaaiworkshop.multimodal;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
public class ImageDetection {
    private final ChatClient chatClient;

    @Value("classpath:/images/machupicchu.jpg")
    Resource sampleImage;


    public ImageDetection(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @GetMapping(path = "/image-to-text", produces = "text/plain; charset=UTF-8")
    public Flux<String> imageToText(){
        return chatClient.prompt()
                .user(u->{
                    u.text("me podrias describir que es lo que ves en la siguiente imagen?");
                    u.media(MimeTypeUtils.IMAGE_JPEG, sampleImage);
                })
                .stream()
                .content();
    }


}
