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
public class NvrServiceImpl extends AbstractEntityService implements NvrService {

    public static final String INCORRECT_TENANT_ID = "Incorrect tenantId ";
    public static final String INCORRECT_PAGE_LINK = "Incorrect page link ";
    public static final String INCORRECT_CUSTOMER_ID = "Incorrect customerId ";
    public static final String INCORRECT_NVR_ID = "Incorrect nvrId ";
    public static final String INCORRECT_CAMERA_ID = "Incorrect cameraId ";

    @Autowired
    private NvrDao nvrDao;

    @Autowired
    private TenantDao tenantDao;

    @Autowired
    private CustomerDao customerDao;

    private DataValidator<Nvr> nvrDataValidator = new DataValidator<Nvr>() {

        @Override
        protected void validateDataImpl(Nvr data) {

            if (StringUtils.isEmpty(data.getType())) {
                throw new DataValidationException("Nvr type should be specified");
            }
            if (StringUtils.isEmpty(data.getName())) {
                throw new DataValidationException("Nvr name should be specified");
            }
            if (StringUtils.isEmpty(data.getIp())) {
                throw new DataValidationException("Nvr host ip should be specified");
            }
            if (StringUtils.isEmpty(data.getBrand())) {
                throw new DataValidationException("Nvr brand should be specified");
            }
            if (StringUtils.isEmpty(data.getModel())) {
                throw new DataValidationException("Nvr model should be specified");
            }
            if (data.getTenantId() == null) {
                throw new DataValidationException("Nvr should be assigned to a tenant");
            } else {
                Tenant tenant = tenantDao.findById(data.getTenantId().getId());
                if (tenant == null) {
                    throw new DataValidationException("Nvr is referencing to non-existing tenant");
                }
            }
            if (!data.getAssignedCustomers().isEmpty()) {
                for (ShortCustomerInfo customerInfo : data.getAssignedCustomers()) {
                    Customer customer = customerDao.findById(customerInfo.getCustomerId().getId());
                    if (customer == null) {
                        throw new DataValidationException("Cannot assign nvr to non-existent customer");
                    }
                    if (!customer.getTenantId().getId().equals(data.getTenantId().getId())) {
                        throw new DataValidationException("Cannot assign nvr to customer from different tenant");
                    }
                }
            }
        }

        @Override
        protected void validateCreate(Nvr data) {
            super.validateCreate(data);
        }

        @Override
        protected void validateUpdate(Nvr data) {
            super.validateUpdate(data);
        }

    };

    private PaginatedRemover<TenantId, Nvr> tenantNvrsRemover = new PaginatedRemover<TenantId, Nvr>() {
        @Override
        protected List<Nvr> findEntities(TenantId id, TextPageLink pageLink) {
            return nvrDao.findNvrsByTenantId(id.getId(), pageLink);
        }

        @Override
        protected void removeEntity(Nvr entity) {
            deleteNvr(entity.getId());
        }
    };


    @Override
    public Nvr findNvrById(NvrId nvrId) {
        validateId(nvrId, INCORRECT_NVR_ID + nvrId);
        return nvrDao.findById(nvrId.getId());
    }

    @Override
    public Nvr findNvrByCameraId(CameraId cameraId) {
        validateId(cameraId, INCORRECT_CAMERA_ID + cameraId);
        return nvrDao.findNvrByCameraId(cameraId.getId());
    }

    @Override
    public ListenableFuture<Nvr> findNvrByIdAsync(NvrId nvrId) {
        validateId(nvrId, INCORRECT_NVR_ID + nvrId);
        return nvrDao.findByIdAsync(nvrId.getId());
    }

    @Override
    public Optional<Nvr> findNvrByTenantIdAndName(TenantId tenantId, String name) {
        validateId(tenantId, INCORRECT_TENANT_ID);
        Optional<Nvr> optionalNvr = nvrDao.findNvrByTenantIdAndName(tenantId.getId(), name);
        if (optionalNvr.isPresent()) {
            return Optional.of(optionalNvr.get());
        }
        return Optional.empty();
    }

    @Override
    public Optional<Nvr> findNvrByTenantIdAndHost(TenantId tenantId, String host) {
        validateId(tenantId, INCORRECT_TENANT_ID + tenantId);
        Optional<Nvr> optionalNvr = nvrDao.findNvrByTenantIdAndHost(tenantId.getId(), host);
        if (optionalNvr.isPresent()) {
            return Optional.of(optionalNvr.get());
        }
        return Optional.empty();
    }

    @Override
    public Nvr saveNvr(Nvr nvr) {
        nvrDataValidator.validate(nvr);
        return nvrDao.save(nvr);
    }

    @Override
    public Nvr assignNvrToCustomer(NvrId nvrId, CustomerId customerId) {
        Nvr nvr = findNvrById(nvrId);
        Customer customer = customerDao.findById(customerId.getId());
        if (customer == null) {
            throw new DataValidationException("Can't assign nvr to non-existent customer");
        }
        if (!customer.getTenantId().getId().equals(nvr.getTenantId().getId())) {
            throw new DataValidationException("Can't assign nvr from different tenant");
        }
        if (nvr.addAssignedCustomer(customer)) {
            try {
                createRelation(new EntityRelation(customerId, nvrId, EntityRelation.CONTAINS_TYPE, RelationTypeGroup.NVR));
            } catch (ExecutionException | InterruptedException e) {
                log.error("[{}] Failed to create nvr relation. Customer Id: [{}]", nvrId, customerId);
                throw new RuntimeException(e);
            }
            return saveNvr(nvr);
        } else {
            return nvr;
        }
    }

    @Override
    public Nvr unassignNvrFromCustomer(NvrId nvrId, CustomerId customerId) {
        Nvr nvr = findNvrById(nvrId);
        Customer customer = customerDao.findById(customerId.getId());
        if (customer == null) {
            throw new DataValidationException("Can't assign nvr to non-existent customer");
        }
        if (!customer.getTenantId().getId().equals(nvr.getTenantId().getId())) {
            throw new DataValidationException("Can't assign nvr from different tenant");
        }
        if (nvr.removeAssignedCustomer(customer)) {
            try {
                deleteRelation(new EntityRelation(customerId, nvrId, EntityRelation.CONTAINS_TYPE, RelationTypeGroup.NVR));
            } catch (ExecutionException | InterruptedException e) {
                log.error("[{}] Failed to delete nvr relation. Customer Id: [{}]", nvrId, customerId);
                throw new RuntimeException(e);
            }
            return saveNvr(nvr);
        } else {
            return nvr;
        }
    }

    @Override
    public void deleteNvr(NvrId nvrId) {
        log.trace("Executing deleteNvr [{}]", nvrId);
        validateId(nvrId, INCORRECT_NVR_ID + nvrId);
        deleteEntityRelations(nvrId);
        nvrDao.removeById(nvrId.getId());
    }

    @Override
    public TextPageData<Nvr> findNvrsByTenantId(TenantId tenantId, TextPageLink pageLink) {
        log.trace("Executing findNvrByTenantId, tenantId [{}], pagelink [{}]", tenantId, pageLink);
        validateId(tenantId.getId(), INCORRECT_TENANT_ID + tenantId);
        validatePageLink(pageLink, INCORRECT_PAGE_LINK + pageLink);
        List<Nvr> nvrs = nvrDao.findNvrsByTenantId(tenantId.getId(), pageLink);
        return new TextPageData<>(nvrs, pageLink);
    }

    @Override
    public TextPageData<Nvr> findNvrsByTenantIdAndType(TenantId tenantId, String type, TextPageLink pageLink) {
        validateId(tenantId.getId(), INCORRECT_TENANT_ID + tenantId);
        validateString(type, "Incorrect type " + type);
        validatePageLink(pageLink, INCORRECT_PAGE_LINK + pageLink);
        List<Nvr> nvrs = nvrDao.findNvrsByTenantIdAndType(tenantId.getId(), type, pageLink);
        return new TextPageData<>(nvrs, pageLink);
    }

    @Override
    public ListenableFuture<List<Nvr>> findNvrsByTenantIdAndIdsAsync(TenantId tenantId, List<NvrId> nvrIds) {
        validateId(tenantId, INCORRECT_TENANT_ID + tenantId);
        validateIds(nvrIds, "Incorrect nvr ids " + nvrIds);
        return nvrDao.findNvrsByTenantIdAndIdsAsync(tenantId.getId(), toUUIDs(nvrIds));
    }

    @Override
    public void deleteNvrsByTenantId(TenantId tenantId) {
        validateId(tenantId, INCORRECT_TENANT_ID + tenantId);
        tenantNvrsRemover.removeEntities(tenantId);
    }

    @Override
    public TextPageData<Nvr> findNvrsByTenantIdAndCustomerId(TenantId tenantId, CustomerId customerId, TextPageLink pageLink) {
        validateId(tenantId, INCORRECT_TENANT_ID + tenantId);
        validateId(customerId, INCORRECT_CUSTOMER_ID + customerId);
        validatePageLink(pageLink, INCORRECT_PAGE_LINK + pageLink);
        return new TextPageData<>(nvrDao.findNvrsByTenantIdAndCustomerId(tenantId.getId(), customerId.getId(), pageLink), pageLink);
    }

    @Override
    public TextPageData<Nvr> findNvrsByTenantIdAndCustomerIdAndType(TenantId tenantId, CustomerId customerId, String type, TextPageLink pageLink) {
        validateId(tenantId, INCORRECT_TENANT_ID + tenantId);
        validateId(customerId, INCORRECT_CUSTOMER_ID + customerId);
        validateString(type, "Incorrect type " + type);
        validatePageLink(pageLink, INCORRECT_PAGE_LINK + pageLink);
        return new TextPageData<>(nvrDao.findNvrsByTenantIdAndCustomerIdAndType(tenantId.getId(), customerId.getId(), type, pageLink), pageLink);
    }

    @Override
    public ListenableFuture<List<Nvr>> findNvrsByTenantIdCustomerIdAndIdsAsync(TenantId tenantId, CustomerId customerId, List<NvrId> nvrIds) {
        validateId(tenantId, INCORRECT_TENANT_ID + tenantId);
        validateId(customerId, INCORRECT_CUSTOMER_ID + customerId);
        validateIds(nvrIds, "Incorrect nvr ids " + nvrIds);
        return nvrDao.findNvrsByTenantIdCustomerIdAndIdsAsync(tenantId.getId(), customerId.getId(), toUUIDs(nvrIds));
    }

    @Override
    public void unassignCustomerNvrs(TenantId tenantId, CustomerId customerId) {
        validateId(tenantId, INCORRECT_TENANT_ID + tenantId);
        validateId(customerId, INCORRECT_CUSTOMER_ID + customerId);
        new CustomerNvrUnassigner(tenantId).removeEntities(customerId);
    }

    @Override
    public ListenableFuture<List<Nvr>> findNvrsByQuery(NvrSearchQuery query) {
        ListenableFuture<List<EntityRelation>> relations = relationService.findByQuery(query.toEntitySearchQuery());
        ListenableFuture<List<Nvr>> nvrs = Futures.transformAsync(relations, (AsyncFunction<List<EntityRelation>, List<Nvr>>) relations1 -> {
            EntitySearchDirection direction = query.toEntitySearchQuery().getParameters().getDirection();
            List<ListenableFuture<Nvr>> futures = new ArrayList<>();
            for (EntityRelation relation : relations1) {
                EntityId entityId = direction == EntitySearchDirection.FROM ? relation.getTo() : relation.getFrom();
                if (entityId.getEntityType() == EntityType.NVR) {
                    futures.add(findNvrByIdAsync(new NvrId(entityId.getId())));
                }
            }
            return Futures.successfulAsList(futures);
        });

        nvrs = Futures.transform(nvrs, new Function<List<Nvr>, List<Nvr>>() {
            @Nullable
            @Override
            public List<Nvr> apply(@Nullable List<Nvr> nvrs) {
                return nvrs == null ? Collections.emptyList() : nvrs.stream().filter(nvr -> query.getNvrTypes().contains(nvr.getType())).collect(Collectors.toList());
            }
        });
        return nvrs;
    }

    @Override
    public ListenableFuture<List<EntitySubtype>> findNvrTypesByTenantId(TenantId tenantId) {
        validateId(tenantId, INCORRECT_TENANT_ID + tenantId);
        ListenableFuture<List<EntitySubtype>> nvrTypeAsync = nvrDao.findTenantNvrTypesAsync(tenantId.getId());
        return Futures.transform(nvrTypeAsync, (Function<List<EntitySubtype>, List<EntitySubtype>>) nvrTypes -> {
            nvrTypes.sort(Comparator.comparing(EntitySubtype::getType));
            return nvrTypes;
        });
    }

    private class CustomerNvrUnassigner extends PaginatedRemover<CustomerId, Nvr> {
        private TenantId tenantId;

        public CustomerNvrUnassigner(TenantId tenantId) {
            this.tenantId = tenantId;
        }

        @Override
        protected List<Nvr> findEntities(CustomerId id, TextPageLink pageLink) {
            validateId(tenantId, INCORRECT_TENANT_ID + tenantId);
            validateId(id, INCORRECT_CUSTOMER_ID + id);
            validatePageLink(pageLink, INCORRECT_PAGE_LINK + pageLink);
            return nvrDao.findNvrsByTenantIdAndCustomerId(tenantId.getId(), id.getId(), pageLink);
        }

        @Override
        protected void removeEntity(Nvr entity) {
            for (ShortCustomerInfo customerInfo : entity.getAssignedCustomers()) {
                unassignNvrFromCustomer(entity.getId(), customerInfo.getCustomerId());
            }
        }
    }

}
