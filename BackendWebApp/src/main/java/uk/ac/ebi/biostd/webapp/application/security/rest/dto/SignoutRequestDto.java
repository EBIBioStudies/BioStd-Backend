package uk.ac.ebi.biostd.webapp.application.security.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SignoutRequestDto {

    private String sessid;
}
