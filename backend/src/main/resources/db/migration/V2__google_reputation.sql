ALTER TABLE companies ADD COLUMN google_place_id VARCHAR(255);
ALTER TABLE companies ADD COLUMN google_rating NUMERIC(2,1);
ALTER TABLE companies ADD COLUMN google_review_count INTEGER;
CREATE INDEX idx_companies_google_rating ON companies (google_rating);
CREATE INDEX idx_companies_google_review_count ON companies (google_review_count);
