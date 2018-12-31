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
package org.thingsboard.server.dao.camera;

import com.google.common.util.concurrent.ListenableFuture;
import org.thingsboard.server.common.data.Camera;
import org.thingsboard.server.common.data.EntitySubtype;
import org.thingsboard.server.common.data.id.CameraId;
import org.thingsboard.server.common.data.id.CustomerId;
import org.thingsboard.server.common.data.id.NvrId;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.page.TextPageData;
import org.thingsboard.server.common.data.page.TextPageLink;

import java.util.List;
import java.util.Optional;

public interface CameraService {

    Camera findCameraById(CameraId cameraId);

    ListenableFuture<Camera> findCameraByIdAsync(CameraId cameraId);

    List<Camera> findCamerasByNvrId(NvrId nvrId);

    Optional<Camera> findCameraByTenantIdAndName(TenantId tenantId, String name);

    Optional<Camera> findCameraByTenantIdAndHost(TenantId tenantId, String host);

    Camera saveCamera(Camera camera);

    Camera assignCameraToCustomer(CameraId cameraId, CustomerId customerId);

    Camera unassignCameraFromCustomer(CameraId cameraId, CustomerId customerId);

    void deleteCamera(CameraId cameraId);

    TextPageData<Camera> findCamerasByTenantId(TenantId tenantId, TextPageLink pageLink);

    TextPageData<Camera> findCamerasByTenantIdAndType(TenantId tenantId, String type, TextPageLink pageLink);

    ListenableFuture<List<Camera>> findCamerasByTenantIdAndIdsAsync(TenantId tenantId, List<CameraId> cameraIds);

    void deleteCamerasByTenantId(TenantId tenantId);

    TextPageData<Camera> findCamerasByTenantIdAndCustomerId(TenantId tenantId, CustomerId customerId, TextPageLink pageLink);

    TextPageData<Camera> findCamerasByTenantIdAndCustomerIdAndType(TenantId tenantId, CustomerId customerId, String type, TextPageLink pageLink);

    ListenableFuture<List<Camera>> findCamerasByTenantIdCustomerIdAndIdsAsync(TenantId tenantId, CustomerId customerId, List<CameraId> cameraIds);

    void unassignCustomerCameras(TenantId tenantId, CustomerId customerId);

    ListenableFuture<List<Camera>> findCamerasByQuery(CameraSearchQuery query);

    ListenableFuture<List<EntitySubtype>> findCameraTypesByTenantId(TenantId tenantId);
}
