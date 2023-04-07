package ch.softwareplus.ai.ocr.config;

import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.client5.http.socket.PlainConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.TrustSelfSignedStrategy;
import org.apache.hc.core5.http.config.Registry;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.ssl.SSLContexts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

/**
 * This class represents the http client configuration.
 */
@Configuration
public class HttpClientConfig {

    @Value("${http.client.pool.maxTotal:10}")
    private Integer poolMaxTotal;
    @Value("${http.client.timeout.connect:5000}")
    private Integer connectTimeout;
    @Value("${http.client.timeout.connectRequest:5000}")
    private Integer connectRequest;
    @Value("${http.client.timeout.read:15000}")
    private Integer readTimeout;

    @Value("${http.client.proxy.url:}")
    private String httpProxyUrl;
    @Value("${http.client.proxy.port:0}")
    private int httpProxyPort;
    @Value("${http.client.proxy.username:}")
    private String httpProxyUsername;
    @Value("${http.client.proxy.password:}")
    private String httpProxyPassword;

    @Bean
    public ClientHttpRequestFactory httpRequestFactory() throws Exception {
        var requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient());
        requestFactory.setConnectTimeout(connectTimeout);
        //requestFactory.setReadTimeout(readTimeout);
        requestFactory.setConnectionRequestTimeout(connectRequest);
        return new BufferingClientHttpRequestFactory(requestFactory);
    }

    @Bean
    public HttpClient httpClient() throws Exception {
        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.getSocketFactory())
                .register("https", createSSLConnectionSocketFactory()).build();

        var connectionManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
        connectionManager.setMaxTotal(poolMaxTotal);

        var builder = HttpClientBuilder
                .create()
                .setConnectionManager(connectionManager);

        return builder.build();

    }

    private SSLConnectionSocketFactory createSSLConnectionSocketFactory()
            throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
        var sslContext = SSLContexts.custom().loadTrustMaterial(null, new TrustSelfSignedStrategy())
                .build();
        return new SSLConnectionSocketFactory(sslContext);
    }
}
