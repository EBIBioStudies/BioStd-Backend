package uk.ac.ebi.biostd.db;

import java.util.HashMap;
import java.util.Map;
import uk.ac.ebi.biostd.authz.AccessTag;
import uk.ac.ebi.biostd.authz.Classifier;
import uk.ac.ebi.biostd.authz.Tag;

public class AdHocTagResolver implements TagResolver {

    private final Map<String, Classifier> clsfMap = new HashMap<>();
    private final Map<String, AccessTag> accTagMap = new HashMap<>();

    private int idGen = 1;

    @Override
    public Tag getTagByName(String clsfName, String tagName) {
        Classifier classifier = clsfMap.get(clsfName);
        Tag tag = null;

        if (classifier == null) {
            clsfMap.put(clsfName, classifier = new Classifier());
            classifier.setName(clsfName);
        } else {
            tag = classifier.getTag(tagName);
        }

        if (tag == null) {
            tag = new Tag();

            tag.setName(tagName);
            tag.setId(idGen++);

            tag.setClassifier(classifier);
            classifier.addTag(tag);
        }

        return tag;
    }

    @Override
    public AccessTag getAccessTagByName(String tagName) {
        AccessTag acct = accTagMap.get(tagName);
        if (acct == null) {
            accTagMap.put(tagName, acct = new AccessTag());

            acct.setId(idGen++);
            acct.setName(tagName);
        }

        return acct;
    }

}
