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
package org.thingsboard.server.dao.ldap.util.cudmap;

import org.springframework.util.MultiValueMap;
import org.thingsboard.server.dao.ldap.model.LdapMap;
import org.thingsboard.server.dao.ldap.util.LdifConstants;
import org.thingsboard.server.dao.ldap.util.StrategyLdifToMap;


public class Add extends StrategyLdifToMap {

    public void startToConvert(LdapMap ldapMap, MultiValueMap<String, String> ldifAttMultivalueMapList) {
        //New record arrival status ldif
        if (ldapMap.getChangeType().equals(LdifConstants.ChangeTypeFields.ADD.name)) {

            ldapMap.setCustomerTitle(ldifAttMultivalueMapList.
                    get(LdifConstants.GroupFields.NAME.name).get(0));
            ldapMap.setUserEmail(ldifAttMultivalueMapList.
                    get(LdifConstants.UserFields.NAME.name).get(0));

            if (ldifAttMultivalueMapList.get(LdifConstants.UserFields.FİRSTNAME.name) != null) {
                ldapMap.setUserFirstName(ldifAttMultivalueMapList.
                        get(LdifConstants.UserFields.FİRSTNAME.name).get(0));
            }
            if (ldifAttMultivalueMapList.get(LdifConstants.UserFields.LASTNAME.name) != null) {
                ldapMap.setUserLastName(ldifAttMultivalueMapList.
                        get(LdifConstants.UserFields.LASTNAME.name).get(0));
            }
            if (ldifAttMultivalueMapList.get(LdifConstants.UserFields.PASSWORD.name) != null) {
                ldapMap.setUserPassword(ldifAttMultivalueMapList.
                        get(LdifConstants.UserFields.PASSWORD.name).get(0));
            }
            System.out.println(ldapMap);
        }

    }
}

