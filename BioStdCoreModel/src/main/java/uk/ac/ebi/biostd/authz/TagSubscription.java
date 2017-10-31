/**
 * Copyright 2014-2017 Functional Genomics Development Team, European Bioinformatics Institute <p> Licensed under the
 * Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may
 * obtain a copy of the License at <p> http://www.apache.org/licenses/LICENSE-2.0 <p> Unless required by applicable law
 * or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * @author Mikhail Gostev <gostev@gmail.com>
 **/

package uk.ac.ebi.biostd.authz;

import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

@Entity
@NamedQueries({@NamedQuery(name = TagSubscription.GetUsersByTagIdsQuery,
        query = "SELECT DISTINCT u FROM TagSubscription ts LEFT JOIN ts.user u where ts.tag.id in (:"
                + TagSubscription.TagIdQueryParameter + ")"), //LEFT JOIN ts.tag tg

        @NamedQuery(name = TagSubscription.GetSubsByTagIdsQuery,
                query = "SELECT ts FROM TagSubscription ts where ts.tag.id in (:" + TagSubscription.TagIdQueryParameter
                        + ")"), //  order by ts.user.id

        @NamedQuery(name = TagSubscription.GetByTagAndUserQuery,
                query = "SELECT ts FROM TagSubscription ts LEFT JOIN ts.user u LEFT JOIN ts.tag tg "
                        + "LEFT JOIN tg.classifier cl where cl.name=:" + TagSubscription.ClassifierNameQueryParameter
                        + " AND tg.name=:" + TagSubscription.TagNameQueryParameter + " AND u.id=:"
                        + TagSubscription.UserIdQueryParameter),

        @NamedQuery(name = TagSubscription.GetByTagIdAndUserQuery,
                query = "SELECT ts FROM TagSubscription ts LEFT JOIN ts.user u LEFT JOIN ts.tag tg  " + "where tg.id=:"
                        + TagSubscription.TagIdQueryParameter + " AND u.id=:" + TagSubscription.UserIdQueryParameter),

        @NamedQuery(name = TagSubscription.GetAllByUserQuery,
                query = "SELECT ts FROM TagSubscription ts LEFT JOIN ts.user u " + "where  u.id=:"
                        + TagSubscription.UserIdQueryParameter)})
public class TagSubscription {

    public static final String GetUsersByTagIdsQuery = "TagSubscription.getUsersByTagIds";
    public static final String GetSubsByTagIdsQuery = "TagSubscription.getSubsByTagIds";
    public static final String GetByTagAndUserQuery = "TagSubscription.getByTagAndUser";
    public static final String GetAllByUserQuery = "TagSubscription.getAllByUserQuery";
    public static final String GetByTagIdAndUserQuery = "TagSubscription.getByTagIdAndUser";
    public static final String UserIdQueryParameter = "uid";
    public static final String TagIdQueryParameter = "tgid";
    public static final String TagNameQueryParameter = "tgname";
    public static final String ClassifierNameQueryParameter = "clname";

    private long id;

    private Tag tag;
    private User user;

    public TagSubscription() {
    }

    @ManyToOne
    @JoinColumn(name = "tag_id", foreignKey = @ForeignKey(name = "tag_fk"))
    public Tag getTag() {
        return tag;
    }

    public void setTag(Tag tag) {
        this.tag = tag;
    }

    @ManyToOne
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "user_fk"))
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
