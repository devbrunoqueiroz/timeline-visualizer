# CLAUDE.md — Chronicle: Interactive Timeline Platform

## Visão do Projeto

**Chronicle** é uma plataforma visual para criação e gerenciamento de timelines interativas, onde o usuário pode:
- Criar timelines com eventos posicionados cronologicamente
- Inserir textos, notas e documentos em pontos específicos da timeline
- Desenhar múltiplas timelines em paralelo (ex: linha do tempo de personagens, de eras, de eventos)
- Conectar eventos entre timelines diferentes
- Navegar visualmente por períodos de tempo

**Stack:** Spring Boot 3.x (backend) + Angular 18+ (frontend)

---

## Arquitetura Geral

```
chronicle/
├── backend/                    # Spring Boot
│   ├── src/main/java/com/chronicle/
│   │   ├── domain/             # Núcleo do domínio (sem dependências externas)
│   │   ├── application/        # Casos de uso (orquestra o domínio)
│   │   ├── infrastructure/     # Implementações concretas (DB, HTTP, etc)
│   │   └── shared/             # Utilitários compartilhados entre camadas
│   └── src/test/
├── frontend/                   # Angular
│   ├── src/app/
│   │   ├── core/               # Serviços singleton, guards, interceptors
│   │   ├── domain/             # Modelos e interfaces de domínio
│   │   ├── features/           # Módulos por funcionalidade
│   │   ├── shared/             # Componentes, pipes e diretivas reutilizáveis
│   │   └── infrastructure/     # Serviços HTTP, adapters
│   └── src/environments/
└── docker-compose.yml
```

---

## Backend — Arquitetura Hexagonal + DDD

### Princípios Fundamentais

**Regra de Dependência:** Dependências sempre apontam para dentro.
```
Infrastructure → Application → Domain
                               ↑
                        (nada aponta pra fora)
```

O domínio não conhece Spring, JPA, HTTP ou qualquer framework externo.

---

### Camada de Domínio (`domain/`)

#### Agregados e Entidades

```
domain/
├── timeline/
│   ├── Timeline.java           # Agregado raiz
│   ├── TimelineId.java         # Value Object de identidade
│   ├── TimelineEvent.java      # Entidade dentro do agregado
│   ├── EventContent.java       # Value Object
│   ├── TimelineRepository.java # Porta (interface)
│   └── TimelineService.java    # Serviço de domínio (regras que envolvem múltiplos agregados)
├── connection/
│   ├── TimelineConnection.java # Agregado — conexão entre eventos de timelines diferentes
│   ├── ConnectionRepository.java
│   └── ...
└── shared/
    ├── DomainEvent.java        # Interface base para eventos de domínio
    └── AggregateRoot.java      # Classe base para agregados
```

#### Regras para Entidades de Domínio

```java
// ✅ CORRETO — Domínio rico, sem frameworks
public class Timeline extends AggregateRoot<TimelineId> {

    private TimelineId id;
    private String name;
    private String description;
    private List<TimelineEvent> events;
    private TimelineVisibility visibility;
    private Instant createdAt;

    // Construtor privado — use factory method
    private Timeline(TimelineId id, String name) {
        this.id = id;
        this.name = name;
        this.events = new ArrayList<>();
        this.createdAt = Instant.now();
        registerEvent(new TimelineCreated(id, name));
    }

    public static Timeline create(String name, String description) {
        validateName(name);
        return new Timeline(TimelineId.generate(), name);
    }

    public TimelineEvent addEvent(String title, EventContent content, Instant occurredAt) {
        validateEventDate(occurredAt);
        var event = TimelineEvent.create(title, content, occurredAt);
        this.events.add(event);
        registerEvent(new EventAddedToTimeline(this.id, event.getId()));
        return event;
    }

    private static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new DomainException("Timeline name cannot be blank");
        }
        if (name.length() > 200) {
            throw new DomainException("Timeline name cannot exceed 200 characters");
        }
    }
}

// ❌ ERRADO — Nunca faça isso no domínio
@Entity // Anotação JPA no domínio = violação da arquitetura
public class Timeline {
    @Autowired // Spring no domínio = violação
    private TimelineRepository repository;
}
```

#### Value Objects

```java
// Value Objects são imutáveis e comparados por valor
public record TimelineId(UUID value) {

    public TimelineId {
        Objects.requireNonNull(value, "TimelineId cannot be null");
    }

    public static TimelineId generate() {
        return new TimelineId(UUID.randomUUID());
    }

    public static TimelineId of(String value) {
        try {
            return new TimelineId(UUID.fromString(value));
        } catch (IllegalArgumentException e) {
            throw new DomainException("Invalid TimelineId: " + value);
        }
    }
}

public record EventContent(String text, ContentType type, Map<String, String> metadata) {

    public EventContent {
        Objects.requireNonNull(text, "Content text cannot be null");
        Objects.requireNonNull(type, "Content type cannot be null");
        metadata = metadata != null ? Map.copyOf(metadata) : Map.of();
    }

    public static EventContent text(String text) {
        return new EventContent(text, ContentType.TEXT, Map.of());
    }

    public static EventContent richText(String text, Map<String, String> metadata) {
        return new EventContent(text, ContentType.RICH_TEXT, metadata);
    }
}
```

#### Portas (Interfaces de Repositório)

```java
// Porta de saída — definida no domínio, implementada na infraestrutura
public interface TimelineRepository {
    void save(Timeline timeline);
    Optional<Timeline> findById(TimelineId id);
    List<Timeline> findAll(TimelineFilter filter);
    void delete(TimelineId id);
    boolean existsById(TimelineId id);
}
```

---

### Camada de Aplicação (`application/`)

#### Casos de Uso

Cada caso de uso é uma classe com um único método público `execute()`.

```
application/
├── timeline/
│   ├── CreateTimeline/
│   │   ├── CreateTimelineUseCase.java
│   │   ├── CreateTimelineCommand.java   # Input
│   │   └── CreateTimelineResult.java    # Output
│   ├── AddEventToTimeline/
│   │   ├── AddEventToTimelineUseCase.java
│   │   ├── AddEventCommand.java
│   │   └── AddEventResult.java
│   ├── GetTimeline/
│   │   ├── GetTimelineUseCase.java
│   │   ├── GetTimelineQuery.java
│   │   └── TimelineView.java            # DTO de leitura (pode ser diferente do domínio)
│   └── DeleteTimeline/
│       └── ...
└── shared/
    ├── UseCase.java                      # Interface genérica
    └── ApplicationException.java
```

```java
// Interface base para casos de uso
public interface UseCase<I, O> {
    O execute(I input);
}

// Caso de uso de criação
@Service
@Transactional
public class CreateTimelineUseCase implements UseCase<CreateTimelineCommand, CreateTimelineResult> {

    private final TimelineRepository timelineRepository;
    private final DomainEventPublisher eventPublisher;

    public CreateTimelineUseCase(TimelineRepository timelineRepository,
                                  DomainEventPublisher eventPublisher) {
        this.timelineRepository = timelineRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public CreateTimelineResult execute(CreateTimelineCommand command) {
        var timeline = Timeline.create(command.name(), command.description());
        timelineRepository.save(timeline);
        eventPublisher.publishAll(timeline.pullDomainEvents());
        return CreateTimelineResult.from(timeline);
    }
}

// Command (imutável)
public record CreateTimelineCommand(
    String name,
    String description,
    TimelineVisibility visibility
) {
    public CreateTimelineCommand {
        Objects.requireNonNull(name, "Name is required");
    }
}
```

---

### Camada de Infraestrutura (`infrastructure/`)

```
infrastructure/
├── persistence/
│   ├── jpa/
│   │   ├── TimelineJpaRepository.java      # Spring Data JPA interface
│   │   ├── TimelineEntity.java             # Entidade JPA (separada do domínio!)
│   │   ├── TimelineRepositoryAdapter.java  # Implementa a porta do domínio
│   │   └── TimelineMapper.java             # Converte entre domínio e JPA
├── web/
│   ├── rest/
│   │   ├── TimelineController.java
│   │   ├── dto/
│   │   │   ├── CreateTimelineRequest.java
│   │   │   └── TimelineResponse.java
│   │   └── mapper/
│   │       └── TimelineRestMapper.java
│   └── exception/
│       └── GlobalExceptionHandler.java
└── config/
    ├── UseCaseConfig.java                  # Wiring dos casos de uso
    └── SecurityConfig.java
```

```java
// Adapter — implementa a porta do domínio usando JPA
@Repository
public class TimelineRepositoryAdapter implements TimelineRepository {

    private final TimelineJpaRepository jpaRepository;
    private final TimelineMapper mapper;

    @Override
    public void save(Timeline timeline) {
        var entity = mapper.toEntity(timeline);
        jpaRepository.save(entity);
    }

    @Override
    public Optional<Timeline> findById(TimelineId id) {
        return jpaRepository.findById(id.value())
            .map(mapper::toDomain);
    }
}

// Controller — fina camada de entrada, só traduz HTTP → UseCase
@RestController
@RequestMapping("/api/v1/timelines")
public class TimelineController {

    private final CreateTimelineUseCase createTimeline;
    private final GetTimelineUseCase getTimeline;
    private final TimelineRestMapper mapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TimelineResponse create(@RequestBody @Valid CreateTimelineRequest request) {
        var command = mapper.toCommand(request);
        var result = createTimeline.execute(command);
        return mapper.toResponse(result);
    }

    @GetMapping("/{id}")
    public TimelineResponse getById(@PathVariable String id) {
        var query = new GetTimelineQuery(TimelineId.of(id));
        var result = getTimeline.execute(query);
        return mapper.toResponse(result);
    }
}
```

---

### Tratamento de Exceções

```java
// Hierarquia de exceções
public class DomainException extends RuntimeException { ... }
public class TimelineNotFoundException extends DomainException { ... }
public class ApplicationException extends RuntimeException { ... }

// Handler global
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ErrorResponse> handleDomain(DomainException ex) {
        return ResponseEntity.badRequest()
            .body(ErrorResponse.of("DOMAIN_ERROR", ex.getMessage()));
    }

    @ExceptionHandler(TimelineNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(TimelineNotFoundException ex) {
        return ResponseEntity.notFound().build();
    }
}
```

---

### Modelo de Dados (JPA — separado do domínio)

```java
@Entity
@Table(name = "timelines")
public class TimelineEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    private TimelineVisibilityEntity visibility;

    @OneToMany(mappedBy = "timeline", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TimelineEventEntity> events;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;
}

@Entity
@Table(name = "timeline_events")
public class TimelineEventEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "timeline_id")
    private TimelineEntity timeline;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String contentText;

    @Enumerated(EnumType.STRING)
    private ContentTypeEntity contentType;

    @Column(nullable = false)
    private Instant occurredAt;

    @Column(nullable = false)
    private Integer displayOrder;
}

@Entity
@Table(name = "timeline_connections")
public class TimelineConnectionEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_event_id")
    private TimelineEventEntity sourceEvent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_event_id")
    private TimelineEventEntity targetEvent;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    private ConnectionTypeEntity connectionType;
}
```

---

### Migrations (Flyway)

```
resources/
└── db/migration/
    ├── V1__create_timelines.sql
    ├── V2__create_timeline_events.sql
    ├── V3__create_timeline_connections.sql
    └── V4__add_indexes.sql
```

Sempre use Flyway. Nunca use `spring.jpa.hibernate.ddl-auto=create` em produção.

---

## Frontend — Angular

### Estrutura de Módulos

```
src/app/
├── core/                           # Singleton — importado apenas no AppModule
│   ├── auth/
│   ├── http/
│   │   └── api-interceptor.ts
│   └── guards/
├── domain/                         # Modelos e interfaces (espelha o domínio do backend)
│   ├── timeline.model.ts
│   ├── timeline-event.model.ts
│   └── timeline-connection.model.ts
├── features/                       # Módulos de funcionalidade (lazy loaded)
│   ├── timeline-canvas/            # Feature principal — canvas visual
│   │   ├── timeline-canvas.module.ts
│   │   ├── components/
│   │   │   ├── canvas/
│   │   │   ├── timeline-track/
│   │   │   ├── event-node/
│   │   │   └── connection-line/
│   │   ├── services/
│   │   │   ├── canvas-state.service.ts
│   │   │   └── canvas-renderer.service.ts
│   │   └── store/                  # NgRx (se usado)
│   ├── timeline-editor/            # Edição de timeline e eventos
│   └── timeline-list/              # Listagem e busca
├── shared/                         # Componentes, pipes, diretivas reutilizáveis
│   ├── components/
│   │   ├── button/
│   │   └── modal/
│   ├── pipes/
│   └── directives/
└── infrastructure/
    └── api/
        ├── timeline-api.service.ts
        └── dto/
            ├── timeline.dto.ts
            └── create-timeline.dto.ts
```

### Modelos de Domínio (Frontend)

```typescript
// domain/timeline.model.ts
export interface Timeline {
  id: string;
  name: string;
  description: string;
  events: TimelineEvent[];
  visibility: TimelineVisibility;
  createdAt: Date;
}

export interface TimelineEvent {
  id: string;
  timelineId: string;
  title: string;
  content: EventContent;
  occurredAt: Date;
  displayOrder: number;
  position?: CanvasPosition; // calculado no frontend
}

export interface EventContent {
  text: string;
  type: ContentType;
  metadata: Record<string, string>;
}

export interface CanvasPosition {
  x: number;
  y: number;
}

export type TimelineVisibility = 'PUBLIC' | 'PRIVATE' | 'UNLISTED';
export type ContentType = 'TEXT' | 'RICH_TEXT' | 'MARKDOWN';
```

### Serviço de API (Adapter)

```typescript
// infrastructure/api/timeline-api.service.ts
@Injectable({ providedIn: 'root' })
export class TimelineApiService {

  private readonly baseUrl = `${environment.apiUrl}/timelines`;

  constructor(private http: HttpClient) {}

  getTimeline(id: string): Observable<Timeline> {
    return this.http.get<TimelineDto>(`${this.baseUrl}/${id}`).pipe(
      map(dto => TimelineMapper.toDomain(dto))
    );
  }

  createTimeline(command: CreateTimelineCommand): Observable<Timeline> {
    const dto = TimelineMapper.toCreateDto(command);
    return this.http.post<TimelineDto>(this.baseUrl, dto).pipe(
      map(response => TimelineMapper.toDomain(response))
    );
  }

  addEvent(timelineId: string, command: AddEventCommand): Observable<TimelineEvent> {
    const dto = TimelineMapper.toAddEventDto(command);
    return this.http.post<TimelineEventDto>(
      `${this.baseUrl}/${timelineId}/events`, dto
    ).pipe(
      map(response => TimelineMapper.toEventDomain(response))
    );
  }
}
```

### Canvas State (Serviço de Estado do Canvas)

```typescript
// features/timeline-canvas/services/canvas-state.service.ts
@Injectable()
export class CanvasStateService {

  private readonly _timelines = signal<Timeline[]>([]);
  private readonly _selectedEvent = signal<TimelineEvent | null>(null);
  private readonly _zoom = signal<number>(1);
  private readonly _offset = signal<CanvasPosition>({ x: 0, y: 0 });

  // Signals públicos (readonly)
  readonly timelines = this._timelines.asReadonly();
  readonly selectedEvent = this._selectedEvent.asReadonly();
  readonly zoom = this._zoom.asReadonly();

  // Computed
  readonly visibleTimelines = computed(() =>
    this._timelines().filter(t => t.events.length > 0)
  );

  addTimeline(timeline: Timeline): void {
    this._timelines.update(timelines => [...timelines, timeline]);
  }

  selectEvent(event: TimelineEvent | null): void {
    this._selectedEvent.set(event);
  }

  setZoom(zoom: number): void {
    const clampedZoom = Math.min(Math.max(zoom, 0.1), 5);
    this._zoom.set(clampedZoom);
  }

  pan(delta: CanvasPosition): void {
    this._offset.update(offset => ({
      x: offset.x + delta.x,
      y: offset.y + delta.y
    }));
  }
}
```

### Componente de Canvas

```typescript
// features/timeline-canvas/components/canvas/canvas.component.ts
@Component({
  selector: 'app-canvas',
  standalone: true,
  imports: [TimelineTrackComponent, ConnectionLineComponent, CommonModule],
  template: `
    <div class="canvas-container"
         (wheel)="onWheel($event)"
         (mousedown)="onMouseDown($event)"
         (mousemove)="onMouseMove($event)"
         (mouseup)="onMouseUp($event)">

      <svg class="connections-layer" [attr.viewBox]="viewBox()">
        @for (connection of connections(); track connection.id) {
          <app-connection-line [connection]="connection" />
        }
      </svg>

      <div class="timelines-layer"
           [style.transform]="canvasTransform()">
        @for (timeline of canvasState.visibleTimelines(); track timeline.id) {
          <app-timeline-track
            [timeline]="timeline"
            [zoom]="canvasState.zoom()"
            (eventSelected)="onEventSelected($event)"
          />
        }
      </div>
    </div>
  `
})
export class CanvasComponent {

  protected readonly canvasState = inject(CanvasStateService);

  protected readonly canvasTransform = computed(() => {
    const zoom = this.canvasState.zoom();
    const offset = this.canvasState['_offset']();
    return `translate(${offset.x}px, ${offset.y}px) scale(${zoom})`;
  });

  protected onWheel(event: WheelEvent): void {
    event.preventDefault();
    const delta = event.deltaY > 0 ? -0.1 : 0.1;
    this.canvasState.setZoom(this.canvasState.zoom() + delta);
  }

  protected onEventSelected(event: TimelineEvent): void {
    this.canvasState.selectEvent(event);
  }
}
```

---

## Padrões de Código

### Nomenclatura

| Elemento | Padrão | Exemplo |
|---|---|---|
| Classes de domínio | PascalCase | `Timeline`, `TimelineEvent` |
| Value Objects | PascalCase | `TimelineId`, `EventContent` |
| Use Cases | PascalCase + UseCase | `CreateTimelineUseCase` |
| Commands/Queries | PascalCase + Command/Query | `CreateTimelineCommand` |
| Repositories (porta) | PascalCase + Repository | `TimelineRepository` |
| Adapters | PascalCase + Adapter | `TimelineRepositoryAdapter` |
| Controllers | PascalCase + Controller | `TimelineController` |
| Angular Services | camelCase + Service | `canvasStateService` |
| Angular Components | kebab-case (seletor) | `app-timeline-track` |

### Regras Gerais

**Backend:**
- Nunca exponha entidades JPA diretamente na API — sempre use DTOs
- Nunca injete repositórios nos controllers — passe pelos use cases
- Use `record` para Commands, Queries e DTOs imutáveis
- Prefira construtores sobre setters nas entidades de domínio
- Sempre valide no domínio, não só na camada web
- Testes unitários para domínio e aplicação, testes de integração para infraestrutura

**Frontend:**
- Use Signals para estado reativo (Angular 17+)
- Use `standalone components` — evite NgModules desnecessários
- Mantenha componentes "burros" (dumb) — lógica nos serviços
- DTOs de API nunca devem vazar para os componentes — use mappers
- Lazy load em todas as features

---

## Testes

### Backend

```
test/
├── unit/
│   ├── domain/
│   │   └── TimelineTest.java           # Testa regras de negócio puras
│   └── application/
│       └── CreateTimelineUseCaseTest.java  # Testa com mocks dos repositórios
└── integration/
    ├── persistence/
    │   └── TimelineRepositoryAdapterTest.java  # Testa com banco real (Testcontainers)
    └── web/
        └── TimelineControllerTest.java         # Testa endpoints (MockMvc)
```

```java
// Exemplo de teste de domínio — sem Spring, rápido
class TimelineTest {

    @Test
    void shouldCreateTimelineWithValidName() {
        var timeline = Timeline.create("Saga Matador de Drakars", "Sete livros");
        assertThat(timeline.getName()).isEqualTo("Saga Matador de Drakars");
        assertThat(timeline.getEvents()).isEmpty();
        assertThat(timeline.getDomainEvents()).hasSize(1);
    }

    @Test
    void shouldRejectBlankName() {
        assertThatThrownBy(() -> Timeline.create("", "desc"))
            .isInstanceOf(DomainException.class)
            .hasMessage("Timeline name cannot be blank");
    }
}
```

### Frontend

```typescript
// Testes com Jest + Testing Library
describe('CanvasStateService', () => {
  let service: CanvasStateService;

  beforeEach(() => {
    service = new CanvasStateService();
  });

  it('should clamp zoom between 0.1 and 5', () => {
    service.setZoom(10);
    expect(service.zoom()).toBe(5);

    service.setZoom(-1);
    expect(service.zoom()).toBe(0.1);
  });
});
```

---

## Configuração do Projeto

### Backend — `application.yml`

```yaml
spring:
  datasource:
    url: ${DB_URL:jdbc:postgresql://localhost:5432/chronicle}
    username: ${DB_USER:chronicle}
    password: ${DB_PASSWORD:chronicle}
  jpa:
    hibernate:
      ddl-auto: validate          # Nunca create/create-drop em produção
    show-sql: false
  flyway:
    enabled: true
    locations: classpath:db/migration

chronicle:
  api:
    prefix: /api/v1
  cors:
    allowed-origins: ${CORS_ORIGINS:http://localhost:4200}
```

### Frontend — `environment.ts`

```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api/v1'
};
```

### Docker Compose

```yaml
version: '3.9'
services:
  postgres:
    image: postgres:16
    environment:
      POSTGRES_DB: chronicle
      POSTGRES_USER: chronicle
      POSTGRES_PASSWORD: chronicle
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

  backend:
    build: ./backend
    ports:
      - "8080:8080"
    depends_on:
      - postgres
    environment:
      DB_URL: jdbc:postgresql://postgres:5432/chronicle
      DB_USER: chronicle
      DB_PASSWORD: chronicle

  frontend:
    build: ./frontend
    ports:
      - "4200:80"
    depends_on:
      - backend

volumes:
  postgres_data:
```

---

## Endpoints da API

```
GET    /api/v1/timelines                    → Lista timelines
POST   /api/v1/timelines                    → Cria timeline
GET    /api/v1/timelines/{id}               → Busca timeline por ID
PUT    /api/v1/timelines/{id}               → Atualiza timeline
DELETE /api/v1/timelines/{id}               → Remove timeline

POST   /api/v1/timelines/{id}/events        → Adiciona evento
PUT    /api/v1/timelines/{id}/events/{eid}  → Atualiza evento
DELETE /api/v1/timelines/{id}/events/{eid}  → Remove evento

POST   /api/v1/connections                  → Cria conexão entre eventos
DELETE /api/v1/connections/{id}             → Remove conexão

GET    /api/v1/timelines/{id}/export        → Exporta timeline (JSON/PDF)
```

---

## Convenções para o Claude

Quando trabalhar neste projeto, sempre:

1. **Respeite a regra de dependência** — infraestrutura depende da aplicação, aplicação depende do domínio, nunca o contrário
2. **Crie testes** para toda lógica de domínio e casos de uso
3. **Use records** para objetos imutáveis (Commands, Queries, DTOs, Value Objects simples)
4. **Separe sempre** entidade JPA da entidade de domínio — são coisas diferentes
5. **Valide no domínio** — não confie só no Bean Validation da camada web
6. **Nomeie pelos casos de uso** — o nome de uma classe deve revelar sua intenção
7. **No Angular**, use Signals em vez de BehaviorSubject para estado local
8. **Não vaze DTOs** da infraestrutura para os componentes Angular — use mappers
9. **Flyway para tudo** — nenhuma mudança de schema sem migration versionada
10. **Commits semânticos**: `feat:`, `fix:`, `refactor:`, `test:`, `docs:`

---

## Roadmap de Features

### MVP
- [x] CRUD de timelines
- [x] CRUD de eventos em uma timeline
- [x] Canvas visual com uma timeline horizontal
- [x] Zoom e pan no canvas
- [x] Inserir texto em eventos

### V2
- [ ] Múltiplas timelines em paralelo no mesmo canvas
- [ ] Conexões visuais entre eventos de timelines diferentes
- [ ] Editor de rich text nos eventos
- [ ] Exportar timeline como imagem

### V3
- [ ] Colaboração em tempo real (WebSocket)
- [ ] Templates de timeline
- [ ] Importar/exportar JSON
- [ ] Tags e filtros de eventos