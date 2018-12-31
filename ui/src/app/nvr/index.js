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

import uiRouter from 'angular-ui-router';
import thingsboardGrid from '../components/grid.directive';
import thingsboardApiUser from '../api/user.service';
import thingsboardApiNvr from '../api/nvr.service';
import thingsboardApiCustomer from '../api/customer.service';

import NvrRoutes from './nvr.routes';
import {NvrController, NvrCardController} from './nvr.controller';
import ManageNvrAssignedCustomersController from './manage-assigned-customers.controller';
import AddNvrsToCustomerController from './add-nvrs-to-customer.controller';
import NvrDirective from './nvr.directive';
import ShowNvrCamerasController from './show-nvr-cameras.controller';

export default angular.module('thingsboard.nvr', [
    uiRouter,
    thingsboardGrid,
    thingsboardApiUser,
    thingsboardApiNvr,
    thingsboardApiCustomer
])
    .config(NvrRoutes)
    .controller('NvrController', NvrController)
    .controller('NvrCardController', NvrCardController)
    .controller('ManageNvrAssignedCustomersController', ManageNvrAssignedCustomersController)
    .controller('AddNvrsToCustomerController', AddNvrsToCustomerController)
    .controller('ShowNvrCamerasController', ShowNvrCamerasController)
    .directive('tbNvr', NvrDirective)
    .name;
    