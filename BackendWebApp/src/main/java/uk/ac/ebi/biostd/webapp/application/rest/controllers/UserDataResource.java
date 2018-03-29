package uk.ac.ebi.biostd.webapp.application.rest.controllers;

import static java.util.stream.Collectors.toList;

import lombok.AllArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseEntity.BodyBuilder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import uk.ac.ebi.biostd.authz.User;
import uk.ac.ebi.biostd.webapp.application.domain.services.UserDataService;
import uk.ac.ebi.biostd.webapp.application.persitence.entities.UserData;
import uk.ac.ebi.biostd.webapp.application.rest.mappers.UserDataMapper;

@AllArgsConstructor
@RestController
@PreAuthorize("isAuthenticated()")
public class UserDataResource {

    private final UserDataService userDataService;
    private final UserDataMapper userDataMapper;

    @GetMapping("/userdata/get")
    public ResponseEntity<String> getData(
            @RequestParam(name = "key") String key,
            @AuthenticationPrincipal uk.ac.ebi.biostd.authz.User user) {

        BodyBuilder builder = ResponseEntity.ok().cacheControl(CacheControl.noCache());
        return userDataService.findByUserAndKey(user.getId(), key)
                .map(UserData::getData)
                .map(builder::body)
                .orElse(builder.build());
    }

    @PostMapping("/userdata/del")
    public void delete(
            @RequestParam(name = "topic") String topic,
            @RequestParam(name = "key") String key,
            @AuthenticationPrincipal uk.ac.ebi.biostd.authz.User user) {
        userDataService.deleteModifiedSubmission(user.getId(), key);
    }

    @GetMapping("/userdata/listjson")
    public ResponseEntity<String> getTopicData(
            @RequestParam(required = false, name = "topic") String topic,
            @AuthenticationPrincipal uk.ac.ebi.biostd.authz.User user) {
        String response = userDataService.findAllByUserAndTopic(user.getId(), topic)
                .stream()
                .map(UserData::getData)
                .collect(toList()).toString();
        return ResponseEntity.ok().cacheControl(CacheControl.noCache()).body(response);
    }

    @PostMapping("/userdata/set")
    public @ResponseBody String save(
            @RequestParam(name = "topic") String topic,
            @RequestParam(name = "key") String key,
            @RequestParam(name = "value") String data,
            @AuthenticationPrincipal User user) {
        return userDataService.update(user.getId(), key, data, topic).getData();
    }
}
