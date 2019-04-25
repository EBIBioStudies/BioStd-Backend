package uk.ac.ebi.biostd.webapp.application.rest.controllers;

import lombok.AllArgsConstructor;
import org.springframework.boot.info.BuildProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class AppResource {

    private final BuildProperties buildProperties;

    @GetMapping("/build-properties")
    @ResponseBody
    public BuildProperties buildProperties() {
        return buildProperties;
    }
}
