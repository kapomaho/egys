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
package org.thingsboard.server.dao.model.nosql;

import com.datastax.driver.core.utils.UUIDs;
import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.thingsboard.server.common.data.Camera;
import org.thingsboard.server.common.data.id.CameraId;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.dao.model.MultipleCustomerAssignmentEntity;
import org.thingsboard.server.dao.model.SearchTextEntity;
import org.thingsboard.server.dao.model.type.JsonCodec;

import java.io.IOException;
import java.util.UUID;

import static org.thingsboard.server.dao.model.ModelConstants.*;

@Table(name = CAMERA_COLUMN_FAMILY_NAME)
@EqualsAndHashCode
@ToString
@Slf4j
public class CameraEntity implements SearchTextEntity<Camera>, MultipleCustomerAssignmentEntity {

    @Getter @Setter
    @PartitionKey(value = 0)
    @Column(name = ID_PROPERTY)
    private UUID id;

    @Getter @Setter
    @PartitionKey(value = 1)
    @Column(name = CAMERA_TENANT_ID_PROPERTY)
    private UUID tenantId;

    @Getter @Setter
    @PartitionKey(value = 2)
    @Column(name = CAMERA_TYPE_PROPERTY)
    private String type;

    @Getter @Setter
    @Column(name = CAMERA_NAME_PROPERTY)
    private String name;

    @Getter @Setter
    @Column(name = CAMERA_ASSIGNED_CUSTOMERS_PROPERTY)
    private String assignedCustomers;

    @Getter @Setter
    @PartitionKey(value = 3)
    @Column(name = CAMERA_HOST_PROPERTY)
    private String host;

    @Getter @Setter
    @Column(name = CAMERA_PORT_PROPERTY)
    private int port;

    @Getter @Setter
    @Column(name = CAMERA_PRIMARY_PATH_PROPERTY)
    private String primaryUrlPath;

    @Getter @Setter
    @Column(name = CAMERA_SECONDARY_PATH_PROPERTY)
    private String secondaryUrlPath;

    @Getter @Setter
    @Column(name = CAMERA_PTZ_PROPERTY)
    private boolean ptz;

    @Getter @Setter
    @Column(name = CAMERA_BRAND_PROPERTY)
    private String brand;

    @Getter @Setter
    @Column(name = CAMERA_MODEL_PROPERTY)
    private String model;

    @Getter @Setter
    @Column(name = CAMERA_RTSP_AUTH_PROPERTY)
    private boolean rtspAuth;

    @Getter @Setter
    @Column(name = CAMERA_USERNAME_PROPERTY)
    private String username;

    @Getter @Setter
    @Column(name = CAMERA_PASSWORD_PROPERTY)
    private String password;

    @Getter @Setter
    @Column(name = CAMERA_CHANNEL_NO_PROPERTY)
    private String channelNo;

    @Getter @Setter
    @Column(name = CAMERA_ADDITIONAL_INFO_PROPERTY, codec = JsonCodec.class)
    private JsonNode additionalInfo;

    @Getter @Setter
    @Column(name = SEARCH_TEXT_PROPERTY)
    private String searchText;

    public CameraEntity() {
        super();
    }

    public CameraEntity(Camera camera) {
        if (camera.getId() != null) {
            id = camera.getId().getId();
        }
        if (camera.getTenantId() != null) {
            tenantId = camera.getTenantId().getId();
        }
        if (camera.getAssignedCustomers() != null) {
            try {
                this.assignedCustomers = objectMapper.writeValueAsString(camera.getAssignedCustomers());
            } catch (JsonProcessingException e) {
                log.error(UNABLE_TO_SERIALIZE_ASSIGNED_CUSTOMERS_TO_STRING, e);
            }
        }

        name = camera.getName();
        type = camera.getType();
        additionalInfo = camera.getAdditionalInfo();
        host = camera.getHost();
        port = camera.getPort();
        primaryUrlPath = camera.getPrimaryUrlPath();
        secondaryUrlPath = camera.getSecondaryUrlPath();
        ptz = camera.isPtz();
        rtspAuth = camera.isRtspAuth();
        username = camera.getUsername();
        password = camera.getPassword();
        brand = camera.getBrand();
        model = camera.getModel();
        channelNo = camera.getChannelNo();
    }

    @Override
    public Camera toData() {
        Camera camera = new Camera(new CameraId(id));
        camera.setCreatedTime(UUIDs.unixTimestamp(id));
        if (tenantId != null) {
            camera.setTenantId(new TenantId(tenantId));
        }
        if (!StringUtils.isEmpty(assignedCustomers)) {
            try {
                camera.setAssignedCustomers(objectMapper.readValue(assignedCustomers, assignedCustomersType));
            } catch (IOException e) {
                log.warn(UNABLE_TO_PARSE_ASSIGNED_CUSTOMERS, e);
            }
        }
        camera.setName(name);
        camera.setType(type);
        camera.setHost(host);
        camera.setPort(port);
        camera.setPrimaryUrlPath(primaryUrlPath);
        camera.setSecondaryUrlPath(secondaryUrlPath);
        camera.setPtz(ptz);
        camera.setRtspAuth(rtspAuth);
        camera.setUsername(username);
        camera.setPassword(password);
        camera.setModel(model);
        camera.setBrand(brand);
        camera.setChannelNo(channelNo);
        camera.setAdditionalInfo(additionalInfo);
        return camera;
    }

    @Override
    public String getSearchTextSource() {
        return name;
    }
}
