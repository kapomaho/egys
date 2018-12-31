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
import org.thingsboard.server.common.data.security.Authority;
import org.thingsboard.server.dao.ldap.model.LdapMap;
import org.thingsboard.server.dao.ldap.util.StrategyMapToCassandra;

import java.util.Optional;

@Component("add")
public class CassAdd extends StrategyMapToCassandra {



    @Override
    public void saveToCassandra(LdapMap ldapMap) {
        Tenant tenant =  tenantService.findTenantByName(ldapMap.getTenantName());

        //saves user directly if user group exists
        Optional<Customer> optionalCustomer = customerService.
                findCustomerByTenantIdAndTitle(tenant.getId(),ldapMap.getCustomerTitle());
        Customer selectedCustomer;
        if(!optionalCustomer.isPresent())
        {
            selectedCustomer = saveCustomer(ldapMap, tenant);
        }
        else
        {
            selectedCustomer = optionalCustomer.get();
        }
        saveUser(ldapMap, tenant, selectedCustomer);
    }

    private void saveUser(LdapMap ldapMap, Tenant tenant, Customer selectedCustomer) {
        User user = new User();
        user.setTenantId(tenant.getId());
        user.setCustomerId(selectedCustomer.getId());
        user.setAuthority(Authority.CUSTOMER_USER);
        user.setEmail(ldapMap.getUserEmail());
        user.setFirstName(ldapMap.getUserFirstName());
        user.setLastName(ldapMap.getUserLastName());
        userService.saveUserWithPassword(user,ldapMap.getUserPassword());
    }

    private Customer saveCustomer(LdapMap ldapMap, Tenant tenant) {
        Customer selectedCustomer;
        Customer customer = new Customer();
        customer.setTenantId(tenant.getId());
        customer.setTitle(ldapMap.getCustomerTitle());
        selectedCustomer = customerService.saveCustomer(customer);
        return selectedCustomer;
    }
}
