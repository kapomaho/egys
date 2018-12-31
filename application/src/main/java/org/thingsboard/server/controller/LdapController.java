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
package org.thingsboard.server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.thingsboard.server.dao.ldap.service.LdapService;
import org.thingsboard.server.common.data.exception.ThingsboardException;

@RestController
@RequestMapping("/api")
public class LdapController extends BaseController {

    @Autowired
    private LdapService ldapService;

    @PreAuthorize("hasAnyAuthority('SYS_ADMIN','TENANT_ADMIN')")
    @RequestMapping(value = "/ldap", method = RequestMethod.POST)
    @ResponseBody
    public void saveChangeLdif(@RequestBody String ldif) throws ThingsboardException {
        try {
            ldapService.saveLdif(ldif);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

}
