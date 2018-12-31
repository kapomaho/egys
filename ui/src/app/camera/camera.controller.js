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

import addCameraTemplate from './add-camera.tpl.html';
import cameraCard from './camera-card.tpl.html';
import manageAssignedCustomersTemplate from './manage-assigned-customers.tpl.html';
import addCamerasToCustomerTemplate from './add-cameras-to-customer.tpl.html';


/* eslint-enable import/no-unresolved, import/default */

/*@ngInject*/
export function CameraCardController(types) {
    var vm = this;

    vm.types = types;

    vm.isAssignedToCustomer = function() {
        return (vm.item && vm.item.assignedCustomers.length !== 0 && vm.parentCtl.camerasScope === 'tenant');
    }

    vm.isPublic = function() {
        return (vm.parentCtl.camerasScope === 'tenant' && vm.item && vm.item.publicCustomerId);
    }

    vm.hasNvr = function() {
        return angular.isDefined(vm.item.nvr);
    }
}

/*@ngInject*/
export function CameraController($rootScope, userService, cameraService, nvrService, customerService, $state, $stateParams, $document, $mdDialog, $q, $translate, types) {
    var customerId = $stateParams.customerId;

    var cameraActionsList = [];

    var cameraGroupActionsList = [];

    var vm = this;

    vm.types = types;

    vm.cameraGridConfig = {
        deleteItemTitleFunc: deleteCameraTitle,
        deleteItemContentFunc: deleteCameraText,
        deleteItemsTitleFunc: deleteCamerasTitle,
        deleteItemsActionTitleFunc: deleteCamerasActionTitle,
        deleteItemsContentFunc: deleteCamerasText,

        saveItemFunc: saveCamera,

        getItemTitleFunc: getCameraTitle,

        itemCardController: 'CameraCardController',
        itemCardTemplateUrl: cameraCard,
        parentCtl: vm,

        actionsList: cameraActionsList,
        groupActionsList: cameraGroupActionsList,

        onGridInited: gridInited,

        addItemTemplateUrl: addCameraTemplate,

        addItemText: function() { return $translate.instant('camera.add-camera-text') },
        noItemsText: function() { return $translate.instant('camera.no-cameras-text') },
        itemDetailsText: function() { return $translate.instant('camera.camera-details') },
        isDetailsReadOnly: isCustomerUser,
        isSelectionEnabled: function () {
            return !isCustomerUser();
        }
    };

    if (angular.isDefined($stateParams.items) && $stateParams.items !== null) {
        vm.cameraGridConfig.items = $stateParams.items;
    }

    if (angular.isDefined($stateParams.topIndex) && $stateParams.topIndex > 0) {
        vm.cameraGridConfig.topIndex = $stateParams.topIndex;
    }

    vm.camerasScope = $state.$current.data.camerasType;

    vm.makePublic = makePublic;
    vm.makePrivate = makePrivate;
    vm.unassignFromCustomer = unassignFromCustomer;
    vm.manageAssignedCustomers = manageAssignedCustomers;

    initController();

    function initController() {
        var fetchCamerasFunction = null;
        var deleteCameraFunction = null;
        var refreshCamerasParamsFunction = null;

        var user = userService.getCurrentUser();

        if (user.authority === 'CUSTOMER_USER') {
            vm.camerasScope = 'customer_user';
            customerId = user.customerId;
        }
        if (customerId) {
            vm.customerCamerasTitle = $translate.instant('customer.cameras');
            customerService.getShortCustomerInfo(customerId).then(
                function success(info) {
                    if (info.isPublic) {
                        vm.customerCamerasTitle = $translate.instant('customer.public-cameras');
                    }
                }
            );
        }

        if (vm.camerasScope === 'tenant') {
            fetchCamerasFunction = function (pageLink, cameraType) {
                var deferred = $q.defer();
                cameraService.getTenantCameras(pageLink, null, cameraType).then(
                    function success(cameras) {
                        for (var i = 0; i < cameras.data.length; i++) {
                            (function(camera, id) {
                                nvrService.getNvrByCameraId(id).then(function(nvr){
                                    camera.nvr = nvr;
                                });
                            })(cameras.data[i], cameras.data[i].id.id);
                        }
                        deferred.resolve(cameras);
                    }, function fail() {
                        deferred.reject();
                    }
                );
                return deferred.promise;
            };
            deleteCameraFunction = function (cameraId) {
                return cameraService.deleteCamera(cameraId);
            };
            refreshCamerasParamsFunction = function() {
                return {"topIndex": vm.topIndex};
            };

            cameraActionsList.push({
                onAction: function ($event, item) {
                    makePublic($event, item);
                },
                name: function() { return $translate.instant('action.share') },
                details: function() { return $translate.instant('camera.make-public') },
                icon: "share",
                isEnabled: function(camera) {
                    return camera && (!camera.publicCustomerId);
                }
            });

            cameraActionsList.push({
                onAction: function ($event, item) {
                    makePrivate($event, item);
                },
                name: function() { return $translate.instant('action.make-private') },
                details: function() { return $translate.instant('camera.make-private') },
                icon: "reply",
                isEnabled: function(camera) {
                    return camera && camera.publicCustomerId;
                }
            });

            cameraActionsList.push(
                {
                    onAction: function ($event, item) {
                        manageAssignedCustomers($event, item);
                    },
                    name: function() { return $translate.instant('action.assign') },
                    details: function() { return $translate.instant('camera.manage-assigned-customers') },
                    icon: "assignment_ind",
                    isEnabled: function(camera) {
                        return camera;
                    }
                }
            );

            cameraActionsList.push(
                {
                    onAction: function ($event, item) {
                        vm.grid.deleteItem($event, item);
                    },
                    name: function() { return $translate.instant('action.delete') },
                    details: function() { return $translate.instant('camera.delete') },
                    icon: "delete"
                }
            );

            cameraGroupActionsList.push(
                {
                    onAction: function ($event, items) {
                        assignCamerasToCustomer($event, items);
                    },
                    name: function() { return $translate.instant('camera.assign-cameras') },
                    details: function(selectedCount) {
                        return $translate.instant('camera.assign-cameras-text', {count: selectedCount}, "messageformat");
                    },
                    icon: "assignment_ind"
                }
            );

            cameraGroupActionsList.push(
                {
                    onAction: function ($event) {
                        vm.grid.deleteItems($event);
                    },
                    name: function() { return $translate.instant('camera.delete-cameras') },
                    details: deleteCamerasActionTitle,
                    icon: "delete"
                }
            );



        } else if (vm.camerasScope === 'customer' || vm.camerasScope === 'customer_user') {
            fetchCamerasFunction = function (pageLink, cameraType) {
                var deferred = $q.defer();
                cameraService.getCustomerCameras(customerId, pageLink, null, cameraType).then(
                    function success(cameras) {
                        for (var i = 0; i < cameras.data.length; i++) {
                            (function(camera, id) {
                                nvrService.getNvrByCameraId(id).then(function(nvr){
                                    camera.nvr = nvr;
                                });
                            })(cameras.data[i], cameras.data[i].id.id);
                        }
                        deferred.resolve(cameras);
                    }, function fail() {
                        deferred.reject();
                    }
                );
                return deferred.promise;
            };
            deleteCameraFunction = function (cameraId) {
                return cameraService.unassignCameraFromCustomer(cameraId);
            };
            refreshCamerasParamsFunction = function () {
                return {"customerId": customerId, "topIndex": vm.topIndex};
            };

            if (vm.camerasScope === 'customer') {
                cameraActionsList.push(
                    {
                        onAction: function ($event, item) {
                            unassignFromCustomer($event, item, customerId);
                        },
                        name: function() { return $translate.instant('action.unassign') },
                        details: function() { return $translate.instant('camera.unassign-from-customer') },
                        icon: "assignment_return",
                        isEnabled: function(camera) {
                            return camera && customerId != camera.publicCustomerId;
                        }
                    }
                );
                cameraActionsList.push(
                    {
                        onAction: function ($event, item) {
                            makePrivate($event, item);
                        },
                        name: function() { return $translate.instant('action.make-private') },
                        details: function() { return $translate.instant('camera.make-private') },
                        icon: "reply",
                        isEnabled: function(camera) {
                            return camera && camera.publicCustomerId;
                        }
                    }
                );

                cameraGroupActionsList.push(
                    {
                        onAction: function ($event, items) {
                            unassignCamerasFromCustomers($event, items);
                        },
                        name: function() { return $translate.instant('camera.unassign-cameras') },
                        details: function(selectedCount) {
                            return $translate.instant('camera.unassign-cameras-action-title', {count: selectedCount}, "messageformat");
                        },
                        icon: "assignment_return"
                    }
                );

                vm.cameraGridConfig.addItemAction = {
                    onAction: function ($event) {
                        addCamerasToCustomer($event);
                    },
                    name: function() { return $translate.instant('camera.assign-cameras') },
                    details: function() { return $translate.instant('camera.assign-new-camera') },
                    icon: "add"
                };


            } else if (vm.camerasScope === 'customer_user') {
                vm.cameraGridConfig.addItemAction = {};
            }
        }

        vm.cameraGridConfig.refreshParamsFunc = refreshCamerasParamsFunction;
        vm.cameraGridConfig.fetchItemsFunc = fetchCamerasFunction;
        vm.cameraGridConfig.deleteItemFunc = deleteCameraFunction;
    }

    function deleteCameraTitle(camera) {
        return $translate.instant('camera.delete-camera-title', {cameraName: camera.name});
    }

    function deleteCameraText() {
        return $translate.instant('camera.delete-camera-text');
    }

    function deleteCamerasTitle(selectedCount) {
        return $translate.instant('camera.delete-cameras-title', {count: selectedCount}, 'messageformat');
    }

    function deleteCamerasActionTitle(selectedCount) {
        return $translate.instant('camera.delete-cameras-action-title', {count: selectedCount}, 'messageformat');
    }

    function deleteCamerasText () {
        return $translate.instant('camera.delete-cameras-text');
    }

    function gridInited(grid) {
        vm.grid = grid;
    }

    function getCameraTitle(camera) {
        return camera ? camera.name : '';
    }

    function saveCamera(camera) {
        var deferred = $q.defer();
        cameraService.saveCamera(camera).then(
            function success() {
                $rootScope.$broadcast('cameraSaved');
                deferred.resolve();
            },
            function fail() {
                deferred.reject();
            }
        );
        return deferred.promise;
    }

    function isCustomerUser() {
        return vm.camerasScope === 'customer_user';
    }

    function addCamerasToCustomer($event) {
        if ($event) {
            $event.stopPropagation();
        }
        var pageSize = 10;
        cameraService.getTenantCameras({limit: pageSize, textSearch: ''}).then(
            function success(_cameras) {
                var cameras = {
                    pageSize: pageSize,
                    data: _cameras.data,
                    nextPageLink: _cameras.nextPageLink,
                    selections: {},
                    selectedCount: 0,
                    hasNext: _cameras.hasNext,
                    pending: false
                };
                if (cameras.hasNext) {
                    cameras.nextPageLink.limit = pageSize;
                }
                $mdDialog.show({
                    controller: 'AddCamerasToCustomerController',
                    controllerAs: 'vm',
                    templateUrl: addCamerasToCustomerTemplate,
                    locals: {customerId: customerId, cameras: cameras},
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

    function assignCamerasToCustomer($event, items) {
        var cameraIds = [];
        for (var id in items.selections) {
            cameraIds.push(id);
        }
        showManageAssignedCustomersDialog($event, cameraIds, 'assign');
    }

    function manageAssignedCustomers($event, camera) {
        showManageAssignedCustomersDialog($event, [camera.id.id], 'manage', camera.assignedCustomersIds);
    }

    function unassignCamerasFromCustomers($event, items) {
        var cameraIds = [];
        for (var id in items.selections) {
            cameraIds.push(id);
        }
        showManageAssignedCustomersDialog($event, cameraIds, 'unassign');
    }

    function showManageAssignedCustomersDialog($event, cameraIds, actionType, assignedCustomers) {
        if ($event) {
            $event.stopPropagation();
        }
        $mdDialog.show({
            controller: 'ManageCameraAssignedCustomersController',
            controllerAs: 'vm',
            templateUrl: manageAssignedCustomersTemplate,
            locals: {actionType: actionType, cameraIds: cameraIds, assignedCustomers: assignedCustomers},
            parent: angular.element($document[0].body),
            fullscreen: true,
            targetEvent: $event
        }).then(function () {
            vm.grid.refreshList();
        }, function () {
        });
    }

    function unassignFromCustomer($event, camera, customerId) {
        if ($event) {
            $event.stopPropagation();
        }
        var title = $translate.instant('camera.unassign-camera-title', {cameraName: camera.name});
        var content = $translate.instant('camera.unassign-camera-text');
        var label = $translate.instant('camera.unassign-camera');
        var confirm = $mdDialog.confirm()
            .targetEvent($event)
            .title(title)
            .htmlContent(content)
            .ariaLabel(label)
            .cancel($translate.instant('action.no'))
            .ok($translate.instant('action.yes'));
        $mdDialog.show(confirm).then(function () {
            cameraService.unassignCameraFromCustomer(camera.id.id, customerId).then(function success() {
                vm.grid.refreshList();
            });
        });
    }

    function makePublic($event, camera) {
        if ($event) {
            $event.stopPropagation();
        }
        var confirm = $mdDialog.confirm()
            .targetEvent($event)
            .title($translate.instant('camera.make-public-camera-title', {cameraName: camera.name}))
            .htmlContent($translate.instant('camera.make-public-camera-text'))
            .ariaLabel($translate.instant('camera.make-public'))
            .cancel($translate.instant('action.no'))
            .ok($translate.instant('action.yes'));
        $mdDialog.show(confirm).then(function () {
            cameraService.makeCameraPublic(camera.id.id).then(function success() {
                vm.grid.refreshList();
            });
        });
    }

    function makePrivate($event, camera) {
        if ($event) {
            $event.stopPropagation();
        }
        var title = $translate.instant('camera.make-private-camera-title', {cameraTitle: camera.title});
        var content = $translate.instant('camera.make-private-camera-text');
        var label = $translate.instant('camera.make-private-camera');
        var confirm = $mdDialog.confirm()
            .targetEvent($event)
            .title(title)
            .htmlContent(content)
            .ariaLabel(label)
            .cancel($translate.instant('action.no'))
            .ok($translate.instant('action.yes'));
        $mdDialog.show(confirm).then(function () {
            cameraService.makeCameraPrivate(camera.id.id).then(function success() {
                vm.grid.refreshList();
            });
        });
    }
}
