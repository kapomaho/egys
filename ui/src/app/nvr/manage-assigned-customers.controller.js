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
export default function ManageAssignedCustomersController($mdDialog, $q, types, nvrService, actionType, nvrIds, assignedCustomers) {

    var vm = this;
    vm.types = types;
    vm.actionType = actionType;
    vm.nvrIds = nvrIds;
    vm.assignedCustomers = assignedCustomers;
    if (actionType != 'manage') {
        vm.assignedCustomers = [];
    }

    if (actionType == 'manage') {
        vm.titleText = 'nvr.manage-assigned-customers';
        vm.labelText = 'nvr.assigned-customers';
        vm.actionName = 'action.update';
    } else if (actionType == 'assign') {
        vm.titleText = 'nvr.assign-to-customers';
        vm.labelText = 'nvr.assign-to-customers-text';
        vm.actionName = 'action.assign';
    } else if (actionType == 'unassign') {
        vm.titleText = 'nvr.unassign-from-customers';
        vm.labelText = 'nvr.unassign-from-customers-text';
        vm.actionName = 'action.unassign';
    }

    vm.submit = submit;
    vm.cancel = cancel;

    function cancel () {
        $mdDialog.cancel();
    }

    function submit () {
        var tasks = [];
        for (var i=0;i<vm.nvrIds.length;i++) {
            var nvrId = vm.nvrIds[i];
            var promise;
            if (vm.actionType == 'manage') {
                promise = nvrService.updateNvrCustomers(nvrId, vm.assignedCustomers);
            } else if (vm.actionType == 'assign') {
                promise = nvrService.addNvrCustomers(nvrId, vm.assignedCustomers);
            } else if (vm.actionType == 'unassign') {
                promise = nvrService.removeNvrCustomers(nvrId, vm.assignedCustomers);
            }
            tasks.push(promise);
        }
        $q.all(tasks).then(function () {
            $mdDialog.hide();
        });
    }

}
