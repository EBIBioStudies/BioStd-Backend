package uk.ac.ebi.biostd.webapp.application.validation.eutoxrisk.services;

import org.junit.Test;
import uk.ac.ebi.biostd.backend.testing.ResourceHandler;
import uk.ac.ebi.biostd.webapp.application.validation.eutoxrisk.dto.EUToxRiskFileValidationError;

import java.util.Collection;
/**
 * @author Olga Melnichuk
 */
public class EUToxRiskValidatorTest {

    @Test
    public void testValidFile() {

//            Collection<EUToxRiskFileValidationError> errors = new EUToxRiskFileValidator(template, url)
 //                   .validate(
  //                          ResourceHandler.getResourceFile("/input/toxrisk_datafile_valid.xlsx"));
           // assertTrue(errors.isEmpty());

    }

    @Test
    public void testInValidFile() {
           // Collection<EUToxRiskFileValidationError> errors = new EUToxRiskFileValidator(template, url)
             //       .validate(
              //              ResourceHandler.getResourceFile("/input/toxrisk_datafile_invalid.xlsx"));

           // assertThat(errors).hasSize(3);
           // assertEquals(errors.size(), 1);

    }
}
