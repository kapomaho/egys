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
package org.thingsboard.server.controller;

import com.google.common.util.concurrent.ListenableFuture;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.thingsboard.server.common.data.*;
import org.thingsboard.server.common.data.audit.ActionType;
import org.thingsboard.server.common.data.exception.ThingsboardException;
import org.thingsboard.server.common.data.id.CameraId;
import org.thingsboard.server.common.data.id.CustomerId;
import org.thingsboard.server.common.data.id.NvrId;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.page.TextPageData;
import org.thingsboard.server.common.data.page.TextPageLink;
import org.thingsboard.server.dao.camera.CameraSearchQuery;
import org.thingsboard.server.dao.exception.IncorrectParameterException;
import org.thingsboard.server.service.security.model.SecurityUser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class CameraController extends BaseController {

    public static final String CAMERA_ID = "cameraId";
    public static final String NVR_ID = "nvrId";

    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN', 'CUSTOMER_USER')")
    @RequestMapping(value = "/camera/{cameraId}", method = RequestMethod.GET)
    @ResponseBody
    public Camera getCameraById(@PathVariable(CAMERA_ID) String strCameraId) throws ThingsboardException {
        checkParameter(CAMERA_ID, strCameraId);
        try {
            CameraId cameraId = new CameraId(toUUID(strCameraId));
            return checkCameraId(cameraId);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN', 'CUSTOMER_USER')")
    @RequestMapping(value = "/cameras", params = {"cameraIds"}, method = RequestMethod.GET)
    @ResponseBody
    public List<Camera> getCamerasByIds(@RequestParam("cameraIds") String[] cameraIds) throws ThingsboardException {
        checkArrayParameter("cameraIds", cameraIds);
        try {
            SecurityUser user = getCurrentUser();
            TenantId tenantId = user.getTenantId();
            CustomerId customerId = user.getCustomerId();
            List<CameraId> cids = new ArrayList<>();
            for (String cameraId : cameraIds) {
                CameraId cid = new CameraId(toUUID(cameraId));
                checkCameraId(cid);
                cids.add(cid);
            }

            ListenableFuture<List<Camera>> cameras;
            if (customerId == null || customerId.isNullUid()) {
                cameras = cameraService.findCamerasByTenantIdAndIdsAsync(tenantId, cids);
            } else {
                cameras = cameraService.findCamerasByTenantIdCustomerIdAndIdsAsync(tenantId, customerId, cids);
            }
            return checkNotNull(cameras.get());
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN', 'CUSTOMER_USER')")
    @RequestMapping(value = "nvr/{nvrId}/cameras", method = RequestMethod.GET)
    @ResponseBody
    public List<Camera> getCamerasByNvrId(@PathVariable(NVR_ID) String strNvrId) throws ThingsboardException {
        checkParameter(NVR_ID, strNvrId);
        try {
            NvrId nvrId = new NvrId(toUUID(strNvrId));
            Nvr nvr = checkNvrId(nvrId);
            List<Camera> cameras = cameraService.findCamerasByNvrId(nvr.getId());
            return cameras;
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN', 'CUSTOMER_USER')")
    @RequestMapping(value = "/camera", method = RequestMethod.POST)
    @ResponseBody
    public Camera saveCamera(@RequestBody Camera camera) throws ThingsboardException {
        try {
            camera.setTenantId(getCurrentUser().getTenantId());
            /*if (getCurrentUser().getAuthority() == Authority.CUSTOMER_USER) {
                if (camera.getId() == null || camera.getId().isNullUid() || camera.getAssignedCustomers().isEmpty()) {
                    throw new ThingsboardException("You do not have permission to perform this operation!", ThingsboardErrorCode.PERMISSION_DENIED);
                } else {
                    for (ShortCustomerInfo customerInfo : camera.getAssignedCustomers()) {
                        checkCustomerId(customerInfo.getCustomerId());
                    }
                }
            }*/
            Camera saveCamera = cameraService.saveCamera(camera);

            logEntityAction(saveCamera.getId(), saveCamera,
                    null,
                    camera.getId() == null ? ActionType.ADDED : ActionType.UPDATED, null);

            return checkNotNull(saveCamera);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN', 'CUSTOMER_USER')")
    @RequestMapping(value = "/camera/{cameraId}", method = RequestMethod.DELETE)
    @ResponseStatus(value = HttpStatus.OK)
    public void deleteCamera(@PathVariable(CAMERA_ID) String cameraId) throws ThingsboardException {
        checkParameter(CAMERA_ID, cameraId);
        try {
            CameraId cid = new CameraId(toUUID(cameraId));
            Camera camera = checkCameraId(cid);
            cameraService.deleteCamera(cid);

            logEntityAction(cid, camera,
                    null,
                    ActionType.DELETED, null, cameraId);
        } catch (Exception e) {
            logEntityAction(emptyId(EntityType.CAMERA),
                    null,
                    null,
                    ActionType.DELETED, e, cameraId);
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/tenant/cameras", params = {"limit"}, method = RequestMethod.GET)
    @ResponseBody
    public TextPageData<Camera> getTenantCameras(
            @RequestParam int limit,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String textSearch,
            @RequestParam(required = false) String idOffset,
            @RequestParam(required = false) String textOffset) throws ThingsboardException {
        try {
            TenantId tenantId = getCurrentUser().getTenantId();
            TextPageLink pageLink = createPageLink(limit, textSearch, idOffset, textOffset);
            if (type != null && type.trim().length() > 0) {
                return checkNotNull(cameraService.findCamerasByTenantIdAndType(tenantId, type, pageLink));
            } else {
                return checkNotNull(cameraService.findCamerasByTenantId(tenantId, pageLink));
            }
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN', 'CUSTOMER_USER')")
    @RequestMapping(value = "/tenant/cameras", params = {"host"}, method = RequestMethod.GET)
    @ResponseBody
    public Camera getTenantCameraByHost(@RequestParam String host) throws ThingsboardException {
        try {
            TenantId tenantId = getCurrentUser().getTenantId();
            return checkNotNull(cameraService.findCameraByTenantIdAndHost(tenantId, host));
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN', 'CUSTOMER_USER')")
    @RequestMapping(value = "/tenant/cameras", params = {"name"}, method = RequestMethod.GET)
    @ResponseBody
    public Camera getTenantCameraByName(@RequestParam String name) throws ThingsboardException {
        try {
            TenantId tenantId = getCurrentUser().getTenantId();
            return checkNotNull(cameraService.findCameraByTenantIdAndName(tenantId, name));
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN', 'CUSTOMER_USER')")
    @RequestMapping(value = "/camera/types", method = RequestMethod.GET)
    @ResponseBody
    public List<EntitySubtype> getCameraTypes() throws ThingsboardException {
        try {
            SecurityUser currentUser = getCurrentUser();
            TenantId tenantId = currentUser.getTenantId();
            ListenableFuture<List<EntitySubtype>> types = cameraService.findCameraTypesByTenantId(tenantId);
            return checkNotNull(types.get());
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN', 'CUSTOMER_USER')")
    @RequestMapping(value = "/customer/{customerId}/camera/{cameraId}", method = RequestMethod.POST)
    @ResponseBody
    public Camera assignCameraToCustomer(@PathVariable("customerId") String customerId,
                                         @PathVariable(CAMERA_ID) String cameraId) throws ThingsboardException {
        checkParameter("customerId", customerId);
        checkParameter(CAMERA_ID, cameraId);

        try {
            CustomerId custid = new CustomerId(toUUID(customerId));
            Customer customer = checkCustomerId(custid);

            CameraId cid = new CameraId(toUUID(cameraId));
            checkCameraId(cid);

            Camera savedCamera = checkNotNull(cameraService.assignCameraToCustomer(cid, custid));
            logEntityAction(cid, savedCamera,
                    custid,
                    ActionType.ASSIGNED_TO_CUSTOMER, null, cameraId, customerId, customer.getName());
            return savedCamera;
        } catch (Exception e) {
            logEntityAction(emptyId(EntityType.CAMERA), null,
                    null,
                    ActionType.ASSIGNED_TO_CUSTOMER, e, cameraId, customerId);
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/customer/{customerId}/camera/{cameraId}", method = RequestMethod.DELETE)
    @ResponseBody
    public Camera unassignCameraFromCustomer(@PathVariable("customerId") String customerId,
                                             @PathVariable(CAMERA_ID) String cameraId) throws ThingsboardException {
        checkParameter(CAMERA_ID, cameraId);
        try {
            CustomerId custid = new CustomerId(toUUID(customerId));
            Customer customer = checkCustomerId(custid);

            CameraId id = new CameraId(toUUID(cameraId));
            Camera camera = checkCameraId(id);

            if (camera.getAssignedCustomers().isEmpty()) {
                throw new IncorrectParameterException("Camera is not assigned to any customer");
            }
            Camera savedCamera = checkNotNull(cameraService.unassignCameraFromCustomer(id, custid));

            logEntityAction(id, savedCamera,
                    custid,
                    ActionType.ASSIGNED_TO_CUSTOMER, null, cameraId, customerId, customer.getName());

            return savedCamera;
        } catch (Exception e) {

            logEntityAction(emptyId(EntityType.CAMERA), null,
                    null,
                    ActionType.ASSIGNED_TO_CUSTOMER, e, cameraId, customerId);

            throw handleException(e);
        }
    }

    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN', 'CUSTOMER_USER')")
    @RequestMapping(value = "/camera/{cameraId}/customers", method = RequestMethod.POST)
    @ResponseBody
    public Camera updateCameraCustomers(@PathVariable(CAMERA_ID) String strCameraId,
                                      @RequestBody String[] strCustomerIds) throws ThingsboardException {
        checkParameter(CAMERA_ID, strCameraId);
        try {
            CameraId cameraId = new CameraId(toUUID(strCameraId));
            Camera camera = checkCameraId(cameraId);

            Set<CustomerId> customerIds = new HashSet<>();
            if (strCustomerIds != null) {
                for (String strCustomerId : strCustomerIds) {
                    customerIds.add(new CustomerId(toUUID(strCustomerId)));
                }
            }

            Set<CustomerId> addedCustomerIds = new HashSet<>();
            Set<CustomerId> removedCustomerIds = new HashSet<>();
            for (CustomerId customerId : customerIds) {
                if (!camera.isAssignedToCustomer(customerId)) {
                    addedCustomerIds.add(customerId);
                }
            }

            Set<ShortCustomerInfo> assignedCustomers = camera.getAssignedCustomers();
            if (assignedCustomers != null) {
                for (ShortCustomerInfo customerInfo : assignedCustomers) {
                    if (!customerIds.contains(customerInfo.getCustomerId())) {
                        removedCustomerIds.add(customerInfo.getCustomerId());
                    }
                }
            }

            if (addedCustomerIds.isEmpty() && removedCustomerIds.isEmpty()) {
                return camera;
            } else {
                Camera savedCamera = null;
                for (CustomerId customerId : addedCustomerIds) {
                    savedCamera = checkNotNull(cameraService.assignCameraToCustomer(cameraId, customerId));
                    ShortCustomerInfo customerInfo = savedCamera.getAssignedCustomerInfo(customerId);
                    logEntityAction(cameraId, savedCamera,
                            customerId,
                            ActionType.ASSIGNED_TO_CUSTOMER, null, strCameraId, customerId.toString(), customerInfo.getTitle());
                }
                for (CustomerId customerId : removedCustomerIds) {
                    ShortCustomerInfo customerInfo = camera.getAssignedCustomerInfo(customerId);
                    savedCamera = checkNotNull(cameraService.unassignCameraFromCustomer(cameraId, customerId));
                    logEntityAction(cameraId, camera,
                            customerId,
                            ActionType.UNASSIGNED_FROM_CUSTOMER, null, strCameraId, customerId.toString(), customerInfo.getTitle());

                }
                return savedCamera;
            }
        } catch (Exception e) {

            logEntityAction(emptyId(EntityType.CAMERA), null,
                    null,
                    ActionType.ASSIGNED_TO_CUSTOMER, e, strCameraId);

            throw handleException(e);
        }
    }

    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN', 'CUSTOMER_USER')")
    @RequestMapping(value = "/camera/{cameraId}/customers/add", method = RequestMethod.POST)
    @ResponseBody
    public Camera addCameraCustomers(@PathVariable(CAMERA_ID) String strCameraId,
                                   @RequestBody String[] strCustomerIds) throws ThingsboardException {
        checkParameter(CAMERA_ID, strCameraId);
        try {
            CameraId cameraId = new CameraId(toUUID(strCameraId));
            Camera camera = checkCameraId(cameraId);

            Set<CustomerId> customerIds = getCustomerIds(strCustomerIds, camera);

            if (customerIds.isEmpty()) {
                return camera;
            } else {
                Camera savedCamera = null;
                for (CustomerId customerId : customerIds) {
                    savedCamera = checkNotNull(cameraService.assignCameraToCustomer(cameraId, customerId));
                    ShortCustomerInfo customerInfo = savedCamera.getAssignedCustomerInfo(customerId);
                    logEntityAction(cameraId, savedCamera,
                            customerId,
                            ActionType.ASSIGNED_TO_CUSTOMER, null, strCameraId, customerId.toString(), customerInfo.getTitle());
                }
                return savedCamera;
            }
        } catch (Exception e) {

            logEntityAction(emptyId(EntityType.CAMERA), null,
                    null,
                    ActionType.ASSIGNED_TO_CUSTOMER, e, strCameraId);

            throw handleException(e);
        }
    }

    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN', 'CUSTOMER_USER')")
    @RequestMapping(value = "/camera/{cameraId}/customers/remove", method = RequestMethod.POST)
    @ResponseBody
    public Camera removeCameraCustomers(@PathVariable(CAMERA_ID) String strCameraId,
                                      @RequestBody String[] strCustomerIds) throws ThingsboardException {
        checkParameter(CAMERA_ID, strCameraId);
        try {
            CameraId cameraId = new CameraId(toUUID(strCameraId));
            Camera camera = checkCameraId(cameraId);

            Set<CustomerId> customerIds = getCustomerIds(strCustomerIds, camera);

            if (customerIds.isEmpty()) {
                return camera;
            } else {
                Camera savedCamera = null;
                for (CustomerId customerId : customerIds) {
                    ShortCustomerInfo customerInfo = camera.getAssignedCustomerInfo(customerId);
                    savedCamera = checkNotNull(cameraService.unassignCameraFromCustomer(cameraId, customerId));
                    logEntityAction(cameraId, camera,
                            customerId,
                            ActionType.UNASSIGNED_FROM_CUSTOMER, null, strCameraId, customerId.toString(), customerInfo.getTitle());

                }
                return savedCamera;
            }
        } catch (Exception e) {

            logEntityAction(emptyId(EntityType.CAMERA), null,
                    null,
                    ActionType.UNASSIGNED_FROM_CUSTOMER, e, strCameraId);

            throw handleException(e);
        }
    }

    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN', 'CUSTOMER_USER')")
    @RequestMapping(value = "/customer/{customerId}/cameras", params = {"limit"}, method = RequestMethod.GET)
    @ResponseBody
    public TextPageData<Camera> getCustomerCameras(
            @PathVariable("customerId") String customerId,
            @RequestParam int limit,
            @RequestParam(required = false) String textSearch,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String idOffset,
            @RequestParam(required = false) String textOffset) throws ThingsboardException {
        checkParameter("customerId", customerId);
        try {
            TenantId tenantId = getCurrentUser().getTenantId();
            CustomerId cid = new CustomerId(toUUID(customerId));
            checkCustomerId(cid);

            TextPageLink pageLink = createPageLink(limit, textSearch, idOffset, textOffset);
            if (type != null && type.trim().length() > 0) {
                return checkNotNull(cameraService.findCamerasByTenantIdAndCustomerIdAndType(tenantId, cid, type, pageLink));
            } else {
                return checkNotNull(cameraService.findCamerasByTenantIdAndCustomerId(tenantId, cid, pageLink));
            }

        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize(("hasAnyAuthority('TENANT_ADMIN', 'CUSTOMER_USER')"))
    @RequestMapping(value = "/cameras", method = RequestMethod.POST)
    @ResponseBody
    public List<Camera> findByQuery(@RequestBody CameraSearchQuery query) throws ThingsboardException {
        checkNotNull(query);
        checkNotNull(query.getCameraTypes());
        checkNotNull(query.getParameters());
        checkEntityId(query.getParameters().getEntityId());
        try {

            List<Camera> cameras = checkNotNull(cameraService.findCamerasByQuery(query)).get();
            cameras = cameras.stream().filter(camera -> {
               try {
                   checkCamera(camera);
                   return true;
               } catch (ThingsboardException e) {
                   return false;
               }
            }).collect(Collectors.toList());
            return cameras;
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/customer/public/camera/{cameraId}", method = RequestMethod.POST)
    @ResponseBody
    public Camera assignCameraToPublicCustomer(@PathVariable(CAMERA_ID) String cameraId) throws ThingsboardException {
        checkParameter(CAMERA_ID, cameraId);
        try {
            CameraId cid = new CameraId(toUUID(cameraId));
            Camera camera = checkCameraId(cid);
            Customer publicCustomer = customerService.findOrCreatePublicCustomer(camera.getTenantId());
            return checkNotNull(cameraService.assignCameraToCustomer(cid, publicCustomer.getId()));
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/customer/public/camera/{cameraId}", method = RequestMethod.DELETE)
    @ResponseBody
    public Camera unassignCameraFromPublicCustomer(@PathVariable(CAMERA_ID) String strCameraId) throws ThingsboardException {
        checkParameter(CAMERA_ID, strCameraId);
        try {
            CameraId cameraId = new CameraId(toUUID(strCameraId));
            Camera camera = checkCameraId(cameraId);
            Customer publicCustomer = customerService.findOrCreatePublicCustomer(camera.getTenantId());

            Camera savedCamera = checkNotNull(cameraService.unassignCameraFromCustomer(cameraId, publicCustomer.getId()));

            logEntityAction(cameraId, camera,
                    publicCustomer.getId(),
                    ActionType.UNASSIGNED_FROM_CUSTOMER, null, strCameraId, publicCustomer.getId().toString(), publicCustomer.getName());

            return savedCamera;
        } catch (Exception e) {

            logEntityAction(emptyId(EntityType.CAMERA), null,
                    null,
                    ActionType.UNASSIGNED_FROM_CUSTOMER, e, strCameraId);

            throw handleException(e);
        }
    }
}
