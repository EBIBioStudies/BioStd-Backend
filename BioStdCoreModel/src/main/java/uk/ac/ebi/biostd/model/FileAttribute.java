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

package uk.ac.ebi.biostd.model;

import java.util.ArrayList;
import java.util.Collection;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import uk.ac.ebi.biostd.authz.Tag;

@Entity
public class FileAttribute extends AbstractAttribute {

    private FileRef host;
    private Collection<FileAttributeTagRef> tagRefs;


    public FileAttribute() {
    }

    public FileAttribute(String name, String value) {
        super(name, value);
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id")
    public FileRef getHost() {
        return host;
    }

    public void setHost(FileRef h) {
        host = h;
    }

    @Override
    @OneToMany(mappedBy = "attribute", cascade = CascadeType.ALL)
    public Collection<FileAttributeTagRef> getTagRefs() {
        return tagRefs;
    }

    public void setTagRefs(Collection<FileAttributeTagRef> tags) {
        tagRefs = tags;

        if (tags != null) {
            for (FileAttributeTagRef str : tags) {
                str.setAttribute(this);
            }
        }
    }


    @Override
    public FileAttributeTagRef addTagRef(Tag t, String val) {
        FileAttributeTagRef ftr = new FileAttributeTagRef();

        ftr.setTag(t);
        ftr.setParameter(val);

        addTagRef(ftr);

        return ftr;
    }

    public void addTagRef(FileAttributeTagRef tr) {
        if (tagRefs == null) {
            tagRefs = new ArrayList<>();
        }

        tr.setAttribute(this);

        tagRefs.add(tr);
    }
}
