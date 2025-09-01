package com.workshop.javaaiworkshop.rag;
import com.workshop.javaaiworkshop.output.Itinerary;
import org.apache.commons.logging.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.Banner;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

//TODO: ver como mejorar el resultado de este rag

@RestController
public class ModelsController {
    private Logger logi = LoggerFactory.getLogger(ModelsController.class);

    private final ChatClient chatClient;

    private VectorStore vectorStore;

    /***
     * lo que falta aca es aniadir el VectorStore para que nuestra pregunta que le hagamos al LLMS tenga ya el contexto
     * de lo que queremos llamar, ene ste caso a nuestro cargado VectorStore
     * */
    public ModelsController(ChatClient.Builder builder, VectorStore vectorStore) {
        //definir un advisor base
        this.chatClient = builder
                .defaultAdvisors(new QuestionAnswerAdvisor(vectorStore))
                .build();

        this.vectorStore = vectorStore;
    }

    @GetMapping("/rag/models")
    public Models faq (
            @RequestParam(value = "message",
                    defaultValue = "Give me a list of the models from OpenAI along with their context windows.")
            String message){

        /***
         * Solucion a probar
         * paso 1: recibimos el Prompt
         * paso 2: con LLM extraemos los keyWords del prompt que obtenemos
         * paso 3: una vez Obtenemos los KeyWords importantes llamamos al similaritySearch
         * paso 4: ejecutamos el similarity Search, responde en json format
         *
         * */

        //paso 2 extraemos el keyword del prompt
        String keyword = extract(message);

        //paso3: similarity Search
        SearchRequest request = SearchRequest.builder()
                .filterExpression("company == '" + keyword + "'")
                .topK(10)
                .build();

        QuestionAnswerAdvisor ad = QuestionAnswerAdvisor.builder(vectorStore)
                .searchRequest(request)
                .build();

        return (Models) chatClient.prompt()
                .system("""
                                You are a JSON extractor.
                                ONLY return models that exist in the provided context (documents).
                                Do not add anything that is not explicitly present in the context.
                                If the context is empty or no matches are found, return {"models": []}.
                                Absolutely forbid inventing companies, models, or contextWindowSize values.
                                Output ONLY valid JSON without markdown.
                        """)
                .user(message)
                .advisors(ad)
                .call()
                .entity(new BeanOutputConverter(Models.class){
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

    //@GetMapping("/rag/messageModels")
    public String extract (String message){

        /***
         * Solucion a probar
         * paso 1: recibimos el Prompt
         * paso 2: con LLM extraemos los keyWords del prompt que obtenemos
         * paso 3: una vez Obtenemos los KeyWords importantes llamamos al similaritySearch
         * paso 4: ejecutamos el similarity Search, responde en json format
         *
         * */

        return chatClient.prompt()
                .system(""" 
                         You are a JSON extractor.
                         ONLY use the models provided in the context (documents).
                         Do not invent companies, models, or context_window_size values.
                         If nothing matches, return {"models": []}.
                        Output ONLY valid JSON without markdown.
                        """)
                .user(message)
                .call()
                .content();
    }



    /***
    @GetMapping("/rag/models")
    public Models faq(
            @RequestParam(value = "message",
                    defaultValue = "Give me a list of all the models from OpenAI along with their context windows.")
            String message) {

        SearchRequest request = SearchRequest.builder()
                .filterExpression("message == 'Give me a list of all the models from OpenAI along with their context windows.'")
                .topK(10)
                .build();

        QuestionAnswerAdvisor ad = QuestionAnswerAdvisor.builder(vectorStore)
                .searchRequest(request)
                .build();

        return (Models) chatClient
                .prompt()
                .user(message)
                .system("""
                              You are a JSON extractor.
                              Return ONLY valid JSON without markdown, no explanations.
                        """)
                .advisors(ad)
                .call()
                .entity(new BeanOutputConverter(Models.class) {
                    @Override
                    public Object convert(String text) {
                        String clean = text
                                .replaceAll("(?i)```json", "")
                                .replaceAll("```", "")
                                .trim();

                        Models parsed = (Models) super.convert(clean);
                        // Validación fuerte: todo debe salir del VectorStore

                        List<Model> corrected = parsed.models().stream()
                                .map(m -> {
                                    logi.info("LLM devolvió modelo: {}", m.model());
                                    List<Document> hits = vectorStore.similaritySearch(
                                            SearchRequest.builder()
                                                    .query(m.model() + " " + m.company())
                                                    .topK(1)
                                                    .build()
                                    );
                                    logi.info("Hits encontrados: {}", hits.size());
                                    hits.forEach(h -> logi.info("Doc recuperado: {}", h));

                                    if (!hits.isEmpty()) {
                                        Document doc = hits.get(0);
                                        return new Model(
                                                (String) doc.getMetadata().get("company"),
                                                (String) doc.getMetadata().get("model"),
                                                (Integer) doc.getMetadata().get("context_window_size")
                                        );
                                    }
                                    return null; // descartar si no está en el store
                                })
                                .filter(Objects::nonNull)
                                .toList();
                        return new Models(corrected);
                    }
                });
    }
*/

/***
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

    */



}
