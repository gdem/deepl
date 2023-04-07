package ch.softwareplus.ai.ocr.prediction.service;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Detection {
    private String name;
    private Double probability;
}
