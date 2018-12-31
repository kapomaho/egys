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

import com.google.common.base.Function;
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.thingsboard.server.common.data.*;
import org.thingsboard.server.common.data.id.*;
import org.thingsboard.server.common.data.page.TextPageData;
import org.thingsboard.server.common.data.page.TextPageLink;
import org.thingsboard.server.common.data.relation.EntityRelation;
import org.thingsboard.server.common.data.relation.EntitySearchDirection;
import org.thingsboard.server.common.data.relation.RelationTypeGroup;
import org.thingsboard.server.dao.customer.CustomerDao;
import org.thingsboard.server.dao.entity.AbstractEntityService;
import org.thingsboard.server.dao.exception.DataValidationException;
import org.thingsboard.server.dao.service.DataValidator;
import org.thingsboard.server.dao.service.PaginatedRemover;
import org.thingsboard.server.dao.tenant.TenantDao;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static org.thingsboard.server.dao.DaoUtil.toUUIDs;
import static org.thingsboard.server.dao.service.Validator.*;

@Service
@Slf4j
public class CameraServiceImpl extends AbstractEntityService implements CameraService {

    public static final String INCORRECT_TENANT_ID = "Incorrect tenantId ";
    public static final String INCORRECT_PAGE_LINK = "Incorrect page link ";
    public static final String INCORRECT_CUSTOMER_ID = "Incorrect customerId ";
    public static final String INCORRECT_CAMERA_ID = "Incorrect cameraId ";
    public static final String INCORRECT_NVR_ID = "Incorrect nvrId ";

    @Autowired
    private CameraDao cameraDao;

    @Autowired
    private TenantDao tenantDao;

    @Autowired
    private CustomerDao customerDao;

    private DataValidator<Camera> cameraValidator = new DataValidator<Camera>() {
        @Override
        protected void validateDataImpl(Camera data) {
            if (StringUtils.isEmpty(data.getType())) {
                throw new DataValidationException("Camera type should be specified");
            }
            if (StringUtils.isEmpty(data.getName())) {
                throw new DataValidationException("Camera name should be specified");
            }
            if (StringUtils.isEmpty(data.getHost())) {
                throw new DataValidationException("Camera host should be specified");
            }
            if (StringUtils.isEmpty(data.getPrimaryUrlPath()) && StringUtils.isEmpty(data.getSecondaryUrlPath())) {
                throw new DataValidationException("Camera stream primary or secondary url path should be specified");
            }
            if (StringUtils.isEmpty(data.getBrand())) {
                throw new DataValidationException("Camera brand should be specified");
            }
            if (StringUtils.isEmpty(data.getModel())) {
                throw new DataValidationException("Camera model should be specified");
            }
            if (data.getTenantId() == null) {
                throw new DataValidationException("Camera should be assigned to tenant");
            } else {
                Tenant tenant = tenantDao.findById(data.getTenantId().getId());
                if (tenant == null) {
                    throw new DataValidationException("Camera is referencing to non-existent tenant");
                }
            }
            if (data.isRtspAuth()) {
                if (StringUtils.isEmpty(data.getUsername())) {
                    throw new DataValidationException("Camera with RTSP authentication does not have a specified username");
                }
                if (StringUtils.isEmpty(data.getPassword())) {
                    throw new DataValidationException("Camera with RTSP authentication does not have a specified password");
                }
            }
            if (!data.getAssignedCustomers().isEmpty()) {
                for (ShortCustomerInfo customerInfo : data.getAssignedCustomers()) {
                    Customer customer = customerDao.findById(customerInfo.getCustomerId().getId());
                    if (customer == null) {
                        throw new DataValidationException("Cannot assign to non-existent customer");
                    }
                    if (!customer.getTenantId().getId().equals(data.getTenantId().getId())) {
                        throw new DataValidationException("Cannot assign camera to customer from different tenant");
                    }
                }
            }
        }

        @Override
        protected void validateCreate(Camera data) {
            super.validateCreate(data);
        }

        @Override
        protected void validateUpdate(Camera data) {
            super.validateUpdate(data);
        }
    };

    private PaginatedRemover<TenantId, Camera> tenantCamerasRemover = new PaginatedRemover<TenantId, Camera>() {
        @Override
        protected List<Camera> findEntities(TenantId id, TextPageLink pageLink) {
            return cameraDao.findCamerasByTenantId(id.getId(), pageLink);
        }

        @Override
        protected void removeEntity(Camera entity) {
            deleteCamera(entity.getId());
        }
    };

    @Override
    public Camera findCameraById(CameraId cameraId) {
        validateId(cameraId, INCORRECT_CAMERA_ID + cameraId);
        return cameraDao.findById(cameraId.getId());
    }

    @Override
    public ListenableFuture<Camera> findCameraByIdAsync(CameraId cameraId) {
        validateId(cameraId, INCORRECT_CAMERA_ID + cameraId);
        return cameraDao.findByIdAsync(cameraId.getId());
    }

    @Override
    public List<Camera> findCamerasByNvrId(NvrId nvrId) {
        validateId(nvrId, INCORRECT_NVR_ID + nvrId);
        return cameraDao.findCamerasByNvrId(nvrId.getId());
    }

    @Override
    public Optional<Camera> findCameraByTenantIdAndName(TenantId tenantId, String name) {
        validateId(tenantId, INCORRECT_TENANT_ID + tenantId);
        Optional<Camera> optionalCamera = cameraDao.findCameraByTenantIdAndName(tenantId.getId(), name);
        if (optionalCamera.isPresent()) {
            return Optional.of(optionalCamera.get());
        }
        return Optional.empty();
    }

    @Override
    public Optional<Camera> findCameraByTenantIdAndHost(TenantId tenantId, String host) {
        validateId(tenantId, INCORRECT_TENANT_ID + tenantId);
        Optional<Camera> optionalCamera = cameraDao.findCameraByTenantIdAndHost(tenantId.getId(), host);
        if (optionalCamera.isPresent()) {
            return Optional.of(optionalCamera.get());
        }
        return Optional.empty();
    }

    @Override
    public Camera saveCamera(Camera camera) {
        cameraValidator.validate(camera);
        return cameraDao.save(camera);
    }

    @Override
    public Camera assignCameraToCustomer(CameraId cameraId, CustomerId customerId) {
        Camera camera = findCameraById(cameraId);
        Customer customer = customerDao.findById(customerId.getId());
        if (customer == null) {
            throw new DataValidationException("Can't assign camera to non-existent customer");
        }
        if (!customer.getTenantId().getId().equals(camera.getTenantId().getId())) {
            throw new DataValidationException("Can't assign camera from different tenant");
        }
        if (camera.addAssignedCustomer(customer)) {
            try {
                createRelation(new EntityRelation(customerId, cameraId, EntityRelation.CONTAINS_TYPE, RelationTypeGroup.CAMERA));
            } catch (ExecutionException | InterruptedException e) {
                log.error("[{}] Failed to create camera relation. Customer Id: [{}]", cameraId, customerId);
                throw new RuntimeException(e);
            }
            return saveCamera(camera);
        } else {
            return camera;
        }
    }

    @Override
    public Camera unassignCameraFromCustomer(CameraId cameraId, CustomerId customerId) {
        Camera camera = findCameraById(cameraId);
        Customer customer = customerDao.findById(customerId.getId());
        if (customer == null) {
            throw new DataValidationException("Can't assign camera to non-existent customer");
        }
        if (!customer.getTenantId().getId().equals(camera.getTenantId().getId())) {
            throw new DataValidationException("Can't assign camera from different tenant");
        }
        if (camera.removeAssignedCustomer(customer)) {
            try {
                deleteRelation(new EntityRelation(customerId, cameraId, EntityRelation.CONTAINS_TYPE, RelationTypeGroup.CAMERA));
            } catch (ExecutionException | InterruptedException e) {
                log.error("[{}] Failed to delete camera relation. Customer Id: [{}]", cameraId, customerId);
                throw new RuntimeException(e);
            }
            return saveCamera(camera);
        } else {
            return camera;
        }
    }

    @Override
    public void deleteCamera(CameraId cameraId) {
        validateId(cameraId, INCORRECT_CAMERA_ID + cameraId);
        deleteEntityRelations(cameraId);
        cameraDao.removeById(cameraId.getId());
    }

    @Override
    public TextPageData<Camera> findCamerasByTenantId(TenantId tenantId, TextPageLink pageLink) {
        log.trace("Executing findCamerasByTenantId, tenantId [{}], pageLink [{}]", tenantId, pageLink);
        validateId(tenantId.getId(), INCORRECT_TENANT_ID + tenantId);
        validatePageLink(pageLink, INCORRECT_PAGE_LINK + pageLink);
        List<Camera> cameras = cameraDao.findCamerasByTenantId(tenantId.getId(), pageLink);
        return new TextPageData<>(cameras, pageLink);
    }

    @Override
    public TextPageData<Camera> findCamerasByTenantIdAndType(TenantId tenantId, String type, TextPageLink pageLink) {
        validateId(tenantId.getId(), INCORRECT_TENANT_ID + tenantId);
        validateString(type, "Incorrect type " + type);
        validatePageLink(pageLink, INCORRECT_PAGE_LINK + pageLink);
        List<Camera> cameras = cameraDao.findCamerasByTenantIdAndType(tenantId.getId(), type, pageLink);
        return new TextPageData<>(cameras, pageLink);
    }

    @Override
    public ListenableFuture<List<Camera>> findCamerasByTenantIdAndIdsAsync(TenantId tenantId, List<CameraId> cameraIds) {
        validateId(tenantId, INCORRECT_TENANT_ID + tenantId);
        validateIds(cameraIds, "Incorrect camera ids " + cameraIds);
        return cameraDao.findCamerasByTenantIdAndIdsAsync(tenantId.getId(), toUUIDs(cameraIds));
    }

    @Override
    public void deleteCamerasByTenantId(TenantId tenantId) {
        validateId(tenantId, INCORRECT_TENANT_ID + tenantId);
        tenantCamerasRemover.removeEntities(tenantId);
    }

    @Override
    public TextPageData<Camera> findCamerasByTenantIdAndCustomerId(TenantId tenantId, CustomerId customerId, TextPageLink pageLink) {
        validateId(tenantId, INCORRECT_TENANT_ID + tenantId);
        validateId(customerId, INCORRECT_CUSTOMER_ID + customerId);
        validatePageLink(pageLink, INCORRECT_PAGE_LINK + pageLink);
        return new TextPageData<>(cameraDao.findCamerasByTenantIdAndCustomerId(tenantId.getId(), customerId.getId(), pageLink), pageLink);
    }

    @Override
    public TextPageData<Camera> findCamerasByTenantIdAndCustomerIdAndType(TenantId tenantId, CustomerId customerId, String type, TextPageLink pageLink) {
        validateId(tenantId, INCORRECT_TENANT_ID + tenantId);
        validateId(customerId, INCORRECT_CUSTOMER_ID + customerId);
        validateString(type, "Incorrect type " + type);
        validatePageLink(pageLink, INCORRECT_PAGE_LINK + pageLink);
        return new TextPageData<>(cameraDao.findCamerasByTenantIdAndCustomerIdAndType(tenantId.getId(), customerId.getId(), type, pageLink), pageLink);
    }

    @Override
    public ListenableFuture<List<Camera>> findCamerasByTenantIdCustomerIdAndIdsAsync(TenantId tenantId, CustomerId customerId, List<CameraId> cameraIds) {
        validateId(tenantId, INCORRECT_TENANT_ID + tenantId);
        validateId(customerId, INCORRECT_CUSTOMER_ID + customerId);
        validateIds(cameraIds, "Incorrect camera ids " + cameraIds);
        return cameraDao.findCamerasByTenantIdCustomerIdAndIdsAsync(tenantId.getId(), customerId.getId(), toUUIDs(cameraIds));
    }

    @Override
    public void unassignCustomerCameras(TenantId tenantId, CustomerId customerId) {
        validateId(tenantId, INCORRECT_TENANT_ID + tenantId);
        validateId(customerId, INCORRECT_CUSTOMER_ID + customerId);
        new CustomerCameraUnassigner(tenantId).removeEntities(customerId);
    }

    @Override
    public ListenableFuture<List<Camera>> findCamerasByQuery(CameraSearchQuery query) {
        ListenableFuture<List<EntityRelation>> relations = relationService.findByQuery(query.toEntitySearchQuery());
        ListenableFuture<List<Camera>> cameras = Futures.transformAsync(relations, (AsyncFunction<List<EntityRelation>, List<Camera>>) relations1 -> {
            EntitySearchDirection direction = query.toEntitySearchQuery().getParameters().getDirection();
            List<ListenableFuture<Camera>> futures = new ArrayList<>();
            for (EntityRelation relation : relations1) {
                EntityId entityId = direction == EntitySearchDirection.FROM ? relation.getTo() : relation.getFrom();
                if (entityId.getEntityType() == EntityType.CAMERA) {
                    futures.add(findCameraByIdAsync(new CameraId(entityId.getId())));
                }
            }
            return Futures.successfulAsList(futures);
        });

        cameras = Futures.transform(cameras, new Function<List<Camera>, List<Camera>>() {
            @Nullable
            @Override
            public List<Camera> apply(@Nullable List<Camera> cameras) {
                return cameras == null ? Collections.emptyList() : cameras.stream().filter(camera -> query.getCameraTypes().contains(camera.getType())).collect(Collectors.toList());
            }
        });

        return cameras;
    }

    @Override
    public ListenableFuture<List<EntitySubtype>> findCameraTypesByTenantId(TenantId tenantId) {
        validateId(tenantId, INCORRECT_TENANT_ID + tenantId);
        ListenableFuture<List<EntitySubtype>> cameraTypesAsync = cameraDao.findTenantCameraTypesAsync(tenantId.getId());
        return Futures.transform(cameraTypesAsync, (Function<List<EntitySubtype>, List<EntitySubtype>>) cameraTypes -> {
            cameraTypes.sort(Comparator.comparing(EntitySubtype::getType));
            return cameraTypes;
        });
    }

    private class CustomerCameraUnassigner extends PaginatedRemover<CustomerId, Camera> {

        private TenantId tenantId;

        public CustomerCameraUnassigner(TenantId tenantId) {
            this.tenantId = tenantId;
        }

        @Override
        protected List<Camera> findEntities(CustomerId id, TextPageLink pageLink) {
            validateId(tenantId, INCORRECT_TENANT_ID + tenantId);
            validateId(id, INCORRECT_CUSTOMER_ID + id);
            validatePageLink(pageLink, INCORRECT_PAGE_LINK + pageLink);
            return cameraDao.findCamerasByTenantIdAndCustomerId(tenantId.getId(), id.getId(), pageLink);
        }

        @Override
        protected void removeEntity(Camera entity) {
            for (ShortCustomerInfo customerInfo : entity.getAssignedCustomers()) {
                unassignCameraFromCustomer(entity.getId(), customerInfo.getCustomerId());
            }
        }
    }
}
