CREATE TABLE companies (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    website VARCHAR(2048),
    email VARCHAR(320),
    phone VARCHAR(64),
    address TEXT,
    city VARCHAR(120),
    state VARCHAR(120),
    latitude NUMERIC(9, 6),
    longitude NUMERIC(10, 6),
    nacsa_licence VARCHAR(255),
    nacsa_status VARCHAR(100),
    source_url VARCHAR(2048),
    contacted BOOLEAN NOT NULL DEFAULT FALSE,
    contacted_at TIMESTAMP WITH TIME ZONE,
    last_verified_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT chk_company_contacted_at CHECK (contacted OR contacted_at IS NULL)
);

CREATE TABLE company_services (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    service VARCHAR(120) NOT NULL,
    source_url VARCHAR(2048),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uq_company_service UNIQUE (company_id, service)
);

CREATE TABLE candidate_profile (
    id UUID PRIMARY KEY,
    name VARCHAR(255),
    email VARCHAR(320),
    cv_file_path VARCHAR(1024),
    cv_text TEXT,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE generated_emails (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    subject VARCHAR(500) NOT NULL,
    body TEXT NOT NULL,
    model VARCHAR(120),
    prompt_version VARCHAR(50),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_companies_name_lower ON companies (LOWER(name));
CREATE INDEX idx_companies_state ON companies (state);
CREATE INDEX idx_companies_city ON companies (city);
CREATE INDEX idx_companies_contacted ON companies (contacted);
CREATE INDEX idx_company_services_service ON company_services (service);
CREATE INDEX idx_generated_emails_company_id ON generated_emails (company_id);

