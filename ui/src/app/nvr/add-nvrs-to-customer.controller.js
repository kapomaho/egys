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

export default function AddNvrsToCustomerController(nvrService, $mdDialog, $q, customerId, nvrs) {
    var vm = this;

    vm.nvrs = nvrs;
    vm.searchText = '';

    vm.assign = assign;
    vm.cancel = cancel;
    vm.hasData = hasData;
    vm.noData = noData;
    vm.searchNvrTextUpdated = searchNvrTextUpdated;
    vm.toggleNvrSelection = toggleNvrSelection;
    
    vm.theNvrs = {
        getItemAtIndex: function (index) {
            if (index > vm.nvrs.data.length) {
                vm.theNvrs.fetchMoreItems_(index);
                return null;
            }
            var item = vm.nvrs.data[index];
            if (item) {
                item.indexNumber = index + 1;
            }
            return item;
        },
        getLength: function () {
            if (vm.nvrs.hasNext) {
                return vm.nvrs.data.length + vm.nvrs.nextPageLink.limit;
            } else {
                return vm.nvrs.data.length;
            }
        },

        fetchMoreItems_: function () {
            if (vm.nvrs.hasNext && !vm.nvrs.pending) {
                vm.nvrs.pending = true;
                nvrService.getTenantNvrs(vm.nvrs.nextPageLink, false).then(
                    function success(nvrs) {
                        vm.nvrs.data = vm.nvrs.data.concat(nvrs.data);
                        vm.nvrs.nextPageLink = nvrs.nextPageLink;
                        vm.nvrs.hasNext = nvrs.hasNext;
                        if (vm.nvrs.hasNext) {
                            vm.nvrs.nextPageLink.limit = vm.nvrs.pageSize;
                        }
                        vm.nvrs.pending = false;
                    }, function fail() {
                        vm.nvrs.hasNext = false;
                        vm.nvrs.pending = false;
                    }
                );
            }
        }
    };

    function cancel() {
        $mdDialog.cancel();
    }

    function assign() {
        var tasks = [];
        for (var nvrId in vm.nvrs.selections) {
            tasks.push(nvrService.assignNvrToCustomer(customerId, nvrId));
        }
        $q.all(tasks).then(function () {
            $mdDialog.hide();
        });
    }

    function noData() {
        return vm.nvrs.data.length == 0 && !vm.nvrs.hasNext;
    }

    function hasData() {
        return vm.nvrs.data.length > 0;
    }

    function toggleNvrSelection($event, nvr) {
        $event.stopPropagation();
        var selected = angular.isDefined(nvr.selected) && nvr.selected;
        nvr.selected = !selected;
        if (nvr.selected) {
            vm.nvrs.selections[nvr.id.id] = true;
            vm.nvrs.selectedCount++;
        } else {
            delete vm.nvrs.selections[nvr.id.id];
            vm.nvrs.selectedCount--;
        }
    }

    function searchNvrTextUpdated() {
        vm.nvrs = {
            pageSize: vm.nvrs.pageSize,
            data: [],
            nextPageLink: {
                limit: vm.nvrs.pageSize,
                textSearch: vm.textSearch
            },
            selections: {},
            selectedCount: 0,
            hasNext: true,
            pending: false
        };
    }
}