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
 * @author Mikhail Gostev <gostev@gmail.com>
 **/

package uk.ac.ebi.biostd.authz;

import java.util.Collection;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@NamedQueries({@NamedQuery(name = Tag.GetByNameQuery,
        query = "SELECT t FROM Tag t LEFT JOIN t.classifier c where t.name=:" + Tag.TagNameQueryParameter
                + " AND c.name=:" + Tag.ClassifierNameQueryParameter),
        @NamedQuery(name = Tag.GetAllQuery, query = "SELECT t FROM Tag t ")})
@Table(indexes = {@Index(name = "name_idx", columnList = "name", unique = true)})
public class Tag {

    public static final String GetByNameQuery = "Tag.getByName";
    public static final String GetAllQuery = "Tag.getAll";
    public static final String TagNameQueryParameter = "tname";
    public static final String ClassifierNameQueryParameter = "cname";
    private long id;
    private String name;
    private String description;
    private Collection<Tag> subTags;
    private Tag parentTag;
    private Classifier classifier;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @OneToMany(mappedBy = "parentTag", cascade = CascadeType.ALL)
    public Collection<Tag> getSubTags() {
        return subTags;
    }

    public void setSubTags(Collection<Tag> subTags) {
        this.subTags = subTags;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_tag_id", foreignKey = @ForeignKey(name = "parent_tag_fk"))
    public Tag getParentTag() {
        return parentTag;
    }

    public void setParentTag(Tag patentTag) {
        parentTag = patentTag;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classifier_id", foreignKey = @ForeignKey(name = "classifier_fk"))
    public Classifier getClassifier() {
        return classifier;
    }

    public void setClassifier(Classifier classifier) {
        this.classifier = classifier;
    }

}
