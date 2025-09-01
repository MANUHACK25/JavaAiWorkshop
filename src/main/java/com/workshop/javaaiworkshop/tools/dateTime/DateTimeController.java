package com.workshop.javaaiworkshop.tools.dateTime;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
public class DateTimeController {
    private final ChatClient chatClient;

    public DateTimeController(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @GetMapping("/tools/datetimetools")
    public Flux<String> tools(){
        return chatClient.prompt()
                .user("what day will be tomorrow?")
                //.tools()
                .tools(new DateTimeTools())
                .stream()
                .content();
    }


}

