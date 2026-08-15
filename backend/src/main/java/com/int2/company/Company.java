package com.int2.company;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "companies")
public class Company {
    @Id @UuidGenerator private UUID id;
    @Column(nullable = false) private String name;
    @Column(columnDefinition = "TEXT") private String description;
    @Column(length = 2048) private String website;
    @Column(length = 320) private String email;
    @Column(length = 64) private String phone;
    @Column(columnDefinition = "TEXT") private String address;
    @Column(length = 120) private String city;
    @Column(length = 120) private String state;
    @Column(precision = 9, scale = 6) private BigDecimal latitude;
    @Column(precision = 10, scale = 6) private BigDecimal longitude;
    @Column(name = "nacsa_licence") private String nacsaLicence;
    @Column(name = "nacsa_status", length = 100) private String nacsaStatus;
    @Column(name = "source_url", length = 2048) private String sourceUrl;
    @Column(name = "google_place_id", length = 255) private String googlePlaceId;
    @Column(name = "google_rating", precision = 2, scale = 1) private BigDecimal googleRating;
    @Column(name = "google_review_count") private Integer googleReviewCount;
    @Column(nullable = false) private boolean contacted;
    @Column(name = "contacted_at") private Instant contactedAt;
    @Column(name = "last_verified_at") private Instant lastVerifiedAt;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<CompanyServiceEntity> services = new LinkedHashSet<>();

    protected Company() {}

    public Company(String name) { this.name = name; }

    @PrePersist void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate void onUpdate() { updatedAt = Instant.now(); }

    public void addService(String service, String sourceUrl) {
        services.add(new CompanyServiceEntity(this, service, sourceUrl));
    }

    public void markContacted(boolean value) {
        contacted = value;
        contactedAt = value ? Instant.now() : null;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }
    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }
    public String getNacsaLicence() { return nacsaLicence; }
    public void setNacsaLicence(String nacsaLicence) { this.nacsaLicence = nacsaLicence; }
    public String getNacsaStatus() { return nacsaStatus; }
    public void setNacsaStatus(String nacsaStatus) { this.nacsaStatus = nacsaStatus; }
    public String getSourceUrl() { return sourceUrl; }
    public void setSourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; }
    public String getGooglePlaceId() { return googlePlaceId; }
    public BigDecimal getGoogleRating() { return googleRating; }
    public Integer getGoogleReviewCount() { return googleReviewCount; }
    public boolean isContacted() { return contacted; }
    public Instant getContactedAt() { return contactedAt; }
    public Instant getLastVerifiedAt() { return lastVerifiedAt; }
    public void setLastVerifiedAt(Instant lastVerifiedAt) { this.lastVerifiedAt = lastVerifiedAt; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Set<CompanyServiceEntity> getServices() { return services; }
}
