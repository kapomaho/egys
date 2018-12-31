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

import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;

public class ThingsBoardLdapContext {

    public static LdapTemplate getLdapTemplate(){
        LdapTemplate ldapSpringTemplate = new LdapTemplate();
        LdapContextSource contextSource = new LdapContextSource();

        try {
            contextSource.setUserDn(LdapProperties.getDn());
            contextSource.setPassword(LdapProperties.getPassword());
            contextSource.setUrl(LdapProperties.getUrl());
            contextSource.setBase(LdapProperties.getBase());
            contextSource.afterPropertiesSet();
            ldapSpringTemplate.setContextSource( contextSource );
            ldapSpringTemplate.afterPropertiesSet();
        } catch (Exception e) {
            System.out.println("Get  " + e.getCause());
        }

        return ldapSpringTemplate;
    }

}
