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
export default angular.module('thingsboard.api.nvr', [])
    .factory('nvrService', NvrService)
    .name;

/*@ngInject*/
function NvrService($http, $q, customerService, userService) {
        
    var service = {
        getNvr: getNvr,
        getNvrs: getNvrs,
        saveNvr: saveNvr,
        deleteNvr: deleteNvr,
        assignNvrToCustomer: assignNvrToCustomer,
        unassignNvrFromCustomer: unassignNvrFromCustomer,
        addNvrCustomers: addNvrCustomers,
        updateNvrCustomers: updateNvrCustomers,
        removeNvrCustomers: removeNvrCustomers,
        makeNvrPublic: makeNvrPublic,
        makeNvrPrivate: makeNvrPrivate,
        getTenantNvrs: getTenantNvrs,
        getCustomerNvrs: getCustomerNvrs,
        findByQuery: findByQuery,
        fetchNvrsByNameFilter: fetchNvrsByNameFilter,
        getNvrTypes: getNvrTypes,
        getNvrByCameraId: getNvrByCameraId
    }

    return service;


    function getNvr(nvrId, ignoreErrors, config) {
        var deferred = $q.defer();
        var url = '/api/nvr/' + nvrId;
        if (!config) {
            config = {};
        }

        config = Object.assign(config, {ignoreErrors: ignoreErrors});
        $http.get(url, config).then(function success(response) {
            deferred.resolve(prepareNvr(response.data));
        }, function fail() {
            deferred.reject();
        });
        return deferred.promise;
    }

    function getNvrs(nvrIds, config) {
        var deferred = $q.defer();
        var ids = '';
        for (var i=0; i < nvrIds.length; i++) {
            if (i>0) {
                ids += ',';
            }
            ids += nvrIds[i];
        }

        var url = '/api/nvrs?nvrIds=' + ids;
        $http.get(url, config).then(function success(response) {
            var nvrs = prepareNvrs(response.data);
            nvrs.sort(function (nvr1, nvr2) {
                var id1 = nvr1.id.id;
                var id2 = nvr2.id.id;
                var index1 = nvrIds.indexOf(id1);
                var index2 = nvrIds.indexOf(id2);
                return index1 - index2;
            });
            deferred.resolve(nvrs);
        },function fail(){
            deferred.reject();
        });
        return deferred.promise;
    }

    function getNvrByCameraId(cameraId, ignoreErrors, config) {
        var deferred = $q.defer();
        var url = '/api/camera/' + cameraId + '/nvr';
        if (!config) {
            config = {};
        }

        config = Object.assign(config, {ignoreErrors: ignoreErrors});
        $http.get(url, config).then(function success(response) {
            deferred.resolve(response.data);
        }, function fail() {
            deferred.reject();
        });
        return deferred.promise;
    }

    function saveNvr(nvr, ignoreErrors, config) {
        var deferred = $q.defer();
        var url = '/api/nvr';
        if (!config) {
            config = {};
        }
        
        config = Object.assign(config, {ignoreErrors: ignoreErrors});
        $http.post(url, nvr, config).then( function success(response) {
            deferred.resolve(prepareNvr(response.data));
        }, function fail() {
            deferred.reject();
        });
        return deferred.promise;
    }

    function deleteNvr(nvrId, ignoreErrors, config) {
        var deferred = $q.defer();
        var url = '/api/nvr/' + nvrId;
        if (!config) {
            config = {}
        }
        config = Object.assign(config, { ignoreErrors: ignoreErrors});
        $http.delete(url, config).then(function success() {
            deferred.resolve();
        }, function fail() {
            deferred.reject();
        });
        return deferred.promise;
    }

    function assignNvrToCustomer(customerId, nvrId, ignoreErrors, config) {
        var deferred = $q.defer();
        var url = '/api/customer/' + customerId + '/nvr/' + nvrId;
        if (!config) {
            config = {}
        }
        config = Object.assign(config, { ignoreErrors: ignoreErrors});
        $http.post(url, null, config).then(function success(response) {
            deferred.resolve(prepareNvr(response.data));
        }, function fail() {
            deferred.reject();
        });
        return deferred.promise;
    }

    function unassignNvrFromCustomer(nvrId, customerId, ignoreErrors, config) {
        var deferred = $q.defer();
        var url = '/api/customer/' + customerId + '/nvr/' + nvrId;
        if (!config) {
            config = {}
        }
        config = Object.assign(config, { ignoreErrors: ignoreErrors});
        $http.delete(url, config).then(function success(response) {
            deferred.resolve(prepareNvr(response.data));
        }, function fail() {
            deferred.reject();
        });
        return deferred.promise;
    }

    function updateNvrCustomers(nvrId, customerIds) {
        var deferred = $q.defer();
        var url = '/api/nvr/' + nvrId + '/customers';
        $http.post(url, customerIds).then(function success(response) {
            deferred.resolve(prepareNvr(response.data));
        }, function fail() {
            deferred.reject();
        });
        return deferred.promise;
    }

    function addNvrCustomers(nvrId, customerIds) {
        var deferred = $q.defer();
        var url = '/api/nvr/' + nvrId + '/customers/add';
        $http.post(url, customerIds).then(function success(response) {
            deferred.resolve(prepareNvr(response.data));
        }, function fail() {
            deferred.reject();
        });
        return deferred.promise;
    }

    function removeNvrCustomers(nvrId, customerIds) {
        var deferred = $q.defer();
        var url = '/api/nvr/' + nvrId + '/customers/remove';
        $http.post(url, customerIds).then(function success(response) {
            deferred.resolve(prepareNvr(response.data));
        }, function fail() {
            deferred.reject();
        });
        return deferred.promise;
    }

    function makeNvrPublic(nvrId, ignoreErrors, config) {
        var deferred = $q.defer();
        var url = '/api/customer/public/nvr/' + nvrId;
        if (!config) {
            config = {};
        }
        config = Object.assign(config, {ignoreErrors: ignoreErrors});
        $http.post(url, null, config).then(function success(response) {
            deferred.resolve(prepareNvr(response.data));
        }, function fail() {
            deferred.reject();
        });
        return deferred.promise;
    }

    function makeNvrPrivate(nvrId) {
        var deferred = $q.defer();
        var url = '/api/customer/public/nvr/' + nvrId;
        $http.delete(url).then(function success(response) {
            deferred.resolve(prepareNvr(response.data));
        }, function fail() {
            deferred.reject();
        });
        return deferred.promise;
    }

    function getTenantNvrs(pageLink, config, type) {
        var deferred = $q.defer();
        var url = '/api/tenant/nvrs?limit=' + pageLink.limit;
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
            deferred.resolve(prepareNvrs(response.data));
        }, function fail() {
            deferred.reject();
        });
        return deferred.promise;
    }

    function getCustomerNvrs(customerId, pageLink, config, type) {
        var deferred = $q.defer();
        var url = '/api/customer/' + customerId + '/nvrs?limit=' + pageLink.limit;
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
            deferred.resolve(prepareNvrs(response.data));
        }, function fail() {
            deferred.reject();
        });
        return deferred.promise;
    }


    function findByQuery(query, ignoreErrors, config) {
        var deferred = $q.defer();
        var url = '/api/nvrs';
        if (!config) {
            config = {};
        }
        config = Object.assign(config, { ignoreErrors: ignoreErrors });
        $http.post(url, query, config).then(function success(response) {
            deferred.resolve(prepareNvrs(response.data));
        }, function fail() {
            deferred.reject();
        });
        return deferred.promise;
    }

    function fetchNvrsByNameFilter(nvrNameFilter, limit, config) {
        var deferred = $q.defer();
        var user = userService.getCurrentUser();
        var promise;
        var pageLink = {limit: limit, textSearch: nvrNameFilter};
        if (user.authority === 'CUSTOMER_USER') {
            var customerId = user.customerId;
            promise = getCustomerNvrs(customerId, pageLink, config);
        } else {
            promise = getTenantNvrs(pageLink, config);
        }
        promise.then(
            function success(result) {
                if (result.data && result.data.length > 0) {
                    deferred.resolve(prepareNvrs(result.data));
                } else {
                    deferred.resolve(null);
                }
            }, function fail() {
                deferred.resolve(null);
            }
        );
        return deferred.promise;
    }

    function getNvrTypes(config) {
        var deferred = $q.defer();
        var url = '/api/nvr/types';
        $http.get(url, config).then(function success(response) {
            deferred.resolve(response.data);
        }, function fail() {
            deferred.reject();
        });
        return deferred.promise;
    }

    function prepareNvrs(nvrsData) {
        if (nvrsData.data) {
            for (var i = 0; i < nvrsData.data.length; i++) {
                nvrsData.data[i] = prepareNvr(nvrsData.data[i]);
            }
        }
        return nvrsData;
    }

    function prepareNvr(nvr) {
        nvr.publicCustomerId = null;
        nvr.assignedCustomersText = "";
        nvr.assignedCustomersIds = [];
        if (nvr.assignedCustomers && nvr.assignedCustomers.length) {
            var assignedCustomersTitles = [];
            for (var i = 0; i < nvr.assignedCustomers.length; i++) {
                var assignedCustomer = nvr.assignedCustomers[i];
                nvr.assignedCustomersIds.push(assignedCustomer.customerId.id);
                if (assignedCustomer.public) {
                    nvr.publicCustomerId = assignedCustomer.customerId.id;
                } else {
                    assignedCustomersTitles.push(assignedCustomer.title);
                }
            }
            nvr.assignedCustomersText = assignedCustomersTitles.join(', ');
        }
        return nvr;
    }
}