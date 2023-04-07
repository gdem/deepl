package ch.softwareplus.ai.ocr.config;

import ai.djl.MalformedModelException;
import ai.djl.inference.Predictor;
import ai.djl.modality.Classifications;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.paddlepaddle.zoo.PpModelZoo;
import ai.djl.paddlepaddle.zoo.cv.imageclassification.PpWordRotateTranslator;
import ai.djl.paddlepaddle.zoo.cv.objectdetection.PpWordDetectionTranslator;
import ai.djl.paddlepaddle.zoo.cv.wordrecognition.PpWordRecognitionTranslator;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.repository.zoo.ZooModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@Configuration
public class DjlConfig {

    @Bean
    public ImageFactory imageFactory() {
        return ImageFactory.getInstance();
    }

    @Bean("wordDetectionCriteria")
    public Criteria<Image, DetectedObjects> wordDetectionCriteria() {
        return Criteria.builder().setTypes(Image.class, DetectedObjects.class)
                .optEngine("PaddlePaddle")
                .optModelUrls("https://resources.djl.ai/test-models/paddleOCR/mobile/det_db.zip")
                .optTranslator(new PpWordDetectionTranslator(new ConcurrentHashMap<String, String>()))
                .build();
    }

    @Bean("wordDirectionCriteria")
    public Criteria<Image, Classifications> wordDirectionCriteria() {
        return Criteria.builder().setTypes(Image.class, Classifications.class)
                .optEngine("PaddlePaddle")
                .optModelUrls("https://resources.djl.ai/test-models/paddleOCR/mobile/cls.zip")
                .optTranslator(new PpWordRotateTranslator())
                .build();
    }

    @Bean("wordRecognitionCriteria")
    public Criteria<Image, String> wordRecognitionCriteria() {
        return Criteria.builder().setTypes(Image.class, String.class)
                .optEngine("PaddlePaddle")
                .optModelUrls("https://resources.djl.ai/test-models/paddleOCR/mobile/rec_crnn.zip")
                .optTranslator(new PpWordRecognitionTranslator())
                .build();
    }


    @Bean
    public ZooModel<Image, DetectedObjects> wordDetectionModel(@Qualifier("wordDetectionCriteria") Criteria<Image, DetectedObjects> criteria)
            throws MalformedModelException, ModelNotFoundException, IOException {
        return PpModelZoo.loadModel(criteria);
    }

    @Bean
    public ZooModel<Image, Classifications> wordDirectionModel(@Qualifier("wordDirectionCriteria") Criteria<Image, Classifications> criteria)
            throws MalformedModelException, ModelNotFoundException, IOException {
        return PpModelZoo.loadModel(criteria);
    }

    @Bean
    public ZooModel<Image, String> wordRecognitionModel(@Qualifier("wordRecognitionCriteria") Criteria<Image, String> criteria)
            throws MalformedModelException, ModelNotFoundException, IOException {
        return PpModelZoo.loadModel(criteria);
    }

    @Bean(destroyMethod = "close")
    @Scope(value = "prototype", proxyMode = ScopedProxyMode.INTERFACES)
    public Predictor<Image, DetectedObjects> wordDetectionPredictor(ZooModel<Image, DetectedObjects> model) {
        return model.newPredictor();
    }

    @Bean(destroyMethod = "close")
    @Scope(value = "prototype", proxyMode = ScopedProxyMode.INTERFACES)
    public Predictor<Image, Classifications> wordDirectionModelPredictor(ZooModel<Image, Classifications> model) {
        return model.newPredictor();
    }

    @Bean(destroyMethod = "close")
    @Scope(value = "prototype", proxyMode = ScopedProxyMode.INTERFACES)
    public Predictor<Image, String> wordRecognitionPredictor(ZooModel<Image, String> model) {
        return model.newPredictor();
    }

    @Bean
    public Supplier<Predictor<Image, DetectedObjects>> wordDetectionPredictorProvider(ZooModel<Image, DetectedObjects> model) {
        return model::newPredictor;
    }

    @Bean
    public Supplier<Predictor<Image, Classifications>> wordDirectionModelPredictorProvider(ZooModel<Image, Classifications> model) {
        return model::newPredictor;
    }

    @Bean
    public Supplier<Predictor<Image, String>> wordRecognitionPredictorProvider(ZooModel<Image, String> model) {
        return model::newPredictor;
    }
}
