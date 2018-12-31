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
const getNvrsUrl = '/api/customer/{customerId}/nvrs';
const deleteNvrUrl = '/api/nvr/{nvrId}';
const saveNvrUrl = '/api/nvr';
const getCamerasByNvrUrl = '/api/nvr/{nvrId}/cameras';
const relationUrl ='/api/relation';

class NvrApi extends BaseApi {
    constructor(http, q) {
        super(http, q);
    }

    getNvrs(pageLink, customerId) {
        var deferred =  this.q.defer();
        var url = getNvrsUrl;
        url = UrlUtils.insertCustomerId(url, customerId);
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

    deleteNvr(id) {
        var deferred = this.q.defer();
        var url = deleteNvrUrl;
        url = UrlUtils.insertNvrId(url, id);
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

    saveNvr(nvr) {
        var deferred = this.q.defer();
        this.http.post(saveNvrUrl, nvr).then(
            function success(response) {
                deferred.resolve(response.data);
            }, function fail() {
                deferred.reject();
            }
        );
        return deferred.promise;
    }

    getCamerasByNvr(nvrId) {
        var deferred = this.q.defer();
        var url = getCamerasByNvrUrl;
        url = UrlUtils.insertNvrId(url, nvrId);
        
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

    getCreateNvrCameraRelationTasks(nvr, cameras) {
        var tasks = [];
        for (var relatedCamera of cameras) {
            tasks.push(this.api.relateNvrWithCamera(nvr.id, relatedCamera.id));
        }
        return tasks;
    }
    
    relateNvrWithCamera(nvrId, cameraId) {
        var deferred = this.q.defer();
        var request = {};
        request.from = nvrId;
        request.to = cameraId;
        request.type = 'Manages';
        this.http.post(relationUrl, request).then(
            function success(response) {
                deferred.resolve(response.data);
            },
            function fail() {
                deferred.reject();
            }
        );
        return deferred.promise;
    }

    getRemoveNvrCameraRelationTasks(nvr, cameras) {
        var tasks = [];
        for (var relatedCamera of cameras) {
            tasks.push(this.api.deleteNvrRelationWithCamera(nvr.id, relatedCamera.id));
        }
        return tasks;
    }

    deleteNvrRelationWithCamera(nvrId, cameraId) {
        var deferred = this.q.defer();
        var url = relationUrl;
        url += '?fromId=' + nvrId.id;
        url += '&fromType=' + nvrId.entityType;
        url += '&relationType=Manages';
        url += '&toId=' + cameraId.id;
        url += '&toType=' + cameraId.entityType;
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

    executeAll(tasks, successCallback, failureCallback) {
        this.q.all(tasks).then(
            successCallback,
            failureCallback
        );
    }
}
class NvrService {
    constructor(http, q) {
        this.api = new NvrApi(http, q);
    }

    getNvrs(pageLink, customerId, successCallback, failureCallback) {
        this.api.getNvrs(pageLink, customerId).then(
            successCallback,
            failureCallback
        );
    }

    deleteNvr(nvr, successCallback, failureCallback) {
        this.api.deleteNvr(nvr.id.id).then(
            function success(data) {
                NotificationUtils.notifyEvent(EntityType.NVR, EventType.DELETE);
                successCallback(data);
            },
            failureCallback
        );
    }

    saveNvr(nvr, successCallback, failureCallback) {
        this.api.saveNvr(nvr).then(
            function success(data) {
                let eventType = (nvr.id) ? EventType.UPDATE : EventType.CREATE;
                NotificationUtils.notifyEvent(EntityType.NVR, eventType);
                successCallback(data);
            },
            failureCallback
        );
    }

    getCamerasByNvr(nvrId, successCallback, failureCallback) {
        this.api.getCamerasByNvr(nvrId).then(
            successCallback,
            failureCallback
        );
    }
    
    executeAllRelationTasks(nvr, camerasToRelate, camerasToDeleteRelations, successCallback, failureCallback) {
        let createTasks = this.api.getCreateNvrCameraRelationTasks(nvr, camerasToRelate);
        let deleteTasks = this.api.getCreateNvrCameraRelationTasks(nvr, camerasToDeleteRelations);
        this.api.executeAll(
            createTasks.concat(deleteTasks), 
            function success(data) {
                NotificationUtils.notifyEvent(EntityType.RELATION, EventType.UPDATE);
                successCallback(data);
            }, 
            failureCallback
        );
    }
}
