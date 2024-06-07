# Iban Payment Validator

## Goal
Create a compact Spring web application for payment processing using Java and an in-memory database to store data.

## Features

### 1. Payment Creation
- Endpoint: `POST /payments`
- Required fields for payment creation:

| Field name | Validity criteria |
|------------|-------------------|
| amount     | Positive decimal  |
| debtorIban | Valid IBAN        |

- `debtorIban` should be a Baltic country (LT, LV, EE) account number.
- Payment creation timestamp should be saved.
- Payment ID (UUID) should be saved.

### 2. Payment Creation from CSV
- Endpoint: `POST /payment-files`
  - Accepts a CSV file and creates the specified payments.
- Payments created from CSV should be validated according to the rules described in the `POST /payments` endpoint.
- Example CSV:
  ```
  "amount","debtorIban"
  "184.00","LV97HABA0012345678910"
  ```

### 3. Payment Querying
- Endpoint: `GET /payments`
  - Returns a list of payments.
- The result should contain the following fields: `id`, `amount`, `debtorIban`, and `createdAt`.
- The endpoint should accept an optional `debtorIban` filter as a query parameter.

### 4. Client Country Logging
- Endpoints: `POST /payments` and `POST /payment-files`
  - Resolve the caller's country using an external service of your choice.
  - Persist the resolved country code with the payment.
- Assume the caller IP is present in the `X-Forwarded-For` request header.
- Do not fail if the IP is not present or country resolution fails.
