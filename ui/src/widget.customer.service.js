/*
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
const getCustomersUrl = '/api/customers';
const saveCustomerUrl = '/api/customer';
const deleteCustomerUrl = '/api/customer/{customerId}';
const assignNvrToCustomerUrl = '/api/customer/{customerId}/nvr/{nvrId}';
const updateNvrCustomersUrl = '/api/nvr/{nvrId}/customers';
const assignCameraToCustomerUrl = '/api/customer/{customerId}/camera/{cameraId}';
const updateCameraCustomersUrl = '/api/camera/{cameraId}/customers';

class CustomerApi extends BaseApi {
    constructor(http, q) {
        super(http, q);
    }

    getCustomers(pageLink) {
        var deferred =  this.q.defer();
        var url = getCustomersUrl;
        url = UrlUtils.insertPageLink(url, pageLink);
        
        this.http.get(url).then(
            function success(response) {
                deferred.resolve(response.data);
            },
            function fail() {
                deferred.reject();
            }
        );
        return deferred.promise;
    }

    deleteCustomer(customerId) {
        var deferred = this.q.defer();
        var url = deleteCustomerUrl;
        url = UrlUtils.insertCustomerId(url, customerId);
        this.http.delete(url).then(
            function success(response) {
                deferred.resolve(response.data);
            },
            function fail() {
                deferred.reject();
            }
        );
        return deferred.promise;
    }

    saveCustomer(customer) {
        var deferred = this.q.defer();
        this.http.post(saveCustomerUrl, customer).then(
            function success(response) {
                deferred.resolve(response.data);
            },
            function fail() {
                deferred.reject();
            }
        );
        return deferred.promise;
    }

    assignNvrToCustomer(nvrId, customerId) {
        var url = assignNvrToCustomerUrl;
        url = UrlUtils.insertCustomerId(url, customerId);
        url = UrlUtils.insertNvrId(url, nvrId);
        var deferred = this.q.defer();
        this.http.post(url, {}).then(function success(response) {
            deferred.resolve(response.data);
        }, function fail() {
            deferred.reject();
        });
        return deferred.promise;
    }

    updateNvrCustomers(nvrId, assignedCustomers) {
        var deferred = this.q.defer();
        var url = updateNvrCustomersUrl;
        url = UrlUtils.insertNvrId(url, nvrId);
        
        this.http.post(url, assignedCustomers).then(
            function success(response) {
                deferred.resolve(response.data);
            }, function fail() {
                deferred.reject();
            }
        );
        return deferred.promise;
    }

    assignCameraToCustomer(camerarId, customerId) {
        var url = assignCameraToCustomerUrl;
        url = UrlUtils.insertCustomerId(url, customerId);
        url = UrlUtils.insertCameraId(url, camerarId);
        var deferred = this.q.defer();
        this.http.post(url, {}).then(function success(response) {
            deferred.resolve(response.data);
        }, function fail() {
            deferred.reject();
        });
        return deferred.promise;
    }

    updateCameraCustomers(cameraId, assignedCustomers) {
        var deferred = this.q.defer();
        var url = updateCameraCustomersUrl;
        url = UrlUtils.insertCameraId(url, cameraId);
        
        this.http.post(url, assignedCustomers).then(
            function success(response) {
                deferred.resolve(response.data);
            }, function fail() {
                deferred.reject();
            }
        );
        return deferred.promise;
    }
}

class CustomerService {
    constructor(http, q) {
        this.api = new CustomerApi(http, q);
    }

    getCustomers(pageLink, successCallback, failureCallback) {
        this.api.getCustomers(pageLink).then(
            successCallback,
            failureCallback
        );
    }

    deleteCustomer(customer, successCallback, failureCallback) {
        this.api.deleteCustomer(customer.id.id).then(
            function success(data) {
                NotificationUtils.notifyEvent(EntityType.CUSTOMER, EventType.DELETE);
                successCallback(data);
            },
            failureCallback
        );
    }

    saveCustomer(customer, successCallback, failureCallback) {
        this.api.saveCustomer(customer).then(
            function success(data) {
                let eventType = (customer.id) ? EventType.UPDATE : EventType.CREATE;
                NotificationUtils.notifyEvent(EntityType.CUSTOMER, eventType);
                successCallback(data);
            },
            failureCallback
        );
    }

    assignNvrToCustomer(nvrId, customerId, successCallback, failureCallback) {
        this.api.assignNvrToCustomer(nvrId, customerId).then(
            function success(data) {
                NotificationUtils.notifyEvent(EntityType.NVR, EventType.UPDATE);
                successCallback(data);
            },
            failureCallback
        );
    }

    updateNvrCustomers(nvrId, assignedCustomers, successCallback, failureCallback) {
        this.api.updateNvrCustomers(nvrId, assignedCustomers).then(
            function success(data) {
                NotificationUtils.notifyEvent(EntityType.NVR, EventType.UPDATE);
                successCallback(data);
            },
            failureCallback
        );
    }

    assignCameraToCustomer(camerarId, customerId, successCallback, failureCallback) {
        this.api.assignCameraToCustomer(camerarId, customerId).then(
            function success(data) {
                NotificationUtils.notifyEvent(EntityType.CAMERA, EventType.UPDATE);
                successCallback(data);
            },
            failureCallback
        );
    }

    updateCameraCustomers(cameraId, assignedCustomers, successCallback, failureCallback) {
        this.api.updateCameraCustomers(cameraId, assignedCustomers).then(
            function success(data) {
                NotificationUtils.notifyEvent(EntityType.CAMERA, EventType.UPDATE);
                successCallback(data);
            },
            failureCallback
        );
    }
}