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

import com.google.common.util.concurrent.ListenableFuture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;
import org.thingsboard.server.common.data.Camera;
import org.thingsboard.server.common.data.EntitySubtype;
import org.thingsboard.server.common.data.EntityType;
import org.thingsboard.server.common.data.Nvr;
import org.thingsboard.server.common.data.id.CameraId;
import org.thingsboard.server.common.data.id.CustomerId;
import org.thingsboard.server.common.data.id.NvrId;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.page.TextPageLink;
import org.thingsboard.server.common.data.relation.EntityRelation;
import org.thingsboard.server.common.data.relation.RelationTypeGroup;
import org.thingsboard.server.dao.DaoUtil;
import org.thingsboard.server.dao.model.sql.NvrEntity;
import org.thingsboard.server.dao.nvr.NvrDao;
import org.thingsboard.server.dao.relation.RelationDao;
import org.thingsboard.server.dao.sql.JpaAbstractSearchTextDao;
import org.thingsboard.server.dao.util.SqlDao;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static org.thingsboard.server.common.data.UUIDConverter.fromTimeUUID;
import static org.thingsboard.server.common.data.UUIDConverter.fromTimeUUIDs;
import static org.thingsboard.server.dao.model.ModelConstants.NULL_UUID_STR;

@Component
@SqlDao
public class JpaNvrDao extends JpaAbstractSearchTextDao<NvrEntity, Nvr> implements NvrDao {

    @Autowired
    private NvrRepository nvrRepository;

    @Autowired
    private RelationDao relationDao;


    @Override
    public Nvr findNvrByCameraId(UUID cameraId) {
        try {
            List<EntityRelation> cameraNvrRelations = relationDao.findAllByToAndType(new CameraId(cameraId), EntityRelation.MANAGES_TYPE, RelationTypeGroup.COMMON).get();
            UUID nvrId = null;
            for (EntityRelation er : cameraNvrRelations) {
                if (er.getFrom().getEntityType() == EntityType.NVR) {
                    nvrId = er.getFrom().getId();
                }
            }
            return findById(nvrId);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Nvr> findNvrsByTenantId(UUID tenantId, TextPageLink pagelink) {
        return DaoUtil.convertDataList(
                nvrRepository.findByTenantId(
                        fromTimeUUID(tenantId),
                        Objects.toString(pagelink.getTextSearch(), ""),
                        pagelink.getIdOffset() == null ? NULL_UUID_STR : fromTimeUUID(pagelink.getIdOffset()),
                        new PageRequest(0, pagelink.getLimit())));
    }

    @Override
    public List<Nvr> findNvrsByTenantIdAndType(UUID tenantId, String type, TextPageLink pageLink) {
        return DaoUtil.convertDataList(
                nvrRepository.findByTenantIdAndType(
                        fromTimeUUID(tenantId),
                        type,
                        Objects.toString(pageLink.getTextSearch(), ""),
                        pageLink.getIdOffset() == null ? NULL_UUID_STR : fromTimeUUID(pageLink.getIdOffset()),
                        new PageRequest(0, pageLink.getLimit())));
    }

    @Override
    public ListenableFuture<List<Nvr>> findNvrsByTenantIdAndIdsAsync(UUID tenantId, List<UUID> nvrIds) {
        return service.submit(() -> DaoUtil.convertDataList(nvrRepository.findByTenantIdAndIdIn(fromTimeUUID(tenantId), fromTimeUUIDs(nvrIds))));
    }

    @Override
    public List<Nvr> findNvrsByTenantIdAndCustomerId(UUID tenantId, UUID customerId, TextPageLink pageLink) {
        List<EntityRelation> relations = relationDao.findRelations(new CustomerId(customerId),
                EntityRelation.CONTAINS_TYPE, RelationTypeGroup.NVR, EntityType.NVR);

        List<Nvr> nvrList = findNvrsByTenantId(tenantId, pageLink);

        return DaoUtil.filterDataListByRelation(relations, nvrList);
    }

    @Override
    public List<Nvr> findNvrsByTenantIdAndCustomerIdAndType(UUID tenantId, UUID customerId, String type, TextPageLink pageLink) {
        List<EntityRelation> relations = relationDao.findRelations(new CustomerId(customerId),
                EntityRelation.CONTAINS_TYPE, RelationTypeGroup.NVR, EntityType.NVR);

        List<Nvr> nvrList = findNvrsByTenantIdAndType(tenantId, type, pageLink);

        return DaoUtil.filterDataListByRelation(relations, nvrList);
    }

    @Override
    public ListenableFuture<List<Nvr>> findNvrsByTenantIdCustomerIdAndIdsAsync(UUID tenantId, UUID customerId, List<UUID> nvrIds) {
        List<EntityRelation> relations = relationDao.findRelations(new CustomerId(customerId),
                EntityRelation.CONTAINS_TYPE, RelationTypeGroup.NVR, EntityType.NVR);

        List<UUID> ids = relations.stream().map(entityRelation -> entityRelation.getTo().getId()).collect(Collectors.toList());
        nvrIds.retainAll(ids);
        return findNvrsByTenantIdAndIdsAsync(tenantId, nvrIds);
    }

    @Override
    public Optional<Nvr> findNvrByTenantIdAndName(UUID tenantId, String name) {
        Nvr data = DaoUtil.getData(nvrRepository.findByTenantIdAndName(fromTimeUUID(tenantId), name));
        return Optional.ofNullable(data);
    }

    @Override
    public Optional<Nvr> findNvrByTenantIdAndHost(UUID tenantId, String host) {
        Nvr data = DaoUtil.getData(nvrRepository.findByTenantIdAndIp(fromTimeUUID(tenantId), host));
        return Optional.ofNullable(data);
    }

    @Override
    public ListenableFuture<List<EntitySubtype>> findTenantNvrTypesAsync(UUID tenantId) {
        return service.submit(() -> convertTenantNvrTypesToDto(tenantId, nvrRepository.findTenantNvrTypes(fromTimeUUID(tenantId))));
    }

    private List<EntitySubtype> convertTenantNvrTypesToDto(UUID tenantId, List<String> types) {
        List<EntitySubtype> list = Collections.emptyList();
        if (types != null && !types.isEmpty()) {
            list = new ArrayList<>();
            for (String type : types) {
                list.add(new EntitySubtype(new TenantId(tenantId), EntityType.NVR, type));
            }
        }
        return list;
    }

    @Override
    protected Class<NvrEntity> getEntityClass() {
        return NvrEntity.class;
    }

    @Override
    protected CrudRepository<NvrEntity, String> getCrudRepository() {
        return nvrRepository;
    }
}
