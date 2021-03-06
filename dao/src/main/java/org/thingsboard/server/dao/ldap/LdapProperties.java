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
package org.thingsboard.server.dao.ldap;



public class LdapProperties {

    private final static String dn= "cn=admin,dc=test-botas,dc=org";
    private final static String password= "1";
    private final static String url= "ldap://10.151.16.86";
    private final static String host= "10.151.16.86";
    private final static String ldapEnabled= "true";
    private final static String base= "dc=test-botas,dc=org";
    private final static String filter= "objectClass=*";
    private final static String ldifFileName= "out.ldif";



    public static String getDn() {
        return dn;
    }

    public static String getPassword() {
        return password;
    }

    public static String getUrl() {
        return url;
    }

    public static String getLdapEnabled() {
        return ldapEnabled;
    }

    public static String getBase() {
        return base;
    }

    public static String getHost() { return host; }

    public static String getFilter() { return filter; }

    public static String getLdifFileName() { return ldifFileName; }
}
