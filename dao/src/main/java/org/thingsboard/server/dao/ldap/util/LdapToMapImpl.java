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


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.thingsboard.server.dao.ldap.model.LdapMap;

import java.util.ArrayList;

@Component
public class LdapToMapImpl implements LdapToMapI {

    @Autowired LdapUtils ldapUtils;

    public ArrayList<LdapMap> convertLdifTextToLdapMaps(String ldapDatas) {

        ArrayList<LdapMap> ldapMapList = new ArrayList<>();
        ArrayList<MultiValueMap<String,String>> ldifAttMultivalueMapList =
                parseAndValidateLdifString(ldapDatas);
        for(int i=0; i < ldifAttMultivalueMapList.size(); i++)
        {
          LdapMap ldapMap = setEssantialAttributes(ldifAttMultivalueMapList.get(i));
          Strategy cudAlghoritm = ldapUtils.takeStrategyClass(ldapMap.getChangeType());
          cudAlghoritm.parseLdifData(ldapMap,ldifAttMultivalueMapList.get(i));
          ldapMapList.add(ldapMap);
        }
         return ldapMapList;
    }


    private LdapMap setEssantialAttributes(MultiValueMap<String, String> ldifAttMultivalueMap) {

        LdapMap ldapMap = new LdapMap();

        java.lang.String changetype = ldifAttMultivalueMap.
                get(LdifConstants.CHANGETYPE).get(0);

        ldapMap.setTenantName(LdifConstants.TENANTNAME);
        ldapMap.setChangeType(changetype);

        if (ldifAttMultivalueMap.get(LdifConstants.OBJECTCLASS).
                contains(LdifConstants.GROUPNAME)) {
            ldapMap.setObjectClass(LdifConstants.GROUPNAME);
            //old customerTitlı ldif is in the ou field. When the group name changes to that field,
            // the current field is in the old field?
            ldapMap.setOldCustomerTitle(ldifAttMultivalueMap.
                    get(LdifConstants.GroupFields.NAME.name).get(0));
        }
        else if (ldifAttMultivalueMap.get(LdifConstants.OBJECTCLASS).
                contains(LdifConstants.USERNAME)) {
            ldapMap.setObjectClass(LdifConstants.USERNAME);
            //old user ldif. When the user name changes to that field,
            // the current field is in the old field?
            ldapMap.setOldUserEmail(ldifAttMultivalueMap.
                    get(LdifConstants.UserFields.NAME.name).get(0));
        }

        return ldapMap;
    }

    private ArrayList<MultiValueMap<String,String>> parseAndValidateLdifString(String ldapDatas) {
        String[] updates = ldapDatas.split("\n\n");
        ArrayList<MultiValueMap<String,String>> updateArrayList = new ArrayList<>();


        for (String updateList : updates) {
            String[] attributeList = updateList.split("\n");

            ArrayList<String> attributeName = new ArrayList<>();
            ArrayList<String> attributeValue = new ArrayList<>();
            MultiValueMap<String, String> ldifAttMultiValueMap = new LinkedMultiValueMap<>();

            for (int i = 0; i < attributeList.length; i++) {

                java.lang.String[] tempArrayList = attributeList[i].split(":");
                attributeName.add(tempArrayList[0]);
                attributeValue.add(tempArrayList[1].trim());
                if (tempArrayList[0].equals("dn")) {
                    java.lang.String tmpDnAttrubiuteList[] = tempArrayList[1].split(", ");
                    for (java.lang.String tmpDnAttrubites : tmpDnAttrubiuteList) {
                        java.lang.String[] tmpDnAttrubite = tmpDnAttrubites.split("=");
                        ldifAttMultiValueMap.add("dn" + tmpDnAttrubite[0].trim(), tmpDnAttrubite[1]);
                    }
                } else {
                    ldifAttMultiValueMap.add(attributeName.get(i), attributeValue.get(i));
                }


            }
            
            new ValidateLdif(ldifAttMultiValueMap);

            updateArrayList.add(ldifAttMultiValueMap);

        }

        return updateArrayList;
    }

}
