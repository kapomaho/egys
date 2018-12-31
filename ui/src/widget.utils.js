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
class BaseApi {
    constructor(http, q) {
        this.http = http;
        this.q = q;
    }
}

const EntityType = {
    NVR: 'nvr',
    CAMERA: 'camera',
    CUSTOMER: 'customer',
    RELATION: 'relation'
}

const EventType = {
    CREATE: 'created',
    DELETE: 'deleted',
    UPDATE: 'updated'
}

class NotificationUtils {

    static getEvent(entityType, eventType) {
        return entityType + '-' + eventType;
    }

    static notifyEvent(entityType, eventType) {
        let $body = angular.element(document.body);
        let $rootScope = $body.injector().get('$rootScope');
        $rootScope.$broadcast(NotificationUtils.getEvent(entityType, eventType));
    }

    static subscribeTo(scope, entityType, eventType, callback) {
        scope.$on(NotificationUtils.getEvent(entityType, eventType), callback);
    }
}

class UrlUtils {
    static insertCustomerId(url, customerId) {
        return url.replace('{customerId}', customerId);
    }

    static insertCameraId(url, cameraId) {
        return url.replace('{cameraId}', cameraId);
    }
    
    static insertNvrId(url, nvrId) {
        return url.replace('{nvrId}', nvrId);
    }
    
    static insertPageLink(url, pageLink) {
        url = url + '?limit=' + pageLink.limit;
        
        if (angular.isDefined(pageLink.textSearch)) {
            url += '&textSearch=' + pageLink.textSearch;
        }
        if (angular.isDefined(pageLink.idOffset)) {
            url += '&idOffset=' + pageLink.idOffset;
        }
        if (angular.isDefined(pageLink.textOffset)) {
            url += '&textOffset=' + pageLink.textOffset;
        }
        return url;
    }
}

class UiUtils {
    constructor(mdDialog, mdToast) {
        this.dialog = mdDialog;
        this.toast = mdToast;
    }

    showDissmissAlert(title, message, buttonTitle) {
        this.dialog.show(
            this.dialog.alert()
            .clickOutsideToClose(true)
            .title(title)
            .textContent(message)
            .ok(buttonTitle)
        );
    }

    showConfirmationAlert(title, message, confirmTitle, cancelTitle, confirmCallback, cancelCallback) {
        var confirm = this.dialog.confirm()
          .title(title)
          .textContent(message)
          .ok(confirmTitle)
          .cancel(cancelTitle);
        
          this.dialog.show(confirm).then(
              function() {
                  confirmCallback();
              },
              function() {
                  cancelCallback();
              }
          );
    }

    showSuccessToast(container, text) {
        this.toast.show(
            this.toast.simple()
                .textContent(text)
                .position('top')
                .parent(container)
                .action('Kapat')
        );
    }
}