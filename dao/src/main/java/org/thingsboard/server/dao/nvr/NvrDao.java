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
import org.thingsboard.server.common.data.page.TextPageLink;
import org.thingsboard.server.dao.Dao;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NvrDao extends Dao<Nvr> {
    Nvr save(Nvr nvr);

    Nvr findNvrByCameraId(UUID cameraId);

    List<Nvr> findNvrsByTenantId(UUID tenantId, TextPageLink pagelink);

    List<Nvr> findNvrsByTenantIdAndType(UUID tenantId, String type, TextPageLink pageLink);

    ListenableFuture<List<Nvr>> findNvrsByTenantIdAndIdsAsync(UUID tenantId, List<UUID> nvrIds);

    List<Nvr> findNvrsByTenantIdAndCustomerId(UUID tenantId, UUID customerId, TextPageLink pageLink);

    List<Nvr> findNvrsByTenantIdAndCustomerIdAndType(UUID tenantId, UUID customerId, String type, TextPageLink pageLink);

    ListenableFuture<List<Nvr>> findNvrsByTenantIdCustomerIdAndIdsAsync(UUID tenantId, UUID customerId, List<UUID> nvrIds);

    Optional<Nvr> findNvrByTenantIdAndName(UUID tenantId, String name);

    Optional<Nvr> findNvrByTenantIdAndHost(UUID tenantId, String host);

    ListenableFuture<List<EntitySubtype>> findTenantNvrTypesAsync(UUID tenantId);
}
