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


public enum BuiltInUsers {
    Guest("@Guest", "Anonymous user"), System("@System", "Represents system owned objects");

    private final String name;
    private final String description;

    BuiltInUsers(String nm, String dsc) {
        name = nm;
        description = dsc;
    }

    public static boolean isBuiltIn(String gname) {
        for (BuiltInUsers g : values()) {
            if (g.getUserName().equals(gname)) {
                return true;
            }
        }

        return false;
    }

    public String getUserName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

}
