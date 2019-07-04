package uk.ac.ebi.biostd.exporter.service;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.ac.ebi.biostd.exporter.model.Attribute;
import uk.ac.ebi.biostd.exporter.model.File;
import uk.ac.ebi.biostd.exporter.model.Link;
import uk.ac.ebi.biostd.exporter.model.Section;
import uk.ac.ebi.biostd.exporter.model.Submission;
import uk.ac.ebi.biostd.exporter.persistence.dao.FilesDao;
import uk.ac.ebi.biostd.exporter.persistence.dao.LinksDao;
import uk.ac.ebi.biostd.exporter.persistence.dao.SectionDao;
import uk.ac.ebi.biostd.exporter.persistence.dao.SubmissionDao;
import uk.ac.ebi.biostd.exporter.utils.DateUtils;

@Slf4j
@Service
@AllArgsConstructor
public class SubmissionService {
    private final SubmissionDao submissionDao;
    private final SectionDao sectionDao;
    private final FilesDao filesDao;
    private final LinksDao linksDao;

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
        List<Attribute> attributes = getAttributes(submission);

        submission.setAccessTags(getAccessTags(submission));
        submission.setAttributes(attributes);

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

    private List<String> getAccessTags(Submission submission) {
        List<String> tags = new ArrayList<>();
        tags.add(submissionDao.getUserEmail(submission.getOwner_id()));
        tags.addAll(submissionDao.getAccessTags(submission.getId()));
        tags.add("#" + submission.getOwner_id());

        return tags;
    }

    private List<Attribute> getAttributes(Submission submission) {
        List<Attribute> subAttributes = submissionDao.getAttributes(submission.getId());
        subAttributes.add(new Attribute("RootSubmissionPath", submission.getRelPath()));
        subAttributes
                .add(new Attribute("ReleaseDate", DateUtils.getFromEpochSeconds(Long.valueOf(submission.getRTime()))));
        subAttributes.add(new Attribute("Title", submission.getTitle()));
        return subAttributes;
    }

    private Section processSection(Section section) {
        long sectionId = section.getId();

        section.setAttributes(sectionDao.getSectionAttributes(section.getId()));
        section.setFiles(getSectionFiles(sectionId));
        section.setLinks(getSectionLinks(sectionId));
        section.setSubsections(getSubsections(sectionId));

        return section;
    }

    private List<File> getSectionFiles(long sectionId) {
        return sectionDao.getSectionFiles(sectionId)
                .stream()
                .map(file -> {
                    file.setAttributes(filesDao.getFilesAttributes(file.getId()));
                    return file;
                })
                .collect(toList());
    }

    private List<Link> getSectionLinks(long sectionId) {
        return linksDao.getLinks(sectionId)
                .stream()
                .map(link -> {
                    link.setAttributes(linksDao.getLinkAttributes(link.getId()));
                    return link;
                })
                .collect(toList());
    }

    private List<Section> getSubsections(long sectionId) {
        return sectionDao.getSectionSections(sectionId)
                .stream()
                .map(subsection -> processSection(subsection))
                .collect(toList());
    }
}
