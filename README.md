# order-service

Microservicio de gestión de órdenes para un ecommerce, construido con **Spring Boot 4 / Java 17** e integrado con servicios de **AWS**.

## ¿Qué hace?

Permite crear y consultar órdenes de compra. Cada vez que se crea una orden, el sistema persiste los datos, publica eventos de mensajería y registra logs en la nube.

## Endpoints REST

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/api/orders` | Crear una orden |
| GET | `/api/orders/{id}` | Consultar una orden por ID |
| GET | `/api/orders/customer/{customerId}` | Consultar órdenes por cliente |

## Integración con AWS

| Servicio | Uso |
|---|---|
| **RDS (PostgreSQL)** | Persiste las órdenes y sus items |
| **SQS - orders-queue** | Publica eventos al crear una orden. El mismo servicio los consume cada 5 segundos |
| **SQS - lambda-orders-queue** | Publica eventos que disparan automáticamente una función Lambda |
| **Lambda** | Función Python que procesa los mensajes de `lambda-orders-queue` de forma event-driven |
| **CloudWatch Logs** | Registra eventos de negocio (órdenes creadas) en el grupo `/order-service/logs` |
| **ECR** | Almacena la imagen Docker del servicio |
| **ECS Fargate** | Despliega el contenedor sin administrar servidores |
| **EKS** | Despliega el servicio en Kubernetes con un LoadBalancer |

## Flujo de una orden

```
Cliente HTTP
     │
     ▼
POST /api/orders
     │
     ├──► RDS PostgreSQL  →  persiste la orden
     ├──► SQS orders-queue  →  evento consumido por OrderEventListener
     ├──► SQS lambda-orders-queue  →  dispara Lambda automáticamente
     └──► CloudWatch Logs  →  registra el evento
```

## Stack técnico

- Java 17 / Spring Boot 4
- Gradle
- AWS SDK v2 con `DefaultCredentialsProvider`
- Docker (imagen `linux/amd64` para compatibilidad con ECS/EKS desde Mac Apple Silicon)

## Ejecución local

Requiere PostgreSQL corriendo (disponible via Docker Compose):

```bash
docker-compose up -d postgres
./gradlew bootRun
```
