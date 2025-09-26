# BTC Identity Cloud — Full Scaffold (API-first, E2E thin slice)

This repo is a production-minded scaffold with a **minimal end-to-end flow**:
Onboarding → Document intake → Biometrics intake → Risk Engine decision → Onboarding decision API.

## Quick Start
```bash
docker compose -f infra/docker/docker-compose.yml up -d --build
# then watch logs if needed:
docker compose -f infra/docker/docker-compose.yml logs -f
```

### Try it (E2E)
1) Start a session
```bash
curl -s -X POST http://localhost:8001/public/v1/sessions -H "Content-Type: application/json" -d '{
  "channel": "public",
  "phone": "+26771234567"
}' | jq
```
Copy `sessionId` from the response.

2) Submit document intake (front URL only for this demo)
```bash
curl -s -X POST http://localhost:8003/public/v1/sessions/<SESSION_ID>/documents   -H "Content-Type: application/json"   -d '{"idType":"OMANG","idNumber":"123456789","frontImageUrl":"https://example.com/front.jpg"}' | jq
```

3) Submit selfie intake
```bash
curl -s -X POST http://localhost:8004/public/v1/sessions/<SESSION_ID>/selfie   -H "Content-Type: application/json"   -d '{"selfieImageUrl":"https://example.com/selfie.jpg"}' | jq
```

4) Trigger decision (risk-engine computes)
```bash
curl -s -X POST http://localhost:8005/internal/v1/decide/<SESSION_ID> | jq
```

5) Get decision from onboarding
```bash
curl -s http://localhost:8001/public/v1/sessions/<SESSION_ID>/decision | jq
```

> The risk rule is intentionally simple for a thin slice: if there is a document with a non-empty `idNumber` and a selfie exists → `AUTO_APPROVED`; if missing `idNumber` → `AUTO_REJECTED`; otherwise → `REVIEW`.

### Services (ports)
- onboarding-service  : http://localhost:8001
- verification-service: http://localhost:8002 (standalone synchronous demo)
- document-service    : http://localhost:8003
- biometrics-service  : http://localhost:8004
- risk-engine         : http://localhost:8005
- Postgres            : localhost:5435 (db=`kyc`, user=`postgres`, pass=`postgres`)

### Notes
- Liquibase applies core tables at onboarding-service startup.
- All services use the same DB for this thin slice. Later you can split schemas or databases per service and add RLS.
- MinIO + RabbitMQ are omitted in this slice to keep it small and E2E. Add them once ready to go async.
