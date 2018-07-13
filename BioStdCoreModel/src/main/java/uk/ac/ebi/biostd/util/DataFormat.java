/**
 * Copyright 2014-2017 Functional Genomics Development Team, European Bioinformatics Institute
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 * @author Mikhail Gostev <gostev@gmail.com>
 **/

package uk.ac.ebi.biostd.util;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

public enum DataFormat {
    xlsx("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"), xls("application/vnd.ms-excel"),
    json("application/json"), ods("application/vnd.oasis.opendocument.spreadsheet"), csv("text/csv"), tsv("text/tsv"),
    csvtsv("text/csvtsv"), xml("text/xml");

    private final String contentType;

    DataFormat(String ctype) {
        contentType = ctype;
    }

    public String getContentType() {
        return contentType;
    }

    public static Optional<DataFormat> fromFileName(String fileName) {
        Optional<String> fileExtension = Optional.ofNullable(fileName)
                .map(fn -> fn.split("\\."))
                .filter(array -> array.length > 0)
                .map(array -> array[array.length - 1]);

        if (!fileExtension.isPresent()) {
            return Optional.empty();
        }

        return Arrays.stream(DataFormat.values())
                .filter(v -> v.toString().equalsIgnoreCase(fileExtension.get()))
                .findFirst();
    }

    public static Optional<DataFormat> fromContentType(String contentType) {
        return Arrays.stream(DataFormat.values())
                .filter(v -> v.getContentType().equalsIgnoreCase(contentType))
                .findFirst();
    }

    public static Optional<DataFormat> fromFileNameOrContentType(String fileName, String contentType) {
        return Stream.of(
                DataFormat.fromFileName(fileName),
                DataFormat.fromContentType(contentType))
                .flatMap(o -> o.map(Stream::of).orElseGet(Stream::empty))
                .findFirst();
    }
}
