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
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.thingsboard.server.common.data.*;
import org.thingsboard.server.common.data.id.CustomerId;
import org.thingsboard.server.common.data.id.NvrId;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.page.TextPageData;
import org.thingsboard.server.common.data.page.TextPageLink;
import org.thingsboard.server.common.data.relation.EntityRelation;
import org.thingsboard.server.common.data.relation.RelationTypeGroup;
import org.thingsboard.server.dao.exception.DataValidationException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

public abstract class BaseNvrServiceTest extends AbstractServiceTest{
    private IdComparator<Nvr> idComparator = new IdComparator<>();
    private TenantId tenantId;

    @Before
    public void before() {
        Tenant tenant = new Tenant();
        tenant.setTitle("My Tenant");
        Tenant savedTenant = tenantService.saveTenant(tenant);
        Assert.assertNotNull(savedTenant);
        tenantId = savedTenant.getId();
    }

    @After
    public void after(){tenantService.deleteTenant(tenantId);}

    private List<Nvr> generateNvrs(int count) throws Exception {
        List<Nvr> nvrs = new ArrayList<>();
        for (int i = 0; i < count; i++){
            Nvr n = new Nvr();
            n.setTenantId(tenantId);
            n.setBrand("ASL");
            n.setIp("192.168.1." + (i+1));
            n.setModel("Sabit");
            n.setName("Nvr" + i);
            n.setType("type" + i%2);
            Nvr nvr = nvrService.saveNvr(n);
            Assert.assertEquals(n.getName(), nvr.getName());
            Assert.assertEquals(n.getBrand(), nvr.getBrand());
            Assert.assertEquals(n.getIp(), nvr.getIp());
            Assert.assertEquals(n.getModel(), nvr.getModel());
            Assert.assertEquals(n.getType(), nvr.getType());
            Assert.assertEquals(n.getTenantId(), nvr.getTenantId());
            nvrs.add(nvr);
        }
        return nvrs;
    }

    private void destructNvrs(List<Nvr> nvrs) throws Exception {
        for (Nvr nvr : nvrs){
            nvrService.deleteNvr(nvr.getId());
        }
    }

    @Test
    public void testSaveNvr(){
        Nvr nvr = new Nvr();
        nvr.setTenantId(tenantId);
        nvr.setName("My Nvr");
        nvr.setType("default");
        nvr.setBrand("ASL");
        nvr.setIp("192.168.1.2");
        nvr.setModel("Sabit");
        Nvr savedNvr = nvrService.saveNvr(nvr);

        Assert.assertNotNull(savedNvr);
        Assert.assertNotNull(savedNvr.getId());
        Assert.assertTrue(savedNvr.getCreatedTime() > 0);
        Assert.assertEquals(nvr.getTenantId(), savedNvr.getTenantId());
        Assert.assertTrue(savedNvr.getAssignedCustomers().isEmpty());
        Assert.assertEquals(nvr.getName(), savedNvr.getName());

        savedNvr.setName("My new nvr");

        nvrService.saveNvr(savedNvr);
        Nvr foundNvr = nvrService.findNvrById(savedNvr.getId());
        Assert.assertEquals(foundNvr.getName(), savedNvr.getName());

        nvrService.deleteNvr(savedNvr.getId());
    }

    @Test(expected = DataValidationException.class)
    public void testSaveNvrWithEmptyName(){
        Nvr nvr = new Nvr();
        nvr.setTenantId(tenantId);
        nvr.setType("default");
        nvrService.saveNvr(nvr);
    }


    @Test(expected = DataValidationException.class)
    public void testSaveNvrWithEmptyType(){
        Nvr nvr = new Nvr();
        nvrService.saveNvr(nvr);
    }

    @Test(expected = DataValidationException.class)
    public void testSaveNvrWithEmptyIp(){
        Nvr nvr = new Nvr();
        nvr.setType("default");
        nvr.setName("My nvr");
        nvrService.saveNvr(nvr);
    }

    @Test(expected = DataValidationException.class)
    public void testSaveNvrWithEmptyBrand(){
        Nvr nvr = new Nvr();
        nvr.setType("default");
        nvr.setName("My nvr");
        nvr.setIp("192.168.1.2");
        nvrService.saveNvr(nvr);
    }

    @Test(expected = DataValidationException.class)
    public void testSaveNvrWithEmptyModel(){
        Nvr nvr = new Nvr();
        nvr.setType("default");
        nvr.setName("My nvr");
        nvr.setIp("192.168.1.2");
        nvr.setBrand("ASL");
        nvrService.saveNvr(nvr);
    }

    @Test(expected = DataValidationException.class)
    public void testSaveNvrWithEmptyTenant(){
        Nvr nvr = new Nvr();
        nvr.setType("default");
        nvr.setName("My nvr");
        nvr.setIp("192.168.1.2");
        nvr.setBrand("ASL");
        nvr.setModel("model1");
        nvrService.saveNvr(nvr);
    }

    @Test(expected = DataValidationException.class)
    public void testSaveNvrToNonExistingCustomer(){
        Nvr nvr = new Nvr();
        nvr.setType("default");
        nvr.setName("My nvr");
        nvr.setIp("192.168.1.2");
        nvr.setBrand("ASL");
        nvr.setModel("model1");
        nvr.setTenantId(tenantId);
        nvr = nvrService.saveNvr(nvr);

        try{
            nvrService.assignNvrToCustomer(nvr.getId(), new CustomerId(UUIDs.timeBased()));
        } finally {
            nvrService.deleteNvr(nvr.getId());
        }
    }

    @Test(expected = DataValidationException.class)
    public void testSaveNvrToCustomerFromDifferentTenant(){
        Nvr nvr = new Nvr();
        nvr.setType("default");
        nvr.setName("My nvr");
        nvr.setIp("192.168.1.2");
        nvr.setBrand("ASL");
        nvr.setModel("model1");
        nvr.setTenantId(tenantId);
        nvr = nvrService.saveNvr(nvr);

        Tenant tenant = new Tenant();
        tenant.setTitle("Test different tenant");
        Customer customer  = new Customer();
        customer.setTenantId(tenant.getId());
        customer.setTitle("Test different customer");
        customer = customerService.saveCustomer(customer);

        try{
            nvrService.assignNvrToCustomer(nvr.getId(), customer.getId());
        } finally {
            nvrService.deleteNvr(nvr.getId());
            tenantService.deleteTenant(tenant.getId());
        }
    }

    @Test(expected = DataValidationException.class)
    public void testSaveNvrWithInvalidTenant(){
        Nvr nvr = new Nvr();
        nvr.setType("default");
        nvr.setName("My nvr");
        nvr.setIp("192.168.1.2");
        nvr.setBrand("ASL");
        nvr.setModel("model1");
        nvr.setTenantId(new TenantId(UUIDs.timeBased()));
        nvrService.saveNvr(nvr);
    }

    @Test
    public void testFindNvrById() {
        Nvr nvr = new Nvr();
        nvr.setTenantId(tenantId);
        nvr.setType("default");
        nvr.setName("My nvr");
        nvr.setIp("192.168.1.2");
        nvr.setBrand("ASL");
        nvr.setModel("model1");
        Nvr savedNvr = nvrService.saveNvr(nvr);
        Nvr foundNvr = nvrService.findNvrById(savedNvr.getId());
        Assert.assertNotNull(foundNvr);
        Assert.assertEquals(savedNvr, foundNvr);
        nvrService.deleteNvr(savedNvr.getId());
    }

    @Test
    public void testFindNvrTypesByTenantId() throws Exception{
        List<Nvr> nvrs = new ArrayList<>();
        try{
            for (int i = 0; i < 3; i++){
                Nvr nvr = new Nvr();
                nvr.setTenantId(tenantId);
                nvr.setType("typeA");
                nvr.setName("My nvr"+i);
                nvr.setIp("192.168.1."+i);
                nvr.setBrand("ASL");
                nvr.setModel("model1");
                nvrs.add(nvrService.saveNvr(nvr));
            }
            for (int i = 0; i < 3; i++){
                Nvr nvr = new Nvr();
                nvr.setTenantId(tenantId);
                nvr.setType("typeB");
                nvr.setName("My nvr"+i);
                nvr.setIp("192.168.1."+i);
                nvr.setBrand("ASL");
                nvr.setModel("model1");
                nvrs.add(nvrService.saveNvr(nvr));
            }
            for (int i = 0; i < 3; i++){
                Nvr nvr = new Nvr();
                nvr.setTenantId(tenantId);
                nvr.setType("typeC");
                nvr.setName("My nvr"+i);
                nvr.setIp("192.168.1."+i);
                nvr.setBrand("ASL");
                nvr.setModel("model1");
                nvrs.add(nvrService.saveNvr(nvr));
            }

            List<EntitySubtype> nvrTypes = nvrService.findNvrTypesByTenantId(tenantId).get();
            Assert.assertNotNull(nvrTypes);
            Assert.assertEquals(3, nvrTypes.size());
            Assert.assertEquals("typeA", nvrTypes.get(0).getType());
            Assert.assertEquals("typeB", nvrTypes.get(1).getType());
            Assert.assertEquals("typeC", nvrTypes.get(2).getType());
        }finally {
            nvrs.forEach((nvr) -> {nvrService.deleteNvr(nvr.getId());});
        }
    }

    @Test
    public void testDeleteNvr(){
        Nvr nvr = new Nvr();
        nvr.setTenantId(tenantId);
        nvr.setType("default");
        nvr.setName("My nvr");
        nvr.setIp("192.168.1.2");
        nvr.setBrand("ASL");
        nvr.setModel("model1");
        Nvr savedNvr = nvrService.saveNvr(nvr);
        Nvr foundNvr = nvrService.findNvrById(savedNvr.getId());
        Assert.assertNotNull(foundNvr);
        nvrService.deleteNvr(savedNvr.getId());
        foundNvr = nvrService.findNvrById(savedNvr.getId());
        Assert.assertNull(foundNvr);
    }

    @Test
    public void testFindNvrByTenantId(){
        Tenant tenant = new Tenant();
        tenant.setTitle("Test tenant");
        tenant = tenantService.saveTenant(tenant);

        TenantId tenantId = tenant.getId();

        List<Nvr> nvrs = new ArrayList<>();
        for (int i = 0; i < 178; i++) {
            Nvr nvr = new Nvr();
            nvr.setTenantId(tenantId);
            nvr.setType("default");
            nvr.setName("My nvr"+i);
            nvr.setIp("192.168.1."+i);
            nvr.setBrand("ASL");
            nvr.setModel("model1");
            nvrs.add(nvrService.saveNvr(nvr));
        }

        List<Nvr> loadedNvrs = new ArrayList<>();
        TextPageLink pageLink = new TextPageLink(23);
        TextPageData<Nvr> pageData = null;

        do {
            pageData = nvrService.findNvrsByTenantId(tenantId, pageLink);
            loadedNvrs.addAll(pageData.getData());
            if (pageData.hasNext()){
                pageLink = pageData.getNextPageLink();
            }
        } while (pageData.hasNext());

        Collections.sort(nvrs, idComparator);
        Collections.sort(loadedNvrs, idComparator);

        Assert.assertEquals(nvrs, loadedNvrs);

        nvrService.deleteNvrsByTenantId(tenantId);

        pageLink = new TextPageLink(33);
        pageData = nvrService.findNvrsByTenantId(tenantId, pageLink);
        Assert.assertFalse(pageData.hasNext());
        Assert.assertTrue(pageData.getData().isEmpty());

        tenantService.deleteTenant(tenantId);
    }


    @Test
    public void testFindNvrByCameraId() {
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

        Nvr nvr = new Nvr();
        String name = "My nvr";
        nvr.setTenantId(tenantId);
        nvr.setType("default");
        nvr.setName(name);
        nvr.setIp("192.168.1.2");
        nvr.setBrand("ASL");
        nvr.setModel("model1");
        Nvr savedNvr = nvrService.saveNvr(nvr);

        EntityRelation relation = new EntityRelation(savedNvr.getId(), savedCamera.getId(), "Manages", RelationTypeGroup.COMMON);
        relationService.saveRelation(relation);

        Nvr foundNvr = nvrService.findNvrByCameraId(savedCamera.getId());
        Assert.assertEquals(foundNvr, savedNvr);

        relationService.deleteRelation(relation);
        nvrService.deleteNvr(savedNvr.getId());
        cameraService.deleteCamera(savedCamera.getId());
    }

    @Test
    public void testFindNvrsByTenantIdAndName(){
        String title = "Nvr title";
        List<Nvr> nvrs = new ArrayList<>();
        for (int i = 0; i < 152; i++){
            Nvr nvr = new Nvr();
            nvr.setTenantId(tenantId);
            nvr.setType("default");
            String suffix = RandomStringUtils.randomAlphanumeric(15);
            String name = title + suffix;
            name = i % 2 == 0 ? name.toLowerCase() : name.toUpperCase();
            nvr.setName(name);
            nvr.setIp("192.168.1."+i);
            nvr.setBrand("ASL");
            nvr.setModel("model1");
            nvrs.add(nvrService.saveNvr(nvr));
        }

        List<Nvr> loadedNvrs = new ArrayList<>();
        TextPageLink pageLink = new TextPageLink(15, title);
        TextPageData<Nvr> pageData = null;

        do {
            pageData = nvrService.findNvrsByTenantId(tenantId, pageLink);
            loadedNvrs.addAll(pageData.getData());
            if (pageData.hasNext()){
                pageLink = pageData.getNextPageLink();
            }
        } while (pageData.hasNext());

        Collections.sort(nvrs, idComparator);
        Collections.sort(loadedNvrs, idComparator);

        Assert.assertEquals(nvrs, loadedNvrs);
        for (Nvr nvr : loadedNvrs){
            nvrService.deleteNvr(nvr.getId());
        }

        pageLink = new TextPageLink(4, title);
        pageData = nvrService.findNvrsByTenantId(tenantId, pageLink);
        Assert.assertFalse(pageData.hasNext());
        Assert.assertEquals(0, pageData.getData().size());
    }

    @Test
    public void testFindNvrByTenantIdAndName(){
        Nvr nvr = new Nvr();
        String name = "My nvr";
        nvr.setTenantId(tenantId);
        nvr.setType("default");
        nvr.setName(name);
        nvr.setIp("192.168.1.2");
        nvr.setBrand("ASL");
        nvr.setModel("model1");
        Nvr savedNvr = nvrService.saveNvr(nvr);
        Optional<Nvr> foundNvr = nvrService.findNvrByTenantIdAndName(tenantId, name);
        Assert.assertEquals(foundNvr.get(), savedNvr);
    }

    @Test
    public void testFindNvrsByTenantIdAndType(){
        String title = "Nvr title";
        String type = "typeA";

        List<Nvr> nvrs = new ArrayList<>();
        for (int i = 0; i < 143; i++){
            Nvr nvr = new Nvr();
            nvr.setTenantId(tenantId);
            nvr.setType(type);
            String suffix = RandomStringUtils.randomAlphanumeric(15);
            String name = title + suffix;
            name = i % 2 == 0 ? name.toLowerCase() : name.toUpperCase();
            nvr.setName(name);
            nvr.setIp("192.168.1."+i);
            nvr.setBrand("ASL");
            nvr.setModel("model1");
            nvrs.add(nvrService.saveNvr(nvr));
        }

        List<Nvr> loadedNvrs = new ArrayList<>();
        TextPageLink pageLink = new TextPageLink(15);
        TextPageData<Nvr> pageData = null;

        do {
            pageData = nvrService.findNvrsByTenantIdAndType(tenantId, type, pageLink);
            loadedNvrs.addAll(pageData.getData());
            if (pageData.hasNext()){
                pageLink = pageData.getNextPageLink();
            }
        } while (pageData.hasNext());

        Collections.sort(nvrs, idComparator);
        Collections.sort(loadedNvrs, idComparator);

        Assert.assertEquals(nvrs, loadedNvrs);

        for (Nvr nvr : loadedNvrs){
            nvrService.deleteNvr(nvr.getId());
        }

        pageLink = new TextPageLink(4, title);
        pageData = nvrService.findNvrsByTenantId(tenantId, pageLink);
        Assert.assertFalse(pageData.hasNext());
        Assert.assertEquals(0, pageData.getData().size());
    }

    @Test
    public void testFindNvrsByTenantIdAndCustomerId() throws ExecutionException, InterruptedException {
        Tenant tenant = new Tenant();
        tenant.setTitle("Test tenant");
        tenant = tenantService.saveTenant(tenant);

        TenantId tenantId = tenant.getId();

        Customer customer = new Customer();
        customer.setTitle("Test customer");
        customer.setTenantId(tenantId);
        customer = customerService.saveCustomer(customer);
        CustomerId customerId = customer.getId();

        List<Nvr> nvrs = new ArrayList<>();
        for (int i = 0; i < 278; i++){
            Nvr nvr = new Nvr();
            nvr.setTenantId(tenantId);
            nvr.setType("default");
            nvr.setName("My nvr"+i);
            nvr.setIp("192.168.1."+i);
            nvr.setBrand("ASL");
            nvr.setModel("model1");
            nvr = nvrService.saveNvr(nvr);
            nvrs.add(nvrService.assignNvrToCustomer(nvr.getId(), customerId));
        }

        List<Nvr> loadedNvrs = new ArrayList<>();
        TextPageLink pageLink = new TextPageLink(23);
        TextPageData<Nvr> pageData = null;
        do{
            pageData = nvrService.findNvrsByTenantIdAndCustomerId(tenantId, customerId, pageLink);
            loadedNvrs.addAll(pageData.getData());
            if (pageData.hasNext()){
                pageLink = pageData.getNextPageLink();
            }
        }while(pageData.hasNext());


        Collections.sort(nvrs, idComparator);
        Collections.sort(loadedNvrs, idComparator);

        Assert.assertEquals(nvrs, loadedNvrs);

        nvrService.unassignCustomerNvrs(tenantId, customerId);

        pageLink = new TextPageLink(33);
        pageData = nvrService.findNvrsByTenantIdAndCustomerId(tenantId, customerId, pageLink);
        Assert.assertFalse(pageData.hasNext());
        Assert.assertTrue(pageData.getData().isEmpty());

        tenantService.deleteTenant(tenantId);
    }

    @Test
    public void testFindNvrByTenantIdAndCustomerIdAndName() throws ExecutionException, InterruptedException {
        Customer customer =  new Customer();
        customer.setTitle("Test customer");
        customer.setTenantId(tenantId);
        customer = customerService.saveCustomer(customer);
        CustomerId customerId = customer.getId();

        String title = "Nvr title";
        List<Nvr> nvrs = new ArrayList<>();
        for (int i = 0; i < 175; i++){
            Nvr nvr = new Nvr();
            nvr.setTenantId(tenantId);
            nvr.setType("default");
            String suffix = RandomStringUtils.randomAlphanumeric(15);
            String name = title + suffix;
            name = i % 2 == 0 ? name.toLowerCase() : name.toUpperCase();
            nvr.setName(name);
            nvr.setIp("192.168.1."+i);
            nvr.setBrand("ASL");
            nvr.setModel("model1");
            nvr = nvrService.saveNvr(nvr);
            nvrs.add(nvrService.assignNvrToCustomer(nvr.getId(), customerId));
        }

        List<Nvr> loadedNvrs = new ArrayList<>();
        TextPageLink pageLink = new TextPageLink(23, title);
        TextPageData<Nvr> pageData = null;

        do{
            pageData = nvrService.findNvrsByTenantIdAndCustomerId(tenantId, customerId, pageLink);
            loadedNvrs.addAll(pageData.getData());
            if (pageData.hasNext()){
                pageLink = pageData.getNextPageLink();
            }
        }while(pageData.hasNext());


        Collections.sort(nvrs, idComparator);
        Collections.sort(loadedNvrs, idComparator);

        Assert.assertEquals(nvrs, loadedNvrs);

        for (Nvr nvr : loadedNvrs) {
            nvrService.deleteNvr(nvr.getId());
        }

        pageLink = new TextPageLink(4, title);
        pageData = nvrService.findNvrsByTenantIdAndCustomerId(tenantId, customerId, pageLink);
        Assert.assertFalse(pageData.hasNext());
        Assert.assertEquals(0, pageData.getData().size());

    }
    @Test
    public void testFindNvrByTenantIdAndCustomerIdAndType() throws ExecutionException, InterruptedException {
        Customer customer =  new Customer();
        customer.setTitle("Test customer");
        customer.setTenantId(tenantId);
        customer = customerService.saveCustomer(customer);
        CustomerId customerId = customer.getId();

        String title = "Nvr title";
        String type = "typeX";
        List<Nvr> nvrs = new ArrayList<>();
        for (int i = 0; i < 222; i++){
            Nvr nvr = new Nvr();
            nvr.setTenantId(tenantId);
            nvr.setType(type);
            String suffix = RandomStringUtils.randomAlphanumeric(15);
            String name = title + suffix;
            name = i % 2 == 0 ? name.toLowerCase() : name.toUpperCase();
            nvr.setName(name);
            nvr.setIp("192.168.1."+i);
            nvr.setBrand("ASL");
            nvr.setModel("model1");
            nvr = nvrService.saveNvr(nvr);
            nvrs.add(nvrService.assignNvrToCustomer(nvr.getId(), customerId));
        }

        List<Nvr> loadedNvrs = new ArrayList<>();
        TextPageLink pageLink = new TextPageLink(17);
        TextPageData<Nvr> pageData = null;

        do{
            pageData = nvrService.findNvrsByTenantIdAndCustomerIdAndType(tenantId,customerId, type, pageLink);
            loadedNvrs.addAll(pageData.getData());
            if (pageData.hasNext()){
                pageLink = pageData.getNextPageLink();
            }
        }while(pageData.hasNext());


        Collections.sort(nvrs, idComparator);
        Collections.sort(loadedNvrs, idComparator);

        Assert.assertEquals(nvrs, loadedNvrs);

        for (Nvr nvr : loadedNvrs){
            nvrService.deleteNvr(nvr.getId());
        }

        pageLink = new TextPageLink(4);
        pageData = nvrService.findNvrsByTenantIdAndCustomerIdAndType(tenantId, customerId, type, pageLink);
        Assert.assertFalse(pageData.hasNext());
        Assert.assertEquals(0, pageData.getData().size());
    }


    @Test
    public void testFindNvrByIdAsync() throws ExecutionException, InterruptedException {
        Nvr nvr = new Nvr();
        nvr.setTenantId(tenantId);
        nvr.setType("default");
        nvr.setName("My nvr");
        nvr.setIp("192.168.1.1");
        nvr.setBrand("ASL");
        nvr.setModel("model1");
        nvr = nvrService.saveNvr(nvr);

        NvrId id = nvr.getId();
        ListenableFuture<Nvr> loadedNvr = nvrService.findNvrByIdAsync(id);

        Assert.assertEquals(nvr, loadedNvr.get());
    }
}
































