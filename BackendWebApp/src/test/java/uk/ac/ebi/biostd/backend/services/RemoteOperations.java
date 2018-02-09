package uk.ac.ebi.biostd.backend.services;


import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static uk.ac.ebi.biostd.backend.parsing.PlainFileParser.LINE_BREAKS_SEPARATOR;
import static uk.ac.ebi.biostd.backend.parsing.PlainFileParser.SEMICOLON_SEPARATOR;

import java.io.File;
import java.nio.file.Files;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map;
import lombok.SneakyThrows;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import uk.ac.ebi.biostd.backend.model.LoginResponse;
import uk.ac.ebi.biostd.backend.model.SubmissionResult;
import uk.ac.ebi.biostd.backend.parsing.PlainFileParser;

public class RemoteOperations {

    private static final String BASE_URL = "http://localhost:%d/biostd/";
    private static final String LOGIN_URL = BASE_URL + "auth/signin?login=%s&password=%s";
    private static final String SUBMIT_URL = BASE_URL + "submit/createupdate?BIOSTDSESS=%s";

    private final RestTemplate restTemplate;
    private final PlainFileParser fileParser;
    private final int applicationPort;

    public RemoteOperations(RestTemplate restTemplate, int applicationPort) {
        this.restTemplate = restTemplate;
        this.applicationPort = applicationPort;
        fileParser = PlainFileParser.builder()
                .lineSeparator(LINE_BREAKS_SEPARATOR)
                .valuesSeparator(SEMICOLON_SEPARATOR)
                .build();
    }

    public LoginResponse login(String user, String password) {
        String response = restTemplate
                .getForObject(String.format(LOGIN_URL, applicationPort, user, password), String.class);
        return LoginResponse.fromList(fileParser.parseFile(response));
    }

    public void refreshCache() {
        String refreshCache = String.format(BASE_URL, applicationPort) + "tools/REFRESH_USERS";
        restTemplate.exchange(refreshCache, HttpMethod.GET, null, String.class);
    }

    @SneakyThrows
    public SubmissionResult createFileSubmission(String sessId, File file) {
        return createSubmission(sessId, Files.readAllBytes(file.toPath()),
                new SimpleEntry<>(CONTENT_TYPE, "application/vnd.ms-excel"));
    }

    public SubmissionResult createJsonSubmission(String sessId, String jsonBody) {
        return createSubmission(sessId, jsonBody, new SimpleEntry<>(CONTENT_TYPE, "application/json"));
    }

    private <T> SubmissionResult createSubmission(String sessId, T body, Map.Entry<String, String> header) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(header.getKey(), header.getValue());
        HttpEntity<T> entity = new HttpEntity<>(body, headers);
        String url = String.format(SUBMIT_URL, applicationPort, sessId);
        ResponseEntity<SubmissionResult> response = restTemplate.postForEntity(url, entity, SubmissionResult.class);
        return response.getBody();
    }

}
