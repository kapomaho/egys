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

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.thingsboard.server.common.data.id.CameraId;
import org.thingsboard.server.common.data.id.TenantId;

import java.util.HashSet;
import java.util.Set;

@EqualsAndHashCode(callSuper = true)
public class Camera extends SearchTextBasedWithAdditionalInfo<CameraId> implements HasName, HasMultipleCustomers {

    @Getter @Setter
    private Set<ShortCustomerInfo> assignedCustomers = new HashSet<>();
    private static final long serialVersionUID = 2807343040519543363L;

    @Getter @Setter
    private TenantId tenantId;
    @Getter @Setter
    private String name;
    @Getter @Setter
    private String type;
    @Getter @Setter
    private String username;
    @Getter @Setter
    private String password;
    @Getter @Setter
    private boolean isRtspAuth;
    @Getter @Setter
    private String primaryUrlPath;
    @Getter @Setter
    private String secondaryUrlPath;
    @Getter @Setter
    private boolean isPtz = false;
    @Getter @Setter
    private String host;
    @Getter @Setter
    private int port;
    @Getter @Setter
    private String brand;
    @Getter @Setter
    private String model;
    @Getter @Setter
    private String channelNo;

    public Camera() {
        super();
    }

    public Camera(CameraId id) {
        super(id);
    }

    public Camera(Camera camera) {
        super(camera);
        this.tenantId = camera.tenantId;
        setAssignedCustomers(camera.getAssignedCustomers());
        this.name = camera.name;
        this.type = camera.type;
    }

    @Override
    public String getSearchText() {
        return getName();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Camera [tenantId=");
        builder.append(tenantId);
        builder.append(", name=");
        builder.append(name);
        builder.append(", type=");
        builder.append(type);
        builder.append(", ip=");
        builder.append(host);
        builder.append(", port=");
        builder.append(port);
        builder.append(", primaryUrl=");
        builder.append(primaryUrlPath);
        builder.append(", secondaryUrl=");
        builder.append(secondaryUrlPath);
        builder.append(", isPtz=");
        builder.append(isPtz);
        builder.append(", isRtspAuth=");
        builder.append(isRtspAuth);
        builder.append(", username=");
        builder.append(username);
        builder.append(", password=");
        builder.append(password);
        builder.append(", brand=");
        builder.append(brand);
        builder.append(", model=");
        builder.append(model);
        builder.append(", channelNo=");
        builder.append(channelNo);
        builder.append(", additionalInfo=");
        builder.append(getAdditionalInfo());
        builder.append(", createdTime=");
        builder.append(createdTime);
        builder.append(", id=");
        builder.append(id);
        builder.append("]");
        return builder.toString();
    }
}
