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
package org.thingsboard.server.dao.service;

import com.datastax.driver.core.utils.UUIDs;
import com.google.common.util.concurrent.ListenableFuture;
import org.junit.*;
import org.junit.rules.ExpectedException;
import org.thingsboard.server.common.data.*;
import org.thingsboard.server.common.data.id.CameraId;
import org.thingsboard.server.common.data.id.CustomerId;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.page.TextPageData;
import org.thingsboard.server.common.data.page.TextPageLink;
import org.thingsboard.server.common.data.relation.EntityRelation;
import org.thingsboard.server.common.data.relation.RelationTypeGroup;
import org.thingsboard.server.dao.exception.DataValidationException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class BaseCameraServiceTest extends AbstractServiceTest {
    private IdComparator<Camera> idComparator = new IdComparator<>();
    private TenantId tenantId;

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Before
    public void before() {
        Tenant tenant = new Tenant();
        tenant.setTitle("My tenant");
        Tenant savedTenant = tenantService.saveTenant(tenant);
        Assert.assertNotNull(savedTenant);
        tenantId = savedTenant.getId();
    }

    @After
    public void after() {
        tenantService.deleteTenant(tenantId);
    }

    private List<Camera> createCameras(int count) throws Exception {
        List<Camera> cameras = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Camera c = new Camera();
            c.setTenantId(tenantId);
            c.setPrimaryUrlPath("/stream1");
            c.setSecondaryUrlPath("/stream2");
            c.setPtz(i%2 == 0);
            c.setType("type" + i%2);
            c.setName("Cam" + i);
            c.setHost("192.168.1." + (i + 1));
            c.setBrand("ASL");
            c.setModel("Model1");
            Camera camera = cameraService.saveCamera(c);
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
            cameraService.deleteCamera(camera.getId());
        }
    }

    @Test
    public void testSaveCamera() {
        Camera c = new Camera();
        c.setTenantId(tenantId);
        c.setPrimaryUrlPath("/stream1");
        c.setSecondaryUrlPath("/stream2");
        c.setPtz(true);
        c.setType("type1");
        c.setName("Cam1");
        c.setHost("192.168.1.2");
        c.setBrand("ASL");
        c.setModel("Model1");
        Camera camera = cameraService.saveCamera(c);

        Assert.assertEquals(c.getName(), camera.getName());
        Assert.assertEquals(c.getHost(), camera.getHost());
        Assert.assertEquals(c.getPrimaryUrlPath(), camera.getPrimaryUrlPath());
        Assert.assertEquals(c.getSecondaryUrlPath(), camera.getSecondaryUrlPath());
        Assert.assertEquals(c.getPort(), camera.getPort());
        Assert.assertEquals(c.getType(), camera.getType());

        cameraService.deleteCamera(camera.getId());
    }

    @Test(expected = DataValidationException.class)
    public void testSaveCameraWithEmptyType() {
        Camera camera = new Camera();
        cameraService.saveCamera(camera);
    }

    @Test(expected = DataValidationException.class)
    public void testSaveCameraWithEmptyName() {
        Camera camera = new Camera();
        camera.setType("Type1");
        cameraService.saveCamera(camera);
    }

    @Test(expected = DataValidationException.class)
    public void testSaveCameraWithEmptyHost() {
        Camera camera = new Camera();
        camera.setType("Type1");
        camera.setName("Cam1");
        cameraService.saveCamera(camera);
    }

    @Test(expected = DataValidationException.class)
    public void testSaveCameraWithEmptyPrimaryUrl() {
        Camera camera = new Camera();
        camera.setType("Type1");
        camera.setName("Cam1");
        camera.setHost("192.168.1.2");
        cameraService.saveCamera(camera);
    }

    @Test(expected = DataValidationException.class)
    public void testSaveCameraWithEmptyBrand() {
        Camera camera = new Camera();
        camera.setType("Type1");
        camera.setName("Cam1");
        camera.setHost("192.168.1.2");
        camera.setPrimaryUrlPath("/stream1");
        cameraService.saveCamera(camera);
    }

    @Test(expected = DataValidationException.class)
    public void testSaveCameraWithEmptyModel() {
        Camera camera = new Camera();
        camera.setType("Type1");
        camera.setName("Cam1");
        camera.setHost("192.168.1.2");
        camera.setPrimaryUrlPath("/stream1");
        camera.setBrand("ASL");
        cameraService.saveCamera(camera);
    }

    @Test(expected = DataValidationException.class)
    public void testSaveCameraWithEmptyTenant() {
        Camera camera = new Camera();
        camera.setType("Type1");
        camera.setName("Cam1");
        camera.setHost("192.168.1.2");
        camera.setPrimaryUrlPath("/stream1");
        camera.setBrand("ASL");
        camera.setModel("Model1");
        cameraService.saveCamera(camera);
    }

    @Test(expected = DataValidationException.class)
    public void testSaveCameraWithRtspAuthWithoutUsername() {
        Camera camera = new Camera();
        camera.setType("Type1");
        camera.setName("Cam1");
        camera.setHost("192.168.1.2");
        camera.setPrimaryUrlPath("/stream1");
        camera.setBrand("ASL");
        camera.setModel("Model1");
        camera.setTenantId(tenantId);
        camera.setRtspAuth(true);
        cameraService.saveCamera(camera);
    }

    @Test(expected = DataValidationException.class)
    public void testSaveCameraWithRtspAuthWithoutPassword() {
        Camera camera = new Camera();
        camera.setType("Type1");
        camera.setName("Cam1");
        camera.setHost("192.168.1.2");
        camera.setPrimaryUrlPath("/stream1");
        camera.setBrand("ASL");
        camera.setModel("Model1");
        camera.setRtspAuth(true);
        camera.setUsername("Asdf");
        cameraService.saveCamera(camera);
    }

    @Test(expected = DataValidationException.class)
    public void testSaveCameraWithRtspAuthInvalidCustomer() {
        Camera camera = new Camera();
        camera.setType("Type1");
        camera.setName("Cam1");
        camera.setHost("192.168.1.2");
        camera.setPrimaryUrlPath("/stream1");
        camera.setBrand("ASL");
        camera.setModel("Model1");
        camera.setRtspAuth(true);
        camera.setUsername("Asdf");
        camera.setPassword("1234");
        cameraService.saveCamera(camera);
        try{
            cameraService.assignCameraToCustomer(camera.getId(), new CustomerId(UUIDs.timeBased()));
        } finally {
            cameraService.deleteCamera(camera.getId());
        }
    }

    @Test(expected = DataValidationException.class)
    public void testSaveCameraToNonExistingCustomer() {
        Camera camera = new Camera();
        camera.setType("Type1");
        camera.setName("Cam1");
        camera.setHost("192.168.1.2");
        camera.setPrimaryUrlPath("/stream1");
        camera.setBrand("ASL");
        camera.setModel("Model1");
        camera.setRtspAuth(false);
        camera.setTenantId(tenantId);
        camera = cameraService.saveCamera(camera);

        try {
            cameraService.assignCameraToCustomer(camera.getId(), new CustomerId(UUIDs.timeBased()));
        } finally {
            cameraService.deleteCamera(camera.getId());
        }
    }

    @Test(expected = DataValidationException.class)
    public void testSaveCameraToCustomerFromDifferentTenant() {
        Camera camera = new Camera();
        camera.setType("Type1");
        camera.setName("Cam1");
        camera.setHost("192.168.1.2");
        camera.setPrimaryUrlPath("/stream1");
        camera.setBrand("ASL");
        camera.setModel("Model1");
        camera.setRtspAuth(false);
        camera.setTenantId(tenantId);
        camera = cameraService.saveCamera(camera);

        Tenant tenant = new Tenant();
        tenant.setTitle("Test different tenant");
        Customer customer  = new Customer();
        customer.setTenantId(tenant.getId());
        customer.setTitle("Test different customer");
        customer = customerService.saveCustomer(customer);

        try {
            cameraService.assignCameraToCustomer(camera.getId(), customer.getId());
        } finally {
            cameraService.deleteCamera(camera.getId());
            tenantService.deleteTenant(tenant.getId());
        }
    }

    @Test
    public void testFindCameraById() {
        Camera camera = new Camera();
        camera.setType("Type1");
        camera.setName("Cam1");
        camera.setHost("192.168.1.2");
        camera.setPrimaryUrlPath("/stream1");
        camera.setBrand("ASL");
        camera.setModel("Model1");
        camera.setRtspAuth(false);
        camera.setTenantId(tenantId);
        Camera savedCamera = cameraService.saveCamera(camera);
        Camera foundCamera = cameraService.findCameraById(savedCamera.getId());
        Assert.assertNotNull(foundCamera);
        Assert.assertEquals(savedCamera, foundCamera);
        cameraService.deleteCamera(savedCamera.getId());
    }

    @Test
    public void testFindCamerasByNvrId() throws Exception {
        Nvr n = new Nvr();
        n.setTenantId(tenantId);
        n.setBrand("ASL");
        n.setIp("192.168.1.1");
        n.setModel("Sabit");
        n.setName("Nvr");
        n.setType("type");
        Nvr nvr = nvrService.saveNvr(n);

        List<Camera> cameras = createCameras(2);

        EntityRelation relation1 = new EntityRelation(nvr.getId(), cameras.get(0).getId(), "Manages", RelationTypeGroup.COMMON);
        EntityRelation relation2 = new EntityRelation(nvr.getId(), cameras.get(1).getId(), "Manages", RelationTypeGroup.COMMON);
        relationService.saveRelation(relation1);
        relationService.saveRelation(relation2);

        List<Camera> foundCameras = cameraService.findCamerasByNvrId(nvr.getId());
        cameras.sort(idComparator);
        foundCameras.sort(idComparator);

        Assert.assertEquals(foundCameras, cameras);

        for (Camera c : cameras){
            relationService.deleteRelation(nvr.getId(), c.getId(), "Manages", RelationTypeGroup.COMMON);
            cameraService.deleteCamera(c.getId());
        }
        nvrService.deleteNvr(nvr.getId());
    }

    @Test
    public void testFindCameraTypesByTenantId() throws Exception{
        List<Camera> cameras = new ArrayList<>();

        try{
            for (int i = 0; i < 3; i++) {
                Camera camera = new Camera();
                camera.setType("Type1");
                camera.setName("Cam" + i);
                camera.setHost("192.168.1." + i);
                camera.setPrimaryUrlPath("/stream1");
                camera.setBrand("ASL");
                camera.setModel("Model1");
                camera.setRtspAuth(false);
                camera.setTenantId(tenantId);
                cameras.add(cameraService.saveCamera(camera));
            }
            for (int i = 0; i < 3; i++) {
                Camera camera = new Camera();
                camera.setType("Type2");
                camera.setName("Cam" + i);
                camera.setHost("192.168.1." + i);
                camera.setPrimaryUrlPath("/stream1");
                camera.setBrand("ASL");
                camera.setModel("Model1");
                camera.setRtspAuth(false);
                camera.setTenantId(tenantId);
                cameras.add(cameraService.saveCamera(camera));
            }
            for (int i = 0; i < 3; i++) {
                Camera camera = new Camera();
                camera.setType("Type3");
                camera.setName("Cam" + i);
                camera.setHost("192.168.1." + i);
                camera.setPrimaryUrlPath("/stream1");
                camera.setBrand("ASL");
                camera.setModel("Model1");
                camera.setRtspAuth(false);
                camera.setTenantId(tenantId);
                cameras.add(cameraService.saveCamera(camera));
            }

            List<EntitySubtype> cameraTypes = cameraService.findCameraTypesByTenantId(tenantId).get();
            Assert.assertNotNull(cameraTypes);
            Assert.assertEquals(3, cameraTypes.size());
            Assert.assertEquals("Type1", cameraTypes.get(0).getType());
            Assert.assertEquals("Type2", cameraTypes.get(1).getType());
            Assert.assertEquals("Type3", cameraTypes.get(2).getType());
        } finally {
            cameras.forEach( (camera) -> {cameraService.deleteCamera(camera.getId());});
        }
    }

    @Test
    public void testDeleteCamera() {
        Camera camera = new Camera();
        camera.setType("Type1");
        camera.setName("Cam1");
        camera.setHost("192.168.1.2");
        camera.setPrimaryUrlPath("/stream1");
        camera.setBrand("ASL");
        camera.setModel("Model1");
        camera.setRtspAuth(false);
        camera.setTenantId(tenantId);
        Camera savedCamera = cameraService.saveCamera(camera);
        Camera foundCamera = cameraService.findCameraById(savedCamera.getId());
        Assert.assertNotNull(foundCamera);
        cameraService.deleteCamera(savedCamera.getId());
        foundCamera = cameraService.findCameraById(savedCamera.getId());
        Assert.assertNull(foundCamera);
    }

    @Test
    public void testFindCamerasByTenantId() {
        Tenant tenant = new Tenant();
        tenant.setTitle("Test tenant");
        tenant = tenantService.saveTenant(tenant);

        TenantId tenantId = tenant.getId();

        List<Camera> cameras = new ArrayList<>();
        for (int i = 0; i < 178; i++) {
            Camera camera = new Camera();
            camera.setType("Type2");
            camera.setName("Cam" + i);
            camera.setHost("192.168.1." + i);
            camera.setPrimaryUrlPath("/stream1");
            camera.setBrand("ASL");
            camera.setModel("Model1");
            camera.setRtspAuth(false);
            camera.setTenantId(tenantId);
            cameras.add(cameraService.saveCamera(camera));
        }

        List<Camera> loadedCameras = new ArrayList<>();
        TextPageLink pageLink = new TextPageLink(23);
        TextPageData<Camera> pageData = null;

        do {
            pageData = cameraService.findCamerasByTenantId(tenantId, pageLink);
            loadedCameras.addAll(pageData.getData());
            if (pageData.hasNext()){
                pageLink = pageData.getNextPageLink();
            }
        } while (pageData.hasNext());

        Collections.sort(cameras, idComparator);
        Collections.sort(loadedCameras, idComparator);

        Assert.assertEquals(cameras, loadedCameras);

        cameraService.deleteCamerasByTenantId(tenantId);

        pageLink = new TextPageLink(33);
        pageData = cameraService.findCamerasByTenantId(tenantId, pageLink);
        Assert.assertFalse(pageData.hasNext());
        Assert.assertTrue(pageData.getData().isEmpty());

        tenantService.deleteTenant(tenantId);
    }

    @Test
    public void testFindCameraByTenantIdAndName() {
        String title = "Camera title";
        List<Camera> cameras = new ArrayList<>();
        for (int i = 0; i < 152; i++) {
            Camera camera = new Camera();
            camera.setType("Type2");
            camera.setName(title + i);
            camera.setHost("192.168.1." + i);
            camera.setPrimaryUrlPath("/stream1");
            camera.setBrand("ASL");
            camera.setModel("Model1");
            camera.setRtspAuth(false);
            camera.setTenantId(tenantId);
            cameras.add(cameraService.saveCamera(camera));
        }

        List<Camera> loadedCameras = new ArrayList<>();
        TextPageLink pageLink = new TextPageLink(15, title);
        TextPageData<Camera> pageData = null;

        do {
            pageData = cameraService.findCamerasByTenantId(tenantId, pageLink);
            loadedCameras.addAll(pageData.getData());
            if (pageData.hasNext()) {
                pageLink = pageData.getNextPageLink();
            }
        } while (pageData.hasNext());

        Collections.sort(cameras, idComparator);
        Collections.sort(loadedCameras, idComparator);

        Assert.assertEquals(cameras, loadedCameras);
        for (Camera camera : loadedCameras) {
            cameraService.deleteCamera(camera.getId());
        }

        pageLink = new TextPageLink(4, title);
        pageData = cameraService.findCamerasByTenantId(tenantId, pageLink);
        Assert.assertFalse(pageData.hasNext());
        Assert.assertEquals(0, pageData.getData().size());
    }

    @Test
    public void testFindCamerasByTenantIdAndType() {
        String title = "Camera title";
        String type = "TypeA";
        List<Camera> cameras = new ArrayList<>();

        for (int i = 0; i < 143; i++) {
            Camera camera = new Camera();
            camera.setType(type);
            camera.setName(title + i);
            camera.setHost("192.168.1." + i);
            camera.setPrimaryUrlPath("/stream1");
            camera.setBrand("ASL");
            camera.setModel("Model1");
            camera.setRtspAuth(false);
            camera.setTenantId(tenantId);
            cameras.add(cameraService.saveCamera(camera));
        }

        List<Camera> loadedCameras = new ArrayList<>();
        TextPageLink pageLink = new TextPageLink(15);
        TextPageData<Camera> pageData = null;

        do {
            pageData = cameraService.findCamerasByTenantIdAndType(tenantId, type, pageLink);
            loadedCameras.addAll(pageData.getData());
            if (pageData.hasNext()) {
                pageLink = pageData.getNextPageLink();
            }
        } while (pageData.hasNext());

        Collections.sort(cameras, idComparator);
        Collections.sort(loadedCameras, idComparator);

        Assert.assertEquals(cameras, loadedCameras);
        for (Camera camera : loadedCameras) {
            cameraService.deleteCamera(camera.getId());
        }

        pageLink = new TextPageLink(4, title);
        pageData = cameraService.findCamerasByTenantId(tenantId, pageLink);
        Assert.assertFalse(pageData.hasNext());
        Assert.assertEquals(0, pageData.getData().size());

    }

    @Test
    public void testFindCamerasByTenantIdAndCustomerId() {
        Tenant tenant = new Tenant();
        tenant.setTitle("Test tenant");
        tenant = tenantService.saveTenant(tenant);

        TenantId tenantId = tenant.getId();

        Customer customer = new Customer();
        customer.setTitle("Test customer");
        customer.setTenantId(tenantId);
        customer = customerService.saveCustomer(customer);
        CustomerId customerId = customer.getId();

        List<Camera> cameras = new ArrayList<>();
        for (int i = 0; i < 187; i++) {
            Camera camera = new Camera();
            camera.setType("default");
            camera.setName("Camera" + i);
            camera.setHost("192.168.1." + i);
            camera.setPrimaryUrlPath("/stream1");
            camera.setBrand("ASL");
            camera.setModel("Model1");
            camera.setRtspAuth(false);
            camera.setTenantId(tenantId);
            camera = cameraService.saveCamera(camera);
            cameras.add(cameraService.assignCameraToCustomer(camera.getId(), customerId));
        }

        List<Camera> loadedCameras = new ArrayList<>();
        TextPageLink pageLink = new TextPageLink(23);
        TextPageData<Camera> pageData = null;

        do {
            pageData = cameraService.findCamerasByTenantIdAndCustomerId(tenantId, customerId, pageLink);
            loadedCameras.addAll(pageData.getData());
            if (pageData.hasNext()) {
                pageLink = pageData.getNextPageLink();
            }
        } while (pageData.hasNext());

        Collections.sort(cameras, idComparator);
        Collections.sort(loadedCameras, idComparator);

        Assert.assertEquals(cameras, loadedCameras);

        cameraService.unassignCustomerCameras(tenantId, customerId);

        pageLink = new TextPageLink(30);
        pageData = cameraService.findCamerasByTenantIdAndCustomerId(tenantId, customerId, pageLink);
        Assert.assertFalse(pageData.hasNext());
        Assert.assertTrue(pageData.getData().isEmpty());

        tenantService.deleteTenant(tenantId);
    }

    @Test
    public void testFindCamerasByTenantIdAndCustomerIdAndType() {
        Tenant tenant = new Tenant();
        tenant.setTitle("Test tenant");
        tenant = tenantService.saveTenant(tenant);

        TenantId tenantId = tenant.getId();

        Customer customer =  new Customer();
        customer.setTitle("Test customer");
        customer.setTenantId(tenantId);
        customer = customerService.saveCustomer(customer);
        CustomerId customerId = customer.getId();

        String title = "Camera title";
        String type = "TypeX";
        List<Camera> cameras = new ArrayList<>();
        for (int i = 0; i < 143; i++) {
            Camera camera = new Camera();
            camera.setType(type);
            camera.setName(title + i);
            camera.setHost("192.168.1." + i);
            camera.setPrimaryUrlPath("/stream1");
            camera.setBrand("ASL");
            camera.setModel("Model1");
            camera.setRtspAuth(false);
            camera.setTenantId(tenantId);
            camera = cameraService.saveCamera(camera);
            cameras.add(cameraService.assignCameraToCustomer(camera.getId(), customerId));
        }

        List<Camera> loadedCameras = new ArrayList<>();
        TextPageLink pageLink = new TextPageLink(15);
        TextPageData<Camera> pageData = null;

        do {
            pageData = cameraService.findCamerasByTenantIdAndCustomerIdAndType(tenantId, customerId, type, pageLink);
            loadedCameras.addAll(pageData.getData());
            if (pageData.hasNext()) {
                pageLink = pageData.getNextPageLink();
            }
        } while (pageData.hasNext());

        Collections.sort(cameras, idComparator);
        Collections.sort(loadedCameras, idComparator);

        Assert.assertEquals(cameras, loadedCameras);

        cameraService.deleteCamerasByTenantId(tenantId);

        pageLink = new TextPageLink(5);
        pageData = cameraService.findCamerasByTenantIdAndCustomerIdAndType(tenantId, customerId, type, pageLink);
        Assert.assertFalse(pageData.hasNext());
        Assert.assertEquals(0, pageData.getData().size());

        tenantService.deleteTenant(tenantId);
    }

    @Test
    public void testFindCameraByIdAsync() throws ExecutionException, InterruptedException{
        Camera c = new Camera();
        c.setTenantId(tenantId);
        c.setPrimaryUrlPath("/stream1");
        c.setSecondaryUrlPath("/stream2");
        c.setPtz(true);
        c.setType("type1");
        c.setName("Cam1");
        c.setHost("192.168.1.2");
        c.setBrand("ASL");
        c.setModel("Model1");
        Camera camera = cameraService.saveCamera(c);

        CameraId id = camera.getId();
        ListenableFuture<Camera> loadedCamera = cameraService.findCameraByIdAsync(id);

        Assert.assertEquals(camera, loadedCamera.get());
    }

    @Test
    public void testFindCamerasByTenantIdAndIdsAsync() throws ExecutionException, InterruptedException{
        Tenant tenant = new Tenant();
        tenant.setTitle("Test tenant");
        tenant = tenantService.saveTenant(tenant);

        TenantId tenantId = tenant.getId();

        List<Camera> cameras = new ArrayList<>();
        List<CameraId> cameraIds = new ArrayList<>();
        for (int i = 0; i < 178; i++) {
            Camera camera = new Camera();
            camera.setType("Type2");
            camera.setName("Cam" + i);
            camera.setHost("192.168.1." + i);
            camera.setPrimaryUrlPath("/stream1");
            camera.setBrand("ASL");
            camera.setModel("Model1");
            camera.setRtspAuth(false);
            camera.setTenantId(tenantId);
            camera = cameraService.saveCamera(camera);
            cameras.add(camera);
            cameraIds.add(camera.getId());
        }

        List<Camera> loadedCameras = new ArrayList<>();
        TextPageLink pageLink = new TextPageLink(23);
        TextPageData<Camera> pageData = null;
        List<Camera> cameraList = null;

        do {
            cameraList = cameraService.findCamerasByTenantIdAndIdsAsync(tenantId, cameraIds).get();
            pageData = new TextPageData<>(cameraList, pageLink);
            loadedCameras.addAll(pageData.getData());
            if (pageData.hasNext()){
                pageLink = pageData.getNextPageLink();
            }
        } while (pageData.hasNext());

        Collections.sort(cameras, idComparator);
        Collections.sort(loadedCameras, idComparator);

        Assert.assertEquals(cameras, loadedCameras);

        cameraService.deleteCamerasByTenantId(tenantId);

        pageLink = new TextPageLink(33);
        pageData = cameraService.findCamerasByTenantId(tenantId, pageLink);
        Assert.assertFalse(pageData.hasNext());
        Assert.assertTrue(pageData.getData().isEmpty());

        tenantService.deleteTenant(tenantId);
    }

    @Test
    public void testFindCamerasByTenantIdCustomerIdAndIdsAsync()  throws ExecutionException, InterruptedException{
        Tenant tenant = new Tenant();
        tenant.setTitle("Test tenant");
        tenant = tenantService.saveTenant(tenant);

        TenantId tenantId = tenant.getId();

        Customer customer = new Customer();
        customer.setTitle("Test customer");
        customer.setTenantId(tenantId);
        customer = customerService.saveCustomer(customer);
        CustomerId customerId = customer.getId();

        List<Camera> cameras = new ArrayList<>();
        List<CameraId> cameraIds = new ArrayList<>();
        for (int i = 0; i < 178; i++) {
            Camera camera = new Camera();
            camera.setType("Type2");
            camera.setName("Cam" + i);
            camera.setHost("192.168.1." + i);
            camera.setPrimaryUrlPath("/stream1");
            camera.setBrand("ASL");
            camera.setModel("Model1");
            camera.setRtspAuth(false);
            camera.setTenantId(tenantId);
            camera = cameraService.saveCamera(camera);
            cameras.add(cameraService.assignCameraToCustomer(camera.getId(), customerId));
            cameraIds.add(camera.getId());
        }

        List<Camera> loadedCameras = new ArrayList<>();
        TextPageLink pageLink = new TextPageLink(23);
        TextPageData<Camera> pageData = null;
        List<Camera> cameraList = null;

        do {
            cameraList = cameraService.findCamerasByTenantIdCustomerIdAndIdsAsync(tenantId, customerId, cameraIds).get();
            pageData = new TextPageData<>(cameraList, pageLink);
            loadedCameras.addAll(pageData.getData());
            if (pageData.hasNext()){
                pageLink = pageData.getNextPageLink();
            }
        } while (pageData.hasNext());

        Collections.sort(cameras, idComparator);
        Collections.sort(loadedCameras, idComparator);

        Assert.assertEquals(cameras, loadedCameras);

        cameraService.deleteCamerasByTenantId(tenantId);

        pageLink = new TextPageLink(33);
        pageData = cameraService.findCamerasByTenantId(tenantId, pageLink);
        Assert.assertFalse(pageData.hasNext());
        Assert.assertTrue(pageData.getData().isEmpty());

        tenantService.deleteTenant(tenantId);
    }


}
