package com.int2.email;

import com.int2.company.Company;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "generated_emails")
public class GeneratedEmail {
    @Id @UuidGenerator private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "company_id") private Company company;
    @Column(nullable = false, length = 500) private String subject;
    @Column(nullable = false, columnDefinition = "TEXT") private String body;
    @Column(length = 120) private String model;
    @Column(name = "prompt_version", length = 50) private String promptVersion;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;

    protected GeneratedEmail() {}
    public GeneratedEmail(Company company, String subject, String body) {
        this.company = company; this.subject = subject; this.body = body;
    }
    @PrePersist void onCreate() { createdAt = Instant.now(); }
    public UUID getId() { return id; }
    public Company getCompany() { return company; }
    public String getSubject() { return subject; }
    public String getBody() { return body; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public String getPromptVersion() { return promptVersion; }
    public void setPromptVersion(String promptVersion) { this.promptVersion = promptVersion; }
    public Instant getCreatedAt() { return createdAt; }
}

