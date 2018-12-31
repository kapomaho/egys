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

import com.datastax.driver.core.utils.UUIDs;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.containsString;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class BaseCameraControllerTest extends AbstractControllerTest {
    private IdComparator<Camera> idComparator = new IdComparator<>();

    private Tenant savedTenant;

    private User tenantAdmin;

    @Before
    public void beforeTest() throws Exception {
        loginSysAdmin();

        Tenant tenant = new Tenant();
        tenant.setTitle("My tenant");
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

        doDelete("/api/tenant/"+savedTenant.getId().getId().toString())
                .andExpect(status().isOk());
    }

    @Test
    public void testSaveCamera() throws Exception {
        List<Camera> cameras = createCameras(1);

        Camera savedCamera = cameras.get(0);

        Assert.assertNotNull(savedCamera);
        Assert.assertNotNull(savedCamera.getTenantId());
        Assert.assertTrue(savedCamera.getCreatedTime() > 0);
        Assert.assertEquals(savedCamera.getTenantId(), savedTenant.getId());
        Assert.assertTrue(savedCamera.getAssignedCustomers().isEmpty());

        Camera foundCamera = doGet("/api/camera/" + savedCamera.getId().toString(), Camera.class);
        Assert.assertEquals(foundCamera, savedCamera);

        destructCameras(cameras);
    }

    @Test
    public void testSaveCameraWithEmptyFields() throws Exception {
        Camera camera = new Camera();
        camera.setPtz(true);
        camera.setSecondaryUrlPath("/stream2");
        camera.setPrimaryUrlPath("/stream1");
        camera.setPort(554);
        camera.setType("Test Camera Type");
        camera.setName("Test Camera");
        doPost("/api/camera", camera)
                .andExpect(status().isBadRequest())
                .andExpect(statusReason(containsString("Camera host should be specified")));

        camera.setHost("192.168.1.45");
        camera.setName(null);
        doPost("/api/camera", camera)
                .andExpect(status().isBadRequest())
                .andExpect(statusReason(containsString("Camera name should be specified")));

        camera.setName("Camera name");
        camera.setType(null);
        doPost("/api/camera", camera)
                .andExpect(status().isBadRequest())
                .andExpect(statusReason(containsString("Camera type should be specified")));

        camera.setType("default");
        camera.setPrimaryUrlPath(null);
        camera.setSecondaryUrlPath(null);
        doPost("/api/camera", camera)
                .andExpect(status().isBadRequest())
                .andExpect(statusReason(containsString("Camera stream primary or secondary url path should be specified")));

        camera.setPrimaryUrlPath("/stream1");
        camera.setBrand(null);
        doPost("/api/camera", camera)
                .andExpect(status().isBadRequest())
                .andExpect(statusReason(containsString("Camera brand should be specified")));

        camera.setBrand("ASL");
        camera.setModel(null);
        doPost("/api/camera", camera)
                .andExpect(status().isBadRequest())
                .andExpect(statusReason(containsString("Camera model should be specified")));

        camera.setModel("Sabit");
        Camera camera1 = doPost("/api/camera", camera, Camera.class);

        List<Camera> cameras = new ArrayList<>();
        cameras.add(camera1);
        destructCameras(cameras);
    }

    @Test
    public void testGetCameraById() throws Exception {
        List<Camera> cameras = createCameras(1);
        Camera camera = cameras.get(0);
        Assert.assertNotNull(camera);
        Camera foundCamera = doGet("/api/camera/" + camera.getId().toString(), Camera.class);
        Assert.assertNotNull(foundCamera);
        Assert.assertEquals(foundCamera, camera);
        destructCameras(cameras);
    }

    @Test
    public void testGetCameraByInvalidOrNonexistentId() throws Exception {
        doGet("/api/camera/invalidid")
                .andExpect(status().isBadRequest())
                .andExpect(statusReason(containsString("Invalid UUID string")));

        Camera camera = new Camera();
        camera.setPtz(true);
        camera.setPrimaryUrlPath("/stream1");
        camera.setSecondaryUrlPath("/stream2");
        camera.setPort(554);
        camera.setModel("Sabit");
        camera.setBrand("ASL");
        camera.setHost("192.168.1.45");
        camera.setType("Test Camera Type");
        camera.setName("Test Camera");

        Camera savedCamera = doPost("/api/camera", camera, Camera.class);

        doDelete("/api/camera/" + savedCamera.getId().toString()).andExpect(status().isOk());
        doGet("/api/camera/" + savedCamera.getId().getId().toString())
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGetCamerasByIds() throws Exception {
        List<String> cameraIds = new ArrayList<>();
        List<Camera> cameraList = createCameras(5);
        for (Camera camera : cameraList) {
            cameraIds.add(camera.getId().getId().toString());
        }
        List<Camera> cameras = doGetTyped(
                "/api/cameras?cameraIds="
                        + cameraIds.get(0)
                        + "," + cameraIds.get(1)
                        + "," + cameraIds.get(2)
                        + "," + cameraIds.get(3)
                        + "," + cameraIds.get(4),
                new com.fasterxml.jackson.core.type.TypeReference<List<Camera>>() {});
        Assert.assertEquals(cameras.size(), cameraList.size());
        cameraList.sort(idComparator);
        cameras.sort(idComparator);
        Assert.assertEquals(cameraList, cameras);
        destructCameras(cameraList);
    }

    @Test
    public void testGetCamerasNvrId() throws Exception {
        List<Camera> cameraList = createCameras(3);
        List<EntityRelation> relationList = new ArrayList<>();

        Nvr nvr = new Nvr();
        nvr.setTenantId(savedTenant.getId());
        nvr.setType("default");
        nvr.setName("My nvr ");
        nvr.setIp("192.168.1.1");
        nvr.setBrand("ASL");
        nvr.setModel("model1");
        Nvr savedNvr = doPost("/api/nvr/", nvr, Nvr.class);

        for (int i = 0; i < cameraList.size(); i++){
            EntityRelation relation = new EntityRelation(savedNvr.getId(), cameraList.get(i).getId(),"Manages", RelationTypeGroup.COMMON);
            relationList.add(relation);
            doPost("/api/relation/", relation);
        }

        List<Camera> nvrCameras = doGetTyped("/api/nvr/" + savedNvr.getId().getId() + "/cameras",
                new com.fasterxml.jackson.core.type.TypeReference<List<Camera>>() {});

        cameraList.sort(idComparator);
        nvrCameras.sort(idComparator);
        Assert.assertEquals(cameraList, nvrCameras);
        destructCameras(cameraList);
        doDelete("/api/nvr/" + savedNvr.getId().toString()).andExpect(status().isOk());

        for (int i = 0; i < relationList.size(); i++) {
            String fromId = relationList.get(0).getFrom().getId().toString();
            String toId = relationList.get(0).getTo().getId().toString();
            String toType = relationList.get(0).getTo().getEntityType().toString();
            String fromType = relationList.get(0).getFrom().getEntityType().toString();
            String relationType = relationList.get(0).getType();
            String relationTypeGroup = RelationTypeGroup.COMMON.toString();

             doDelete("/api/relation?relationTypeGroup=" + relationTypeGroup + "&fromId=" + fromId +
                             "&fromType=" + fromType + "&relationType=" + relationType + "&toId=" + toId + "&toType=" + toType);
        }
    }

    @Test
    public void testDeleteNonexistentCamera() throws Exception {
        Camera camera = new Camera();
        camera.setPtz(true);
        camera.setSecondaryUrlPath("/stream2");
        camera.setPrimaryUrlPath("/stream1");
        camera.setPort(554);
        camera.setBrand("ASL");
        camera.setModel("Sabit");
        camera.setHost("192.168.1.45");
        camera.setType("Test Camera Type");
        camera.setName("Test Camera");

        Camera savedCamera = doPost("/api/camera", camera, Camera.class);

        doDelete("/api/camera/" + savedCamera.getId().toString()).andExpect(status().isOk());
        doDelete("/api/camera/" + savedCamera.getId().toString()).andExpect(status().isNotFound());
    }

    @Test
    public void testDeleteCamera() throws Exception {
        Camera camera = new Camera();
        camera.setPtz(true);
        camera.setPrimaryUrlPath("/stream1");
        camera.setSecondaryUrlPath("/stream2");
        camera.setPort(554);
        camera.setHost("192.168.1.45");
        camera.setType("Test Camera Type");
        camera.setName("Test Camera");
        camera.setBrand("ASL");
        camera.setModel("Sabit");

        Camera savedCamera = doPost("/api/camera", camera, Camera.class);
        Camera foundCamera = doGet("/api/camera/" + savedCamera.getId().toString(), Camera.class);
        Assert.assertEquals(foundCamera, savedCamera);

        doDelete("/api/camera/" + savedCamera.getId().toString()).andExpect(status().isOk());
        doGet("/api/camera/" + savedCamera.getId().toString()).andExpect(status().isNotFound());
    }

    private List<Camera> createCameras(int count) throws Exception {
        List<Camera> cameras = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Camera c = new Camera();
            c.setPrimaryUrlPath("/stream1");
            c.setPtz(i%2 == 0);
            c.setType("type" + i%2);
            c.setName("Cam" + i);
            c.setBrand("ASL");
            c.setModel("Sabit");
            c.setHost("192.168.1." + (i + 1));
            Camera camera = doPost("/api/camera", c, Camera.class);
            Assert.assertEquals(c.getName(), camera.getName());
            Assert.assertEquals(c.getHost(), camera.getHost());
            Assert.assertEquals(c.getPrimaryUrlPath(), camera.getPrimaryUrlPath());
            Assert.assertEquals(c.getPort(), camera.getPort());
            Assert.assertEquals(c.getType(), camera.getType());
            cameras.add(camera);
        }
        return cameras;
    }

    private void destructCameras(List<Camera> cameras) throws Exception {
        for (Camera camera : cameras) {
            doDelete("/api/camera/" + camera.getId().toString()).andExpect(status().isOk());
        }
    }

    @Test
    public void testGetTenantCameras() throws Exception {
        List<Camera> cameras = createCameras(50);

        TextPageLink pageLink = new TextPageLink(10);
        List<Camera> foundCameras = new ArrayList<>();
        TextPageData<Camera> pageData = null;

        do {
            pageData = doGetTypedWithPageLink(
                    "/api/tenant/cameras?",
                    new TypeReference<TextPageData<Camera>>() {},
                    pageLink
            );
            foundCameras.addAll(pageData.getData());
            if (pageData.hasNext()) {
                pageLink = pageData.getNextPageLink();
            }
        } while ((pageData.hasNext()));

        cameras.sort(idComparator);
        foundCameras.sort(idComparator);

        Assert.assertEquals(cameras, foundCameras);

        destructCameras(cameras);
    }

    @Test
    public void testGetTenantCameraByHost() throws Exception {
        List<Camera> cameras = createCameras(100);
        Camera toBeFound = cameras.get(10);
        String name = toBeFound.getHost();

        Camera foundCamera = doGet("/api/tenant/cameras?host=" + name, Camera.class);

        Assert.assertEquals(toBeFound, foundCamera);

        destructCameras(cameras);
    }

    @Test
    public void testGetTenantCameraByName() throws Exception {
        List<Camera> cameras = createCameras(100);
        Camera toBeFound = cameras.get(10);
        String name = toBeFound.getName();

        Camera foundCamera = doGet("/api/tenant/cameras?name=" + name, Camera.class);

        Assert.assertEquals(toBeFound, foundCamera);

        destructCameras(cameras);
    }

    @Test
    public void testFindTenantCamerasByName() throws Exception {
        List<Camera> cameras = createCameras(100);

        String name = "Cam1";

        List<Camera> toBeFound = new ArrayList<>();
        for (Camera camera : cameras) {
            if (camera.getName().contains(name)) toBeFound.add(camera);
        }

        List<Camera> found = new ArrayList<>();

        TextPageLink pageLink = new TextPageLink(5, name);
        TextPageData<Camera> pageData = null;

        do {
            pageData = doGetTypedWithPageLink("/api/tenant/cameras?", new TypeReference<TextPageData<Camera>>() {
            }, pageLink);

            found.addAll(pageData.getData());

            if (pageData.hasNext()) {
                pageLink = pageData.getNextPageLink();
            }

        } while (pageData.hasNext());


        Assert.assertEquals(toBeFound.size(), found.size());
        toBeFound.sort(idComparator);
        found.sort(idComparator);
        Assert.assertEquals(toBeFound, found);

        destructCameras(cameras);
    }

    @Test
    public void testGetTenantCamerasByType() throws Exception {
        List<Camera> cameras = createCameras(50);
        List<Camera> toBeFound = new ArrayList<>();
        for (Camera camera : cameras) {
            if (camera.getType().equals("type0")) toBeFound.add(camera);
        }

        List<Camera> foundCameras = new ArrayList<>();
        TextPageLink pageLink = new TextPageLink(10);
        TextPageData<Camera> pageData = null;

        do {
            pageData = doGetTypedWithPageLink("/api/tenant/cameras?type={type}&",
                    new TypeReference<TextPageData<Camera>>() {
                    }, pageLink, "type0");
            foundCameras.addAll(pageData.getData());
            if (pageData.hasNext()) {
                pageLink = pageData.getNextPageLink();
            }
        } while (pageData.hasNext());

        toBeFound.sort(idComparator);
        foundCameras.sort(idComparator);
        Assert.assertEquals(toBeFound.size(), foundCameras.size());
        Assert.assertEquals(toBeFound, foundCameras);

        destructCameras(cameras);
    }

    @Test
    public void testGetCameraTypes() throws Exception {
        List<EntitySubtype> subtypes = doGetTyped("/api/camera/types", new com.fasterxml.jackson.core.type.TypeReference<List<EntitySubtype>>() {});
        Assert.assertEquals(subtypes, Collections.emptyList());

        Camera camera = new Camera();
        camera.setPtz(true);
        camera.setPrimaryUrlPath("/stream2");
        camera.setPort(554);
        camera.setHost("192.168.1.45");
        camera.setName("Test Camera");
        camera.setBrand("ASL");
        camera.setModel("Sabit");

        for (int i = 0; i< 5; i++) {
            camera.setType("TestCameraType" + i);
            doPost("/api/camera", camera).andExpect(status().isOk());
        }

        subtypes = doGetTyped("/api/camera/types", new com.fasterxml.jackson.core.type.TypeReference<List<EntitySubtype>>() {});
        Assert.assertEquals(5, subtypes.size());
        Assert.assertEquals("TestCameraType0", subtypes.get(0).getType());
        Assert.assertEquals("TestCameraType1", subtypes.get(1).getType());
        Assert.assertEquals("TestCameraType2", subtypes.get(2).getType());
        Assert.assertEquals("TestCameraType3", subtypes.get(3).getType());
        Assert.assertEquals("TestCameraType4", subtypes.get(4).getType());
    }

    @Test
    public void testGetCustomerCameras() throws Exception {
        Customer customer = new Customer();
        customer.setTitle("Camera Test Customer");
        customer = doPost("/api/customer", customer, Customer.class);
        CustomerId id = customer.getId();

        List<Camera> cameras = createCameras(10);
        List<Camera> customerCameras = new ArrayList<>();
        for (Camera camera : cameras) {
            Camera cCam = doPost("/api/customer/" + id.getId().toString() + "/camera/" + camera.getId().toString(), Camera.class);
            customerCameras.add(cCam);
        }

        List<Camera> found = new ArrayList<>();
        TextPageLink pageLink = new TextPageLink(10);
        TextPageData<Camera> pageData = null;

        do {
            pageData = doGetTypedWithPageLink("/api/customer/" + id.getId().toString() + "/cameras?",
                    new TypeReference<TextPageData<Camera>>() {}, pageLink);
            found.addAll(pageData.getData());
            if (pageData.hasNext()) {
                pageLink = pageData.getNextPageLink();
            }
        } while (pageData.hasNext());

        found.sort(idComparator);
        cameras.sort(idComparator);
        customerCameras.sort(idComparator);

        Assert.assertEquals(customerCameras, found);

        destructCameras(cameras);
    }

    @Test
    public void testAssignUnassignCameraToCustomer() throws Exception {
        List<Camera> cameras = createCameras(1);
        Camera camera = cameras.get(0);

        Customer customer = new Customer();
        customer.setTitle("Camera Test Customer");
        Customer savedCustomer = doPost("/api/customer", customer, Customer.class);

        Assert.assertTrue(camera.getAssignedCustomers().isEmpty());

        Camera assignedCamera = doPost("/api/customer/" + savedCustomer.getId().toString() + "/camera/" + camera.getId().toString(), Camera.class);
        Assert.assertFalse(assignedCamera.getAssignedCustomers().isEmpty());
        List<CustomerId> idList = new ArrayList<>();
        for (ShortCustomerInfo customerInfo : assignedCamera.getAssignedCustomers()) {
            idList.add(customerInfo.getCustomerId());
        }
        idList.retainAll(Collections.singletonList(savedCustomer.getId()));
        Assert.assertEquals(idList, Collections.singletonList(savedCustomer.getId()));

        Camera foundCamera = doGet("/api/camera/" + camera.getId().getId().toString(), Camera.class);
        Assert.assertEquals(foundCamera.getId(), camera.getId());

        idList.clear();
        for (ShortCustomerInfo customerInfo : assignedCamera.getAssignedCustomers()) {
            idList.add(customerInfo.getCustomerId());
        }
        idList.retainAll(Collections.singletonList(savedCustomer.getId()));
        Assert.assertEquals(idList, Collections.singletonList(savedCustomer.getId()));

        String url = "/api/customer/" + savedCustomer.getId().toString() + "/camera/" + camera.getId().getId().toString();
        Camera unassignedCamera = doDelete(url, Camera.class);
        Assert.assertTrue(unassignedCamera.getAssignedCustomers().isEmpty());

        foundCamera = doGet("/api/camera/" + camera.getId().getId().toString(), Camera.class);
        Assert.assertTrue(foundCamera.getAssignedCustomers().isEmpty());

        destructCameras(cameras);
    }

    @Test
    public void testAssignCameraToNonExistentCustomer() throws Exception {
        List<Camera> cameras = createCameras(1);

        doPost("/api/customer/" + UUIDs.timeBased().toString() + "/camera/" + cameras.get(0).getId().getId().toString()).andExpect(status().isNotFound());

        destructCameras(cameras);
    }

    @Test
    public void testAssignCameraToCustomerFromDifferentTenant() throws Exception {
        loginSysAdmin();

        Tenant tenant = new Tenant();
        tenant.setTitle("Different tenant");
        Tenant savedTenant = doPost("/api/tenant", tenant, Tenant.class);
        Assert.assertNotNull(savedTenant);

        User user = new User();
        user.setAuthority(Authority.TENANT_ADMIN);
        user.setTenantId(savedTenant.getId());
        user.setEmail("ssoysal1@havelsan.com.tr");
        user.setFirstName("Soner");
        user.setLastName("Soysal");

        user = createUserAndLogin(user, "qwe123");

        Customer customer = new Customer();
        customer.setTitle("Different customer");
        Customer savedCustomer = doPost("/api/customer", customer, Customer.class);
        Assert.assertNotNull(savedCustomer);

        login(tenantAdmin.getEmail(), "testPassword1");

        List<Camera> cameras = createCameras(1);
        Camera savedCamera = cameras.get(0);

        doPost("/api/customer/"
                + savedCustomer.getId().getId().toString()
                + "/camera/"
                + savedCamera.getId().getId().toString())
                .andExpect(status().isForbidden());

        loginSysAdmin();

        doDelete("/api/tenant/" + savedTenant.getId().getId().toString())
                .andExpect(status().isOk());

        login(tenantAdmin.getEmail(), "testPassword1");
        destructCameras(cameras);
    }

    @Test
    public void testGetCustomerCamerasByName() throws Exception {
        Customer customer = new Customer();
        customer.setTitle("Test customer");
        customer = doPost("/api/customer", customer, Customer.class);

        List<Camera> cameras = createCameras(50);

        List<Camera> customerCameras = new ArrayList<>();

        for (Camera camera : cameras) {
            if (camera.getName().contains("Cam1")) {
                Camera c = doPost("/api/customer/" + customer.getId().toString() + "/camera/" + camera.getId().toString(), Camera.class);
                customerCameras.add(c);
            }
        }

        List<Camera> found = new ArrayList<>();
        TextPageLink pageLink = new TextPageLink(10, "Cam1");
        TextPageData<Camera> pageData = null;

        do {
            pageData = doGetTypedWithPageLink("/api/customer/" + customer.getId().toString() + "/cameras?", new TypeReference<TextPageData<Camera>>() {}, pageLink);
            found.addAll(pageData.getData());

            if (pageData.hasNext()) {
                pageLink = pageData.getNextPageLink();
            }

        } while (pageData.hasNext());

        found.sort(idComparator);
        customerCameras.sort(idComparator);

        Assert.assertEquals(found, customerCameras);

        for (Camera customerCamera : customerCameras) {
            doDelete("/api/customer/"+  customer.getId().toString() + "/camera/" + customerCamera.getId().getId().toString()).andExpect(status().isOk());
        }

        destructCameras(cameras);
    }

    @Test
    public void testGetCustomerCamerasByType() throws Exception {
        Customer customer = new Customer();
        customer.setTitle("Test Customer for Type");
        customer = doPost("/api/customer", customer, Customer.class);

        List<Camera> cameras = createCameras(50);
        List<Camera> toBeFound = new ArrayList<>();
        String type = "type0";

        for (Camera camera : cameras) {
            if (camera.getName().contains(type)) {
                Camera c = doPost("/api/customer/" + customer.getId().toString() + "/camera/" + camera.getId().toString(), Camera.class);
                toBeFound.add(c);
            }
        }

        List<Camera> found = new ArrayList<>();
        TextPageLink pageLink = new TextPageLink(10);
        TextPageData<Camera> pageData = null;

        do {
            pageData = doGetTypedWithPageLink("/api/customer/" + customer.getId().toString() + "/cameras?type={type}&",
                    new TypeReference<TextPageData<Camera>>() {}, pageLink, type);
            found.addAll(pageData.getData());

            if (pageData.hasNext()) {
                pageLink = pageData.getNextPageLink();
            }

        } while (pageData.hasNext());

        found.sort(idComparator);
        toBeFound.sort(idComparator);

        Assert.assertEquals(found, toBeFound);

        for (Camera c : toBeFound) {
            doDelete("/api/customer/"+  customer.getId().toString() + "/camera/" + c.getId().getId().toString()).andExpect(status().isOk());
        }

        destructCameras(cameras);
    }
}
