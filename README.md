# transactions-service

## Descripcion
Microservicio de transacciones financieras, transferencias y reportes de movimientos.

## Endpoints
- `GET /api/v1/transactions`
- `POST /api/v1/transactions`
- `GET /api/v1/transactions/{id}`
- `PUT /api/v1/transactions/{id}`
- `DELETE /api/v1/transactions/{id}`
- `GET /api/v1/transactions/product/{productId}`
- `POST /api/v1/transactions/transfer`
- `GET /api/v1/reports/movements/{productId}`

## Nota
El `docker-compose.yml` del entorno esta en (`yanki-service`).

## Proyectos relacionados
- https://github.com/vjoyaroj/bank-config-repo
- https://github.com/vjoyaroj/microservices-config
- https://github.com/vjoyaroj/eureka-server
- https://github.com/vjoyaroj/yanki-service
- https://github.com/vjoyaroj/api-gateway
- https://github.com/vjoyaroj/transactions-service
- https://github.com/vjoyaroj/debit-cards-service
- https://github.com/vjoyaroj/customer-service
- https://github.com/vjoyaroj/credits-service
- https://github.com/vjoyaroj/accounts-service
