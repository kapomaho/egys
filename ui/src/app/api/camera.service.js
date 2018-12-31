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
import thingsboardTypes from '../common/types.constant';

export default angular.module('thingsboard.api.camera', [thingsboardTypes])
    .factory('cameraService', CameraService)
    .name;

function CameraService($http, $q, userService) {

    var service = {
        saveCamera: saveCamera,
        deleteCamera: deleteCamera,
        getTenantCameras: getTenantCameras,
        getCamera: getCamera,
        getCameras: getCameras,
        getCameraTypes: getCameraTypes,
        assignCameraToCustomer: assignCameraToCustomer,
        unassignCameraFromCustomer: unassignCameraFromCustomer,
        updateCameraCustomers: updateCameraCustomers,
        addCameraCustomers: addCameraCustomers,
        removeCameraCustomers: removeCameraCustomers,
        makeCameraPublic: makeCameraPublic,
        makeCameraPrivate: makeCameraPrivate,
        getCustomerCameras: getCustomerCameras,
        findByQuery: findByQuery,
        fetchCamerasByNameFilter: fetchCamerasByNameFilter,
        getCamerasByNvrId: getCamerasByNvrId
    }

    return service;

    function getTenantCameras(pageLink, config, type) {

        var deferred = $q.defer();
        var url = '/api/tenant/cameras?limit=' + pageLink.limit;

        if (angular.isDefined(pageLink.textSearch)) {
            url += '&textSearch=' + pageLink.textSearch;
        }
        if (angular.isDefined(pageLink.idOffset)) {
            url += '&idOffset=' + pageLink.idOffset;
        }
        if (angular.isDefined(pageLink.textOffset)) {
            url += '&textOffset=' + pageLink.textOffset;
        }
        if (angular.isDefined(type) && type.length) {
            url += '&type=' + type;
        }

        $http.get(url, config).then(
            function success(response) {
                var cameras = prepareCameras(response.data);
                deferred.resolve(cameras);
            },
            function fail() {
                deferred.reject()
            }
        );
        return deferred.promise;
    }

    function getCamera(cameraId, ignoreErrors, config) {
        var deferred = $q.defer();
        var url = '/api/camera/' + cameraId;

        if (!config) {
            config = {};
        }

        config = Object.assign(config, {ignoreErrors: ignoreErrors});

        $http.get(url, config).then(
            function success(response) {
                deferred.resolve(prepareCamera(response.data));
            },
            function fail() {
                deferred.reject();
            }
        );
        return deferred.promise;
    }

    function getCameras(cameraIds, config) {
        var deferred = $q.defer();
        var url = '/api/cameras?cameraIds=';
        var ids = '';
        for (var i = 0; i < cameraIds.length; i++) {
            if (i > 0) ids += ',';
            ids += cameraIds[i];
        }
        url += ids;

        $http.get(url, config).then(
            function success(response) {
                var cameras = prepareCameras(response.data);
                cameras.sort(function (c1, c2) {
                    var id1 = c1.id.id;
                    var id2 = c2.id.id;
                    var index1 = cameraIds.indexOf(id1);
                    var index2 = cameraIds.indexOf(id2);    
                    return index1 - index2;
                });

                deferred.resolve(cameras);
            },
            function fail(response) {
                deferred.reject(response.data);
            }
        );

        return deferred.promise;
    }

    function getCamerasByNvrId(nvrId) {
        var deferred = $q.defer();
        var url = "/api/nvr/" + nvrId + "/cameras";

        $http.get(url).then(
            function success(response) {
                deferred.resolve(response.data);
            },
            function fail(response) {
                deferred.reject(response.data);
            }
        );
        return deferred.promise;
    }

    function saveCamera(camera) {
        var deferred = $q.defer();
        var url = '/api/camera';
        $http.post(url, camera).then(
            function success(response) {
                deferred.resolve(prepareCamera(response.data));
            },
            function fail() {
                deferred.reject();
            }
        );
        return deferred.promise;
    }

    function deleteCamera(cameraId) {
        var deferred = $q.defer();
        var url = '/api/camera/' + cameraId;

        $http.delete(url).then(
            function success() {
                deferred.resolve();
            },
            function fail() {
                deferred.reject();
            }
        );

        return deferred.promise;
    }

    function getCameraTypes() {
        var deferred = $q.defer();
        var url = '/api/camera/types';

        $http.get(url).then(
            function success(response) {
                deferred.resolve(response.data);
            },
            function fail() {
                deferred.reject();
            }
        );

        return deferred.promise;
    }

    function assignCameraToCustomer(customerId, cameraId, ignoreErrors, config) {
        var deferred = $q.defer();
        var url = '/api/customer/' + customerId + '/camera/' + cameraId;
        if (!config) {
            config = {};
        }
        config = Object.assign(config, { ignoreErrors: ignoreErrors });
        $http.post(url, null, config).then(function success(response) {
            deferred.resolve(prepareCamera(response.data));
        }, function fail() {
            deferred.reject();
        });
        return deferred.promise;
    }

    function unassignCameraFromCustomer(cameraId, customerId, ignoreErrors, config) {
        var deferred = $q.defer();
        var url = '/api/customer/' + customerId + '/camera/' + cameraId;
        if (!config) {
            config = {};
        }
        config = Object.assign(config, { ignoreErrors: ignoreErrors });
        $http.delete(url, config).then(function success(response) {
            deferred.resolve(prepareCamera(response.data));
        }, function fail() {
            deferred.reject();
        });
        return deferred.promise;
    }

    function updateCameraCustomers(cameraId, customerIds) {
        var deferred = $q.defer();
        var url = '/api/camera/' + cameraId + '/customers';
        $http.post(url, customerIds).then(function success(response) {
            deferred.resolve(prepareCamera(response.data));
        }, function fail() {
            deferred.reject();
        });
        return deferred.promise;
    }

    function addCameraCustomers(cameraId, customerIds) {
        var deferred = $q.defer();
        var url = '/api/camera/' + cameraId + '/customers/add';
        $http.post(url, customerIds).then(function success(response) {
            deferred.resolve(prepareCamera(response.data));
        }, function fail() {
            deferred.reject();
        });
        return deferred.promise;
    }

    function removeCameraCustomers(cameraId, customerIds) {
        var deferred = $q.defer();
        var url = '/api/camera/' + cameraId + '/customers/remove';
        $http.post(url, customerIds).then(function success(response) {
            deferred.resolve(prepareCamera(response.data));
        }, function fail() {
            deferred.reject();
        });
        return deferred.promise;
    }

    function makeCameraPublic(cameraId, ignoreErrors, config) {
        var deferred = $q.defer();
        var url = '/api/customer/public/camera/' + cameraId;
        if (!config) {
            config = {};
        }
        config = Object.assign(config, { ignoreErrors: ignoreErrors });
        $http.post(url, null, config).then(function success(response) {
            deferred.resolve(prepareCamera(response.data));
        }, function fail() {
            deferred.reject();
        });
        return deferred.promise;
    }

    function makeCameraPrivate(cameraId) {
        var deferred = $q.defer();
        var url = '/api/customer/public/camera/' + cameraId;
        $http.delete(url).then(function success(response) {
            deferred.resolve(prepareCamera(response.data));
        }, function fail() {
            deferred.reject();
        });
        return deferred.promise;
    }

    function getCustomerCameras(customerId, pageLink, config, type) {
        var deferred = $q.defer();
        var url = '/api/customer/' + customerId + '/cameras?limit=' + pageLink.limit;
        if (angular.isDefined(pageLink.textSearch)) {
            url += '&textSearch=' + pageLink.textSearch;
        }
        if (angular.isDefined(pageLink.idOffset)) {
            url += '&idOffset=' + pageLink.idOffset;
        }
        if (angular.isDefined(pageLink.textOffset)) {
            url += '&textOffset=' + pageLink.textOffset;
        }
        if (angular.isDefined(type) && type.length) {
            url += '&type=' + type;
        }
        $http.get(url, config).then(function success(response) {
            var cameras = prepareCameras(response.data);
            deferred.resolve(cameras);
        }, function fail() {
            deferred.reject();
        });

        return deferred.promise;
    }

    function findByQuery(query, ignoreErrors, config) {
        var deferred = $q.defer();
        var url = '/api/cameras';
        if (!config) {
            config = {};
        }
        config = Object.assign(config, { ignoreErrors: ignoreErrors });
        $http.post(url, query, config).then(function success(response) {
            deferred.resolve(prepareCameras(response.data));
        }, function fail() {
            deferred.reject();
        });
        return deferred.promise;
    }

    function fetchCamerasByNameFilter(cameraNameFilter, limit, config) {
        var deferred = $q.defer();
        var user = userService.getCurrentUser();
        var promise;
        var pageLink = {limit: limit, textSearch: cameraNameFilter};
        if (user.authority === 'CUSTOMER_USER') {
            var customerId = user.customerId;
            promise = getCustomerCameras(customerId, pageLink, config);
        } else {
            promise = getTenantCameras(pageLink, config);
        }
        promise.then(
            function success(result) {
                if (result.data && result.data.length > 0) {
                    deferred.resolve(prepareCameras(result.data));
                } else {
                    deferred.resolve(null);
                }
            },
            function fail() {
                deferred.resolve(null);
            }
        );
        return deferred.promise;
    }

    function prepareCameras(camerasData) {
        if (camerasData.data) {
            for (var i = 0; i < camerasData.data.length; i++) {
                camerasData.data[i] = prepareCamera(camerasData.data[i]);
            }
        }
        return camerasData;
    }

    function prepareCamera(camera) {
        camera.publicCustomerId = null;
        camera.assignedCustomersText = "";
        camera.assignedCustomersIds = [];
        if (camera.assignedCustomers && camera.assignedCustomers.length) {
            var assignedCustomersTitles = [];
            for (var i = 0; i < camera.assignedCustomers.length; i++) {
                var assignedCustomer = camera.assignedCustomers[i];
                camera.assignedCustomersIds.push(assignedCustomer.customerId.id);
                if (assignedCustomer.public) {
                    camera.publicCustomerId = assignedCustomer.customerId.id;
                } else {
                    assignedCustomersTitles.push(assignedCustomer.title);
                }
            }
            camera.assignedCustomersText = assignedCustomersTitles.join(', ');
        }
        return camera;
    }

}
