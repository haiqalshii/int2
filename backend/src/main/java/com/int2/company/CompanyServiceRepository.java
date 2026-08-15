package com.int2.company;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyServiceRepository extends JpaRepository<CompanyServiceEntity, UUID> {}

