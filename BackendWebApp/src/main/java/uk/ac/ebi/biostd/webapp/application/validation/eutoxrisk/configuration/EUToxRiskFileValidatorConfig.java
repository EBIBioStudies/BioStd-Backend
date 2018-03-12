package uk.ac.ebi.biostd.webapp.application.validation.eutoxrisk.configuration;

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestTemplate;
import uk.ac.ebi.biostd.webapp.application.validation.eutoxrisk.common.EUToxRiskFileValidationException;
import uk.ac.ebi.biostd.webapp.application.validation.eutoxrisk.services.EUToxRiskFileValidator;

import javax.net.ssl.SSLContext;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

@Configuration
public class EUToxRiskFileValidatorConfig {

    @Value("${eutoxrisk-file-validator.threadpool.corepoolsize:1}")
    private int corePoolSize;

    @Value("${eutoxrisk-file-validator.threadpool.maxpoolsize:1}")
    private int maxPoolSize;

    @Bean
    @Qualifier("eutoxrisk-file-validator.RestTemplate")
    public RestTemplate restTemplate(SSLContext sslContext) throws EUToxRiskFileValidationException {
        SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext);

        CloseableHttpClient httpClient = HttpClients.custom()
                .setSSLSocketFactory(csf)
                .build();

        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        requestFactory.setHttpClient(httpClient);

        return new RestTemplate(requestFactory);
    }

    @Bean
    public SSLContext sslContext() throws EUToxRiskFileValidationException {
        try {
            TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;

            return org.apache.http.ssl.SSLContexts.custom()
                    .loadTrustMaterial(null, acceptingTrustStrategy)
                    .build();
        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
            throw new EUToxRiskFileValidationException(e);
        }
    }

    @Bean
    @Qualifier("eutoxrisk-file-validator.TaskExecutor")
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor pool = new ThreadPoolTaskExecutor();
        pool.setCorePoolSize(corePoolSize);
        pool.setMaxPoolSize(maxPoolSize);
        pool.setWaitForTasksToCompleteOnShutdown(false);
        return pool;
    }
}
