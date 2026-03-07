# Chronicle — Interactive Timeline Platform

A visual platform for creating and managing interactive timelines. Events are placed chronologically on parallel tracks, and can be connected across timelines using typed narrative relations.

## Stack

| Layer | Technology |
|---|---|
| Backend | Spring Boot 3.4 · Java 21 · Hexagonal Architecture + DDD |
| Frontend | Angular 18+ · Signals · Standalone Components |
| Database | PostgreSQL 16 · Flyway migrations |
| Infrastructure | Docker · Docker Compose · Nginx |

---

## Features

- Create multiple timelines and visualize them in parallel on a canvas
- Add events with custom temporal positions (Gregorian or custom calendar)
- Write event descriptions with **rich text** (TipTap editor)
- Connect events across timelines using **10 typed relations**
- Zoom, pan, and navigate the canvas freely
- **Graph Mode** — visualize all connections as a directed graph (Cytoscape.js + dagre layout)
- **Export** the canvas as a PNG image
- Edit any timeline loaded in the canvas from the sidebar

### Relation Types

| Type | Description |
|---|---|
| `CAUSAL` | One event directly causes another |
| `TEMPORAL` | Sequential ordering between events |
| `REFERENCE` | One event references or alludes to another |
| `CONTRAST` | Events that oppose or contradict each other |
| `PREREQUISITE` | An event must have occurred for another to be possible |
| `FORESHADOW` | An event hints at something that will happen later |
| `REVEAL` | An event discloses information about a past event |
| `ESCALATION` | An event increases the intensity of another |
| `RESOLUTION` | An event resolves the conflict started by another |
| `PARALLEL` | Events that mirror each other narratively |

---

## Getting Started

### Prerequisites

- [Docker Desktop](https://www.docker.com/products/docker-desktop/)

### Run

```bash
docker compose up --build
```

| Service | URL |
|---|---|
| Frontend | http://localhost:4200 |
| Backend API | http://localhost:8080/api/v1 |
| PostgreSQL | localhost:5432 (user: `chronicle`, db: `chronicle`) |

### Stop

```bash
docker compose down
```

To also remove the database volume:

```bash
docker compose down -v
```

---

## Project Structure

```
chronicle/
├── backend/                        # Spring Boot
│   └── src/main/java/com/chronicle/
│       ├── domain/                 # Aggregates, entities, value objects, ports
│       ├── application/            # Use cases (one class per use case)
│       ├── infrastructure/         # JPA adapters, REST controllers, config
│       └── shared/                 # Cross-cutting utilities
├── frontend/                       # Angular
│   └── src/app/
│       ├── domain/                 # TypeScript interfaces mirroring the domain
│       ├── features/               # Lazy-loaded feature modules
│       │   ├── timeline-canvas/    # Main canvas (zoom, pan, SVG connections)
│       │   ├── timeline-editor/    # Sidebar event editor (rich text, CRUD)
│       │   ├── timeline-list/      # Timeline listing and creation
│       │   └── graph-view/         # Graph Mode (Cytoscape.js)
│       ├── infrastructure/api/     # HTTP services and DTO mappers
│       └── shared/                 # Reusable components (rich-text-editor, etc.)
└── docker-compose.yml
```

---

## API Reference

```
GET    /api/v1/timelines                      List all timelines
POST   /api/v1/timelines                      Create a timeline
GET    /api/v1/timelines/{id}                 Get timeline with events
PUT    /api/v1/timelines/{id}                 Update timeline metadata
DELETE /api/v1/timelines/{id}                 Delete timeline

POST   /api/v1/timelines/{id}/events          Add event to timeline
PUT    /api/v1/timelines/{id}/events/{eid}    Update event
DELETE /api/v1/timelines/{id}/events/{eid}    Remove event

GET    /api/v1/connections?eventIds=...       Get connections for a set of events
POST   /api/v1/connections                    Create a connection between events
DELETE /api/v1/connections/{id}               Delete a connection

GET    /api/v1/graph?timelineIds=...          Get full event graph (nodes + edges)
```

### Example — create a connection

```json
POST /api/v1/connections
{
  "sourceEventId": "uuid-of-source-event",
  "targetEventId": "uuid-of-target-event",
  "description": "The battle escalated into a full siege",
  "connectionType": "ESCALATION"
}
```

---

## Architecture Notes

The backend follows **Hexagonal Architecture** with a strict dependency rule:

```
Infrastructure → Application → Domain
```

- The **domain** layer has zero framework dependencies (no Spring, no JPA)
- Each use case is a single class implementing `UseCase<Input, Output>`
- JPA entities are completely separate from domain entities; mappers translate between them
- Flyway manages all schema changes — `ddl-auto` is set to `validate`

The frontend uses **Angular Signals** for all reactive state and standalone components throughout. Heavy libraries (Cytoscape, html2canvas, TipTap) are lazy-loaded and never included in the initial bundle.

---

## Development (without Docker)

### Backend

```bash
cd backend
./mvnw spring-boot:run
```

Requires a local PostgreSQL instance. Override the connection via environment variables:

```bash
DB_URL=jdbc:postgresql://localhost:5432/chronicle \
DB_USER=chronicle \
DB_PASSWORD=chronicle \
./mvnw spring-boot:run
```

### Frontend

```bash
cd frontend
npm install
npx ng serve
```

The dev server proxies `/api` to `http://localhost:8080` by default.
