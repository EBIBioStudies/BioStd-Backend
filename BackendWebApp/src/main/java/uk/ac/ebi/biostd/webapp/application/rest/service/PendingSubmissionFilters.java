package uk.ac.ebi.biostd.webapp.application.rest.service;

import uk.ac.ebi.biostd.webapp.application.rest.dto.PendingSubmissionListItemDto;
import uk.ac.ebi.biostd.webapp.application.rest.dto.SubmissionListFiltersDto;

import java.util.function.Predicate;

public class PendingSubmissionFilters {

    public PendingSubmissionFilters(SubmissionListFiltersDto filters) {
     //TODO
    }

    public Predicate<? super PendingSubmissionListItemDto> asPredicate() {
        //TODO
        return null;
    }
}
