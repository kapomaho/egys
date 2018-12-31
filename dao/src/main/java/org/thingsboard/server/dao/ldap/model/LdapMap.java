/**
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
package org.thingsboard.server.dao.ldap.model;

public class LdapMap {

   private String changeType;

   private String tenantName;

   private String customerTitle;
   private String customerAdress;
   private String customerPhone;
   private String customerEmail;

   private String userFirstName;
   private String userLastName;
   private String userEmail;
   private String userPassword;


   private String oldCustomerTitle;
   private String oldCustomerAdress;
   private String oldCustomerPhone;
   private String oldCustomerEmail;

   private String oldUserFirstName;
   private String oldUserLastName;
   private String oldUserEmail;
   private String oldUserpassword;

   private String objectClass;
   private String changeEntity;

   public String getTenantName() {
      return tenantName;
   }

   public void setTenantName(String tenantName) {
      this.tenantName = tenantName;
   }

   public String getCustomerTitle() {
      return customerTitle;
   }

   public void setCustomerTitle(String customerTitle) {
      this.customerTitle = customerTitle;
   }

   public String getCustomerAdress() {
      return customerAdress;
   }

   public void setCustomerAdress(String customerAdress) {
      this.customerAdress = customerAdress;
   }

   public String getCustomerPhone() {
      return customerPhone;
   }

   public void setCustomerPhone(String customerPhone) {
      this.customerPhone = customerPhone;
   }

   public String getCustomerEmail() {
      return customerEmail;
   }

   public void setCustomerEmail(String customerEmail) {
      this.customerEmail = customerEmail;
   }

   public String getUserFirstName() {
      return userFirstName;
   }

   public void setUserFirstName(String userFirstName) {
      this.userFirstName = userFirstName;
   }

   public String getUserLastName() {
      return userLastName;
   }

   public void setUserLastName(String userLastName) {
      this.userLastName = userLastName;
   }

   public String getUserEmail() {
      return userEmail;
   }

   public void setUserEmail(String userEmail) {
      this.userEmail = userEmail;
   }

   public String getUserPassword() {
      return userPassword;
   }

   public void setUserPassword(String userPassword) {
      this.userPassword = userPassword;
   }

   public String getObjectClass() {
      return objectClass;
   }

   public void setObjectClass(String objectClass) {
      this.objectClass = objectClass;
   }

   public String getChangeEntity() {
      return changeEntity;
   }

   public void setChangeEntity(String changeEntity) {
      this.changeEntity = changeEntity;
   }

   public String getChangeType() {
      return changeType;
   }

   public void setChangeType(String changeType) {
      this.changeType = changeType;
   }

   public String getOldCustomerTitle() {
      return oldCustomerTitle;
   }

   public void setOldCustomerTitle(String oldCustomerTitle) {
      this.oldCustomerTitle = oldCustomerTitle;
   }

   public String getOldCustomerAdress() {
      return oldCustomerAdress;
   }

   public void setOldCustomerAdress(String oldCustomerAdress) {
      this.oldCustomerAdress = oldCustomerAdress;
   }

   public String getOldCustomerPhone() {
      return oldCustomerPhone;
   }

   public void setOldCustomerPhone(String oldCustomerPhone) {
      this.oldCustomerPhone = oldCustomerPhone;
   }

   public String getOldCustomerEmail() {
      return oldCustomerEmail;
   }

   public void setOldCustomerEmail(String oldCustomerEmail) {
      this.oldCustomerEmail = oldCustomerEmail;
   }

   public String getOldUserFirstName() {
      return oldUserFirstName;
   }

   public void setOldUserFirstName(String oldUserFirstName) {
      this.oldUserFirstName = oldUserFirstName;
   }

   public String getOldUserLastName() {
      return oldUserLastName;
   }

   public void setOldUserLastName(String oldUserLastName) {
      this.oldUserLastName = oldUserLastName;
   }

   public String getOldUserEmail() {
      return oldUserEmail;
   }

   public void setOldUserEmail(String oldUserEmail) {
      this.oldUserEmail = oldUserEmail;
   }

   public String getOldUserpassword() {
      return oldUserpassword;
   }

   public void setOldUserpassword(String oldUserpassword) {
      this.oldUserpassword = oldUserpassword;
   }
}
