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

import nvrsTemplate from './nvrs.tpl.html';

/* eslint-enable import/no-unresolved, import/default */

/*@ngInject*/
export default function nvrRoutes($stateProvider, types) {
    $stateProvider
        .state('home.nvrs', {
            url: '/nvrs',
            params: {'topIndex': 0},
            module: 'private',
            auth: ['TENANT_ADMIN', 'CUSTOMER_USER'],
            views: {
                "content@home": {
                    templateUrl: nvrsTemplate,
                    controller: 'NvrController',
                    controllerAs: 'vm'
                }
            },
            data: {
                nvrsType: 'tenant',
                searchEnabled: true,
                searchByEntitySubtype: true,
                searchEntityType: types.entityType.nvr,
                pageTitle: 'nvr.nvrs'
            },
            ncyBreadcrumb: {
                label: '{"icon": "video_library", "label": "nvr.nvrs"}'
            }
        })
        .state('home.customers.nvrs', {
            url: '/:customerId/nvrs',
            params: {'topIndex': 0},
            module: 'private',
            auth: ['TENANT_ADMIN'],
            views: {
                "content@home": {
                    templateUrl: nvrsTemplate,
                    controllerAs: 'vm',
                    controller: 'NvrController'
                }
            },
            data: {
                nvrsType: 'customer',
                searchEnabled: true,
                searchByEntitySubtype: true,
                searchEntityType: types.entityType.nvr,
                pageTitle: 'customer.nvrs'
            },
            ncyBreadcrumb: {
                label: '{"icon": "video_library", "label": "{{ vm.customerNvrsTitle }}", "translate": "false"}'
            }
        });     
    }
