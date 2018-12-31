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

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.thingsboard.server.common.data.*;
import org.thingsboard.server.common.data.id.CustomerId;
import org.thingsboard.server.common.data.page.TextPageData;
import org.thingsboard.server.common.data.page.TextPageLink;
import org.thingsboard.server.common.data.relation.EntityRelation;
import org.thingsboard.server.common.data.relation.RelationTypeGroup;
import org.thingsboard.server.common.data.security.Authority;
import org.thingsboard.server.dao.model.ModelConstants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.containsString;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class BaseNvrControllerTest extends AbstractControllerTest {
    private IdComparator<Nvr> idComparator = new IdComparator<>();
    private Tenant savedTenant;
    private User tenantAdmin;

    @Before
    public void beforeTest() throws Exception {
        loginSysAdmin();

        Tenant tenant = new Tenant();
        tenant.setTitle("MyTenant");
        savedTenant = doPost("/api/tenant", tenant, Tenant.class);
        Assert.assertNotNull(savedTenant);

        tenantAdmin = new User();
        tenantAdmin.setAuthority(Authority.TENANT_ADMIN);
        tenantAdmin.setTenantId(savedTenant.getId());
        tenantAdmin.setEmail("tenant2@thingsboard.org");
        tenantAdmin.setFirstName("Joe");
        tenantAdmin.setLastName("Downs");

        tenantAdmin = createUserAndLogin(tenantAdmin, "testPassword1");
    }

    @After
    public void afterTest() throws Exception {
        loginSysAdmin();

        doDelete("/api/tenant/" + savedTenant.getId().getId().toString())
                .andExpect(status().isOk());
    }

    private List<Nvr> generateNvrs(int count) throws Exception {
        List<Nvr> nvrList = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Nvr nvr = new Nvr();
            nvr.setTenantId(savedTenant.getId());
            nvr.setType("default" + i % 2);
            nvr.setName("My nvr " + i);
            nvr.setIp("192.168.1." + i);
            nvr.setBrand("ASL");
            nvr.setModel("model1");
            Nvr savedNvr = doPost("/api/nvr/", nvr, Nvr.class);
            Assert.assertEquals(nvr.getType(), savedNvr.getType());
            Assert.assertEquals(nvr.getName(), savedNvr.getName());
            Assert.assertEquals(nvr.getIp(), savedNvr.getIp());
            Assert.assertEquals(nvr.getBrand(), savedNvr.getBrand());
            Assert.assertEquals(nvr.getModel(), savedNvr.getModel());
            nvrList.add(savedNvr);
        }
        return nvrList;
    }

    private void destructNvrs(List<Nvr> nvrs) throws Exception {
        for (Nvr nvr : nvrs) {
            doDelete("/api/nvr/" + nvr.getId().toString()).andExpect(status().isOk());
        }
    }

    @Test
    public void testGetNvrByCameraId() throws Exception {
        Nvr nvr = new Nvr();
        nvr.setTenantId(savedTenant.getId());
        nvr.setType("default");
        nvr.setName("My nvr ");
        nvr.setIp("192.168.1.1");
        nvr.setBrand("ASL");
        nvr.setModel("model1");
        Nvr savedNvr = doPost("/api/nvr/", nvr, Nvr.class);

        Camera c = new Camera();
        c.setPrimaryUrlPath("/stream1");
        c.setPtz(true);
        c.setType("type");
        c.setName("Cam");
        c.setBrand("ASL");
        c.setModel("Sabit");
        c.setHost("192.168.1.1");
        Camera camera = doPost("/api/camera", c, Camera.class);

        EntityRelation relation = new EntityRelation(savedNvr.getId(), camera.getId(), "Manages", RelationTypeGroup.COMMON);
        doPost("/api/relation/", relation).andExpect(status().isOk());

        Nvr cameraNvr = doGetTyped("/api/camera/" + camera.getId().getId().toString() + "/nvr",
                new com.fasterxml.jackson.core.type.TypeReference<Nvr>() {});

        Assert.assertEquals(cameraNvr, savedNvr);

        String fromId = relation.getFrom().getId().toString();
        String toId = relation.getTo().getId().toString();
        String toType = relation.getTo().getEntityType().toString();
        String fromType = relation.getFrom().getEntityType().toString();
        String relationType = relation.getType();
        String relationTypeGroup = RelationTypeGroup.COMMON.toString();

        doDelete("/api/nvr/" + savedNvr.getId().getId().toString());
        doDelete("/api/camera/" + camera.getId().getId().toString());
        doDelete("/api/relation?relationTypeGroup=" + relationTypeGroup + "&fromId=" + fromId +
                "&fromType=" + fromType + "&relationType=" + relationType + "&toId=" + toId + "&toType=" + toType);
    }

    @Test
    public void testSaveNvrAndGetNvr() throws Exception {
        List<Nvr> nvrs = generateNvrs(1);
        Nvr savedNvr = nvrs.get(0);

        Assert.assertNotNull(savedNvr);
        Assert.assertNotNull(savedNvr.getTenantId());
        Assert.assertTrue(savedNvr.getCreatedTime() > 0);
        Assert.assertEquals(savedNvr.getTenantId(), savedTenant.getId());
        Assert.assertTrue(savedNvr.getAssignedCustomers().isEmpty());

        Nvr foundNvr = doGet("/api/nvr/" + savedNvr.getId().toString(), Nvr.class);
        Assert.assertNotNull(foundNvr);
        Assert.assertEquals(savedNvr, foundNvr);

        destructNvrs(nvrs);

        doGet("/api/nvr/invalidid")
                .andExpect(status().isBadRequest())
                .andExpect(statusReason(containsString("Invalid UUID string")));
    }

    @Test
    public void testSaveNvrWithEmptyFields() throws Exception {
        Nvr nvr = new Nvr();
        nvr.setTenantId(savedTenant.getId());
        nvr.setType("default");
        nvr.setName("My nvr ");
        nvr.setBrand("ASL");
        nvr.setModel("model1");

        doPost("/api/nvr", nvr)
                .andExpect(status().isBadRequest())
                .andExpect(statusReason(containsString("Nvr host ip should be specified")));

        nvr.setIp("192.168.1.1");
        nvr.setType(null);

        doPost("/api/nvr", nvr)
                .andExpect(status().isBadRequest())
                .andExpect(statusReason(containsString("Nvr type should be specified")));

        nvr.setType("default");
        nvr.setName(null);


        doPost("/api/nvr", nvr)
                .andExpect(status().isBadRequest())
                .andExpect(statusReason(containsString("Nvr name should be specified")));

        nvr.setName("My nvr");
        nvr.setBrand(null);

        doPost("/api/nvr", nvr)
                .andExpect(status().isBadRequest())
                .andExpect(statusReason(containsString("Nvr brand should be specified")));

        nvr.setBrand("ASL");
        nvr.setModel(null);

        doPost("/api/nvr", nvr)
                .andExpect(status().isBadRequest())
                .andExpect(statusReason(containsString("Nvr model should be specified")));
    }

    @Test
    public void testDeleteNvr() throws Exception {
        Nvr nvr = new Nvr();
        nvr.setTenantId(savedTenant.getId());
        nvr.setType("default");
        nvr.setIp("192.168.1.1");
        nvr.setName("My nvr ");
        nvr.setBrand("ASL");
        nvr.setModel("model1");

        Nvr nvr2 = doPost("/api/nvr/", nvr, Nvr.class);
        Nvr foundNvr = doGet("/api/nvr/" + nvr2.getId().toString(), Nvr.class);
        Assert.assertEquals(nvr2, foundNvr);

        doDelete("/api/nvr/" + nvr2.getId().toString()).andExpect(status().isOk());
        doGet("/api/nvr/" + nvr2.getId().toString()).andExpect(status().isNotFound());
        doDelete("/api/nvr/" + nvr2.getId().toString()).andExpect(status().isNotFound());
    }

    @Test
    public void testAssignNvrToCustomer() throws Exception {
        Nvr nvr = new Nvr();
        nvr.setTenantId(savedTenant.getId());
        nvr.setType("default");
        nvr.setIp("192.168.1.1");
        nvr.setName("My nvr ");
        nvr.setBrand("ASL");
        nvr.setModel("model1");
        Nvr savedNvr = doPost("/api/nvr/", nvr, Nvr.class);


        Customer customer = new Customer();
        customer.setTitle("Test customer");
        customer.setTenantId(savedTenant.getId());
        Customer savedCustomer = doPost("/api/customer/", customer, Customer.class);

        String nvrId = savedNvr.getId().toString();
        String customerId = savedCustomer.getId().toString();

        doPost("/api/customer/" + customerId + "/nvr/" + nvrId).andExpect(status().isOk());
        doPost("/api/customer/" + ModelConstants.NULL_UUID + "/nvr/" + nvrId).andExpect(status().isNotFound());
        doPost("/api/customer/invalidId" + "/nvr/" + nvrId).andExpect(status().isBadRequest());

        doDelete("/api/nvr/" + nvrId).andExpect(status().isOk());
        doDelete("/api/customer/" + customerId).andExpect(status().isOk());
    }

    @Test
    public void testUnassignNvrFromCustomer() throws Exception {
        List<Nvr> nvrs = generateNvrs(1);
        Nvr savedNvr = nvrs.get(0);

        Customer customer = new Customer();
        customer.setTenantId(savedTenant.getId());
        customer.setTitle("Test customer");
        Customer savedCustomer = doPost("/api/customer/", customer, Customer.class);

        Nvr assignedNvr = doPost("/api/customer/" + savedCustomer.getId().toString() + "/nvr/" + savedNvr.getId().toString(), Nvr.class);
        Assert.assertFalse(assignedNvr.getAssignedCustomers().isEmpty());
        List<CustomerId> idList = new ArrayList<>();
        for (ShortCustomerInfo customerInfo : assignedNvr.getAssignedCustomers()) {
            idList.add(customerInfo.getCustomerId());
        }
        idList.retainAll(Collections.singletonList(savedCustomer.getId()));
        Assert.assertEquals(idList, Collections.singletonList(savedCustomer.getId()));

        Nvr foundNvr = doGet("/api/nvr/" + savedNvr.getId().toString(), Nvr.class);
        Assert.assertEquals(foundNvr.getId(), savedNvr.getId());
        idList.clear();
        for (ShortCustomerInfo customerInfo : foundNvr.getAssignedCustomers()) {
            idList.add(customerInfo.getCustomerId());
        }
        idList.retainAll(Collections.singletonList(savedCustomer.getId()));
        Assert.assertEquals(idList, Collections.singletonList(savedCustomer.getId()));

        Nvr unassignedNvr = doDelete("/api/customer/"+ savedCustomer.getId().toString() +"/nvr/" + savedNvr.getId().toString(), Nvr.class);
        Assert.assertTrue(unassignedNvr.getAssignedCustomers().isEmpty());

        destructNvrs(nvrs);
    }

    @Test
    public void testAssignNvrToCustomerFromDifferentTenant() throws Exception {
        loginSysAdmin();

        Tenant tenant = new Tenant();
        tenant.setTitle("Different tenant");
        Tenant savedTenant = doPost("/api/tenant", tenant, Tenant.class);
        Assert.assertNotNull(savedTenant);

        User user = new User();
        user.setAuthority(Authority.TENANT_ADMIN);
        user.setTenantId(savedTenant.getId());
        user.setEmail("asd@havelsan.com.tr");
        user.setFirstName("sad");
        user.setLastName("das");

        user = createUserAndLogin(user, "123456");

        Customer customer = new Customer();
        customer.setTitle("Different customer");
        Customer savedCustomer = doPost("/api/customer", customer, Customer.class);
        Assert.assertNotNull(savedCustomer);

        login(tenantAdmin.getEmail(), "testPassword1");

        List<Nvr> nvrs = generateNvrs(1);
        Nvr savedNvr = nvrs.get(0);

        doPost("/api/customer/" + savedCustomer.getId().toString() + "/nvr/" + savedNvr.getId().toString())
                .andExpect(status().isForbidden());

        loginSysAdmin();

        doDelete("/api/tenant/" + savedTenant.getId().getId().toString())
                .andExpect(status().isOk());

        login(tenantAdmin.getEmail(), "testPassword1");
        destructNvrs(nvrs);
    }

    @Test
    public void testGetTenantNvrs() throws Exception {
        List<Nvr> nvrs = generateNvrs(185);
        List<Nvr> loadedNvrs = new ArrayList<>();
        TextPageLink textPageLink = new TextPageLink(23);
        TextPageData<Nvr> pageData = null;

        do {
            pageData = doGetTypedWithPageLink("/api/tenant/nvrs?",
                    new TypeReference<TextPageData<Nvr>>() {
                    }, textPageLink);
            loadedNvrs.addAll(pageData.getData());
            if (pageData.hasNext()) {
                textPageLink = pageData.getNextPageLink();
            }
        } while (pageData.hasNext());

        Collections.sort(nvrs, idComparator);
        Collections.sort(loadedNvrs, idComparator);

        Assert.assertEquals(nvrs, loadedNvrs);
    }

    @Test
    public void testGetTenantNvrByName() throws Exception {
        List<Nvr> nvrs = generateNvrs(10);
        Nvr savedNvr = nvrs.get(3);
        String name = savedNvr.getName();

        Nvr foundNvr = doGet("/api/tenant/nvrs?name=" + name, Nvr.class);

        Assert.assertEquals(savedNvr, foundNvr);

        destructNvrs(nvrs);
    }

    @Test
    public void testGetTenantNvrByHost() throws Exception {
        List<Nvr> nvrs = generateNvrs(10);
        Nvr savedNvr = nvrs.get(3);
        String host = savedNvr.getIp();

        Nvr foundNvr = doGet("/api/tenant/nvrs?host=" + host, Nvr.class);

        Assert.assertEquals(savedNvr, foundNvr);

        destructNvrs(nvrs);
    }

    @Test
    public void tesFindTenantNvrsByName() throws Exception {
        List<Nvr> nvrs = generateNvrs(150);
        String namePrefix = "My nvr 1";

        List<Nvr> selectedNvrs = new ArrayList<>();
        for (Nvr nvr : nvrs) {
            if (nvr.getName().contains(namePrefix))
                selectedNvrs.add(nvr);
        }

        List<Nvr> foundNvrs = new ArrayList<>();
        TextPageLink pageLink = new TextPageLink(5, namePrefix);
        TextPageData<Nvr> pageData = null;

        do {
            pageData = doGetTypedWithPageLink("/api/tenant/nvrs?",
                    new TypeReference<TextPageData<Nvr>>() {
                    }, pageLink);
            foundNvrs.addAll(pageData.getData());

            if (pageData.hasNext()) {
                pageLink = pageData.getNextPageLink();
            }
        } while (pageData.hasNext());


        Collections.sort(foundNvrs, idComparator);
        Collections.sort(selectedNvrs, idComparator);

        Assert.assertEquals(foundNvrs, selectedNvrs);
        destructNvrs(nvrs);
    }

    @Test
    public void testFindTenantNvrsByType() throws Exception {
        List<Nvr> nvrs = generateNvrs(100);
        List<Nvr> type0Nvrs = new ArrayList<>();
        String type0 = "default0";
        for (Nvr nvr : nvrs) {
            if (nvr.getType().equals(type0)) {
                type0Nvrs.add(nvr);
            }
        }

        List<Nvr> foundNvrs = new ArrayList<>();
        TextPageLink pageLink = new TextPageLink(24);
        TextPageData pageData = null;
        do {
            pageData = doGetTypedWithPageLink("/api/tenant/nvrs?type={type}&",
                    new TypeReference<TextPageData<Nvr>>() {
                    }, pageLink, type0);
            foundNvrs.addAll(pageData.getData());
            if (pageData.hasNext()) {
                pageLink = pageData.getNextPageLink();
            }
        } while (pageData.hasNext());

        type0Nvrs.sort(idComparator);
        foundNvrs.sort(idComparator);
        Assert.assertEquals(type0Nvrs, foundNvrs);

        destructNvrs(nvrs);
    }

    @Test
    public void testGetCustomerNvrs() throws Exception {
        Customer customer = new Customer();
        customer.setTitle("New Customer");
        Customer savedCustomer = doPost("/api/customer", customer, Customer.class);
        String customerId = savedCustomer.getId().toString();

        List<Nvr> nvrs = generateNvrs(10);
        List<Nvr> customerNvrs = new ArrayList<>();
        for (Nvr nvr : nvrs) {
            Nvr tempNvr = doPost("/api/customer/" + customerId + "/nvr/" + nvr.getId().toString(), Nvr.class);
            customerNvrs.add(tempNvr);
        }

        List<Nvr> foundNvrs = new ArrayList<>();
        TextPageLink pageLink = new TextPageLink(24);
        TextPageData pageData = null;
        do {
            pageData = doGetTypedWithPageLink("/api/customer/" + customerId + "/nvrs?",
                    new TypeReference<TextPageData<Nvr>>() {
                    }, pageLink);
            foundNvrs.addAll(pageData.getData());
            if (pageData.hasNext()) {
                pageLink = pageData.getNextPageLink();
            }
        } while (pageData.hasNext());

        Collections.sort(customerNvrs, idComparator);
        Collections.sort(foundNvrs, idComparator);
        Assert.assertEquals(customerNvrs, foundNvrs);
        destructNvrs(nvrs);
    }

    @Test
    public void testGetCustomerNvrsByName() throws Exception {
        Customer customer = new Customer();
        customer.setTitle("New Customer");
        Customer savedCustomer = doPost("/api/customer", customer, Customer.class);
        String customerId = savedCustomer.getId().toString();
        String namePrefix = "Cam1";

        List<Nvr> nvrs = generateNvrs(110);
        List<Nvr> customerNvrs = new ArrayList<>();
        for (Nvr nvr : nvrs) {
            if (nvr.getName().contains(namePrefix)) {
                Nvr tempNvr = doPost("/api/customer/" + customerId + "/nvr/" + nvr.getId().toString(), Nvr.class);
                customerNvrs.add(tempNvr);
            }
        }

        List<Nvr> foundNvrs = new ArrayList<>();
        TextPageLink pageLink = new TextPageLink(24, namePrefix);
        TextPageData pageData = null;
        do {
            pageData = doGetTypedWithPageLink("/api/customer/" + customerId + "/nvrs?",
                    new TypeReference<TextPageData<Nvr>>() {
                    }, pageLink);
            foundNvrs.addAll(pageData.getData());
            if (pageData.hasNext()) {
                pageLink = pageData.getNextPageLink();
            }
        } while (pageData.hasNext());

        Collections.sort(customerNvrs, idComparator);
        Collections.sort(foundNvrs, idComparator);
        Assert.assertEquals(customerNvrs, foundNvrs);
        destructNvrs(nvrs);
    }

    @Test
    public void testGetCustomerNvrsByType() throws Exception {
        Customer customer = new Customer();
        customer.setTitle("New Customer");
        Customer savedCustomer = doPost("/api/customer", customer, Customer.class);
        String customerId = savedCustomer.getId().toString();
        String type0 = "default0";

        List<Nvr> nvrs = generateNvrs(110);
        List<Nvr> customerNvrs = new ArrayList<>();
        for (Nvr nvr : nvrs) {
            if (nvr.getType().equals(type0)) {
                Nvr tempNvr = doPost("/api/customer/" + customerId + "/nvr/" + nvr.getId().toString(), Nvr.class);
                customerNvrs.add(tempNvr);
            }
        }

        List<Nvr> foundNvrs = new ArrayList<>();
        TextPageLink pageLink = new TextPageLink(24);
        TextPageData pageData = null;
        do {
            pageData = doGetTypedWithPageLink("/api/customer/" + customerId + "/nvrs?type={type}&",
                    new TypeReference<TextPageData<Nvr>>() {
                    }, pageLink, type0);
            foundNvrs.addAll(pageData.getData());
            if (pageData.hasNext()) {
                pageLink = pageData.getNextPageLink();
            }
        } while (pageData.hasNext());

        Collections.sort(customerNvrs, idComparator);
        Collections.sort(foundNvrs, idComparator);
        Assert.assertEquals(customerNvrs, foundNvrs);
        destructNvrs(nvrs);
    }

    @Test
    public void testGetNvrsByIds() throws Exception {
        List<Nvr> nvrs = generateNvrs(3);
        List<String> nvrIds = new ArrayList<>();

        for (Nvr nvr : nvrs) {
            nvrIds.add(nvr.getId().toString());
        }

        List<Nvr> foundNvrs = doGetTyped(
                "/api/nvrs?nvrIds="
                        + nvrIds.get(0)
                        + "," + nvrIds.get(1)
                        + "," + nvrIds.get(2),
                new TypeReference<List<Nvr>>() {
                });

        nvrs.sort(idComparator);
        foundNvrs.sort(idComparator);
        Assert.assertEquals(nvrs, foundNvrs);
        destructNvrs(nvrs);
    }

    @Test
    public void testGetNvrTypes() throws Exception {
        List<EntitySubtype> types = new ArrayList<>();

        Nvr nvr = new Nvr();
        nvr.setTenantId(savedTenant.getId());
        nvr.setIp("192.168.1.1");
        nvr.setName("My nvr ");
        nvr.setBrand("ASL");
        nvr.setModel("model1");

        for (int i = 0; i < 5; i++) {
            nvr.setType("default" + i);
            doPost("/api/nvr/", nvr).andExpect(status().isOk());
        }

        types = doGetTyped("/api/nvr/types", new TypeReference<List<EntitySubtype>>() {
        });
        Assert.assertEquals(types.size(), 5);
        Assert.assertEquals("default0", types.get(0).getType());
        Assert.assertEquals("default1", types.get(1).getType());
        Assert.assertEquals("default2", types.get(2).getType());
        Assert.assertEquals("default3", types.get(3).getType());
        Assert.assertEquals("default4", types.get(4).getType());
    }

}





















































