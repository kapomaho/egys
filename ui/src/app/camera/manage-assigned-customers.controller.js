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
/*@ngInject*/
export default function ManageAssignedCustomersController($mdDialog, $q, types, cameraService, actionType, cameraIds, assignedCustomers) {

    var vm = this;
    vm.types = types;
    vm.actionType = actionType;
    vm.cameraIds = cameraIds;
    vm.assignedCustomers = assignedCustomers;
    if (actionType != 'manage') {
        vm.assignedCustomers = [];
    }

    if (actionType == 'manage') {
        vm.titleText = 'camera.manage-assigned-customers';
        vm.labelText = 'camera.assigned-customers';
        vm.actionName = 'action.update';
    } else if (actionType == 'assign') {
        vm.titleText = 'camera.assign-to-customers';
        vm.labelText = 'camera.assign-to-customers-text';
        vm.actionName = 'action.assign';
    } else if (actionType == 'unassign') {
        vm.titleText = 'camera.unassign-from-customers';
        vm.labelText = 'camera.unassign-from-customers-text';
        vm.actionName = 'action.unassign';
    }

    vm.submit = submit;
    vm.cancel = cancel;

    function cancel () {
        $mdDialog.cancel();
    }

    function submit () {
        var tasks = [];
        for (var i=0;i<vm.cameraIds.length;i++) {
            var cameraId = vm.cameraIds[i];
            var promise;
            if (vm.actionType == 'manage') {
                promise = cameraService.updateCameraCustomers(cameraId, vm.assignedCustomers);
            } else if (vm.actionType == 'assign') {
                promise = cameraService.addCameraCustomers(cameraId, vm.assignedCustomers);
            } else if (vm.actionType == 'unassign') {
                promise = cameraService.removeCameraCustomers(cameraId, vm.assignedCustomers);
            }
            tasks.push(promise);
        }
        $q.all(tasks).then(function () {
            $mdDialog.hide();
        });
    }

}
