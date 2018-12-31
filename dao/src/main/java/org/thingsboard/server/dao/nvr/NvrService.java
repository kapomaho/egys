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

package org.thingsboard.server.dao.nvr;

import com.google.common.util.concurrent.ListenableFuture;
import org.thingsboard.server.common.data.EntitySubtype;
import org.thingsboard.server.common.data.Nvr;
import org.thingsboard.server.common.data.id.CameraId;
import org.thingsboard.server.common.data.id.CustomerId;
import org.thingsboard.server.common.data.id.NvrId;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.page.TextPageData;
import org.thingsboard.server.common.data.page.TextPageLink;

import java.util.List;
import java.util.Optional;

public interface NvrService {

    Nvr findNvrById(NvrId nvrId);

    Nvr findNvrByCameraId(CameraId cameraId);

    ListenableFuture<Nvr> findNvrByIdAsync(NvrId nvrId);

    Optional<Nvr> findNvrByTenantIdAndName(TenantId tenantId, String name);

    Optional<Nvr> findNvrByTenantIdAndHost(TenantId tenantId, String host);

    Nvr saveNvr(Nvr nvr);

    Nvr assignNvrToCustomer(NvrId nvrId, CustomerId customerId);

    Nvr unassignNvrFromCustomer(NvrId nvrId, CustomerId customerId);

    void deleteNvr(NvrId nvrId);

    TextPageData<Nvr> findNvrsByTenantId(TenantId tenantId, TextPageLink pageLink);

    TextPageData<Nvr> findNvrsByTenantIdAndType(TenantId tenantId, String type, TextPageLink pageLink);

    ListenableFuture<List<Nvr>> findNvrsByTenantIdAndIdsAsync(TenantId tenantId, List<NvrId> nvrIds);

    void deleteNvrsByTenantId(TenantId tenantId);

    TextPageData<Nvr> findNvrsByTenantIdAndCustomerId(TenantId tenantId, CustomerId customerId, TextPageLink pageLink);

    TextPageData<Nvr> findNvrsByTenantIdAndCustomerIdAndType(TenantId tenantId, CustomerId customerId, String type, TextPageLink pageLink);

    ListenableFuture<List<Nvr>> findNvrsByTenantIdCustomerIdAndIdsAsync(TenantId tenantId, CustomerId customerId, List<NvrId> nvrIds);

    void unassignCustomerNvrs(TenantId tenantId, CustomerId customerId);

    ListenableFuture<List<Nvr>> findNvrsByQuery(NvrSearchQuery query);

    ListenableFuture<List<EntitySubtype>> findNvrTypesByTenantId(TenantId tenantId);
}
