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

package uk.ac.ebi.biostd.util;

import java.nio.file.Path;

public class FilePointer {

    private Path archivePath;
    private Path fullPath;
    private Path relativePath;
    private String archiveInternalPath;
    private boolean directory;
    private long size;
    private long groupID = 0;

    public long getGroupID() {
        return groupID;
    }

    public void setGroupID(long groupID) {
        this.groupID = groupID;
    }

    public Path getFullPath() {
        return fullPath;
    }

    public void setFullPath(Path fullPath) {
        this.fullPath = fullPath;
    }


    public Path getArchivePath() {
        return archivePath;
    }

    public void setArchivePath(Path archivePath) {
        this.archivePath = archivePath;
    }


    public String getArchiveInternalPath() {
        return archiveInternalPath;
    }

    public void setArchiveInternalPath(String archiveInternalPath) {
        this.archiveInternalPath = archiveInternalPath;
    }

    public Path getRelativePath() {
        return relativePath;
    }

    public void setRelativePath(Path relativePath) {
        this.relativePath = relativePath;
    }

    @Override
    public String toString() {
        return fullPath.toString();
    }

    public boolean isDirectory() {
        return directory;
    }

    public void setDirectory(boolean directory) {
        this.directory = directory;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }
}
