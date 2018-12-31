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

import camerasTemplate from './cameras.tpl.html';

/* eslint-enable import/no-unresolved, import/default */

/*@ngInject*/
export default function CameraRoutes($stateProvider, types) {
    $stateProvider
        .state('home.cameras', {
            url: '/cameras',
            params: {'topIndex': 0},
            module: 'private',
            auth: ['TENANT_ADMIN', 'CUSTOMER_USER'],
            views: {
                "content@home": {
                    templateUrl: camerasTemplate,
                    controller: 'CameraController',
                    controllerAs: 'vm'
                }
            },
            data: {
                camerasType: 'tenant',
                searchEnabled: true,
                searchByEntitySubtype: true,
                searchEntityType: types.entityType.camera,
                pageTitle: 'camera.cameras'
            },
            ncyBreadcrumb: {
                label: '{"icon": "camera_alt", "label": "camera.cameras"}'
            }
        })
        .state('home.customers.cameras', {
            url: '/:customerId/cameras',
            params: {'topIndex': 0},
            module: 'private',
            auth: ['TENANT_ADMIN'],
            views: {
                "content@home": {
                    templateUrl: camerasTemplate,
                    controllerAs: 'vm',
                    controller: 'CameraController'
                }
            },
            data: {
                camerasType: 'customer',
                searchEnabled: true,
                searchByEntitySubtype: true,
                searchEntityType: types.entityType.camera,
                pageTitle: 'customer.cameras'
            },
            ncyBreadcrumb: {
                label: '{"icon": "camera_alt", "label": "{{ vm.customerCamerasTitle }}", "translate": "false"}'
            }
        });

}
