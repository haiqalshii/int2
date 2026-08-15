package com.int2.candidate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "candidate_profile")
public class CandidateProfile {
    @Id @UuidGenerator private UUID id;
    private String name;
    @Column(length = 320) private String email;
    @Column(name = "cv_file_path", length = 1024) private String cvFilePath;
    @Column(name = "cv_text", columnDefinition = "TEXT") private String cvText;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;

    protected CandidateProfile() {}
    public CandidateProfile(String name, String email) { this.name = name; this.email = email; }
    @PrePersist @PreUpdate void touch() { updatedAt = Instant.now(); }
    public UUID getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getCvFilePath() { return cvFilePath; }
    public void setCvFilePath(String cvFilePath) { this.cvFilePath = cvFilePath; }
    public String getCvText() { return cvText; }
    public void setCvText(String cvText) { this.cvText = cvText; }
    public Instant getUpdatedAt() { return updatedAt; }
}

