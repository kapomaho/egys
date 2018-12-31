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
package org.thingsboard.server.dao.model.sql;

import com.datastax.driver.core.utils.UUIDs;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.springframework.util.StringUtils;
import org.thingsboard.server.common.data.Camera;
import org.thingsboard.server.common.data.id.CameraId;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.dao.model.BaseSqlEntity;
import org.thingsboard.server.dao.model.MultipleCustomerAssignmentEntity;
import org.thingsboard.server.dao.model.SearchTextEntity;
import org.thingsboard.server.dao.util.mapping.JsonStringType;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.IOException;

import static org.thingsboard.server.dao.model.ModelConstants.*;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@TypeDef(name = "json", typeClass = JsonStringType.class)
@Table(name = CAMERA_COLUMN_FAMILY_NAME)
@Slf4j
public class CameraEntity extends BaseSqlEntity<Camera> implements SearchTextEntity<Camera>, MultipleCustomerAssignmentEntity {

    @Column(name = CAMERA_TENANT_ID_PROPERTY)
    private String tenantId;

    @Column(name = CAMERA_ASSIGNED_CUSTOMERS_PROPERTY)
    private String assignedCustomers;

    @Column(name = CAMERA_TYPE_PROPERTY)
    private String type;

    @Column(name = CAMERA_NAME_PROPERTY)
    private String name;

    @Column(name = CAMERA_HOST_PROPERTY)
    private String host;

    @Column(name = CAMERA_PORT_PROPERTY)
    private int port;

    @Column(name = CAMERA_PRIMARY_PATH_PROPERTY)
    private String primaryUrlPath;

    @Column(name = CAMERA_SECONDARY_PATH_PROPERTY)
    private String secondaryUrlPath;

    @Column(name = CAMERA_PTZ_PROPERTY)
    private boolean ptz;

    @Column(name = CAMERA_BRAND_PROPERTY)
    private String brand;

    @Column(name = CAMERA_MODEL_PROPERTY)
    private String model;

    @Column(name = CAMERA_RTSP_AUTH_PROPERTY)
    private boolean rtspAuth;

    @Column(name = CAMERA_USERNAME_PROPERTY)
    private String username;

    @Column(name = CAMERA_PASSWORD_PROPERTY)
    private String password;

    @Type(type = "json")
    @Column(name = CAMERA_ADDITIONAL_INFO_PROPERTY)
    private JsonNode additionalInfo;

    @Column(name = SEARCH_TEXT_PROPERTY)
    private String searchText;

    @Column(name = CAMERA_CHANNEL_NO_PROPERTY)
    private String channelNo;

    public CameraEntity() {
        super();
    }

    public CameraEntity(Camera camera) {
        if (camera.getId() != null) {
            this.setId(camera.getId().getId());
        }
        if (camera.getTenantId() != null) {
            tenantId = toString(camera.getTenantId().getId());
        }
        if (camera.getAssignedCustomers() != null) {
            try {
                assignedCustomers = objectMapper.writeValueAsString(camera.getAssignedCustomers());
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
    public String getSearchTextSource() {
        return name;
    }

    @Override
    public Camera toData() {
        Camera camera = new Camera(new CameraId(getId()));
        camera.setCreatedTime(UUIDs.unixTimestamp(getId()));
        if (tenantId != null) {
            camera.setTenantId(new TenantId(toUUID(tenantId)));
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
        camera.setPtz(ptz);
        camera.setPrimaryUrlPath(primaryUrlPath);
        camera.setSecondaryUrlPath(secondaryUrlPath);
        camera.setRtspAuth(rtspAuth);
        camera.setUsername(username);
        camera.setPassword(password);
        camera.setModel(model);
        camera.setBrand(brand);
        camera.setChannelNo(channelNo);
        camera.setAdditionalInfo(additionalInfo);
        return camera;
    }
}
