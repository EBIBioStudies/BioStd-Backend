package uk.ac.ebi.biostd.webapp.application.rest.controllers;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.ac.ebi.biostd.authz.User;
import uk.ac.ebi.biostd.webapp.application.domain.services.UserDataService;
import uk.ac.ebi.biostd.webapp.application.rest.dto.UserDataDto;
import uk.ac.ebi.biostd.webapp.application.rest.mappers.UserDataMapper;

@AllArgsConstructor
@RestController
@PreAuthorize("isAuthenticated()")
public class UserDataResource {

    private final UserDataService userDataService;
    private final UserDataMapper userDataMapper;

    @GetMapping("/userdata/get")
    public ResponseEntity<UserDataDto> getData(
            @RequestParam(name = "key") String key,
            @AuthenticationPrincipal uk.ac.ebi.biostd.authz.User user) {
        return userDataService.findByUserAndKey(user.getId(), key)
                .map(userDataMapper::map)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.ok().build());
    }

    @PostMapping("/userdata/del")
    public void delete(
            @RequestParam(name = "topic") String topic,
            @RequestParam(name = "key") String key,
            @AuthenticationPrincipal uk.ac.ebi.biostd.authz.User user) {
        userDataService.deleteModifiedSubmission(user.getId(), key);
    }

    @GetMapping("/userdata/listjson")
    public List<UserDataDto> getTopicData(
            @RequestParam(required = false, name = "topic") String topic,
            @AuthenticationPrincipal uk.ac.ebi.biostd.authz.User user) {
        return userDataMapper.map(userDataService.findAllByUserAndTopic(user.getId(), topic));
    }

    @PostMapping("/userdata/set")
    public UserDataDto save(
            @RequestParam(name = "topic") String topic,
            @RequestParam(name = "key") String key,
            @RequestParam(name = "value") String data,
            @AuthenticationPrincipal User user) {
        return userDataMapper.map(userDataService.update(user.getId(), key, data, topic));
    }
}
