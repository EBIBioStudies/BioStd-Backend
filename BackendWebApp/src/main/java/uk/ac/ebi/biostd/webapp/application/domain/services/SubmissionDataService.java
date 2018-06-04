package uk.ac.ebi.biostd.webapp.application.domain.services;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import uk.ac.ebi.biostd.webapp.application.domain.model.SubmissionFilter;
import uk.ac.ebi.biostd.webapp.application.persitence.common.OffsetLimitPageable;
import uk.ac.ebi.biostd.webapp.application.persitence.entities.Submission;
import uk.ac.ebi.biostd.webapp.application.persitence.repositories.SubmissionRepository;

@AllArgsConstructor
@Service
public class SubmissionDataService {

    private final SubmissionRepository submissionRepository;

    public List<Submission> getSubmissionsByUser(long userId, SubmissionFilter filter) {
        Specification<Submission> filterSpec = Specification.where(
                withUser(userId)).and(withVersionGretherThan(0));

        if (filter.hasAccession()) {
            filterSpec = filterSpec.and(withAccession(filter.getAccNo()));
        }

        if (filter.hasFromDate()) {
            filterSpec = filterSpec.and(withFrom(filter.getRTimeFrom()));
        }

        if (filter.hasToDate()) {
            filterSpec = filterSpec.and(withTo(filter.getRTimeTo()));
        }

        if (filter.hasKeyWords()) {
            filterSpec = filterSpec.and(withTitleLike(filter.getKeywords()));
        }

        return submissionRepository.findAll(filterSpec, new OffsetLimitPageable(filter.getOffset(),
                filter.getLimit(), Sort.by("rTime").descending())).getContent();
    }


    private static Specification<Submission> withVersionGretherThan(int version) {
        return (root, query, cb) -> cb.greaterThan(root.get("version"), version);
    }

    private static Specification<Submission> withAccession(String accNo) {
        return (root, query, cb) -> cb.equal(root.get("accNo"), accNo);
    }

    private static Specification<Submission> withTitleLike(String title) {
        return (root, query, cb) -> cb.like(cb.lower(root.get("title")), "%" + title.toLowerCase() + "%");
    }

    private static Specification<Submission> withUser(long user) {
        return (root, query, cb) -> cb.equal(root.get("ownerId"), user);
    }

    private static Specification<Submission> withFrom(long from) {
        return (root, query, cb) -> cb.greaterThan(root.get("rTime"), from);
    }

    private static Specification<Submission> withTo(long to) {
        return (root, query, cb) -> cb.lessThan(root.get("rTime"), to);
    }
}
