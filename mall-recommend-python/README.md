# mall-recommend-python

SASRec recommendation service (Python side) used by the Java backend.

## Run

```bash
pip install -r requirements.txt
uvicorn app:app --host 0.0.0.0 --port 8008
```

## APIs

- `GET /health`
- `POST /train`
- `POST /predict`

Current implementation is an integration stub for end-to-end connectivity. Replace `SASRecStub` with real SASRec pipeline when training artifacts are ready.
