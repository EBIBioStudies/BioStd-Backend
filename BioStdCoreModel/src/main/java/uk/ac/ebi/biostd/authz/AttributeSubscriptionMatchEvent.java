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
package uk.ac.ebi.biostd.authz;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import uk.ac.ebi.biostd.model.Submission;


/**
 * Created by andrew on 24/03/2017.
 */

@Entity
@NamedQueries({@NamedQuery(name = AttributeSubscriptionMatchEvent.GetAllUsersWithEventsQuery,
        query = "SELECT DISTINCT u FROM AttributeSubscriptionMatchEvent event LEFT JOIN event.user u"),

        @NamedQuery(name = AttributeSubscriptionMatchEvent.GetEventsByUserIdQuery,
                query = "SELECT event FROM AttributeSubscriptionMatchEvent event LEFT JOIN event.user u "
                        + "LEFT JOIN event.subscription ts LEFT JOIN event.submission subm " + "where u.id=:"
                        + AttributeSubscriptionMatchEvent.UserIdQueryParameter),

        @NamedQuery(name = AttributeSubscriptionMatchEvent.DeleteEventsByUserIdQuery,
                query = "delete from AttributeSubscriptionMatchEvent event " + "where event.user.id=:"
                        + AttributeSubscriptionMatchEvent.UserIdQueryParameter)})
public class AttributeSubscriptionMatchEvent {

    public static final String GetAllUsersWithEventsQuery =
            "AttributeSubscriptionMatchEvent" + ".getAllUsersWithEventsQuery";
    public static final String GetEventsByUserIdQuery = "AttributeSubscriptionMatchEvent.getEventsByUserIdQuery";
    public static final String DeleteEventsByUserIdQuery = "AttributeSubscriptionMatchEvent.deleteEventsByUserIdQuery";

    public static final String UserIdQueryParameter = "userId";


    private long id;
    private User user;
    private AttributeSubscription subscription;
    private Submission submission;

    public AttributeSubscriptionMatchEvent() {
    }

    @ManyToOne
    @JoinColumn(name = "subscription_id")
    public AttributeSubscription getSubscription() {
        return subscription;
    }

    public void setSubscription(AttributeSubscription subscription) {
        this.subscription = subscription;
    }

    @ManyToOne
    @JoinColumn(name = "submission_id")
    public Submission getSubmission() {
        return submission;
    }

    public void setSubmission(Submission submission) {
        this.submission = submission;
    }

    @ManyToOne
    @JoinColumn(name = "user_id")
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

}
