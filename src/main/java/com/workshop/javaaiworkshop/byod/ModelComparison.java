package com.workshop.javaaiworkshop.byod;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
@RestController
public class ModelComparison {
    private final ChatClient chatClient;
    public ModelComparison(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @GetMapping("models")
    public Flux<String> models(){
        return chatClient.prompt()
                .user("puedes hacerme una lista con los modelos de llm mas populares y sus vectanas de contexto?")
                .stream()
                .content();
    }

    /***
     * Useful for Static data like company policy where we dont need any automatically call to any API
     * also useful for static documentation
     * */
    @GetMapping(value = "stuffModelPrompt", produces = "text/plain; charset=UTF-8")
    public Flux<String> modelStuffPrompt(){
        var system = """
                If you're asked up to date language models and their context window, here is some information to help you with your response:
                [
                { "company": "OpenAI",       "model": "GPT-4o",          "context_window_size": 128000 },
                { "company": "OpenAI",       "model": "GPT-4o-preview",  "context_window_size": 128000 },
                                
                { "company": "Anthropic",    "model": "Claude Opus 4",   "context_window_size": 200000 },
                { "company": "Anthropic",    "model": "Claude Sonnet 4", "context_window_size": 200000 },
                                
                { "company": "Google",       "model": "Gemini 2.5 Pro",  "context_window_size": 1000000 },
                { "company": "Google",       "model": "Gemini 2.0 Pro (Exp.)", "context_window_size": 200000 },
                { "company": "Google",       "model": "Gemini 2.0 Flash", "context_window_size": 1000000 },
                                
                { "company": "Meta AI",      "model": "Llama 3.1 405B",  "context_window_size": 128000 },
                                
                { "company": "xAI",          "model": "Grok 3",          "context_window_size": 1000000 },
                                
                { "company": "Mistral AI",   "model": "Mistral Large 2", "context_window_size": 128000 },
                                
                { "company": "Alibaba Cloud", "model": "Qwen 2.5 72B",   "context_window_size": 128000 },
                                
                { "company": "DeepSeek",     "model": "DeepSeek R1",     "context_window_size": 128000 }]
                """;
        return chatClient.prompt()
                .system(system)
                .user("puedes hacerme una lista con los modelos de llm mas populares y sus vectanas de contexto?")
                .stream()
                .content();
    }
}
