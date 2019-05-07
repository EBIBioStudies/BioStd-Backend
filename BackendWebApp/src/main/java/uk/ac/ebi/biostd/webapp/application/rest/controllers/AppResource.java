package uk.ac.ebi.biostd.webapp.application.rest.controllers;

import lombok.AllArgsConstructor;
import org.springframework.boot.actuate.info.GitInfoContributor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class AppResource {

    private final GitInfoContributor buildProperties;

    @GetMapping("/build-properties")
    @ResponseBody
    public GitInfoContributor buildProperties() {
        return buildProperties;
    }
}
