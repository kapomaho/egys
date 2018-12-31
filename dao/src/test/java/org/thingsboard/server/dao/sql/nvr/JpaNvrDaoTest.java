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

package org.thingsboard.server.dao.sql.nvr;

import com.datastax.driver.core.utils.UUIDs;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.thingsboard.server.common.data.EntitySubtype;
import org.thingsboard.server.common.data.Nvr;
import org.thingsboard.server.common.data.id.NvrId;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.page.TextPageLink;
import org.thingsboard.server.dao.AbstractJpaDaoTest;
import org.thingsboard.server.dao.nvr.NvrDao;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import com.google.common.util.concurrent.ListenableFuture;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.*;

public class JpaNvrDaoTest extends AbstractJpaDaoTest {

    @Autowired
    private NvrDao nvrDao;

    @Test
    public void testFindNvrsByTenantId() {
        UUID tenantId1 = UUIDs.timeBased();
        UUID tenantId2 = UUIDs.timeBased();

        for (int i = 0; i < 60; i++) {
            UUID nvrId = UUIDs.timeBased();
            UUID tenantId = i % 2 == 0 ? tenantId1 : tenantId2;
            saveNvr(nvrId, tenantId, "NVR_" + i, "TYPE_1");
        }

        assertEquals(60, nvrDao.find().size());

        TextPageLink pageLink1 = new TextPageLink(20, "NVR_");
        List<Nvr> nvrs1 = nvrDao.findNvrsByTenantId(tenantId1, pageLink1);
        assertEquals(20, nvrs1.size());

        TextPageLink pageLink2 = new TextPageLink(20, "NVR_", nvrs1.get(19).getId().getId(), null);
        List<Nvr> nvrs2 = nvrDao.findNvrsByTenantId(tenantId1, pageLink2);
        assertEquals(10, nvrs2.size());


        TextPageLink pageLink3 = new TextPageLink(20, "NVR_", nvrs2.get(9).getId().getId(), null);
        List<Nvr> nvrs3 = nvrDao.findNvrsByTenantId(tenantId1, pageLink3);
        assertEquals(0, nvrs3.size());
    }

    @Test
    public void testFindNvrsByTenantIdAndIdsAsync() throws ExecutionException, InterruptedException {
        UUID tenantId = UUIDs.timeBased();
        List<UUID> searchIds = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            UUID nvrId = UUIDs.timeBased();
            saveNvr(nvrId, tenantId, "NVR_" + i, "TYPE_1");
            if (i % 3 == 0) {
                searchIds.add(nvrId);
            }
        }

        ListenableFuture<List<Nvr>> nvrsFuture = nvrDao.findNvrsByTenantIdAndIdsAsync(tenantId, searchIds);
        List<Nvr> nvrs = nvrsFuture.get();
        assertNotNull(nvrs);
        assertEquals(10, nvrs.size());
    }

    @Test
    public void testFindNvrsByTenantIdCustomerIdAndIdsAsync() throws ExecutionException, InterruptedException {
        UUID tenantId = UUIDs.timeBased();
        UUID customerId1 = UUIDs.timeBased();
        List<UUID> searchIds = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            UUID nvrId = UUIDs.timeBased();
            saveNvr(nvrId, tenantId, "NVR_" + i, "TYPE_1");
            if (i % 3 == 0) {
                searchIds.add(nvrId);
            }
        }

        ListenableFuture<List<Nvr>> nvrsFuture = nvrDao.findNvrsByTenantIdCustomerIdAndIdsAsync(tenantId, customerId1, searchIds);
        List<Nvr> nvrs = nvrsFuture.get();
        assertNotNull(nvrs);
        assertEquals(5, nvrs.size());
    }

    @Test
    public void testFindNvrsByTenantIdAndName() {
        UUID nvrId1 = UUIDs.timeBased();
        UUID nvrId2 = UUIDs.timeBased();
        UUID tenantId1 = UUIDs.timeBased();
        UUID tenantId2 = UUIDs.timeBased();
        String name = "TEST_NVR";
        saveNvr(nvrId1, tenantId1, name, "TYPE_1");
        saveNvr(nvrId2, tenantId2, name, "TYPE_1");

        Optional<Nvr> nvrOpt1 = nvrDao.findNvrByTenantIdAndName(tenantId2, name);
        assertTrue("Optional expected to be non-empty", nvrOpt1.isPresent());
        assertEquals(nvrId2, nvrOpt1.get().getId().getId());

        Optional<Nvr> nvrOpt2 = nvrDao.findNvrByTenantIdAndName(tenantId2, "NON_EXISTENT_NAME");
        assertFalse("Optional expected to be empty", nvrOpt2.isPresent());
    }

    @Test
    public void testFindTenantNvrTypesAsync() throws ExecutionException, InterruptedException {
        UUID tenantId1 = UUIDs.timeBased();
        UUID tenantId2 = UUIDs.timeBased();
        saveNvr(UUIDs.timeBased(), tenantId1, "TEST_NVR_1", "TYPE_1");
        saveNvr(UUIDs.timeBased(), tenantId1, "TEST_NVR_2", "TYPE_1");
        saveNvr(UUIDs.timeBased(), tenantId1, "TEST_NVR_3", "TYPE_2");
        saveNvr(UUIDs.timeBased(), tenantId1, "TEST_NVR_4", "TYPE_3");
        saveNvr(UUIDs.timeBased(), tenantId1, "TEST_NVR_5", "TYPE_3");
        saveNvr(UUIDs.timeBased(), tenantId1, "TEST_NVR_6", "TYPE_3");

        saveNvr(UUIDs.timeBased(), tenantId2, "TEST_NVR_7", "TYPE_4");
        saveNvr(UUIDs.timeBased(), tenantId2, "TEST_NVR_8", "TYPE_1");
        saveNvr(UUIDs.timeBased(), tenantId2, "TEST_NVR_9", "TYPE_1");

        List<EntitySubtype> tenant1Types = nvrDao.findTenantNvrTypesAsync(tenantId1).get();
        assertNotNull(tenant1Types);
        List<EntitySubtype> tenant2Types = nvrDao.findTenantNvrTypesAsync(tenantId2).get();
        assertNotNull(tenant2Types);

        assertEquals(3, tenant1Types.size());
        assertTrue(tenant1Types.stream().anyMatch(t -> t.getType().equals("TYPE_1")));
        assertTrue(tenant1Types.stream().anyMatch(t -> t.getType().equals("TYPE_2")));
        assertTrue(tenant1Types.stream().anyMatch(t -> t.getType().equals("TYPE_3")));
        assertFalse(tenant1Types.stream().anyMatch(t -> t.getType().equals("TYPE_4")));

        assertEquals(2, tenant2Types.size());
        assertTrue(tenant2Types.stream().anyMatch(t -> t.getType().equals("TYPE_1")));
        assertTrue(tenant2Types.stream().anyMatch(t -> t.getType().equals("TYPE_4")));
        assertFalse(tenant2Types.stream().anyMatch(t -> t.getType().equals("TYPE_2")));
        assertFalse(tenant2Types.stream().anyMatch(t -> t.getType().equals("TYPE_3")));
    }

    private void saveNvr(UUID id, UUID tenantId, String name, String type) {
        Nvr nvr = new Nvr();
        nvr.setId(new NvrId(id));
        nvr.setTenantId(new TenantId(tenantId));
        nvr.setName(name);
        nvr.setType(type);
        nvrDao.save(nvr);
    }
}
