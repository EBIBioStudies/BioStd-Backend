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

import java.util.ArrayList;
import java.util.Collection;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(indexes = {@Index(name = "name_idx", columnList = "name", unique = true)})
@NamedQueries({@NamedQuery(name = Classifier.GetByNameQuery,
        query = "SELECT c FROM Classifier c where c.name=:" + Classifier.NameQueryParameter),
        @NamedQuery(name = Classifier.GetAllQuery, query = "SELECT c FROM Classifier c ")})
public class Classifier {

    public static final String GetByNameQuery = "Classifier.getByName";
    public static final String GetAllQuery = "Classifier.getAll";
    public static final String NameQueryParameter = "name";
    private long id;
    private String name;
    private String description;
    private Collection<Tag> tags = new ArrayList<>();


    public Classifier() {
    }

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

    @OneToMany(mappedBy = "classifier", cascade = CascadeType.ALL)
    public Collection<Tag> getTags() {
        return tags;
    }

    public void setTags(Collection<Tag> tags) {
        this.tags = tags;
    }

    @Transient
    public Tag getTag(String tagId) {
        for (Tag t : tags) {
            if (t.getName().equals(tagId)) {
                return t;
            }
        }

        return null;
    }

    public void addTag(Tag tb) {
        tags.add(tb);
    }

}
