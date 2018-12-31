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
import org.thingsboard.server.common.data.Nvr;
import org.thingsboard.server.common.data.id.NvrId;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.dao.model.MultipleCustomerAssignmentEntity;
import org.thingsboard.server.dao.model.SearchTextEntity;
import org.thingsboard.server.dao.model.type.JsonCodec;

import java.io.IOException;
import java.util.UUID;

import static org.thingsboard.server.dao.model.ModelConstants.*;

@Table(name = NVR_COLUMN_FAMILY_NAME)
@EqualsAndHashCode
@ToString
@Slf4j
public class NvrEntity implements SearchTextEntity<Nvr>, MultipleCustomerAssignmentEntity {


    @Getter
    @Setter
    @PartitionKey(value = 0)
    @Column(name = ID_PROPERTY)
    private UUID id;

    @Getter @Setter
    @PartitionKey(value = 1)
    @Column(name = NVR_TENANT_ID_PROPERTY)
    private UUID tenantId;

    @Getter @Setter
    @PartitionKey(value = 2)
    @Column(name = NVR_TYPE_PROPERTY)
    private String type;

    @Getter @Setter
    @Column(name = NVR_NAME_PROPERTY)
    private String name;

    @Getter @Setter
    @Column(name = NVR_ASSIGNED_CUSTOMERS_PROPERTY)
    private String assignedCustomers;

    @Getter @Setter
    @PartitionKey(value = 3)
    @Column(name = NVR_HOST_PROPERTY)
    private String ip;

    @Getter @Setter
    @Column(name = NVR_PORT_PROPERTY)
    private int port;

    @Getter @Setter
    @Column(name = NVR_BRAND_PROPERTY)
    private String brand;

    @Getter @Setter
    @Column(name = NVR_MODEL_PROPERTY)
    private String model;

    @Getter @Setter
    @Column(name = NVR_USERNAME_PROPERTY)
    private String username;

    @Getter @Setter
    @Column(name = NVR_PASSWORD_PROPERTY)
    private String password;

    @Getter @Setter
    @Column(name = NVR_ADDITIONAL_INFO_PROPERTY, codec = JsonCodec.class)
    private JsonNode additionalInfo;

    @Getter @Setter
    @Column(name = SEARCH_TEXT_PROPERTY)
    private String searchText;

    public NvrEntity() {
        super();
    }

    public NvrEntity(Nvr nvr){
        if (nvr.getId() != null){
            id = nvr.getId().getId();
        }
        if (nvr.getTenantId() != null){
            tenantId = nvr.getTenantId().getId();
        }
        if (nvr.getAssignedCustomers() != null) {
            try {
                this.assignedCustomers = objectMapper.writeValueAsString(nvr.getAssignedCustomers());
            } catch (JsonProcessingException e) {
                log.error(UNABLE_TO_SERIALIZE_ASSIGNED_CUSTOMERS_TO_STRING, e);
            }
        }
        name = nvr.getName();
        type = nvr.getType();
        additionalInfo = nvr.getAdditionalInfo();
        ip = nvr.getIp();
        port = nvr.getPort();
        username = nvr.getUsername();
        password = nvr.getPassword();
        brand = nvr.getBrand();
        model = nvr.getModel();
    }

    @Override
    public String getSearchTextSource() {
        return name;
    }

    @Override
    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public void setId(UUID id) {
        this.id = id;
    }

    @Override
    public Nvr toData() {
        Nvr nvr = new Nvr(new NvrId(id));
        nvr.setCreatedTime(UUIDs.unixTimestamp(id));
        if (tenantId != null) {
            nvr.setTenantId(new TenantId(tenantId));
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
        nvr.setUsername(username);
        nvr.setPassword(password);
        nvr.setModel(model);
        nvr.setBrand(brand);
        nvr.setAdditionalInfo(additionalInfo);

        return nvr;
    }
}
