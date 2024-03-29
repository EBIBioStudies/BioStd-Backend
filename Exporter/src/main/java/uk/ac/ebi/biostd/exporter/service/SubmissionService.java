package uk.ac.ebi.biostd.exporter.service;

import static java.lang.Long.parseLong;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static uk.ac.ebi.biostd.exporter.jobs.full.job.PublicSubmissionFilter.PUBLIC_ACCESS_TAG;
import static uk.ac.ebi.biostd.exporter.utils.DateUtils.getFromEpochSeconds;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.ac.ebi.biostd.exporter.model.Attribute;
import uk.ac.ebi.biostd.exporter.model.File;
import uk.ac.ebi.biostd.exporter.model.Link;
import uk.ac.ebi.biostd.exporter.model.Section;
import uk.ac.ebi.biostd.exporter.model.Submission;
import uk.ac.ebi.biostd.exporter.persistence.dao.FilesDao;
import uk.ac.ebi.biostd.exporter.persistence.dao.LinksDao;
import uk.ac.ebi.biostd.exporter.persistence.dao.SectionDao;
import uk.ac.ebi.biostd.exporter.persistence.dao.StatsDao;
import uk.ac.ebi.biostd.exporter.persistence.dao.SubmissionDao;

@Slf4j
@Service
@AllArgsConstructor
public class SubmissionService {
    private final SubmissionDao submissionDao;
    private final SectionDao sectionDao;
    private final FilesDao filesDao;
    private final LinksDao linksDao;
    private final StatsDao statsDao;

    public List<Submission> getUpdatedSubmissions(long syncTime) {
        List<Submission> submissions = submissionDao.getUpdatedSubmissions(syncTime);
        submissions.forEach(this::processSubmission);
        return submissions;
    }

    public List<String> getDeletedSubmissions(long syncTime) {
        return submissionDao.getDeletedSubmissions(syncTime);
    }

    public Submission processSubmission(Submission submission) {
        log.debug("processing submissions with accno: '{}' and id '{}'", submission.getAccno(), submission.getId());

        List<String> accessTags = submissionDao.getAccessTags(submission.getId());
        List<Attribute> attributes = getAttributes(submission, accessTags);

        submission.setAccessTags(getAccessTags(submission, accessTags));
        submission.setAttributes(attributes);
        submission.setViews(statsDao.getViews(submission.getAccno()));

        if (submission.getRootSection_id() != 0) {
            submission.setSection(processSection(sectionDao.getSection(submission.getRootSection_id())));
        }

        return submission;
    }

    public Submission getSubmission(String accNo) {
        Submission submission = submissionDao.getSubmissionByAccNo(accNo);
        processSubmission(submission);
        return submission;
    }

    private List<String> getAccessTags(Submission submission, List<String> accessTags) {
        Set<String> tags = new HashSet<>();
        tags.add(submissionDao.getUserEmail(submission.getOwner_id()));
        tags.addAll(accessTags);
        tags.add("#" + submission.getOwner_id());
        if (submission.isReleased()) {
            tags.add(PUBLIC_ACCESS_TAG);
        }

        return new ArrayList<>(tags);
    }

    private List<Attribute> getAttributes(Submission submission, List<String> accessTags) {
        List<Attribute> subAttributes = submissionDao.getAttributes(submission.getId());
        String submissionTitle = submission.getTitle() != null ? submission.getTitle() : EMPTY;

        subAttributes.add(new Attribute("Title", submissionTitle));
        subAttributes.add(new Attribute("ReleaseDate", getFromEpochSeconds(parseLong(submission.getRtime()))));
        accessTags.forEach(tag -> addProjectAttribute(subAttributes, tag));

        return subAttributes;
    }

    private void addProjectAttribute(List<Attribute> subAttributes, String accessTag) {
        if (accessTag.equals("Public")) {
            return;
        }

        if (subAttributes.stream().noneMatch(attribute -> hasSameValue(attribute, accessTag))) {
            subAttributes.add(new Attribute("AttachTo", accessTag));
        }
    }

    private Boolean hasSameValue(Attribute attribute, String value) {
        return attribute.getValue() != null && attribute.getValue().equals(value);
    }

    private Section processSection(Section section) {
        long sectionId = section.getId();
        section.setAttributes(getSectionAttributes(section));
        section.setFiles(getSectionFiles(sectionId));
        section.setLinks(getSectionLinks(sectionId));
        section.setSubsections(getSubsections(sectionId));
        return section;
    }

    private List<Attribute> getSectionAttributes(Section section) {
        String fileListName = section.getFileListName();
        List<Attribute> attributes =
            sectionDao
                .getSectionAttributes(section.getId())
                .stream()
                .filter(attribute -> !attribute.getName().equals("File List"))
                .collect(toList());

        if (StringUtils.isNotEmpty(fileListName)) {
            attributes.add(new Attribute("File List", fileListName + ".json"));
        }

        return attributes;
    }

    private List<File> getSectionFiles(long sectionId) {
        return sectionDao.getSectionFiles(sectionId)
            .stream()
            .peek(file -> file.setAttributes(filesDao.getFilesAttributes(file.getId())))
            .collect(toList());
    }

    private List<Link> getSectionLinks(long sectionId) {
        return linksDao.getLinks(sectionId)
            .stream()
            .peek(link -> link.setAttributes(linksDao.getLinkAttributes(link.getId())))
            .collect(toList());
    }

    private List<Section> getSubsections(long sectionId) {
        return sectionDao.getSectionSections(sectionId)
                .stream()
                .map(this::processSection)
                .collect(toList());
    }
}
