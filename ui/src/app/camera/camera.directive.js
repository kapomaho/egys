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
/* eslint-disable import/no-unresolved, import/default */

import cameraFieldsetTemplate from './camera-fieldset.tpl.html';

/* eslint-enable import/no-unresolved, import/default */

/*@ngInject*/
export default function CameraDirective($compile, $templateCache, toast, $translate, types) {
    var linker = function (scope, element) {
        var template = $templateCache.get(cameraFieldsetTemplate);
        element.html(template);

        scope.types = types;
        scope.isAssignedToCustomer = false;
        scope.isPublic = false;
        scope.assignedCustomers = null;

        scope.$watch('camera', function(newVal) {
            if (newVal) {
                if (scope.camera.assignedCustomers && scope.camera.assignedCustomers.length != 0) {
                    scope.isAssignedToCustomer = true;
                    scope.assignedCustomers = scope.camera.assignedCustomersText;
                } else {
                    scope.isAssignedToCustomer = false;
                    scope.isPublic = false;
                    scope.assignedCustomers = null;
                }
            }
        });

        scope.onCameraIdCopied = function() {
            toast.showSuccess($translate.instant('camera.idCopiedMessage'), 750, angular.element(element).parent().parent(), 'bottom left');
        };

        $compile(element.contents())(scope);
    }
    return {
        restrict: "E",
        link: linker,
        scope: {
            camera: '=',
            isEdit: '=',
            cameraScope: '=',
            theForm: '=',
            onAssignToCustomer: '&',
            onMakePublic: '&',
            onMakePrivate: '&',
            onManageAssignedCustomers: '&',
            onUnassignFromCustomer: '&',
            onDeleteCamera: '&'
        }
    };
}
