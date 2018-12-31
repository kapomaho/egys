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

import addNvrTemplate from './add-nvr.tpl.html';
import nvrCard from './nvr-card.tpl.html';
import addNvrsToCustomerTemplate from './add-nvrs-to-customer.tpl.html';
import manageAssignedCustomersTemplate from './manage-assigned-customers.tpl.html';
import showNvrCamerasTemplate from './show-nvr-cameras.tpl.html';

/* eslint-enable import/no-unresolved, import/default */

/*@ngInject*/

export function NvrCardController(types) {
    var vm = this;

    vm.types = types;

    vm.isAssignedToCustomer = function() {
        return (vm.item && vm.item.assignedCustomers.length !== 0 && vm.parentCtl.nvrsScope === 'tenant');
    }

    vm.isPublic = function() {
        return (vm.parentCtl.nvrsScope === 'tenant' && vm.item && vm.item.publicCustomerId);
    }
}


/*@ngInject*/
export function NvrController($rootScope, userService, nvrService, cameraService, customerService, $state, $stateParams, 
    $document, $mdDialog, $q, $translate, types) {
    
    var customerId = $stateParams.customerId;
    var nvrActionsList = [];
    var nvrGroupActionsList = [];
    var vm = this;
    vm.types = types;

    vm.nvrGridConfig = {
        deleteItemTitleFunc: deleteNvrTitle,
        deleteItemContentFunc: deleteNvrText,
        deleteItemsTitleFunc: deleteNvrsTitle,
        deleteItemsActionTitleFunc: deleteNvrsActionTitle,
        deleteItemsContentFunc: deleteNvrsText,

        saveItemFunc: saveNvr,
        
        getItemTitleFunc: getNvrTitle,

        itemCardController: 'NvrCardController',
        itemCardTemplateUrl: nvrCard,
        parentCtl: vm,

        actionsList: nvrActionsList,
        groupActionsList: nvrGroupActionsList,

        onGridInited: gridInited,

        addItemTemplateUrl: addNvrTemplate,

        addItemText: function() { return $translate.instant('nvr.add-nvr-text')},
        noItemsText: function() { return $translate.instant('nvr.no-nvrs-text')},
        itemDetailsText: function() { return $translate.instant('nvr.nvr-details')},
        isDetailsReadOnly: isCustomerUser,
        isSelectionEnabled: function () {
            return !isCustomerUser();
        }
    };

    if (angular.isDefined($stateParams.items) && $stateParams.items !== null) {
        vm.nvrGridConfig.items = $stateParams.items;
    }
    
    if (angular.isDefined($stateParams.topIndex) && $stateParams.topIndex > 0) {
        vm.nvrGridConfig.topIndex = $stateParams.topIndex;
    }

    vm.nvrsScope = $state.$current.data.nvrsType;

    vm.makePublic = makePublic;
    vm.makePrivate = makePrivate;
    vm.unassignFromCustomer = unassignFromCustomer;
    vm.manageAssignedCustomers = manageAssignedCustomers;
    vm.showAssignedCameras = showAssignedCameras;

    initController();

    function initController() {
        var fetchNvrsFunction = null;
        var deleteNvrFunction = null;
        var refreshNvrsParamsFunction = null;

        var user = userService.getCurrentUser();
        
        if (user.authority === 'CUSTOMER_USER') {
            vm.nvrsScope = 'customer_user';
            customerId = user.customerId;
        }
        
        if (customerId) {
            vm.customerNvrsTitle = $translate.instant('customer.nvrs');
            customerService.getShortCustomerInfo(customerId).then(
                function success(info) {
                    if (info.isPublic) {
                        vm.customerNvrsTitle = $translate.instant('customer.public-nvrs');
                    }
                }
            );
        }

        if (vm.nvrsScope === 'tenant') {
            fetchNvrsFunction = function (pageLink, nvrType) {
                return nvrService.getTenantNvrs(pageLink, null, nvrType);
            };
            deleteNvrFunction = function (nvrId) {
                return nvrService.deleteNvr(nvrId);
            };
            refreshNvrsParamsFunction = function() {
                return {"topIndex": vm.topIndex};
            };

            nvrActionsList.push({
                onAction: function ($event, item) {
                    makePublic($event, item);
                },
                name: function() { return $translate.instant('action.share')},
                details: function() { return $translate.instant('nvr.make-public')},
                icon: "share",
                isEnabled: function(nvr) {
                    return nvr && (!nvr.publicCustomerId);
                }  
            });

            nvrActionsList.push({
                onAction: function ($event, item) {
                    showAssignedCameras($event, item);
                },
                name: function() { return $translate.instant('action.view')},
                details: function() { return $translate.instant('nvr.show-assigned-cameras')},
                icon: "camera",
                isEnabled: function(nvr) {
                    return nvr;
                }  
            });

            nvrActionsList.push({
                onAction: function ($event, item) {
                    makePrivate($event, item);
                },
                name: function() { return $translate.instant('action.make-private') },
                details: function() { return $translate.instant('nvr.make-private') },
                icon: "reply",
                isEnabled: function(nvr) {
                    return nvr && nvr.publicCustomerId;
                }
            });

            nvrActionsList.push(
                {
                    onAction: function ($event, item) {
                        manageAssignedCustomers($event, item);
                    },
                    name: function() { return $translate.instant('action.assign') },
                    details: function() { return $translate.instant('nvr.manage-assigned-customers') },
                    icon: "assignment_ind",
                    isEnabled: function(nvr) {
                        return nvr;
                    }
                }
            );

            nvrActionsList.push(
                {
                    onAction: function ($event, item) {
                        vm.grid.deleteItem($event, item);
                    },
                    name: function() { return $translate.instant('action.delete') },
                    details: function() { return $translate.instant('nvr.delete') },
                    icon: "delete"
                }
            );

            nvrGroupActionsList.push(
                {
                    onAction: function ($event, items) {
                        assignNvrsToCustomers($event, items);
                    },
                    name: function() { return $translate.instant('nvr.assign-nvrs') },
                    details: function(selectedCount) {
                        return $translate.instant('nvr.assign-nvrs-text', {count: selectedCount}, "messageformat");
                    },
                    icon: "assignment_ind"
                }
            );

            nvrGroupActionsList.push(
                {
                    onAction: function ($event) {
                        vm.grid.deleteItems($event);
                    },
                    name: function() { return $translate.instant('nvr.delete-nvrs') },
                    details: deleteNvrsActionTitle,
                    icon: "delete"
                }
            );
        } else if (vm.nvrsScope === 'customer' || vm.nvrsScope === 'customer_user') {
            fetchNvrsFunction = function (pageLink, nvrType) {
                return nvrService.getCustomerNvrs(customerId, pageLink, null, nvrType);
            };

            deleteNvrFunction = function (nvrId) {
                return nvrService.unassignNvrFromCustomer(nvrId);
            };

            refreshNvrsParamsFunction = function() {
                return {"customerId": customerId, "topIndex": vm.topIndex};
            };

            if (vm.nvrsScope === 'customer') {
                nvrActionsList.push(
                    {
                        onAction: function ($event, item) {
                            unassignFromCustomer($event, item, customerId);
                        },
                        name: function() { return $translate.instant('action.unassign') },
                        details: function() { return $translate.instant('nvr.unassign-from-customer') },
                        icon: "assignment_return",
                        isEnabled: function(nvr) {
                            return nvr && customerId != nvr.publicCustomerId;
                        }
                    }
                );
                nvrActionsList.push(
                    {
                        onAction: function ($event, item) {
                            makePrivate($event, item);
                        },
                        name: function() { return $translate.instant('action.make-private') },
                        details: function() { return $translate.instant('nvr.make-private') },
                        icon: "reply",
                        isEnabled: function(nvr) {
                            return nvr && nvr.publicCustomerId;
                        }
                    }
                );

                nvrGroupActionsList.push(
                    {
                        onAction: function ($event, items) {
                            unassignNvrsFromCustomers($event, items);
                        },
                        name: function() { return $translate.instant('nvr.unassign-nvrs') },
                        details: function(selectedCount) {
                            return $translate.instant('nvr.unassign-nvrs-action-title', {count: selectedCount}, "messageformat");
                        },
                        icon: "assignment_return"
                    }
                );

                vm.nvrGridConfig.addItemAction = {
                    onAction: function ($event) {
                        addNvrsToCustomer($event);
                    },
                    name: function() { return $translate.instant('nvr.assign-nvrs') },
                    details: function() { return $translate.instant('nvr.assign-new-nvr') },
                    icon: "add"
                };
            } else if (vm.nvrsScope === 'customer_user') {
                vm.nvrGridConfig.addItemAction = {};
            }
        }

        vm.nvrGridConfig.refreshParamsFunc = refreshNvrsParamsFunction;
        vm.nvrGridConfig.fetchItemsFunc = fetchNvrsFunction;
        vm.nvrGridConfig.deleteItemFunc = deleteNvrFunction;
    }

    function deleteNvrTitle(nvr) {
        return $translate.instant('nvr.delete-nvr-title', {nvrName: nvr.name});
    }

    function deleteNvrText() {
        return $translate.instant('nvr.delete-nvr-text');
    }

    function deleteNvrsTitle(selectedCount) {
        return $translate.instant('nvr.delete-nvrs-title', {count: selectedCount}, 'messageformat');
    }

    function deleteNvrsActionTitle(selectedCount) {
        return $translate.instant('nvr.delete-nvrs-action-title', {count: selectedCount}, 'messageformat');
    }

    function deleteNvrsText() {
        return $translate.instant('nvr.delete-nvrs-text') 
    }

    function gridInited(grid) {
        vm.grid = grid;
    }

    function getNvrTitle(nvr) {
        return nvr ? nvr.name : '';
    }

    function saveNvr(nvr) {
        var deferred = $q.defer();
        nvrService.saveNvr(nvr).then(
            function success() {
                $rootScope.$broadcast('nvrSaved');
                deferred.resolve();
            },
            function fail() {
                deferred.reject();
            }
        );
        return deferred.promise;
    }

    function isCustomerUser() {
        return vm.nvrsScope === 'customer_user';
    }

    function addNvrsToCustomer($event) {
        if ($event) {
            $event.stopPropagation();
        }
        var pageSize = 10;
        nvrService.getTenantNvrs({limit: pageSize, textSearch: ''}).then(
            function success(_nvrs) {
                var nvrs = {
                    pageSize: pageSize,
                    data: _nvrs.data,
                    nextPageLink: _nvrs.nextPageLink,
                    selections: {},
                    selectedCount: 0,
                    hasNext: _nvrs.hasNext,
                    pending: false
                };
                if (nvrs.hasNext) {
                    nvrs.nextPageLink.limit = pageSize;
                }
                $mdDialog.show({
                    controller: 'AddNvrsToCustomerController',
                    controllerAs: 'vm',
                    templateUrl: addNvrsToCustomerTemplate,
                    locals: {customerId: customerId, nvrs: nvrs},
                    parent: angular.element($document[0].body),
                    fullscreen: true,
                    targetEvent: $event
                }).then(function () {
                    vm.grid.refreshList();
                }, function () {
                });
            },
            function fail() {
            });
    }

    function manageAssignedCustomers($event, nvr) {
        showManageAssignedCustomersDialog($event, [nvr.id.id], 'manage', nvr.assignedCustomersIds);
    }

    function assignNvrsToCustomers($event, items) {
        var nvrIds = [];
        for (var id in items.selections) {
            nvrIds.push(id);
        }
        showManageAssignedCustomersDialog($event, nvrIds, 'assign');
    }

    function unassignNvrsFromCustomers($event, items) {
        var nvrIds = [];
        for (var id in items.selections) {
            nvrIds.push(id);
        }
        showManageAssignedCustomersDialog($event, nvrIds, 'unassign');
    }

    function showManageAssignedCustomersDialog($event, nvrIds, actionType, assignedCustomers) {
        if ($event) {
            $event.stopPropagation();
        }
        $mdDialog.show({
            controller: 'ManageNvrAssignedCustomersController',
            controllerAs: 'vm',
            templateUrl: manageAssignedCustomersTemplate,
            locals: {actionType: actionType, nvrIds: nvrIds, assignedCustomers: assignedCustomers},
            parent: angular.element($document[0].body),
            fullscreen: true,
            targetEvent: $event
        }).then(function () {
            vm.grid.refreshList();
        }, function () {
        });
    }

    function unassignFromCustomer($event, nvr, customerId) {
        if ($event) {
            $event.stopPropagation();
        }
        var title = $translate.instant('nvr.unassign-nvr-title', {nvrName: nvr.name});
        var content = $translate.instant('nvr.unassign-nvr-text');
        var label = $translate.instant('nvr.unassign-nvr');
        var confirm = $mdDialog.confirm()
            .targetEvent($event)
            .title(title)
            .htmlContent(content)
            .ariaLabel(label)
            .cancel($translate.instant('action.no'))
            .ok($translate.instant('action.yes'));
        $mdDialog.show(confirm).then(function () {
            nvrService.unassignNvrFromCustomer(nvr.id.id, customerId).then(function service () {
                vm.grid.refreshList();
            });
        });
    }

    function makePublic($event, nvr) {
        if ($event) {
            $event.stopPropagation();
        }
        var confirm = $mdDialog.confirm()
            .targetEvent($event)
            .title($translate.instant('nvr.make-public-nvr-title', {nvrName: nvr.name}))
            .htmlContent($translate.instant('nvr.make-public-nvr-text'))
            .ariaLabel($translate.instant('nvr.make-public'))
            .cancel($translate.instant('action.no'))
            .ok($translate.instant('action.yes'));
        $mdDialog.show(confirm).then(function () {
            nvrService.makeNvrPublic(nvr.id.id).then(function success() {
                vm.grid.refreshList();
            })
        })
    }

    function makePrivate($event, nvr) {
        if ($event) {
            $event.stopPropagation();
        }
        var title = $translate.instant('nvr.make-private-nvr-title', {nvrTitle: nvr.title});
        var content = $translate.instant('nvr.make-private-nvr-text');
        var label = $translate.instant('nvr.make-private-nvr');
        var confirm = $mdDialog.confirm()
            .targetEvent($event)
            .title(title)
            .htmlContent(content)
            .ariaLabel(label)
            .cancel($translate.instant('action.no'))
            .ok($translate.instant('action.yes'));
        $mdDialog.show(confirm).then(function () {
            nvrService.makeNvrPrivate(nvr.id.id).then(function success() {
                vm.grid.refreshList();
            });
        });
    }

    function showAssignedCameras($event, nvr){
        if ($event) {
            $event.stopPropagation();
        }
        cameraService.getCamerasByNvrId(nvr.id.id).then(
            function success(_cameras) {
                var cameras = _cameras;
                $mdDialog.show({
                    controller: 'ShowNvrCamerasController',
                    controllerAs: 'vm',
                    templateUrl: showNvrCamerasTemplate,
                    locals: {cameras: cameras},
                    parent: angular.element($document[0].body),
                    fullscreen: true,
                    targetEvent: $event
                }).then(function () {
                    vm.grid.refreshList();
                }, function () {
                });
            },
            function fail() {
            });
    }

}