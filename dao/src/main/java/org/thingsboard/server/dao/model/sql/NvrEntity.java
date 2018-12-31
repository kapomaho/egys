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

import static org.thingsboard.server.dao.model.ModelConstants.*;

import com.datastax.driver.core.utils.UUIDs;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.springframework.util.StringUtils;
import org.thingsboard.server.common.data.Nvr;
import org.thingsboard.server.common.data.UUIDConverter;
import org.thingsboard.server.common.data.id.NvrId;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.dao.model.BaseSqlEntity;
import org.thingsboard.server.dao.model.ModelConstants;
import org.thingsboard.server.dao.model.MultipleCustomerAssignmentEntity;
import org.thingsboard.server.dao.model.SearchTextEntity;
import org.thingsboard.server.dao.util.mapping.JsonStringType;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Column;
import java.io.IOException;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@TypeDef(name = "json", typeClass = JsonStringType.class)
@Table(name = NVR_COLUMN_FAMILY_NAME)
@Slf4j

public class NvrEntity extends BaseSqlEntity<Nvr> implements SearchTextEntity<Nvr>, MultipleCustomerAssignmentEntity {

    @Column(name = NVR_TENANT_ID_PROPERTY)
    private String tenantId;

    @Column(name = NVR_ASSIGNED_CUSTOMERS_PROPERTY)
    private String assignedCustomers;

    @Column(name = NVR_NAME_PROPERTY)
    private String name;

    @Column(name = NVR_TYPE_PROPERTY)
    private String type;

    @Column(name = NVR_HOST_PROPERTY)
    private String ip;

    @Column(name = NVR_PORT_PROPERTY)
    private int port;

    @Column(name = NVR_BRAND_PROPERTY)
    private String brand;

    @Column(name = NVR_MODEL_PROPERTY)
    private String model;

    @Column(name = NVR_USERNAME_PROPERTY)
    private String username;

    @Column(name = NVR_PASSWORD_PROPERTY)
    private String password;

    @Column(name = SEARCH_TEXT_PROPERTY)
    private String searchText;

    @Type(type = "json")
    @Column(name = ModelConstants.NVR_ADDITIONAL_INFO_PROPERTY)
    private JsonNode additionalInfo;

    public NvrEntity() {
        super();
    }

    public NvrEntity(Nvr nvr) {
        if (nvr.getId() != null) {
            this.setId(nvr.getId().getId());
        }
        if (nvr.getTenantId() != null) {
            this.tenantId = UUIDConverter.fromTimeUUID(nvr.getTenantId().getId());
        }
        if (nvr.getAssignedCustomers() != null) {
            try {
                this.assignedCustomers = objectMapper.writeValueAsString(nvr.getAssignedCustomers());
            } catch (JsonProcessingException e) {
                log.error(UNABLE_TO_SERIALIZE_ASSIGNED_CUSTOMERS_TO_STRING, e);
            }
        }
        this.name = nvr.getName();
        this.type = nvr.getType();
        this.ip = nvr.getIp();
        this.port = nvr.getPort();
        this.brand = nvr.getBrand();
        this.model = nvr.getModel();
        this.username = nvr.getUsername();
        this.password = nvr.getPassword();
        this.additionalInfo = nvr.getAdditionalInfo();
    }

    @Override
    public String getSearchTextSource() {
        return name;
    }

    @Override
    public Nvr toData() {
        Nvr nvr = new Nvr(new NvrId(UUIDConverter.fromString(id)));
        nvr.setCreatedTime(UUIDs.unixTimestamp(UUIDConverter.fromString(id)));
        if (tenantId != null) {
            nvr.setTenantId(new TenantId(UUIDConverter.fromString(tenantId)));
        }
        if (!StringUtils.isEmpty(assignedCustomers)) {
            try {
                nvr.setAssignedCustomers(objectMapper.readValue(assignedCustomers, assignedCustomersType));
            } catch (IOException e) {
                log.warn(UNABLE_TO_PARSE_ASSIGNED_CUSTOMERS, e);
            }
        }
        nvr.setName(name);
        nvr.setType(type);
        nvr.setIp(ip);
        nvr.setPort(port);
        nvr.setBrand(brand);
        nvr.setModel(model);
        nvr.setUsername(username);
        nvr.setPassword(password);
        nvr.setAdditionalInfo(additionalInfo);
        return nvr;
    }
}
