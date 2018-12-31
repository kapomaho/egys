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
export default function ShowNvrCamerasController($mdDialog, cameras) {  
    var vm = this;
    vm.cameras = cameras;
    vm.theCameras = {
        getItemAtIndex: function (index) {
            var item = vm.cameras[index];
            if (item) {
                item.indexNumber = index + 1;
            }
            return item;
        },
        getLength: function () {
            return vm.cameras.length;
        },
    };

    vm.cancel = cancel;
    vm.hasData = hasData;
    vm.noData = noData;

    function cancel () {
        $mdDialog.cancel();
    }

    function noData() {
        return vm.cameras.length == 0;
    }

    function hasData() {
        return vm.cameras.length > 0;
    }
}