package uk.ac.ebi.biostd.remote.service;

import static java.util.Collections.singletonMap;
import static java.util.stream.Collectors.toList;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;

import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriTemplateHandler;
import uk.ac.ebi.biostd.remote.dto.LoginResponseDto;
import uk.ac.ebi.biostd.remote.dto.SignInRequestDto;
import uk.ac.ebi.biostd.remote.dto.SubmissionResultDto;

public class RemoteService {

    private static final String LOGIN_URL = "/auth/signin";
    private static final String SUBMIT_URL = "/submit/createupdate";
    private static final String SESSION_PARAM = "BIOSTDSESS";

    private final RestTemplate restTemplate;

    public RemoteService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public RemoteService(String baseUrl) {
        DefaultUriTemplateHandler defaultUriTemplateHandler = new DefaultUriTemplateHandler();
        defaultUriTemplateHandler.setBaseUrl(baseUrl);
        restTemplate = new RestTemplate();
        restTemplate.setUriTemplateHandler(defaultUriTemplateHandler);
    }

    public LoginResponseDto login(String user, String password) {
        SignInRequestDto signInRequest = new SignInRequestDto();
        signInRequest.setLogin(user);
        signInRequest.setPassword(password);

        return restTemplate.postForObject(LOGIN_URL, signInRequest, LoginResponseDto.class);
    }

    public SubmissionResultDto createJsonSubmission(String sessId, String jsonBody) {
        return createSubmission(
                jsonBody,
                singletonMap(CONTENT_TYPE, "application/json"),
                singletonMap(SESSION_PARAM, sessId));
    }

    private SubmissionResultDto createSubmission(
            Object body, Map<String, String> headersMap, Map<String, String> paramsMap) {

        HttpHeaders headers = new HttpHeaders();
        headersMap.forEach(headers::set);
        String url = getUrl(SUBMIT_URL, paramsMap);

        ResponseEntity<SubmissionResultDto> response =
                restTemplate.postForEntity(url, new HttpEntity<>(body, headers), SubmissionResultDto.class);
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
