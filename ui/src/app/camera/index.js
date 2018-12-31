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
import thingsboardApiCamera from '../api/camera.service';
import thingsboardApiCustomer from '../api/customer.service';

import CameraRoutes from './camera.routes';
import {CameraController, CameraCardController} from './camera.controller';
import ManageCameraAssignedCustomersController from './manage-assigned-customers.controller';
import AddCamerasToCustomerController from './add-cameras-to-customer.controller';
import CameraDirective from './camera.directive';

export default angular.module('thingsboard.camera', [
    uiRouter,
    thingsboardGrid,
    thingsboardApiUser,
    thingsboardApiCamera,
    thingsboardApiCustomer
])
    .config(CameraRoutes)
    .controller('CameraController', CameraController)
    .controller('CameraCardController', CameraCardController)
    .controller('ManageCameraAssignedCustomersController', ManageCameraAssignedCustomersController)
    .controller('AddCamerasToCustomerController', AddCamerasToCustomerController)
    .directive('tbCamera', CameraDirective)
    .name;
