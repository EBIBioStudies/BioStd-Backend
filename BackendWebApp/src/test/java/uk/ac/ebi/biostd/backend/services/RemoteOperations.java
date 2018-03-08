package uk.ac.ebi.biostd.backend.services;


import static java.lang.String.format;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;

import java.io.File;
import java.nio.file.Files;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map;
import lombok.SneakyThrows;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import uk.ac.ebi.biostd.backend.model.SubmissionResult;
import uk.ac.ebi.biostd.webapp.application.security.entities.SignInRequest;
import uk.ac.ebi.biostd.webapp.application.security.rest.dto.LoginResponseDto;

public class RemoteOperations {

    private static final String LOGIN_URL = "/auth/signin";
    private static final String SUBMIT_URL = "/submit/createupdate?BIOSTDSESS=%s";

    private final TestRestTemplate restTemplate;

    public RemoteOperations(TestRestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public LoginResponseDto login(String user, String password) {
        SignInRequest signInRequest = new SignInRequest();
        signInRequest.setLogin(user);
        signInRequest.setPassword(password);

        return restTemplate.postForObject(format(LOGIN_URL), signInRequest, LoginResponseDto.class);
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
        ResponseEntity<SubmissionResult> response = restTemplate.postForEntity(
                format(SUBMIT_URL, sessId),
                new HttpEntity<>(body, headers),
                SubmissionResult.class);
        return response.getBody();
    }

}
