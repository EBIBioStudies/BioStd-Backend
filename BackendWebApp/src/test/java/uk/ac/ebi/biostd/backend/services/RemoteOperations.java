package uk.ac.ebi.biostd.backend.services;


import static java.lang.String.format;
import static java.util.Collections.singletonMap;
import static java.util.stream.Collectors.toList;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;

import java.io.File;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import uk.ac.ebi.biostd.backend.model.SubmissionResult;
import uk.ac.ebi.biostd.webapp.application.security.rest.dto.LoginResponseDto;
import uk.ac.ebi.biostd.webapp.application.security.rest.dto.SignInRequestDto;

public class RemoteOperations {

    private static final String LOGIN_URL = "/auth/signin";
    private static final String SUBMIT_URL = "/submit/createupdate";
    private static final String SESSION_PARAM = "BIOSTDSESS";

    private final TestRestTemplate restTemplate;

    public RemoteOperations(TestRestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public LoginResponseDto login(String user, String password) {
        SignInRequestDto signInRequest = new SignInRequestDto();
        signInRequest.setLogin(user);
        signInRequest.setPassword(password);

        return restTemplate.postForObject(format(LOGIN_URL), signInRequest, LoginResponseDto.class);
    }

    @SneakyThrows
    public SubmissionResult createFileSubmission(String sessId, File file) {
        return createSubmission(
                Files.readAllBytes(file.toPath()),
                singletonMap(CONTENT_TYPE, "application/vnd.ms-excel"),
                Collections.singletonMap(SESSION_PARAM, sessId));
    }

    public SubmissionResult createJsonSubmission(String sessId, String jsonBody) {
        return createSubmission(
                jsonBody,
                singletonMap(CONTENT_TYPE, "application/json"),
                singletonMap(SESSION_PARAM, sessId));
    }

    public SubmissionResult createJsonSubmission(String jsonBody, Map<String, String> paramsMap) {
        return createSubmission(
                jsonBody,
                Collections.singletonMap(CONTENT_TYPE, "application/json"),
                paramsMap);
    }

    private SubmissionResult createSubmission(
            Object body, Map<String, String> headersMap, Map<String, String> paramsMap) {

        HttpHeaders headers = new HttpHeaders();
        headersMap.forEach(headers::set);
        String url = getUrl(SUBMIT_URL, paramsMap);

        ResponseEntity<SubmissionResult> response =
                restTemplate.postForEntity(url, new HttpEntity<>(body, headers), SubmissionResult.class);
        return response.getBody();
    }

    private String getUrl(String url, Map<String, String> paramsMap) {
        if (paramsMap.isEmpty()) {
            return url;
        }

        List<String> params = paramsMap.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(toList());
        return url + "?" + StringUtils.join(params, "&");
    }


}
