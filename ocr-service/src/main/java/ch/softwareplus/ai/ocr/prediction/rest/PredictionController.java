package ch.softwareplus.ai.ocr.prediction.rest;

import ch.softwareplus.ai.ocr.prediction.service.Detection;
import ch.softwareplus.ai.ocr.prediction.service.PredictionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "Prediction Resource")
@RestController
@RequiredArgsConstructor
@Slf4j
public class PredictionController {

    private final PredictionService predictionService;

    @PostMapping(path = "/predict", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public List<Detection> uploadFile(
            @RequestParam("file") MultipartFile multipartFile)
            throws Exception {

        var result = predictionService.predict(multipartFile.getInputStream());
        return result;
    }
}
