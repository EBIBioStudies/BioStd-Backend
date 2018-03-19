/**
 * Copyright 2014-2017 Functional Genomics Development Team, European Bioinformatics Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 * @author Andrew Tikhonov <andrew.tikhonov@gmail.com>
 **/
package uk.ac.ebi.biostd.webapp.server.mng.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.TypedQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.biostd.authz.AttributeSubscription;
import uk.ac.ebi.biostd.authz.AttributeSubscriptionMatchEvent;
import uk.ac.ebi.biostd.model.AbstractAttribute;
import uk.ac.ebi.biostd.model.FileRef;
import uk.ac.ebi.biostd.model.Link;
import uk.ac.ebi.biostd.model.Section;
import uk.ac.ebi.biostd.model.Submission;
import uk.ac.ebi.biostd.webapp.server.config.BackendConfig;


/**
 * Created by andrew on 24/03/2017.
 */

public class AttributeSubscriptionProcessor implements Runnable {

    public static final int IDLE_TIME_SEC = 30;
    public static final String AttributePlaceHolderRx = "\\{ATTRIBUTE\\}";
    public static final String PatternPlaceHolderRx = "\\{PATTERN\\}";
    public static final String SubscriptionPlaceHolderRx = "\\{SUBSCRIPTIONS\\}";
    public static final String ResultsPlaceHolderRx = "\\{RESULTS\\}";
    public static final String SubscriptionStartTag = "{SUBSCRIPTIONS}";
    public static final String SubscriptionEndTag = "{/SUBSCRIPTIONS}";
    public static final String ResultsStartTag = "{RESULTS}";
    public static final String ResultsEndTag = "{/RESULTS}";
    private static boolean isProcessorRunning = true;
    private static Logger log = null;
    private static LinkedBlockingQueue<ProcessorRequest> queue;

    private static Logger logger() {
        if (log == null) {
            log = LoggerFactory.getLogger(AttributeSubscriptionProcessor.class);
        }

        return log;
    }

    public static void processAsync(Submission submission) {

        ProcessorRequest request = new ProcessorRequest();
        request.text = submission.getTitle();
        request.accNo = submission.getAccNo();
        request.sbmId = submission.getId();

        while (true) {
            try {
                getQueue().put(request);
                break;
            } catch (InterruptedException e) {
            }
        }

    }

    private static synchronized BlockingQueue<ProcessorRequest> getQueue() {
        if (queue == null) {
            queue = new LinkedBlockingQueue<>();
            new Thread(new AttributeSubscriptionProcessor(), "AttributeSubscriptionProcessor").start();
        }

        return queue;
    }

    private static synchronized void destroyQueue() {
        if (queue != null) {
            queue.clear();
        }

        queue = null;
    }

    public static EmailTemplates parseMessage(String text) {
        EmailTemplates parts = new EmailTemplates();

        int resultStart = text.indexOf(ResultsStartTag);
        int resultEnd = text.indexOf(ResultsEndTag);

        if (resultStart != -1 && resultEnd != -1) {
            parts.result = text.substring(resultStart +
                    ResultsStartTag.length(), resultEnd);

            text = text.substring(0, resultStart + ResultsStartTag.length()) +
                    text.substring(resultEnd + ResultsEndTag.length());
        }

        int subscriptionStart = text.indexOf(SubscriptionStartTag);
        int subscriptionEnd = text.indexOf(SubscriptionEndTag);

        if (subscriptionStart != -1 && subscriptionEnd != -1) {
            parts.subscription = text.substring(subscriptionStart +
                    SubscriptionStartTag.length(), subscriptionEnd);

            text = text.substring(0, subscriptionStart + SubscriptionStartTag.length()) +
                    text.substring(subscriptionEnd + SubscriptionEndTag.length());

        }

        parts.mainBody = text;

        return parts;
    }

    @Override
    public void run() {
        ProcessorRequest request = null;

        EntityManager entityMan = BackendConfig.getEntityManagerFactory().createEntityManager();
        TypedQuery<Submission> query = entityMan.createNamedQuery(Submission.GetByIdQuery, Submission.class);

        while (isProcessorRunning) {

            while (isProcessorRunning) {
                try {
                    request = null;
                    request = queue.poll(IDLE_TIME_SEC, TimeUnit.SECONDS);
                    break;
                } catch (InterruptedException e) {
                }
            }

            if (request == null) {
                destroyQueue();
                break;
            }

            query.setParameter("id", request.sbmId);
            List<Submission> submissions = query.getResultList();

            if (submissions.size() != 1) {
                logger().warn(
                        "AttributeSubscriptionProcessor: submission not found or multiple results id=" + request.sbmId);
                continue;
            }

            Submission subm = submissions.get(0);

            processOneSubmission(entityMan, subm);
        }

        entityMan.close();

    }

    private AttributeContainer collectAttributes(AttributeContainer container, String accession,
            String sectionName, List<? extends AbstractAttribute> attributes) {
        container.nameset.add(sectionName);
        if (accession != null) {
            container.map.put(sectionName, accession);
        }

        for (AbstractAttribute a : attributes) {
            String name = a.getName();
            String key = sectionName + "." + name;

            String value0 = container.map.get(key);
            container.map.put(key,
                    value0 == null ? a.getValue() :
                            value0 + "," + a.getValue());
            container.nameset.add(name);
        }

        return container;
    }

    private void traverseSections(AttributeContainer container, Section section) {

        collectAttributes(container, section.getAccNo(), section.getType(), section.getAttributes());

        for (FileRef ref : section.getFileRefs()) {
            collectAttributes(container, ref.getName(), "File", ref.getAttributes());
        }

        for (Link l : section.getLinks()) {
            collectAttributes(container, l.getUrl(), "Links", l.getAttributes());
        }

        for (Section s : section.getSections()) {
            traverseSections(container, s);
        }
    }

    private void processOneSubmission(EntityManager entityMan, Submission submission) {

        try {
            AttributeContainer container = new AttributeContainer();

            // collect submission acttributes
            //
            collectAttributes(container, submission.getAccNo(),
                    "Submission", submission.getAttributes());

            // traverse sections and collect attributes
            //
            Section rootSection = submission.getRootSection();
            if (rootSection != null) {
                traverseSections(container, rootSection);
            }

            // get defined subscriptions
            //
            TypedQuery<AttributeSubscription> subscriptionQuery = entityMan.createNamedQuery(
                    AttributeSubscription.GetAllByAttributeQuery, AttributeSubscription.class);
            subscriptionQuery.setParameter(AttributeSubscription.AttributeQueryParameter, container.nameset);
            List<AttributeSubscription> subscriptionList = subscriptionQuery.getResultList();

            Set<Map.Entry<String, String>> set = container.map.entrySet();

            for (AttributeSubscription subscription : subscriptionList) {

                // check map contains our attribute
                //
                for (Map.Entry<String, String> entry : set) {
                    if (!entry.getKey().contains(subscription.getAttribute())) {
                        continue;
                    }

                    // check value matches the pattern
                    //
                    if (entry.getValue().contains(subscription.getPattern())) {

                        // create event
                        //
                        EntityTransaction transaction = entityMan.getTransaction();

                        AttributeSubscriptionMatchEvent event = new AttributeSubscriptionMatchEvent();
                        event.setSubmission(submission);
                        event.setSubscription(subscription);
                        event.setUser(subscription.getUser());

                        transaction.begin();

                        entityMan.persist(event);

                        transaction.commit();

                        // subscription matched
                        break;
                    }
                }
            }
        } catch (Exception ex) {
            log.error("Error!", ex);
        }

        /*
        new Thread( new Runnable() {
            public void run() {
                processEvents();
            }
        }).start();
        */


        /*
        new Thread( () -> processEvents() ).start();
        new Thread( AttributeSubscriptionProcessor::processEvents ).start();
        */

    }

    static class ProcessorRequest {

        long sbmId;
        String accNo;
        String text;
    }

    static class AttributeContainer {

        Map<String, String> map;
        Set<String> nameset;

        AttributeContainer() {
            map = new HashMap<>();
            nameset = new HashSet<>();
        }
    }

    public static class EmailTemplates {

        public String mainBody;
        public String subscription;
        public String result;
    }

    public static class SubscriptionBatch {

        public String htmlSummary;
        public String textSummary;
        public StringBuilder htmlList;
        public StringBuilder textList;
    }
}
