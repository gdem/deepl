package ch.softwareplus.ai.ocr.config.cors;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

/**
 * This class is a configuration properties for "cors" settings.
 */
@Data
@ConfigurationProperties(prefix = "cors")
public class CorsProperties {

  private List<Cors> rules = new ArrayList<>();

  @Data
  public static class Cors {

    private String path;
    private boolean allowedCredentials = false;
    private List<String> allowedOrigins = new ArrayList<>();
    private List<String> allowedHeaders = new ArrayList<>();
    private List<String> allowedMethods = new ArrayList<>();
    private List<String> exposedHeaders = new ArrayList<>();
  }
}
