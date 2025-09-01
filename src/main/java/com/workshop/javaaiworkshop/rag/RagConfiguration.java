package com.workshop.javaaiworkshop.rag;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.antlr.runtime.Token;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@Configuration
public class RagConfiguration {

    private static final Logger log = LoggerFactory.getLogger(RagConfiguration.class);
    private final String vectorStoreName = "vectorestore.json";

    @Value("classpath:/data/models.json")
    private Resource models;

    @Bean
    public SimpleVectorStore simpleVectorStore(EmbeddingModel embeddingModel) throws IOException {
        //el SimpleVector Store toma como parametro el emebeddingModel
        var simpleVectorStore = SimpleVectorStore.builder(embeddingModel).build();
        File vectorStoreFile = getVectorStoreFile();
        if(vectorStoreFile.exists()){
            //si existe ya ese file entonces ya no se crea el vectorStore
            log.info("Vector Store ya existe en {}", vectorStoreFile.getAbsolutePath());
            simpleVectorStore.load(vectorStoreFile);

        }else {
            log.info("Creando un nuevo Vector Store en {}", vectorStoreFile.getAbsolutePath());

            // 1. Parseamos tu archivo JSON
            ObjectMapper mapper = new ObjectMapper();
            List<Map<String, Object>> rawModels = mapper.readValue(
                    models.getInputStream(),
                    new TypeReference<>() {}
            );

            // 2. Convertimos cada entrada en un Document con metadata
            List<Document> docs = rawModels.stream()
                    .map(entry -> {
                        String company = (String) entry.get("company");
                        String model = (String) entry.get("model");
                        Integer context = (Integer) entry.get("context_window_size");

                        return new Document(
                                company + " " + model, // texto a vectorizar
                                Map.of(
                                        "company", company,
                                        "model", model,
                                        "context_window_size", context
                                )
                        );
                    })
                    .toList();

            // 3. Añadimos al VectorStore
            simpleVectorStore.add(docs);
            simpleVectorStore.save(vectorStoreFile);

            /**
            //sino existe, enotnces se crea el VectorStore
            log.info("creando un Vector Store en {}", vectorStoreFile.getAbsolutePath());
            //reading Ressources
            TextReader textReader = new TextReader(models);
            textReader.getCustomMetadata().put("filename", "models.txt");

            //lsita de documents
            List<Document> documents = textReader.get();

            //Split the texts
            TokenTextSplitter tokenTextSplitter = new TokenTextSplitter();

            //apply es para ahcer el split /chunking
            /**
             *
             * en este casos olo estamos haciendo un splitting de solo texto, pero es diferente
             * cuando hacemos PDFs o algun otro tipo de Documents  */

            /**
            List<Document> docuSplitter = tokenTextSplitter.apply(documents);

            //add the splitt into the VectorStore
            simpleVectorStore.add(docuSplitter);
            //save the whole file into the VectorStore
            simpleVectorStore.save(vectorStoreFile);
            **/
        }

        return simpleVectorStore;
    }

    private File getVectorStoreFile(){
        Path path = Paths.get("src", "main", "resources", "data");
        String absoluthPath = path.toFile().getAbsolutePath() + "/" + vectorStoreName;
        return new File(absoluthPath);
    }





}
