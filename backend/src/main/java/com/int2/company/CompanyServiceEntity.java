package com.int2.company;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "company_services", uniqueConstraints = @UniqueConstraint(name = "uq_company_service", columnNames = {"company_id", "service"}))
public class CompanyServiceEntity {
    @Id @UuidGenerator private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "company_id") private Company company;
    @Column(nullable = false, length = 120) private String service;
    @Column(name = "source_url", length = 2048) private String sourceUrl;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;

    protected CompanyServiceEntity() {}
    CompanyServiceEntity(Company company, String service, String sourceUrl) {
        this.company = company; this.service = service; this.sourceUrl = sourceUrl;
    }
    @PrePersist void onCreate() { createdAt = Instant.now(); }
    public UUID getId() { return id; }
    public String getService() { return service; }
    public String getSourceUrl() { return sourceUrl; }
    public Instant getCreatedAt() { return createdAt; }
}

