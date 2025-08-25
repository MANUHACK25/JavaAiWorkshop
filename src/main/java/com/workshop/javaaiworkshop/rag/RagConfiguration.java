package com.workshop.javaaiworkshop.rag;

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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Configuration
public class RagConfiguration {

    private static final Logger log = LoggerFactory.getLogger(RagConfiguration.class);
    private final String vectorStoreName = "vectorestore.json";

    @Value("classpath:/data/models.json")
    private Resource models;

    @Bean
    public SimpleVectorStore simpleVectorStore(EmbeddingModel embeddingModel){
        //el SimpleVector Store toma como parametro el emebeddingModel
        var simpleVectorStore = SimpleVectorStore.builder(embeddingModel).build();
        File vectorStoreFile = getVectorStoreFile();
        if(vectorStoreFile.exists()){
            //si existe ya ese file entonces ya no se crea el vectorStore
            log.info("Vector Store ya existe en {}", vectorStoreFile.getAbsolutePath());
            simpleVectorStore.load(vectorStoreFile);

        }else {
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
            List<Document> docuSplitter = tokenTextSplitter.apply(documents);

            //add the splitt into the VectorStore
            simpleVectorStore.add(docuSplitter);
            //save the whole file into the VectorStore
            simpleVectorStore.save(vectorStoreFile);
        }

        return simpleVectorStore;
    }

    private File getVectorStoreFile(){
        Path path = Paths.get("src", "main", "resources", "data");
        String absoluthPath = path.toFile().getAbsolutePath() + "/" + vectorStoreName;
        return new File(absoluthPath);
    }





}
