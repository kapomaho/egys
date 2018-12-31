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
const getCamerasUrl = '/api/customer/{customerId}/cameras';
const deleteCameraUrl = '/api/camera/{cameraId}';
const saveCameraUrl = '/api/camera';
const getNvrByCameraUrl = '/api/camera/{cameraId}/nvr';

class CameraApi extends BaseApi {
    constructor(http, q) {
        super(http, q);
    }

    getCameras(pageLink, customerId) {
        var deferred =  this.q.defer();
        var url = getCamerasUrl;
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

    deleteCamera(id) {
        var deferred = this.q.defer();
        var url = deleteCameraUrl;
        url = UrlUtils.insertCameraId(url, id);
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

    saveCamera(camera) {
        var deferred = this.q.defer();
        this.http.post(saveCameraUrl, camera).then(
            function success(response) {
                deferred.resolve(response.data);
            }, function fail() {
                deferred.reject();
            }
        );
        return deferred.promise;
    }

    getNvrByCamera(cameraId) {
        var deferred = this.q.defer();
        var url = getNvrByCameraUrl;
        url = UrlUtils.insertCameraId(url, cameraId);
        
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

    executeAll(tasks,successCallback, failureCallback) {
        this.q.all(tasks).then(
            successCallback,
            failureCallback
        );
    }
}

class CameraService {
    constructor(http, q) {
        this.api = new CameraApi(http, q);
    }

    getCameras(pageLink, customerId, successCallback, failureCallback) {
        this.api.getCameras(pageLink, customerId).then(
            successCallback,
            failureCallback
        );
    }

    deleteCamera(camera, successCallback, failureCallback) {
        this.api.deleteCamera(camera.id.id).then(
            function success(data) {
                NotificationUtils.notifyEvent(EntityType.CAMERA, EventType.DELETE);
                successCallback(data);
            },
            failureCallback
        );
    }

    saveCamera(camera, successCallback, failureCallback) {
        this.api.saveCamera(camera).then(
            function success(data) {
                let eventType = (camera.id) ? EventType.UPDATE : EventType.CREATE;
                NotificationUtils.notifyEvent(EntityType.CAMERA, eventType);
                successCallback(data);
            },
            failureCallback
        );
    }

    getCameraNvrs(cameras, successCallback, failureCallback) {
        var tasks = [];
        for (var camera of cameras) {
            tasks.push(this.api.getNvrByCamera(camera.id.id));
        }
        this.api.executeAll(
            tasks, 
            function success(results) {
                var cameraNvrMap = {};
                for (var index in results) {
                    var camera = cameras[index];
                    if (!cameraNvrMap[camera.id.id]) {
                        cameraNvrMap[camera.id.id] = results[index];
                    }
                }
                successCallback(cameraNvrMap);
            },
            failureCallback
        );
    }
}
