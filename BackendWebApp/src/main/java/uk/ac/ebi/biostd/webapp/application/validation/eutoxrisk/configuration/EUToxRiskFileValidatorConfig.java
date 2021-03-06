package uk.ac.ebi.biostd.webapp.application.validation.eutoxrisk.configuration;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import javax.net.ssl.SSLContext;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestTemplate;
import uk.ac.ebi.biostd.webapp.application.validation.eutoxrisk.common.EUToxRiskFileValidationException;

@Configuration
public class EUToxRiskFileValidatorConfig {

    private static final int CORE_POOL_SIZE = 1;
    private static final int MAX_POOL_SIZE = 10;

    @Bean
    @Qualifier("eutoxrisk-file-validator-rest-template")
    public RestTemplate restTemplate(SSLContext sslContext) throws EUToxRiskFileValidationException {
        CloseableHttpClient httpClient = HttpClients.custom()
                .setSSLSocketFactory(new SSLConnectionSocketFactory(sslContext))
                .build();

        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        requestFactory.setHttpClient(httpClient);

        RestTemplate restTemplate = new RestTemplate(requestFactory);
        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        return restTemplate;
    }

    @Bean
    public SSLContext sslContext() throws EUToxRiskFileValidationException {
        try {
            TrustStrategy acceptingTrustStrategy = (chain, authType) -> true;

            return org.apache.http.ssl.SSLContexts.custom()
                    .loadTrustMaterial(null, acceptingTrustStrategy)
                    .build();
        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
            throw new EUToxRiskFileValidationException(e);
        }
    }

    @Bean
    @Qualifier("eutoxrisk-file-validator-executor")
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor pool = new ThreadPoolTaskExecutor();
        pool.setCorePoolSize(CORE_POOL_SIZE);
        pool.setMaxPoolSize(MAX_POOL_SIZE);
        pool.setWaitForTasksToCompleteOnShutdown(false);
        return pool;
    }

    @Bean
    @ConfigurationProperties("biostd.eutoxrisk-file-validator")
    public EUToxRiskFileValidatorProperties validatorProperties() {
        return new EUToxRiskFileValidatorProperties();
    }
}
