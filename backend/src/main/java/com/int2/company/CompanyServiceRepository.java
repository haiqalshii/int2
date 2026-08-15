package com.int2.company;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CompanyServiceRepository extends JpaRepository<CompanyServiceEntity, UUID> {
    @Query("select service from CompanyServiceEntity service where service.company.id in :companyIds order by service.service")
    List<CompanyServiceEntity> findByCompanyIds(@Param("companyIds") Collection<UUID> companyIds);
}
