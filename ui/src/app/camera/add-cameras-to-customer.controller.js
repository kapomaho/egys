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
export default function AddCamerasToCustomerController(cameraService, $mdDialog, $q, customerId, cameras) {

    var vm = this;

    vm.cameras = cameras;
    vm.searchText = '';

    vm.assign = assign;
    vm.cancel = cancel;
    vm.hasData = hasData;
    vm.noData = noData;
    vm.searchCameraTextUpdated = searchCameraTextUpdated;
    vm.toggleCameraSelection = toggleCameraSelection;

    vm.theCameras = {
        getItemAtIndex: function (index) {
            if (index > vm.cameras.data.length) {
                vm.theCameras.fetchMoreItems_(index);
                return null;
            }
            var item = vm.cameras.data[index];
            if (item) {
                item.indexNumber = index + 1;
            }
            return item;
        },

        getLength: function () {
            if (vm.cameras.hasNext) {
                return vm.cameras.data.length + vm.cameras.nextPageLink.limit;
            } else {
                return vm.cameras.data.length;
            }
        },

        fetchMoreItems_: function () {
            if (vm.cameras.hasNext && !vm.cameras.pending) {
                vm.cameras.pending = true;
                cameraService.getTenantCameras(vm.cameras.nextPageLink).then(
                    function success(cameras) {
                        vm.cameras.data = vm.cameras.data.concat(cameras.data);
                        vm.cameras.nextPageLink = cameras.nextPageLink;
                        vm.cameras.hasNext = cameras.hasNext;
                        if (vm.cameras.hasNext) {
                            vm.cameras.nextPageLink.limit = vm.cameras.pageSize;
                        }
                        vm.cameras.pending = false;
                    },
                    function fail() {
                        vm.cameras.hasNext = false;
                        vm.cameras.pending = false;
                    });
            }
        }
    };

    function cancel () {
        $mdDialog.cancel();
    }

    function assign() {
        var tasks = [];
        for (var cameraId in vm.cameras.selections) {
            tasks.push(cameraService.assignCameraToCustomer(customerId, cameraId));
        }
        $q.all(tasks).then(function () {
            $mdDialog.hide();
        });
    }

    function noData() {
        return vm.cameras.data.length == 0 && !vm.cameras.hasNext;
    }

    function hasData() {
        return vm.cameras.data.length > 0;
    }

    function toggleCameraSelection($event, camera) {
        $event.stopPropagation();
        var selected = angular.isDefined(camera.selected) && camera.selected;
        camera.selected = !selected;
        if (camera.selected) {
            vm.cameras.selections[camera.id.id] = true;
            vm.cameras.selectedCount++;
        } else {
            delete vm.cameras.selections[camera.id.id];
            vm.cameras.selectedCount--;
        }
    }

    function searchCameraTextUpdated() {
        vm.cameras = {
            pageSize: vm.cameras.pageSize,
            data: [],
            nextPageLink: {
                limit: vm.cameras.pageSize,
                textSearch: vm.searchText
            },
            selections: {},
            selectedCount: 0,
            hasNext: true,
            pending: false
        };
    }

}
