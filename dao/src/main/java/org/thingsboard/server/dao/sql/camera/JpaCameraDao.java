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
package org.thingsboard.server.dao.sql.camera;

import com.google.common.util.concurrent.ListenableFuture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;
import org.thingsboard.server.common.data.Camera;
import org.thingsboard.server.common.data.EntitySubtype;
import org.thingsboard.server.common.data.EntityType;
import org.thingsboard.server.common.data.id.CustomerId;
import org.thingsboard.server.common.data.id.NvrId;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.page.TextPageLink;
import org.thingsboard.server.common.data.relation.EntityRelation;
import org.thingsboard.server.common.data.relation.RelationTypeGroup;
import org.thingsboard.server.dao.DaoUtil;
import org.thingsboard.server.dao.camera.CameraDao;
import org.thingsboard.server.dao.model.sql.CameraEntity;
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
public class JpaCameraDao extends JpaAbstractSearchTextDao<CameraEntity, Camera> implements CameraDao {

    @Autowired
    private CameraRepository cameraRepository;

    @Autowired
    private RelationDao relationDao;

    @Override
    public List<Camera> findCamerasByTenantId(UUID tenantId, TextPageLink pageLink) {
        return DaoUtil.convertDataList(
                cameraRepository
                        .findByTenantId(
                                fromTimeUUID(tenantId),
                                Objects.toString(pageLink.getTextSearch(), ""),
                                pageLink.getIdOffset() == null ? NULL_UUID_STR : fromTimeUUID(pageLink.getIdOffset()),
                                new PageRequest(0, pageLink.getLimit())));
    }

    @Override
    public List<Camera> findCamerasByNvrId(UUID nvrId) {
        List<EntityRelation> cameraNvrRelations = null;
        List<Camera> cameraList = new ArrayList<>();
        try {
            cameraNvrRelations = relationDao.findAllByFromAndType(new NvrId(nvrId), EntityRelation.MANAGES_TYPE, RelationTypeGroup.COMMON).get();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        for (EntityRelation er : cameraNvrRelations) {
            if (er.getTo().getEntityType() == EntityType.CAMERA){
                UUID id = er.getTo().getId();
                Camera c = findById(id);
                if (c != null){
                    cameraList.add(c);
                }
            }
        }

        return cameraList;
    }

    @Override
    public List<Camera> findCamerasByTenantIdAndType(UUID tenantId, String type, TextPageLink pageLink) {
        return DaoUtil.convertDataList(cameraRepository
                .findByTenantIdAndType(
                        fromTimeUUID(tenantId),
                        type,
                        Objects.toString(pageLink.getTextSearch(), ""),
                        pageLink.getIdOffset() == null ? NULL_UUID_STR : fromTimeUUID(pageLink.getIdOffset()),
                        new PageRequest(0, pageLink.getLimit())
                ));
    }

    @Override
    public ListenableFuture<List<Camera>> findCamerasByTenantIdAndIdsAsync(UUID tenantId, List<UUID> cameraIds) {
        return service.submit(() -> DaoUtil.convertDataList(cameraRepository.findByTenantIdAndIdIn(fromTimeUUID(tenantId), fromTimeUUIDs(cameraIds))));
    }

    @Override
    public List<Camera> findCamerasByTenantIdAndCustomerId(UUID tenantId, UUID customerId, TextPageLink pageLink) {
        List<EntityRelation> relations = relationDao.findRelations(new CustomerId(customerId),
                EntityRelation.CONTAINS_TYPE, RelationTypeGroup.CAMERA, EntityType.CAMERA);

        List<Camera> cameraList = findCamerasByTenantId(tenantId, pageLink);

        return DaoUtil.filterDataListByRelation(relations, cameraList);
    }

    @Override
    public List<Camera> findCamerasByTenantIdAndCustomerIdAndType(UUID tenantId, UUID customerId, String type, TextPageLink pageLink) {
        List<EntityRelation> relations = relationDao.findRelations(new CustomerId(customerId),
                EntityRelation.CONTAINS_TYPE, RelationTypeGroup.CAMERA, EntityType.CAMERA);

        List<Camera> cameraList = findCamerasByTenantIdAndType(tenantId, type, pageLink);

        return DaoUtil.filterDataListByRelation(relations, cameraList);
    }

    @Override
    public ListenableFuture<List<Camera>> findCamerasByTenantIdCustomerIdAndIdsAsync(UUID tenantId, UUID customerId, List<UUID> cameraIds) {
        List<EntityRelation> relations = relationDao.findRelations(new CustomerId(customerId),
                EntityRelation.CONTAINS_TYPE, RelationTypeGroup.CAMERA, EntityType.CAMERA);

        List<UUID> ids = relations.stream().map(entityRelation -> entityRelation.getTo().getId()).collect(Collectors.toList());
        cameraIds.retainAll(ids);
        return findCamerasByTenantIdAndIdsAsync(tenantId, cameraIds);
    }

    @Override
    public Optional<Camera> findCameraByTenantIdAndName(UUID tenantId, String name) {
        Camera data = DaoUtil.getData(cameraRepository.findByTenantIdAndName(fromTimeUUID(tenantId), name));
        return Optional.ofNullable(data);
    }

    @Override
    public Optional<Camera> findCameraByTenantIdAndHost(UUID tenantId, String host) {
        Camera data = DaoUtil.getData(cameraRepository.findByTenantIdAndHost(fromTimeUUID(tenantId), host));
        return Optional.ofNullable(data);
    }

    @Override
    public ListenableFuture<List<EntitySubtype>> findTenantCameraTypesAsync(UUID tenantId) {
        return service.submit(() -> convertTenantCameraTypesToDto(tenantId, cameraRepository.findTenantCameraTypes(fromTimeUUID(tenantId))));
    }

    private List<EntitySubtype> convertTenantCameraTypesToDto(UUID tenantId, List<String> types) {
        List<EntitySubtype> list = Collections.emptyList();
        if (types != null && !types.isEmpty()) {
            list = new ArrayList<>();
            for (String type : types) {
                list.add(new EntitySubtype(new TenantId(tenantId), EntityType.CAMERA, type));
            }
        }
        return list;
    }

    @Override
    protected Class<CameraEntity> getEntityClass() {
        return CameraEntity.class;
    }

    @Override
    protected CrudRepository<CameraEntity, String> getCrudRepository() {
        return cameraRepository;
    }
}
