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

import org.springframework.stereotype.Component;

@Component
public class LdapUtils {

    public Strategy takeStrategyClass(String changeType) {

        Strategy alghoritm=null;
        try {
            Class myClass = Class.forName(("org.thingsboard.server.dao.ldap.util.cudmap.")+
                    changeType.substring(0,1).toUpperCase()+
                    changeType.substring(1));
            alghoritm = (Strategy) myClass.newInstance();
        } catch (InstantiationException | ClassNotFoundException | IllegalAccessException e) {
            e.printStackTrace();
        }

        return  alghoritm;
    }
}
