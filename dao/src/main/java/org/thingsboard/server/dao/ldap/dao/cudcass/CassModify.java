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
package org.thingsboard.server.dao.ldap.dao.cudcass;

import org.springframework.stereotype.Component;
import org.thingsboard.server.common.data.Customer;
import org.thingsboard.server.common.data.Tenant;
import org.thingsboard.server.common.data.User;
import org.thingsboard.server.common.data.security.UserCredentials;
import org.thingsboard.server.dao.ldap.model.LdapMap;
import org.thingsboard.server.dao.ldap.util.LdifConstants;
import org.thingsboard.server.dao.ldap.util.StrategyMapToCassandra;

@Component("modify")
public class CassModify extends StrategyMapToCassandra {


    @Override
    public void saveToCassandra(LdapMap ldapMap) {
        //changedLdapMap.changeTitle; which groups have changed
        //changedLdapMap.changeEntity; which data is changed
        Tenant tenant =  tenantService.findTenantByName(ldapMap.getTenantName());

        //change of user group information
        if(ldapMap.getObjectClass().equals(LdifConstants.GROUPNAME) )
        {
            saveCustomer(ldapMap, tenant);
        }

        //change of user information
        else if(ldapMap.getObjectClass().equals(LdifConstants.USERNAME))
        {
            saveUser(ldapMap);
        }
    }

    private void saveUser(LdapMap ldapMap) {
        User selectedUser = userService.findUserByEmail(ldapMap.getOldUserEmail());

        //change in which area is it done
        if(ldapMap.getChangeEntity().equals(LdifConstants.UserFields.NAME.name)) {
            selectedUser.setEmail(ldapMap.getUserEmail());
        }
        else if(ldapMap.getChangeEntity().equals(LdifConstants.UserFields.FİRSTNAME.name))
        {
            selectedUser.setFirstName(ldapMap.getUserFirstName());
        }
        else if(ldapMap.getChangeEntity().equals(LdifConstants.UserFields.LASTNAME.name))
        {
            selectedUser.setLastName(ldapMap.getUserLastName());
        }

        else if(ldapMap.getChangeEntity().equals(LdifConstants.UserFields.PASSWORD))
        {
            UserCredentials userCredentials = userService.findUserCredentialsByUserId(selectedUser.getId());
            userCredentials.setPassword(ldapMap.getUserPassword());
            userCredentialsDao.save(userCredentials);
        }

        userService.saveUser(selectedUser);
    }

    private void saveCustomer(LdapMap ldapMap, Tenant tenant) {
        //old user group
        Customer selectedCustomer = customerService.
                findCustomerByTenantIdAndTitle(tenant.getId(),ldapMap.getOldCustomerTitle()).get();

        //change is done
        if(ldapMap.getChangeEntity().equals(LdifConstants.GroupFields.NAME.name)) {
            selectedCustomer.setTitle(ldapMap.getCustomerTitle());
        }
        else if(ldapMap.getChangeEntity().equals(LdifConstants.GroupFields.ADRESS.name))
        {
            selectedCustomer.setAddress(ldapMap.getCustomerAdress());
        }
        else if(ldapMap.getChangeEntity().equals(LdifConstants.GroupFields.PHONE.name))
        {
            selectedCustomer.setPhone(ldapMap.getCustomerPhone());
        }
        else if(ldapMap.getChangeEntity().equals(LdifConstants.GroupFields.EMAIL.name))
        {
            selectedCustomer.setEmail(ldapMap.getCustomerEmail());
        }

        customerService.saveCustomer(selectedCustomer);
    }
}
