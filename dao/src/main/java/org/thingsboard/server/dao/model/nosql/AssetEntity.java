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
import org.thingsboard.server.common.data.asset.Asset;
import org.thingsboard.server.common.data.id.AssetId;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.dao.model.MultipleCustomerAssignmentEntity;
import org.thingsboard.server.dao.model.SearchTextEntity;
import org.thingsboard.server.dao.model.type.JsonCodec;

import java.io.IOException;
import java.util.UUID;

import static org.thingsboard.server.dao.model.ModelConstants.*;

@Table(name = ASSET_COLUMN_FAMILY_NAME)
@EqualsAndHashCode
@ToString
@Slf4j
public final class AssetEntity implements SearchTextEntity<Asset>, MultipleCustomerAssignmentEntity {

    @Getter @Setter
    @PartitionKey(value = 0)
    @Column(name = ID_PROPERTY)
    private UUID id;

    @Getter @Setter
    @PartitionKey(value = 1)
    @Column(name = ASSET_TENANT_ID_PROPERTY)
    private UUID tenantId;

    @Getter @Setter
    @PartitionKey(value = 2)
    @Column(name = ASSET_TYPE_PROPERTY)
    private String type;

    @Getter @Setter
    @Column(name = ASSET_ASSIGNED_CUSTOMERS_PROPERTY)
    private String assignedCustomers;

    @Getter @Setter
    @Column(name = ASSET_NAME_PROPERTY)
    private String name;

    @Getter @Setter
    @Column(name = SEARCH_TEXT_PROPERTY)
    private String searchText;

    @Getter @Setter
    @Column(name = ASSET_ADDITIONAL_INFO_PROPERTY, codec = JsonCodec.class)
    private JsonNode additionalInfo;

    public AssetEntity() {
        super();
    }

    public AssetEntity(Asset asset) {
        if (asset.getId() != null) {
            this.id = asset.getId().getId();
        }
        if (asset.getTenantId() != null) {
            this.tenantId = asset.getTenantId().getId();
        }
        if (asset.getAssignedCustomers() != null) {
            try {
                this.assignedCustomers = objectMapper.writeValueAsString(asset.getAssignedCustomers());
            } catch (JsonProcessingException e) {
                log.error(UNABLE_TO_SERIALIZE_ASSIGNED_CUSTOMERS_TO_STRING, e);
            }
        }
        this.name = asset.getName();
        this.type = asset.getType();
        this.additionalInfo = asset.getAdditionalInfo();
    }

    @Override
    public String getSearchTextSource() {
        return name;
    }

    @Override
    public Asset toData() {
        Asset asset = new Asset(new AssetId(id));
        asset.setCreatedTime(UUIDs.unixTimestamp(id));
        if (tenantId != null) {
            asset.setTenantId(new TenantId(tenantId));
        }
        if (!StringUtils.isEmpty(assignedCustomers)) {
            try {
                asset.setAssignedCustomers(objectMapper.readValue(assignedCustomers, assignedCustomersType));
            } catch (IOException e) {
                log.warn(UNABLE_TO_PARSE_ASSIGNED_CUSTOMERS, e);
            }
        }
        asset.setName(name);
        asset.setType(type);
        asset.setAdditionalInfo(additionalInfo);
        return asset;
    }
}