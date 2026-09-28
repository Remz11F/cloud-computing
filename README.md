# Rental Orchestrator – Cloud Computing Coursework

A Java (JAX-RS) REST **orchestrator service** for a peer-to-peer item rental platform. It brings together three backing services:

- **Azure Cosmos DB** – stores rentable items and rental requests
- **OSRM** (Open Source Routing Machine) – calculates driving routes, distances and journey times
- **RabbitMQ** – publishes events whenever a rental request changes state

## Architecture

```
            ┌──────────────────────────────┐
 Client ──► │  Orchestrator (JAX-RS)       │
 X-API-KEY  │  /webresources/...           │
            │   ├─ items     ──────────────┼──► Azure Cosmos DB  (items, Requests)
            │   ├─ requests  ──────────────┼──► Azure Cosmos DB
            │   │     └─ publish event ────┼──► RabbitMQ  (fanout: orchestrator.events)
            │   ├─ route     ──────────────┼──► Cosmos DB + OSRM public API
            │   └─ osrm      ──────────────┼──► OSRM public API
            └──────────────────────────────┘
                                               RabbitMQSubscriber ◄── orchestrator.events.queue
```

## API Endpoints

The base path is `/webresources`. Every endpoint except `/osrm` needs the header `X-API-KEY`, which is checked by `ApiKeyFilter`.

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/items?location=&name=` | Search items by exact location and/or a keyword in the name (case-insensitive) |
| `GET` | `/requests` | Health check for the requests endpoint |
| `POST` | `/requests` | Create a rental request (`item_id`, `user_id`). Its status is set to `pending` and a `request.created` event is published |
| `PUT` | `/requests/{id}/approve` | Approve a request, mark the item unavailable, and publish `request.approved` |
| `PUT` | `/requests/{id}/cancel` | Cancel a request, mark the item available again, and publish `request.cancelled` |
| `GET` | `/route?location=&startLat=&startLon=&endLat=&endLon=` | Find items in a location, look up an OSRM route for each, and return the **3 fastest**, ranked, with distance in km and duration in minutes |
| `GET` | `/osrm?startLat=&startLon=&endLat=&endLon=` | Raw OSRM route summary (distance and duration) between two points |

When no coordinates are given, `/route` starts from Nottingham. It knows destination coordinates for `nottingham`, `derby` and `liverpool`.

### Example

```bash
curl -H "X-API-KEY: <your-key>" \
  "http://localhost:8080/<app>/webresources/route?location=Derby"
```

```json
{
  "location": "Derby",
  "startLat": 52.9548, "startLon": -1.1505,
  "endLat": 52.9225,  "endLon": -1.4746,
  "results": [
    { "rank": 1, "itemId": "...", "name": "Drill", "distanceKm": 25.4, "durationMins": 27.1, ... }
  ]
}
```

## Data Models

- **Item** – `id`, `item_id`, `owner_id`, `name`, `category`, `location`, `daily_rate`, `available`, `condition`, `description`
- **Request** – `id`, `item_id`, `user_id`, `status` (`pending` / `approved` / `cancelled`)
- **RequestEvent** (RabbitMQ message) – `eventType`, `timestamp`, `requestId`, `itemId`, `userId`, `status`

## Source Files

| Area | Files |
|------|-------|
| App config | `ApplicationConfig.java` |
| Controllers | `ItemController`, `RentalRequestController`, `RouteController`, `OsrmService` |
| Data access | `CosmosDBClient`, `CosmosTest` |
| Messaging | `RabbitMQClient`, `RabbitMQPublisher`, `RabbitMQSubscriber` |
| Security | `ApiKeyFilter`, `ApiKeyRequired` (a JAX-RS name-binding annotation) |
| Models / DTOs | `ItemModel`, `RequestModel`, `RequestEventModel`, `OsrmResponse`, `Route`, `RouteSummary`, `RouteResponseModel` |

> The files are uploaded flat. In the original NetBeans project they live in the packages `Orchestrator`, `Orchestrator.controllers`, `Orchestrator.data`, `Orchestrator.models`, `Orchestrator.rabbitmq` and `Orchestrator.security`. Recreate those folders under `src/main/java/` to build.

## Running

**Requirements:** Java 8+, a Java EE / Jakarta EE 8 server such as GlassFish or Payara, and these dependencies: `azure-cosmos`, `amqp-client` and `jackson-databind`.

1. Create a Cosmos DB database named `Coursework` with the containers `items` and `Requests`.
2. Set your Cosmos endpoint and key. **Use environment variables**, not values committed to source control.
3. Start RabbitMQ locally (the defaults are `127.0.0.1:5672`, `guest/guest`):
   ```bash
   docker run -d -p 5672:5672 -p 15672:15672 rabbitmq:3-management
   ```
4. Deploy the WAR to your application server.
5. Optionally run `RabbitMQSubscriber.main()` to watch events arrive live.

## Security Notes / Improvements

- Credentials (the Cosmos key, the API key, and RabbitMQ's default `guest/guest` login) are hardcoded constants. Load them from environment variables or Azure Key Vault instead.
- The API key comparison uses `String.equals`. `MessageDigest.isEqual` gives a constant-time comparison.
- `/route` calls OSRM once per item, one after another. Because every item in a location currently has the same destination, a single OSRM call could be shared.
