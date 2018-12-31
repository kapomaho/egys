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

public class Modify extends StrategyLdifToMap {

    MultiValueMap<String, String> ldifAttMultivalueMapList;

    @Override
    public void startToConvert(LdapMap ldapMap, MultiValueMap<String, String> ldifAttMultivalueMapList) {

        this.ldifAttMultivalueMapList = ldifAttMultivalueMapList;

        String changedValue = setEssantialAttributes(ldapMap);
        setCustomerAttributes(ldapMap, changedValue);
        setUserAttributes(ldapMap, changedValue);

    }

    private void setUserAttributes(LdapMap ldapMap, String changedValue) {

        if (ldifAttMultivalueMapList.
                get(LdifConstants.UserFields.FİRSTNAME.name) != null) {
            ldapMap.setUserFirstName(changedValue);
        } else if (ldifAttMultivalueMapList.
                get(LdifConstants.UserFields.LASTNAME.name) != null) {
            ldapMap.setUserLastName(changedValue);
        } else if (ldifAttMultivalueMapList.
                get(LdifConstants.UserFields.PASSWORD.name) != null) {
            ldapMap.setUserPassword(changedValue);
        }
    }

    private void setCustomerAttributes(LdapMap ldapMap, String changedValue) {

        if (ldifAttMultivalueMapList.
                get(LdifConstants.GroupFields.ADRESS.name) != null) {
            ldapMap.setCustomerAdress(changedValue);
        } else if (ldifAttMultivalueMapList.
                get(LdifConstants.GroupFields.PHONE.name) != null) {
            ldapMap.setCustomerPhone(changedValue);
        } else if (ldifAttMultivalueMapList.
                get(LdifConstants.GroupFields.EMAIL.name) != null) {
            ldapMap.setCustomerEmail(changedValue);
        }
    }

    private String setEssantialAttributes(LdapMap ldapMap) {

        String changeField = ldifAttMultivalueMapList.
                get(LdifConstants.CHANGEFIELD).get(0);
        //Since the user uid is added to the field under both main ldifde and replace,
        //3 uid is created. The former is looking at the last two to be distinguished.
        String oldValue;
        String changedValue;
        if (changeField.equals(LdifConstants.UserFields.NAME.name)) {
            oldValue = ldifAttMultivalueMapList.get(changeField).get(1);
            changedValue = ldifAttMultivalueMapList.get(changeField).get(2);
        } else {
            oldValue = ldifAttMultivalueMapList.get(changeField).get(0);
            changedValue = ldifAttMultivalueMapList.get(changeField).get(1);
        }

        ldapMap.setChangeEntity(changeField);

        if (changeField.equals(LdifConstants.GroupFields.CHANGEDNAME.name)) {
            ldapMap.setOldCustomerTitle(oldValue);
            ldapMap.setCustomerTitle(changedValue);

        }
        if (changeField.equals(LdifConstants.UserFields.NAME.name)) {
            ldapMap.setOldUserEmail(oldValue);
            ldapMap.setUserEmail(changedValue);
        }
        return changedValue;
    }
}
