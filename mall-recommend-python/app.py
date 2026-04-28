from fastapi import FastAPI
from pydantic import BaseModel, Field
from typing import List
from collections import defaultdict

app = FastAPI(title="mall-sasrec-service", version="0.1.0")


class PredictReq(BaseModel):
    userId: int
    scene: str = "guess"
    limit: int = Field(default=8, ge=1, le=30)
    sequence: List[int] = []
    timeoutMs: int = 1200


class TrainReq(BaseModel):
    modelVersion: str
    timeoutMs: int = 1200


class SASRecStub:
    """
    Placeholder model for integration stage.
    Replace with real SASRec training/inference pipeline.
    """

    def __init__(self):
        self.version = "bootstrap"
        self.hot = []
        self.user_last_seq = defaultdict(list)

    def train(self, req: TrainReq):
        self.version = req.modelVersion
        return {"ok": True, "modelVersion": self.version}

    def predict(self, req: PredictReq):
        seq = [int(x) for x in req.sequence if x is not None]
        if seq:
            # Keep order stable and deduplicate.
            dedup = []
            seen = set()
            for x in seq:
                if x not in seen:
                    dedup.append(x)
                    seen.add(x)
            self.user_last_seq[req.userId] = dedup[:100]
            # Simple heuristic placeholder: reverse recency.
            return list(reversed(dedup))[: req.limit]
        saved = self.user_last_seq.get(req.userId, [])
        return list(reversed(saved))[: req.limit]


model = SASRecStub()


@app.get("/health")
def health():
    return {"ok": True, "modelVersion": model.version}


@app.post("/train")
def train(req: TrainReq):
    return model.train(req)


@app.post("/predict")
def predict(req: PredictReq):
    item_ids = model.predict(req)
    return {"itemIds": item_ids, "modelVersion": model.version}
