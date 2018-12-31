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
package org.thingsboard.server.dao.sql.nvr;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.thingsboard.server.dao.model.sql.NvrEntity;
import org.thingsboard.server.dao.util.SqlDao;

import java.util.List;

@SqlDao
public interface NvrRepository extends CrudRepository<NvrEntity, String> {

    @Query("SELECT n " +
            "FROM NvrEntity n " +
            "WHERE n.tenantId = :tenantId " +
            "and LOWER(n.searchText) like LOWER(concat(:textSearch, '%')) " +
            "and n.id > :idOffset " +
            "ORDER BY n.id")
    List<NvrEntity> findByTenantId(@Param("tenantId") String tenantId,
                                   @Param("textSearch") String textSearch,
                                   @Param("idOffset") String idOffset,
                                   Pageable pageable);


    @Query("SELECT n " +
            "FROM NvrEntity n " +
            "WHERE n.tenantId = :tenantId " +
            "AND LOWER(n.searchText) LIKE LOWER(concat(:textSearch, '%')) " +
            "AND n.id > :idOffset " +
            "AND n.id IN :nvrIds " +
            "ORDER BY n.id")
    List<NvrEntity> findByTenantIdAndSearchTextAndIdIn(@Param("tenantId") String tenantId,
                                                       @Param("nvrIds") List<String> nvrIds,
                                                       @Param("textSearch") String textSearch,
                                                       @Param("idOffset") String idOffset,
                                                       Pageable pageable);

    List<NvrEntity> findByTenantIdAndIdIn(String tenantId, List<String> nvrIds);

    NvrEntity findByTenantIdAndName(String tenantId, String name);

    NvrEntity findByTenantIdAndIp(String tenantId, String ip);

    @Query("SELECT n " +
            "FROM NvrEntity n " +
            "WHERE n.tenantId = :tenantId " +
            "AND n.type = :type " +
            "AND LOWER(n.searchText) like LOWER(concat(:textSearch, '%')) " +
            "AND n.id > :idOffset " +
            "ORDER BY n.id")
    List<NvrEntity> findByTenantIdAndType(@Param("tenantId") String tenantId,
                                          @Param("type") String type,
                                          @Param("textSearch") String textSearch,
                                          @Param("idOffset") String idOffset,
                                          Pageable pageable);

    @Query("SELECT n " +
            "FROM NvrEntity n " +
            "WHERE n.tenantId = :tenantId " +
            "AND LOWER(n.searchText) LIKE LOWER(CONCAT(:textSearch, '%')) " +
            "AND n.type = :type " +
            "AND n.id > :idOffset " +
            "AND n.id IN :nvrIds " +
            "ORDER BY n.id")
    List<NvrEntity> findByTenantIdAndTypeAndSearchTextAndIdIn(@Param("tenantId") String tenantId,
                                                              @Param("type") String type,
                                                              @Param("nvrIds") List<String> nvrIds,
                                                              @Param("textSearch") String textSearch,
                                                              @Param("idOffset") String idOffset,
                                                              Pageable pageable);

    @Query("SELECT DISTINCT n.type " +
            "FROM NvrEntity n " +
            "WHERE n.tenantId = :tenantId")
    List<String> findTenantNvrTypes(@Param("tenantId") String tenantId);


}