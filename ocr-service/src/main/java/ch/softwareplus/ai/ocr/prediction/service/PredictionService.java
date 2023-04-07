package ch.softwareplus.ai.ocr.prediction.service;

import ai.djl.inference.Predictor;
import ai.djl.modality.Classifications;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.output.BoundingBox;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.modality.cv.util.NDImageUtils;
import ai.djl.ndarray.NDManager;
import ai.djl.translate.TranslateException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
@Slf4j
public class PredictionService {

    private final Supplier<Predictor<Image, DetectedObjects>> wordDetectionProvider;
    private final Supplier<Predictor<Image, Classifications>> wordDirectionModelProvider;
    private final Supplier<Predictor<Image, String>> wordRecognitionProvider;
    private final ImageFactory imageFactory;

    public List<Detection> predict(InputStream is) {
        var result = new LinkedList<Detection>();
        try (var detector = wordDetectionProvider.get()) {
            // get image from input stream
            var img = imageFactory.fromInputStream(is);
            // first of all detect all words
            var detections = detector.predict(img);
            List<DetectedObjects.DetectedObject> boxes = detections.items();
            log.debug("Detected {} object(s) on image", detections.getNumberOfObjects());

            // iterate through every detected box
            for (int i = 0; i < boxes.size(); i++) {
                var obj = boxes.get(i);
                var subImg = getSubImage(img, obj.getBoundingBox());
                if (subImg.getHeight() * 1.0 / subImg.getWidth() > 1.5) {
                    subImg = rotate(subImg);
                }
                try (var rotator = wordDirectionModelProvider.get()) {
                    Classifications.Classification classif = rotator.predict(subImg).get("Rotate");
                    if ("Rotate".equals(classif.getClassName()) && classif.getProbability() > 0.8) {
                        subImg = rotate(subImg);
                    }
                    try (var predictor = wordRecognitionProvider.get()) {
                        var name = predictor.predict(subImg);

                        result.add(Detection.builder()
                                .name(name)
                                .probability(-1.0)
                                //.boundingBox(obj.getBoundingBox())
                                .build());

                    } catch (TranslateException ex) {
                        log.error("An translate error occurred", ex);
                    }
                } catch (TranslateException ex) {
                    log.error("An translate error occurred", ex);
                }
            }
        } catch (IOException ex) {
            log.error("An I/O error occurred", ex);
        } catch (TranslateException ex) {
            log.error("An translate error occurred", ex);
        }
        return result;
    }

    private Image rotate(Image image) {
        try (NDManager manager = NDManager.newBaseManager()) {
            var rotated = NDImageUtils.rotate90(image.toNDArray(manager), 1);
            return imageFactory.fromNDArray(rotated);
        }
    }

    private Image getSubImage(Image img, BoundingBox box) {
        var rect = box.getBounds();
        double[] extended = extendRect(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
        int width = img.getWidth();
        int height = img.getHeight();
        int[] recovered = {
                (int) (extended[0] * width),
                (int) (extended[1] * height),
                (int) (extended[2] * width),
                (int) (extended[3] * height)
        };
        return img.getSubImage(recovered[0], recovered[1], recovered[2], recovered[3]);
    }

    private double[] extendRect(double xmin, double ymin, double width, double height) {
        double centerx = xmin + width / 2;
        double centery = ymin + height / 2;
        if (width > height) {
            width += height * 2.0;
            height *= 3.0;
        } else {
            height += width * 2.0;
            width *= 3.0;
        }
        double newX = centerx - width / 2 < 0 ? 0 : centerx - width / 2;
        double newY = centery - height / 2 < 0 ? 0 : centery - height / 2;
        double newWidth = newX + width > 1 ? 1 - newX : width;
        double newHeight = newY + height > 1 ? 1 - newY : height;
        return new double[]{newX, newY, newWidth, newHeight};
    }

}
