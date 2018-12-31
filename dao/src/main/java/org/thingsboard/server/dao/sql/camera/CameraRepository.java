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
package org.thingsboard.server.dao.sql.camera;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.thingsboard.server.dao.model.sql.CameraEntity;
import org.thingsboard.server.dao.util.SqlDao;

import java.util.List;

@SqlDao
public interface CameraRepository extends CrudRepository<CameraEntity, String> {

    @Query("SELECT c " +
            "FROM CameraEntity c " +
            "where c.tenantId = :tenantId " +
            "and LOWER(c.searchText) like lower(concat(:textSearch, '%')) " +
            "and c.id > :idOffset " +
            "order by c.id")
    List<CameraEntity> findByTenantId(@Param("tenantId") String tenantId,
                                          @Param("textSearch") String textSearch,
                                          @Param("idOffset") String idOffset,
                                          Pageable pageable);

    /*@Query("select c " +
            "from CameraEntity c " +
            "where c.tenantId = :tenantId " +
            "and c.customerId = :customerId " +
            "and lower(c.searchText) like lower(concat(:textSearch, '%')) " +
            "and c.id > :idOffset " +
            "order by c.id")
    List<CameraEntity> findByTenantIdAndCustomerId(@Param("tenantId") String tenantId,
                                                   @Param("customerId") String customerId,
                                                   @Param("textSearch") String textSearch,
                                                   @Param("idOffset") String idOffset,
                                                   Pageable pageable);*/

    @Query("SELECT c FROM CameraEntity c WHERE c.tenantId = :tenantId " +
            "AND LOWER(c.searchText) LIKE LOWER(CONCAT(:textSearch, '%')) " +
            "AND c.id > :idOffset " +
            "AND c.id IN :cameraIds ORDER BY c.id")
    List<CameraEntity> findByTenantIdAndSearchTextAndIdIn(@Param("tenantId") String tenantId,
                                                         @Param("cameraIds") List<String> cameraIds,
                                                         @Param("textSearch") String textSearch,
                                                         @Param("idOffset") String idOffset,
                                                         Pageable pageable);

    List<CameraEntity> findByTenantIdAndIdIn(String tenantId, List<String> cameraIds);

    /*List<CameraEntity> findByTenantIdAndCustomerIdAndIdIn(String tenantId, String customerId, List<String> cameraIds);*/

    CameraEntity findByTenantIdAndName(String tenantId, String name);

    CameraEntity findByTenantIdAndHost(String tenantId, String host);

    @Query("SELECT c " +
            "FROM CameraEntity c " +
            "WHERE c.tenantId = :tenantId " +
            "AND c.type = :type " +
            "AND LOWER(c.searchText) LIKE LOWER(CONCAT(:textSearch, '%')) " +
            "AND c.id > :idOffset " +
            "ORDER BY c.id")
    List<CameraEntity> findByTenantIdAndType(@Param("tenantId") String tenantId,
                                            @Param("type") String type,
                                            @Param("textSearch") String textSearch,
                                            @Param("idOffset") String idOffset,
                                            Pageable pageable);

    /*@Query("SELECT c " +
            "FROM CameraEntity c " +
            "WHERE c.tenantId = :tenantId " +
            "AND c.customerId = :customerId " +
            "AND c.type = :type " +
            "AND LOWER(c.searchText) LIKE LOWER(CONCAT(:textSearch, '%')) " +
            "AND c.id > :idOffset " +
            "ORDER BY c.id")
    List<CameraEntity> findByTenantIdAndCustomerIdAndType(@Param("tenantId") String tenantId,
                                                         @Param("customerId") String customerId,
                                                         @Param("type") String type,
                                                         @Param("textSearch") String textSearch,
                                                         @Param("idOffset") String idOffset,
                                                         Pageable pageable);*/

    @Query("SELECT c FROM CameraEntity c WHERE c.tenantId = :tenantId " +
            "AND LOWER(c.searchText) LIKE LOWER(CONCAT(:textSearch, '%'))" +
            "AND c.type = :type " +
            "AND c.id > :idOffset " +
            "AND c.id IN :cameraIds ORDER BY c.id")
    List<CameraEntity> findByTenantIdAndTypeAndSearchTextAndIdIn(@Param("tenantId") String tenantId,
                                                                @Param("type") String type,
                                                                @Param("cameraIds") List<String> cameraIds,
                                                                @Param("textSearch") String textSearch,
                                                                @Param("idOffset") String idOffset,
                                                                Pageable pageable);

    @Query("SELECT DISTINCT c.type " +
            "FROM CameraEntity c " +
            "WHERE c.tenantId = :tenantId")
    List<String> findTenantCameraTypes(@Param("tenantId") String tenantId);
}
