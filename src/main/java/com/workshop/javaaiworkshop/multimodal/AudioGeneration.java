package com.workshop.javaaiworkshop.multimodal;

import ai.djl.modality.audio.Audio;
/**
 * ver com hacerlo solo con Open SOurce models
 * **/
public class AudioGeneration {
    private final Audio speechModel;


    public AudioGeneration(Audio speechModel) {
        this.speechModel = speechModel;
    }
}
