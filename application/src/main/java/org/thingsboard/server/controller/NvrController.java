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
import org.thingsboard.server.dao.exception.IncorrectParameterException;
import org.thingsboard.server.dao.nvr.NvrSearchQuery;
import org.thingsboard.server.service.security.model.SecurityUser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class NvrController extends BaseController {

    public static final String NVR_ID = "nvrId";
    public static final String CAMERA_ID = "cameraId";

    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN', 'CUSTOMER_USER')")
    @RequestMapping(value = "/camera/{cameraId}/nvr", method = RequestMethod.GET)
    @ResponseBody
    public Nvr getNvrByCameraId(@PathVariable(CAMERA_ID) String strCameraId) throws ThingsboardException {
        checkParameter(CAMERA_ID, strCameraId);
        try {
            CameraId cameraId = new CameraId(toUUID(strCameraId));
            return nvrService.findNvrByCameraId(cameraId);
        } catch ( Exception e) {
            throw handleException(e);
        }
    }


    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN', 'CUSTOMER_USER')")
    @RequestMapping(value = "/nvr/{nvrId}", method = RequestMethod.GET)
    @ResponseBody
    public Nvr getNvrById(@PathVariable(NVR_ID) String strNvrId) throws ThingsboardException {
        checkParameter(NVR_ID, strNvrId);
        try {
            NvrId nvrId = new NvrId(toUUID(strNvrId));
            return checkNvrId(nvrId);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN', 'CUSTOMER_USER')")
    @RequestMapping(value = "/nvr", method = RequestMethod.POST)
    @ResponseBody
    public Nvr saveNvr(@RequestBody Nvr nvr) throws ThingsboardException {
        try {
            nvr.setTenantId(getCurrentUser().getTenantId());
            /*if (getCurrentUser().getAuthority() == Authority.CUSTOMER_USER) {
                if (nvr.getId() == null || nvr.getId().isNullUid() || nvr.getAssignedCustomers().isEmpty()) {
                    throw new ThingsboardException("You don't have permission to create new nvr!",
                            ThingsboardErrorCode.PERMISSION_DENIED);
                } else {
                    for (ShortCustomerInfo customerInfo : nvr.getAssignedCustomers()) {
                        checkCustomerId(customerInfo.getCustomerId());
                    }
                }
            }*/
            Nvr savedNvr = checkNotNull(nvrService.saveNvr(nvr));

            logEntityAction(savedNvr.getId(), savedNvr, null,
                    nvr.getId() == null ? ActionType.ADDED : ActionType.UPDATED, null);

            return savedNvr;
        } catch (Exception e) {
            logEntityAction(emptyId(EntityType.NVR), nvr, null,
                    nvr.getId() == null ? ActionType.ADDED : ActionType.UPDATED, e);
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN', 'CUSTOMER_USER')")
    @RequestMapping(value = "/nvr/{nvrId}", method = RequestMethod.DELETE)
    @ResponseStatus(value = HttpStatus.OK)
    public void deleteNvr(@PathVariable(NVR_ID) String strNvrId) throws ThingsboardException {
        checkParameter(NVR_ID, strNvrId);
        try {
            NvrId nvrId = new NvrId(toUUID(strNvrId));
            Nvr nvr = checkNvrId(nvrId);
            nvrService.deleteNvr(nvrId);

            logEntityAction(nvrId, nvr, null,
                    ActionType.DELETED, null, strNvrId);
        } catch (Exception e) {
            logEntityAction(emptyId(EntityType.NVR), null, null,
                    ActionType.DELETED, e, strNvrId);
            throw handleException(e);
        }
    }


    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN', 'CUSTOMER_USER')")
    @RequestMapping(value = "/customer/{customerId}/nvr/{nvrId}", method = RequestMethod.POST)
    @ResponseBody
    public Nvr assignNvrToCustomer(@PathVariable("customerId") String strCustomerId,
                                   @PathVariable(NVR_ID) String strNvrId) throws ThingsboardException {
        checkParameter("customerId", strCustomerId);
        checkParameter(NVR_ID, strNvrId);

        try {
            CustomerId customerId = new CustomerId(toUUID(strCustomerId));
            Customer customer = checkCustomerId(customerId);

            NvrId nvrId = new NvrId(toUUID(strNvrId));
            checkNvrId(nvrId);

            Nvr savedNvr = checkNotNull(nvrService.assignNvrToCustomer(nvrId, customerId));

            logEntityAction(nvrId, savedNvr, customerId,
                    ActionType.ASSIGNED_TO_CUSTOMER, null, strNvrId, strCustomerId, customer.getName());

            return savedNvr;
        } catch (Exception e) {
            logEntityAction(emptyId(EntityType.NVR), null,
                    null,
                    ActionType.ASSIGNED_TO_CUSTOMER, e, strNvrId, strCustomerId);

            throw handleException(e);
        }

    }

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/customer/{customerId}/nvr/{nvrId}", method = RequestMethod.DELETE)
    @ResponseBody
    public Nvr unassignNvrFromCustomer(@PathVariable("customerId") String strCustomerId,
                                       @PathVariable(NVR_ID) String strNvrId) throws ThingsboardException {
        checkParameter(NVR_ID, strNvrId);
        try {
            CustomerId customerId = new CustomerId(toUUID(strCustomerId));
            Customer customer = checkCustomerId(customerId);

            NvrId nvrId = new NvrId(toUUID(strNvrId));
            Nvr nvr = checkNvrId(nvrId);

            if (nvr.getAssignedCustomers().isEmpty()) {
                throw new IncorrectParameterException("Nvr isn't assigned to any customer!");
            }

            Nvr savedNvr = checkNotNull(nvrService.unassignNvrFromCustomer(nvrId, customerId));

            logEntityAction(nvrId, nvr, customerId,
                    ActionType.UNASSIGNED_FROM_CUSTOMER, null, strNvrId, customer.getId().toString(), customer.getName());

            return savedNvr;
        } catch (Exception e) {

            logEntityAction(emptyId(EntityType.NVR), null,
                    null,
                    ActionType.UNASSIGNED_FROM_CUSTOMER, e, strNvrId);

            throw handleException(e);
        }
    }

    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN', 'CUSTOMER_USER')")
    @RequestMapping(value = "/nvr/{nvrId}/customers", method = RequestMethod.POST)
    @ResponseBody
    public Nvr updateNvrCustomers(@PathVariable(NVR_ID) String strNvrId,
                                      @RequestBody String[] strCustomerIds) throws ThingsboardException {
        checkParameter(NVR_ID, strNvrId);
        try {
            NvrId nvrId = new NvrId(toUUID(strNvrId));
            Nvr nvr = checkNvrId(nvrId);

            Set<CustomerId> customerIds = new HashSet<>();
            if (strCustomerIds != null) {
                for (String strCustomerId : strCustomerIds) {
                    customerIds.add(new CustomerId(toUUID(strCustomerId)));
                }
            }

            Set<CustomerId> addedCustomerIds = new HashSet<>();
            Set<CustomerId> removedCustomerIds = new HashSet<>();
            for (CustomerId customerId : customerIds) {
                if (!nvr.isAssignedToCustomer(customerId)) {
                    addedCustomerIds.add(customerId);
                }
            }

            Set<ShortCustomerInfo> assignedCustomers = nvr.getAssignedCustomers();
            if (assignedCustomers != null) {
                for (ShortCustomerInfo customerInfo : assignedCustomers) {
                    if (!customerIds.contains(customerInfo.getCustomerId())) {
                        removedCustomerIds.add(customerInfo.getCustomerId());
                    }
                }
            }

            if (addedCustomerIds.isEmpty() && removedCustomerIds.isEmpty()) {
                return nvr;
            } else {
                Nvr savedNvr = null;
                for (CustomerId customerId : addedCustomerIds) {
                    savedNvr = checkNotNull(nvrService.assignNvrToCustomer(nvrId, customerId));
                    ShortCustomerInfo customerInfo = savedNvr.getAssignedCustomerInfo(customerId);
                    logEntityAction(nvrId, savedNvr,
                            customerId,
                            ActionType.ASSIGNED_TO_CUSTOMER, null, strNvrId, customerId.toString(), customerInfo.getTitle());
                }
                for (CustomerId customerId : removedCustomerIds) {
                    ShortCustomerInfo customerInfo = nvr.getAssignedCustomerInfo(customerId);
                    savedNvr = checkNotNull(nvrService.unassignNvrFromCustomer(nvrId, customerId));
                    logEntityAction(nvrId, nvr,
                            customerId,
                            ActionType.UNASSIGNED_FROM_CUSTOMER, null, strNvrId, customerId.toString(), customerInfo.getTitle());

                }
                return savedNvr;
            }
        } catch (Exception e) {

            logEntityAction(emptyId(EntityType.NVR), null,
                    null,
                    ActionType.ASSIGNED_TO_CUSTOMER, e, strNvrId);

            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/nvr/{nvrId}/customers/add", method = RequestMethod.POST)
    @ResponseBody
    public Nvr addNvrCustomers(@PathVariable(NVR_ID) String strNvrId,
                                   @RequestBody String[] strCustomerIds) throws ThingsboardException {
        checkParameter(NVR_ID, strNvrId);
        try {
            NvrId nvrId = new NvrId(toUUID(strNvrId));
            Nvr nvr = checkNvrId(nvrId);

            Set<CustomerId> customerIds = getCustomerIds(strCustomerIds, nvr);

            if (customerIds.isEmpty()) {
                return nvr;
            } else {
                Nvr savedNvr = null;
                for (CustomerId customerId : customerIds) {
                    savedNvr = checkNotNull(nvrService.assignNvrToCustomer(nvrId, customerId));
                    ShortCustomerInfo customerInfo = savedNvr.getAssignedCustomerInfo(customerId);
                    logEntityAction(nvrId, savedNvr,
                            customerId,
                            ActionType.ASSIGNED_TO_CUSTOMER, null, strNvrId, customerId.toString(), customerInfo.getTitle());
                }
                return savedNvr;
            }
        } catch (Exception e) {

            logEntityAction(emptyId(EntityType.NVR), null,
                    null,
                    ActionType.ASSIGNED_TO_CUSTOMER, e, strNvrId);

            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/nvr/{nvrId}/customers/remove", method = RequestMethod.POST)
    @ResponseBody
    public Nvr removeNvrCustomers(@PathVariable(NVR_ID) String strNvrId,
                                      @RequestBody String[] strCustomerIds) throws ThingsboardException {
        checkParameter(NVR_ID, strNvrId);
        try {
            NvrId nvrId = new NvrId(toUUID(strNvrId));
            Nvr nvr = checkNvrId(nvrId);

            Set<CustomerId> customerIds = getCustomerIds(strCustomerIds, nvr);

            if (customerIds.isEmpty()) {
                return nvr;
            } else {
                Nvr savedNvr = null;
                for (CustomerId customerId : customerIds) {
                    ShortCustomerInfo customerInfo = nvr.getAssignedCustomerInfo(customerId);
                    savedNvr = checkNotNull(nvrService.unassignNvrFromCustomer(nvrId, customerId));
                    logEntityAction(nvrId, nvr,
                            customerId,
                            ActionType.UNASSIGNED_FROM_CUSTOMER, null, strNvrId, customerId.toString(), customerInfo.getTitle());

                }
                return savedNvr;
            }
        } catch (Exception e) {

            logEntityAction(emptyId(EntityType.NVR), null,
                    null,
                    ActionType.UNASSIGNED_FROM_CUSTOMER, e, strNvrId);

            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/customer/public/nvr/{nvrId}", method = RequestMethod.POST)
    @ResponseBody
    public Nvr assignNvrToPublicCustomer(@PathVariable(NVR_ID) String strNvrId) throws ThingsboardException {
        checkParameter(NVR_ID, strNvrId);
        try {
            NvrId nvrId = new NvrId(toUUID(strNvrId));
            Nvr nvr = checkNvrId(nvrId);
            Customer publicCustomer = customerService.findOrCreatePublicCustomer(nvr.getTenantId());
            Nvr savedNvr = checkNotNull(nvrService.assignNvrToCustomer(nvrId, publicCustomer.getId()));

            logEntityAction(nvrId, savedNvr, null, ActionType.ASSIGNED_TO_CUSTOMER,
                    null, strNvrId, publicCustomer.getId().toString(), publicCustomer.getName());

            return savedNvr;
        } catch (Exception e) {
            logEntityAction(emptyId(EntityType.NVR), null, null,
                    ActionType.ASSIGNED_TO_CUSTOMER, e, strNvrId);
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/customer/public/nvr/{nvrId}", method = RequestMethod.DELETE)
    @ResponseBody
    public Nvr unassignNvrFromPublicCustomer(@PathVariable(NVR_ID) String strNvrId) throws ThingsboardException {
        checkParameter(NVR_ID, strNvrId);
        try {
            NvrId nvrId = new NvrId(toUUID(strNvrId));
            Nvr nvr = checkNvrId(nvrId);
            Customer publicCustomer = customerService.findOrCreatePublicCustomer(nvr.getTenantId());

            Nvr savedNvr = checkNotNull(nvrService.unassignNvrFromCustomer(nvrId, publicCustomer.getId()));

            logEntityAction(nvrId, nvr,
                    publicCustomer.getId(),
                    ActionType.UNASSIGNED_FROM_CUSTOMER, null, strNvrId, publicCustomer.getId().toString(), publicCustomer.getName());

            return savedNvr;
        } catch (Exception e) {

            logEntityAction(emptyId(EntityType.NVR), null,
                    null,
                    ActionType.UNASSIGNED_FROM_CUSTOMER, e, strNvrId);

            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/tenant/nvrs", params = {"limit"}, method = RequestMethod.GET)
    @ResponseBody
    public TextPageData<Nvr> getTenantNvrs(
            @RequestParam int limit,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String textSearch,
            @RequestParam(required = false) String idOffset,
            @RequestParam(required = false) String textOffset) throws ThingsboardException {

        try {
            TenantId tenantId = getCurrentUser().getTenantId();
            TextPageLink textPageLink = createPageLink(limit, textSearch, idOffset, textOffset);
            if (type != null && type.trim().length() > 0) {
                return checkNotNull(nvrService.findNvrsByTenantIdAndType(tenantId, type, textPageLink));
            } else {
                return checkNotNull(nvrService.findNvrsByTenantId(tenantId, textPageLink));
            }
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/tenant/nvrs", params = {"name"}, method = RequestMethod.GET)
    @ResponseBody
    public Nvr getTenantNvrByName(
            @RequestParam String name) throws ThingsboardException {
        try {
            TenantId tenantId = getCurrentUser().getTenantId();
            return checkNotNull(nvrService.findNvrByTenantIdAndName(tenantId, name));
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/tenant/nvrs", params = {"host"}, method = RequestMethod.GET)
    @ResponseBody
    public Nvr getTenantNvrByHost(
            @RequestParam String host) throws ThingsboardException {
        try {
            TenantId tenantId = getCurrentUser().getTenantId();
            return checkNotNull(nvrService.findNvrByTenantIdAndHost(tenantId, host));
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN', 'CUSTOMER_USER')")
    @RequestMapping(value = "/customer/{customerId}/nvrs", params = {"limit"}, method = RequestMethod.GET)
    @ResponseBody
    public TextPageData<Nvr> getCustomerNvrs(
            @PathVariable("customerId") String strCustomerId,
            @RequestParam int limit,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String textSearch,
            @RequestParam(required = false) String idOffset,
            @RequestParam(required = false) String textOffset) throws ThingsboardException {
        checkParameter("customerId", strCustomerId);
        try {
            TenantId tenantId = getCurrentUser().getTenantId();
            CustomerId customerId = new CustomerId(toUUID(strCustomerId));
            checkCustomerId(customerId);
            TextPageLink pageLink = createPageLink(limit, textSearch, idOffset, textOffset);
            if (type != null && type.trim().length() > 0) {
                return checkNotNull(nvrService.findNvrsByTenantIdAndCustomerIdAndType(tenantId, customerId, type, pageLink));
            } else {
                return checkNotNull(nvrService.findNvrsByTenantIdAndCustomerId(tenantId, customerId, pageLink));
            }
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN', 'CUSTOMER_USER')")
    @RequestMapping(value = "/nvrs", params = {"nvrIds"}, method = RequestMethod.GET)
    @ResponseBody
    public List<Nvr> getNvrsByIds(
            @RequestParam("nvrIds") String[] strNvrIds) throws ThingsboardException {
        checkArrayParameter("nvrIds", strNvrIds);
        try {
            SecurityUser user = getCurrentUser();
            TenantId tenantId = user.getTenantId();
            CustomerId customerId = user.getCustomerId();
            List<NvrId> nvrIds = new ArrayList<>();
            for (String strNvrId : strNvrIds) {
                NvrId nvrId = new NvrId(toUUID(strNvrId));
                checkNvrId(nvrId);
                nvrIds.add(nvrId);
            }
            ListenableFuture<List<Nvr>> nvrs;
            if (customerId == null || customerId.isNullUid()) {
                nvrs = nvrService.findNvrsByTenantIdAndIdsAsync(tenantId, nvrIds);
            } else {
                nvrs = nvrService.findNvrsByTenantIdCustomerIdAndIdsAsync(tenantId, customerId, nvrIds);
            }
            return checkNotNull(nvrs.get());

        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN', 'CUSTOMER_USER')")
    @RequestMapping(value = "/nvrs", method = RequestMethod.POST)
    @ResponseBody
    public List<Nvr> findByQuery(@RequestBody NvrSearchQuery query) throws ThingsboardException {
        checkNotNull(query);
        checkNotNull(query.getParameters());
        checkNotNull(query.getNvrTypes());
        checkEntityId(query.getParameters().getEntityId());

        try {
            List<Nvr> nvrs = checkNotNull(nvrService.findNvrsByQuery(query).get());
            nvrs = nvrs.stream().filter(nvr -> {
                try {
                    checkNvr(nvr);
                    return true;
                } catch (ThingsboardException e) {
                    return false;
                }
            }).collect(Collectors.toList());
            return nvrs;
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN', 'CUSTOMER_USER')")
    @RequestMapping(value = "/nvr/types", method = RequestMethod.GET)
    @ResponseBody
    public List<EntitySubtype> getNvrTypes() throws ThingsboardException {
        try {
            SecurityUser user = getCurrentUser();
            TenantId tenantId = user.getTenantId();
            ListenableFuture<List<EntitySubtype>> nvrTypes = nvrService.findNvrTypesByTenantId(tenantId);
            return checkNotNull(nvrTypes.get());
        } catch (Exception e) {
            throw handleException(e);
        }
    }

}







































