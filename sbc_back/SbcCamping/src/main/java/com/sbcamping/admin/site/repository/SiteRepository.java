package com.sbcamping.admin.site.repository;


import com.sbcamping.domain.Site;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SiteRepository extends JpaRepository<Site,Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Site s where s.siteId = :siteId")
    Optional<Site> findByIdForUpdate(@Param("siteId") Long siteId);

}
