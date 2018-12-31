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
package org.thingsboard.server.dao.ldap.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.thingsboard.server.dao.ldap.model.LdapMap;
import org.thingsboard.server.dao.ldap.util.LdapToMapI;
import org.thingsboard.server.dao.ldap.util.Strategy;

import java.util.ArrayList;

@Service
@Slf4j
public class LdapServiceImpl  implements LdapService {

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    LdapToMapI ldapToMap;

    @Override
    public void saveLdif(String ldif) {

        ArrayList<LdapMap> ldapMapArrayList = ldapToMap.convertLdifTextToLdapMaps(ldif);
        for(LdapMap ldapMap: ldapMapArrayList){
            Strategy cudAlghoritm = (Strategy) applicationContext.getBean(ldapMap.getChangeType());
            cudAlghoritm.saveLdifData(ldapMap);
        }
    }
}
