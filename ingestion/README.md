# INT² ingestion

Python 3.12+ NACSA ingestion workspace. The collector uses NACSA's public structured endpoint and never scrapes the rendered table.

```powershell
Set-Location ingestion
python -m venv .venv
.\.venv\Scripts\Activate.ps1
pip install -r requirements.txt
python -m nacsa.collector
$env:SUPABASE_DATABASE_URL = "postgresql://..."
python -m nacsa.importer ../data/raw/nacsa/YYYY-MM-DD.json
```

Raw snapshots are immutable and gitignored. The importer includes business licence holders only, conservatively deduplicates normalized exact names, and leaves fields absent from NACSA as `NULL`.

Run tests with `python -m unittest discover -s tests`.
