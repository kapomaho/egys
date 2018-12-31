/**
 * Copyright © 2016-2018 The Thingsboard Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.thingsboard.server.dao.ldap.util;

public class LdifConstants {

    final static public String GROUPNAME = "group";
    final static public String USERNAME = "person";
    final static public String TENANTNAME = "tenant tcdd";
    final static public String CHANGETYPE = "changetype";
    final static public String CHANGEFIELD = "replace";
    final static public String OBJECTCLASS = "objectclass";

    public enum ChangeTypeFields{
        ADD("add"),
        MODIFY("modify"),
        DELETE("delete");

        public final String name;

        ChangeTypeFields(String name) {
            this.name = name;
        }
    }

    public enum GroupFields{
        NAME("dnou"),
        CHANGEDNAME("ou"),
        ADRESS("adress"),
        PHONE("telephonenumber"),
        EMAIL("email");

        public final String name;

        GroupFields(String name) {
            this.name = name;
        }
    }

    public enum UserFields{
        NAME("uid"),
        FİRSTNAME("firstname"),
        LASTNAME("lastname"),
        PASSWORD("password");

        public final String name;

        UserFields(String otherName)
        {
            this.name = otherName;
        }

    }
}
