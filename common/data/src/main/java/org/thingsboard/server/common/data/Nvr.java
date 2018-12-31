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

package org.thingsboard.server.common.data;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.thingsboard.server.common.data.id.NvrId;
import org.thingsboard.server.common.data.id.TenantId;

import java.util.HashSet;
import java.util.Set;

@EqualsAndHashCode(callSuper = true)
public class Nvr extends SearchTextBasedWithAdditionalInfo<NvrId> implements HasName, HasMultipleCustomers {
    private static final long serialVersionUID = 2807343040519543363L;

    @Getter @Setter
    private TenantId tenantId;
    @Getter @Setter
    private String name;
    @Getter @Setter
    private String type;
    @Getter @Setter
    private String ip;
    @Getter @Setter
    private int port;
    @Getter @Setter
    private String username;
    @Getter @Setter
    private String password;
    @Getter @Setter
    private String brand;
    @Getter @Setter
    private String model;
    @Getter @Setter
    private transient JsonNode additionalInfo;
    @Getter @Setter
    private Set<ShortCustomerInfo> assignedCustomers = new HashSet<>();

    public Nvr() {
        super();
    }

    public Nvr(NvrId id) {
        super(id);
    }

    public Nvr(Nvr nvr) {
        super(nvr);
        this.tenantId = nvr.tenantId;
        this.assignedCustomers = nvr.getAssignedCustomers();
        this.name = nvr.name;
        this.type = nvr.type;
        this.ip = nvr.ip;
        this.port = nvr.port;
        this.username = nvr.username;
        this.password = nvr.password;
        this.brand = nvr.brand;
        this.model = nvr.model;
        this.additionalInfo = nvr.additionalInfo;
    }

    @Override
    public String getSearchText() {
        return getName();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Nvr [tenantId=");
        builder.append(tenantId);
        builder.append(", name=");
        builder.append(name);
        builder.append(", type=");
        builder.append(type);
        builder.append(", ip=");
        builder.append(ip);
        builder.append(", port=");
        builder.append(port);
        builder.append(", username=");
        builder.append(username);
        builder.append(", password=");
        builder.append(password);
        builder.append(", brand=");
        builder.append(brand);
        builder.append(", model=");
        builder.append(model);
        builder.append(", additionalInfo=");
        builder.append(additionalInfo);
        builder.append(", createdTime=");
        builder.append(createdTime);
        builder.append(", id=");
        builder.append(id);
        builder.append("]");
        return builder.toString();
    }
}
