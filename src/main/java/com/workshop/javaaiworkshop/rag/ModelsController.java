package com.workshop.javaaiworkshop.rag;
import io.micrometer.core.instrument.search.Search;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
public class ModelsController {
    private final ChatClient chatClient;

    private final VectorStore vectorStore;
    /***
     * lo que falta aca es aniadir el VectorStore para que nuestra pregunta que le hagamos al LLMS tenga ya el contexto
     * de lo que queremos llamar, ene ste caso a nuestro cargado VectorStore
     * */
    public ModelsController(ChatClient.Builder builder, VectorStore vectorStore) {
        SearchRequest request = SearchRequest.builder()
                .filterExpression("company == 'OpenAI'" )
                .topK(5)
                .build();

        QuestionAnswerAdvisor advisor = QuestionAnswerAdvisor.builder(vectorStore)
                .searchRequest(request)
                .build();

        //definir un advisor base
        this.chatClient = builder
                .defaultAdvisors(advisor)
                .build();
        this.vectorStore = vectorStore;
    }

    @GetMapping("/rag/models")
    public Models faq(@RequestParam(value = "message", defaultValue = "Based on the list of models, tell me all the models from OpenAi along with their context windows.") String message){
        return (Models) chatClient.prompt()
                .user(message + "\n\nReturn ONLY valid JSON")
                .system("""
                        You are a STRICT extractor.
                        Use only the provided RAG context.
                        Do not invent anything. Output RFC8259 JSON only.
                        """)
                .call()
                .entity(new BeanOutputConverter(Models.class){
                    @Override
                    public Object convert(String text){
                        String clean = text.replace("(?i)```json", "")
                                .replaceAll("```", "")
                                .replaceAll("(?i)^json\\s*", "")
                                .trim();

                        //parseamos a Models
                        Models result = (Models) super.convert(clean);

                        List<Model> fixed = result.models().stream()
                                .map(m -> {
                                    Integer size = findContextWindowsSizeFromStore(m.model());
                                    //ojo remplazarlo el contextt windows por el nuevo SIze osea el FInal Size
                                    assert size != null;
                                    return new Model(m.company(), m.model(), size);
                                }).toList();

                        return new Models(fixed);
                    }

                    /***
                     * Buscamos en VectorStore docuentos cercanos al nombre de modleo
                     * y extratemos context_windows_size dle Json embebido en el texto
                     * */

                    private Integer findContextWindowsSizeFromStore(String modelName){
                        List<Document> hits = vectorStore.similaritySearch(SearchRequest.builder()
                                .query(modelName)
                                .topK(5)
                                .build());

                        for (Document d : hits) {
                            Integer exact = extractSizeFromTextForModel(d.getText(), modelName);
                            if (exact != null) return exact;
                        }
                        return null;
                    }

                    /**
                     * Extraemos ahora el contextWIndowsSize del Json
                     * en si buscara un objeto {..} q contenga "model": "<modelName>"
                     * y por cada uno sacra a contextWindoswsize : number dentro del blpque
                     * */

                    private Integer extractSizeFromTextForModel(String text, String modelName){
                        // 1) aislar el objeto del modelo
                        String objRegex = "\\{[^}]*?\"model\"\\s*:\\s*\"" + Pattern.quote(modelName) + "\"[^}]*?\\}";
                        Matcher mObj = Pattern.compile(objRegex, Pattern.DOTALL).matcher(text);
                        if(mObj.find()){
                            String block = mObj.group();
                            Integer size = extractSizeFromBlock(block);
                            if (size != null) return size;
                        }
                        // 2) fallback: intenta extraer cualquier context_window_size del doc (menos fiable)
                        return extractSizeFromBlock(text);
                    }

                    private Integer extractSizeFromBlock(String block){
                        Matcher m = Pattern.compile("\"context_window_size\"\\s*:\\s*(\\d+)").matcher(block);
                        if(m.find()){
                            try {
                                return Integer.parseInt(m.group(1));
                            } catch (NumberFormatException ignored) { }
                        }
                        return null;
                    }

                });
    }



}
