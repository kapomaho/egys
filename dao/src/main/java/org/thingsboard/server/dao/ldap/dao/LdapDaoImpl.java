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
package org.thingsboard.server.dao.ldap.dao;

import org.springframework.beans.factory.annotation.Autowired;
import static org.springframework.ldap.query.LdapQueryBuilder.query;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.query.SearchScope;
import org.springframework.stereotype.Component;
import org.thingsboard.server.dao.customer.CustomerService;
import org.thingsboard.server.dao.tenant.TenantService;
import org.thingsboard.server.dao.user.UserCredentialsDao;
import org.thingsboard.server.dao.user.UserService;

import java.util.List;


@Component
public class LdapDaoImpl implements LdapDao{

	@Autowired
	protected TenantService tenantService;

	@Autowired
	protected CustomerService customerService;

	@Autowired
	protected UserService userService;

	@Autowired
	protected UserCredentialsDao userCredentialsDao;

	private LdapTemplate ldapTemplate;

	public void setLdapTemplate(LdapTemplate ldapTemplate) {
		this.ldapTemplate = ldapTemplate;
	}

	@Override
	public List<String> getPersonInfo(String userName) {
		return ldapTemplate.search(query()
						.searchScope(SearchScope.SUBTREE)
						.where("uid").is(userName),
				(AttributesMapper<String>) attrs -> attrs.get("uid").get().toString());
	}

	@Override
	public List<String> exportLdif() {

		return ldapTemplate.search(query().searchScope(SearchScope.SUBTREE).where("dn")
				.is("dc=test-botas,dc=org"),(AttributesMapper<String>) attrs -> attrs.getAll().toString());
	}

}
