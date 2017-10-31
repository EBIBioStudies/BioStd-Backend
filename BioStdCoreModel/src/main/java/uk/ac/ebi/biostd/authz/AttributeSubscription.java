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
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 * Created by andrew on 23/03/2017.
 */

@Entity
@NamedQueries({@NamedQuery(name = AttributeSubscription.GetUsersByAttributeQuery,
        query = "SELECT DISTINCT u FROM AttributeSubscription subs LEFT JOIN subs.user u "
                + "where subs.attribute in (:" + AttributeSubscription.AttributeQueryParameter + ")"),

        @NamedQuery(name = AttributeSubscription.GetByAttributeAndUserIdQuery,
                query = "SELECT subs FROM AttributeSubscription subs where subs.attribute in (:"
                        + AttributeSubscription.AttributeQueryParameter + ") order by subs.user.id"),

        @NamedQuery(name = AttributeSubscription.GetBySubscriptionIdQuery,
                query = "SELECT subs FROM AttributeSubscription subs where subs.id=:"
                        + AttributeSubscription.SubscriptionIdQueryParameter),

        @NamedQuery(name = AttributeSubscription.GetExactSubscriptonQuery,
                query = "SELECT subs FROM AttributeSubscription subs LEFT JOIN subs.user u "
                        + "where subs.attribute in (:" + AttributeSubscription.AttributeQueryParameter + ") and "
                        + "subs.pattern in (:" + AttributeSubscription.PatternQueryParameter + ") and " + "u.id=:"
                        + AttributeSubscription.UserIdQueryParameter),

        @NamedQuery(name = AttributeSubscription.GetAllByAttributeQuery,
                query = "SELECT subs FROM AttributeSubscription subs LEFT JOIN subs.user u "
                        + "where subs.attribute in (:" + AttributeSubscription.AttributeQueryParameter + ")"),

        @NamedQuery(name = AttributeSubscription.GetAllByUserIdQuery,
                query = "SELECT subs FROM AttributeSubscription subs LEFT JOIN subs.user u " + "where  u.id=:"
                        + AttributeSubscription.UserIdQueryParameter)})
@Table(indexes = {@Index(name = "attribute_index", columnList = "attribute", unique = false)})
public class AttributeSubscription {

    public static final String GetUsersByAttributeQuery = "AttributeSubscription.getUsersByAttribute";
    public static final String GetByAttributeAndUserIdQuery = "AttributeSubscription.getByAttributeIdAndUser";


    public static final String GetBySubscriptionIdQuery = "AttributeSubscription.getBySubscriptionIdQuery";
    public static final String GetExactSubscriptonQuery = "AttributeSubscription.getExactSubscriptionQuery";
    public static final String GetAllByAttributeQuery = "AttributeSubscription.getAllByAttributeQuery";
    public static final String GetAllByUserIdQuery = "AttributeSubscription.getAllByUserIdQuery";

    public static final String UserIdQueryParameter = "userId";
    public static final String AttributeQueryParameter = "attribute";
    public static final String PatternQueryParameter = "pattern";
    public static final String SubscriptionIdQueryParameter = "subscriptionId";


    private long id;
    private User user;
    private String attribute;
    private String pattern;

    public AttributeSubscription() {
    }

    public String getAttribute() {
        return attribute;
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
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
