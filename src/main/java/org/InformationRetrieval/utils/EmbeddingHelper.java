package org.InformationRetrieval.utils;



import ai.djl.Application;
import ai.djl.ModelException;
import ai.djl.inference.Predictor;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelZoo;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.translate.TranslateException;

import java.io.IOException;

public class EmbeddingHelper {

    private static final ZooModel<String, float[]> model;
    private static final Predictor<String, float[]> predictor;

    static {
        try {
        	Criteria<String, float[]> criteria = Criteria.builder()
        	        .optApplication(Application.NLP.TEXT_EMBEDDING)
        	        .setTypes(String.class, float[].class)
        	        .build();



            model = ModelZoo.loadModel(criteria);
            predictor = model.newPredictor();

        } catch (IOException | ModelException e) {
        	throw new RuntimeException("\u274C Failed to load embedding model", e);

        }
    }

    public static float[] embedText(String text) throws TranslateException {
        return predictor.predict(text);
    }
  
    public static void close() {
        predictor.close();
		model.close();
    }
}

