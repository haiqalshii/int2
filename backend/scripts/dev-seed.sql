-- Run manually against a development database only.
INSERT INTO companies (
    id, name, description, website, email, address, city, state,
    latitude, longitude, nacsa_licence, nacsa_status, source_url,
    contacted, created_at, updated_at
) VALUES
('10000000-0000-0000-0000-000000000001', 'Aegis Cyber Defence Sdn Bhd', 'Development-only sample company.', 'https://example.com/aegis', 'careers@example.com', 'Kuala Lumpur', 'Kuala Lumpur', 'Kuala Lumpur', 3.139000, 101.686900, 'Cyber Security Service Provider', 'Active', 'https://example.com/source/aegis', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('10000000-0000-0000-0000-000000000002', 'Selangor Security Labs Sdn Bhd', 'Development-only sample company.', NULL, NULL, 'Shah Alam', 'Shah Alam', 'Selangor', 3.073800, 101.518300, 'Cyber Security Service Provider', 'Active', 'https://example.com/source/ssl', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('10000000-0000-0000-0000-000000000003', 'Borneo Digital Forensics Sdn Bhd', 'Development-only sample company.', NULL, NULL, 'Kuching', 'Kuching', 'Sarawak', 1.553300, 110.359200, 'Cyber Security Service Provider', 'Active', 'https://example.com/source/bdf', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO company_services (id, company_id, service, source_url, created_at) VALUES
('20000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000001', 'SOC', 'https://example.com/aegis/services', CURRENT_TIMESTAMP),
('20000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000001', 'Incident Response', 'https://example.com/aegis/services', CURRENT_TIMESTAMP),
('20000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000002', 'Penetration Testing', 'https://example.com/ssl/services', CURRENT_TIMESTAMP),
('20000000-0000-0000-0000-000000000004', '10000000-0000-0000-0000-000000000003', 'Digital Forensics', 'https://example.com/bdf/services', CURRENT_TIMESTAMP);

