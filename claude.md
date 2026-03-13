# CLAUDE.md — Chronicle: Interactive Timeline Platform

## Visão do Projeto

**Chronicle** é uma plataforma visual para criação e gerenciamento de timelines interativas, onde o usuário pode:
- Criar timelines com eventos posicionados cronologicamente
- Inserir textos, notas e documentos em pontos específicos da timeline
- Desenhar múltiplas timelines em paralelo (ex: linha do tempo de personagens, de eras, de eventos)
- Conectar eventos entre timelines diferentes
- Navegar visualmente por períodos de tempo
- Executar e simular narrativas interativas através da **Story Engine**

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
├── story/                      # Story Engine — núcleo narrativo
│   ├── world/
│   │   ├── WorldModel.java         # Agregado raiz do mundo narrativo
│   │   ├── WorldModelId.java
│   │   ├── WorldState.java         # Estado atual do mundo (versionável)
│   │   ├── WorldStateVersion.java  # Value Object de versão
│   │   └── WorldModelRepository.java
│   ├── entity/
│   │   ├── StoryEntity.java        # Entidade narrativa base (Character, Location, etc.)
│   │   ├── StoryEntityId.java
│   │   ├── EntityProperty.java     # Value Object — propriedade mutável/imutável
│   │   ├── EntityRelation.java     # Value Object — relação entre entidades
│   │   └── EntityType.java         # Enum: CHARACTER, LOCATION, FACTION, ARTIFACT, ...
│   ├── fact/
│   │   ├── NarrativeFact.java      # Fato narrativo verificável
│   │   ├── FactId.java
│   │   ├── FactPredicate.java      # Value Object — sujeito, predicado, objeto
│   │   └── FactRepository.java
│   ├── scene/
│   │   ├── Scene.java              # Agregado — cena narrativa
│   │   ├── SceneId.java
│   │   ├── SceneRequirement.java   # Condição de ativação da cena
│   │   ├── SceneEffect.java        # Efeito produzido pela cena no mundo
│   │   ├── NarrativePriority.java  # Value Object de prioridade
│   │   └── SceneRepository.java
│   ├── timeline/
│   │   ├── NarrativeTimeline.java  # Linha do tempo narrativa (ordem e dependências)
│   │   ├── NarrativeEvent.java     # Evento registrado na timeline narrativa
│   │   └── NarrativeTimelineRepository.java
│   ├── character/
│   │   ├── CharacterModel.java     # Modelo rico de personagem
│   │   ├── CharacterGoal.java      # Value Object — objetivo
│   │   ├── CharacterBelief.java    # Value Object — crença
│   │   ├── CharacterEmotion.java   # Value Object — emoção atual
│   │   └── SocialRelation.java     # Value Object — relação social
│   ├── rule/
│   │   ├── NarrativeRule.java      # Regra narrativa (interface)
│   │   ├── UniqueEventRule.java    # Evento único não pode repetir
│   │   ├── AlivePresenceRule.java  # Morto não aparece em cena
│   │   └── CausalityRule.java      # Causalidade deve ser respeitada
│   ├── conflict/
│   │   ├── NarrativeConflict.java  # Conflito ou contradição detectada
│   │   └── ConflictType.java       # Enum: DEAD_CHARACTER_IN_SCENE, DUPLICATE_FACT, etc.
│   └── engine/
│       ├── StoryEngine.java        # Serviço de domínio principal da engine
│       ├── SceneSelector.java      # Lógica de escolha narrativa
│       └── NarrativeValidator.java # Valida requisitos e consistência antes de executar
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
├── story/                               # Story Engine — casos de uso
│   ├── AdvanceStory/
│   │   ├── AdvanceStoryUseCase.java
│   │   ├── AdvanceStoryCommand.java
│   │   └── AdvanceStoryResult.java
│   ├── GetAvailableScenes/
│   │   ├── GetAvailableScenesUseCase.java
│   │   ├── GetAvailableScenesQuery.java
│   │   └── AvailableScenesView.java
│   ├── ApplyEvent/
│   │   ├── ApplyEventUseCase.java
│   │   ├── ApplyEventCommand.java
│   │   └── ApplyEventResult.java
│   ├── QueryWorldState/
│   │   ├── QueryWorldStateUseCase.java
│   │   ├── WorldStateQuery.java
│   │   └── WorldStateView.java
│   ├── GetCharacterState/
│   │   ├── GetCharacterStateUseCase.java
│   │   ├── GetCharacterStateQuery.java
│   │   └── CharacterStateView.java
│   ├── SeedNarrative/
│   │   ├── SeedNarrativeUseCase.java   # Inicializa história com estado e personagens iniciais
│   │   ├── SeedNarrativeCommand.java
│   │   └── SeedNarrativeResult.java
│   └── SimulateFutures/
│       ├── SimulateFuturesUseCase.java  # Simula múltiplos cenários possíveis
│       ├── SimulateFuturesQuery.java
│       └── SimulationView.java
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
    ├── V4__add_indexes.sql
    ├── V5__create_world_models.sql
    ├── V6__create_story_entities.sql
    ├── V7__create_narrative_facts.sql
    ├── V8__create_scenes.sql
    └── V9__create_narrative_timeline.sql
```

Sempre use Flyway. Nunca use `spring.jpa.hibernate.ddl-auto=create` em produção.

---

## Story Engine

A **Story Engine** é o subsistema narrativo do Chronicle. Ela gerencia o estado de um mundo narrativo, executa cenas, aplica efeitos, detecta contradições e simula futuros possíveis. É completamente separada do motor de timelines visuais — ambos coexistem no mesmo projeto mas com domínios distintos.

### Princípio de Separação

```
Story Engine
├── Motor Narrativo   → lógica, regras, simulação          (domínio/story/engine)
└── Conteúdo Narrativo → cenas, personagens, fatos iniciais  (seeds / content files)
```

O motor nunca conhece o conteúdo específico de uma história. O conteúdo é injetado via seeds e repositórios.

---

### 1. Modelo de Mundo (World Model)

Representa estruturalmente o mundo de uma história. Cada entidade possui ID único, propriedades mutáveis, propriedades imutáveis e relações com outras entidades.

```java
public class WorldModel extends AggregateRoot<WorldModelId> {

    private WorldModelId id;
    private String name;
    private WorldState currentState;
    private List<StoryEntity> entities;

    public StoryEntity addEntity(String name, EntityType type,
                                  Map<String, Object> mutableProps,
                                  Map<String, Object> immutableProps) {
        var entity = StoryEntity.create(name, type, mutableProps, immutableProps);
        this.entities.add(entity);
        registerEvent(new EntityAddedToWorld(this.id, entity.getId()));
        return entity;
    }

    public void addRelation(StoryEntityId from, StoryEntityId to, String relationType) {
        var fromEntity = findEntityById(from);
        fromEntity.addRelation(EntityRelation.of(to, relationType));
    }
}

// Tipos de entidade suportados
public enum EntityType {
    CHARACTER, LOCATION, FACTION, ARTIFACT, EVENT_HISTORICAL, GLOBAL_CONDITION
}

// Propriedade de entidade — mutável ou imutável
public record EntityProperty(String key, Object value, boolean immutable) {
    public EntityProperty withValue(Object newValue) {
        if (immutable) throw new DomainException("Cannot mutate immutable property: " + key);
        return new EntityProperty(key, newValue, false);
    }
}
```

---

### 2. Estado do Mundo (World State)

Snapshot versionável do mundo narrativo em um dado momento.

```java
public class WorldState {

    private WorldStateVersion version;
    private Map<StoryEntityId, EntitySnapshot> entitySnapshots;
    private List<NarrativeFact> activeFacts;
    private List<NarrativeEvent> executedEvents;
    private Map<String, Object> globalConditions; // guerra, paz, destruição, etc.

    public WorldState advance(List<SceneEffect> effects) {
        // Cria nova versão do estado aplicando os efeitos
        var builder = WorldStateBuilder.from(this).nextVersion();
        effects.forEach(effect -> effect.applyTo(builder));
        return builder.build();
    }

    public WorldState rollbackTo(WorldStateVersion version) {
        // Retrocede para uma versão anterior
    }
}

public record WorldStateVersion(int major, int minor) implements Comparable<WorldStateVersion> {
    public WorldStateVersion next() {
        return new WorldStateVersion(major, minor + 1);
    }
}
```

---

### 3. Sistema de Fatos (Facts)

Fatos narrativos verificáveis com sujeito, predicado, objeto opcional e validade temporal.

```java
public class NarrativeFact {

    private FactId id;
    private FactPredicate predicate;
    private Instant validFrom;
    private Instant validUntil;     // null = válido indefinidamente
    private boolean contradicted;

    public boolean isActiveAt(Instant moment) {
        return !contradicted
            && !moment.isBefore(validFrom)
            && (validUntil == null || moment.isBefore(validUntil));
    }

    public void contradict() {
        this.contradicted = true;
        registerEvent(new FactContradicted(this.id));
    }
}

public record FactPredicate(
    StoryEntityId subject,
    String predicate,          // "IS_ALIVE", "TRUSTS", "POSSESSES", "IS_DESTROYED"
    StoryEntityId object       // opcional — null para predicados unários
) {}

// Exemplos de fatos:
// FactPredicate(kingId, "IS_ALIVE", null)
// FactPredicate(characterId, "TRUSTS", otherId)
// FactPredicate(characterId, "POSSESSES", artifactId)
// FactPredicate(cityId, "IS_DESTROYED", null)
```

---

### 4. Sistema de Requisitos (Requirements)

Condições que devem ser satisfeitas para uma cena ser ativável.

```java
public interface SceneRequirement {
    boolean isSatisfiedBy(WorldState state);
    String describe();
}

// Implementações concretas
public record CharacterAliveRequirement(StoryEntityId characterId) implements SceneRequirement {
    public boolean isSatisfiedBy(WorldState state) {
        return state.hasFact(characterId, "IS_ALIVE");
    }
}

public record CharacterInLocationRequirement(StoryEntityId characterId,
                                              StoryEntityId locationId) implements SceneRequirement {
    public boolean isSatisfiedBy(WorldState state) {
        return state.hasFact(characterId, "IS_AT", locationId);
    }
}

public record FactExistsRequirement(FactPredicate predicate) implements SceneRequirement {
    public boolean isSatisfiedBy(WorldState state) {
        return state.hasFact(predicate);
    }
}

public record PriorEventExecutedRequirement(SceneId sceneId) implements SceneRequirement {
    public boolean isSatisfiedBy(WorldState state) {
        return state.hasExecuted(sceneId);
    }
}
```

---

### 5. Sistema de Efeitos (Effects)

Efeitos que uma cena produz no estado do mundo ao ser executada.

```java
public interface SceneEffect {
    void applyTo(WorldStateBuilder stateBuilder);
    String describe();
}

// Implementações concretas
public record KillCharacterEffect(StoryEntityId characterId) implements SceneEffect {
    public void applyTo(WorldStateBuilder builder) {
        builder.removeFact(characterId, "IS_ALIVE");
        builder.addFact(characterId, "IS_DEAD");
    }
}

public record MoveCharacterEffect(StoryEntityId characterId,
                                   StoryEntityId targetLocationId) implements SceneEffect {
    public void applyTo(WorldStateBuilder builder) {
        builder.removeFactsWithSubjectAndPredicate(characterId, "IS_AT");
        builder.addFact(characterId, "IS_AT", targetLocationId);
    }
}

public record ChangeAllegianceEffect(StoryEntityId characterId,
                                      StoryEntityId newFactionId) implements SceneEffect {
    public void applyTo(WorldStateBuilder builder) {
        builder.removeFactsWithSubjectAndPredicate(characterId, "BELONGS_TO");
        builder.addFact(characterId, "BELONGS_TO", newFactionId);
    }
}

public record CreateFactEffect(FactPredicate predicate) implements SceneEffect {
    public void applyTo(WorldStateBuilder builder) {
        builder.addFact(predicate);
    }
}

public record RemoveFactEffect(FactPredicate predicate) implements SceneEffect {
    public void applyTo(WorldStateBuilder builder) {
        builder.removeFact(predicate);
    }
}
```

---

### 6. Sistema de Cenas (Scenes)

Uma cena é a unidade executável da narrativa.

```java
public class Scene extends AggregateRoot<SceneId> {

    private SceneId id;
    private String narrativeDescription;
    private List<StoryEntityId> involvedCharacters;
    private List<SceneRequirement> requirements;
    private List<SceneEffect> effects;
    private NarrativePriority priority;
    private List<String> thematicTags;
    private boolean unique;           // cena única não pode repetir

    public boolean canExecuteIn(WorldState state) {
        return requirements.stream().allMatch(req -> req.isSatisfiedBy(state));
    }

    public WorldState execute(WorldState currentState) {
        return currentState.advance(effects);
    }
}

public record NarrativePriority(int value) implements Comparable<NarrativePriority> {
    public static NarrativePriority of(int value) {
        if (value < 0 || value > 100) throw new DomainException("Priority must be 0-100");
        return new NarrativePriority(value);
    }
}
```

---

### 7. Sistema de Linha Temporal Narrativa (Narrative Timeline)

Registra a ordem dos eventos, dependências e histórico do mundo.

```java
public class NarrativeTimeline extends AggregateRoot<NarrativeTimelineId> {

    private List<NarrativeEvent> events;
    private List<WorldState> stateHistory; // um estado por evento

    public void record(Scene executedScene, WorldState resultingState) {
        var event = NarrativeEvent.of(executedScene.getId(), Instant.now(), resultingState.getVersion());
        this.events.add(event);
        this.stateHistory.add(resultingState);
        registerEvent(new NarrativeEventRecorded(event));
    }

    public WorldState getStateAt(WorldStateVersion version) {
        return stateHistory.stream()
            .filter(s -> s.getVersion().equals(version))
            .findFirst()
            .orElseThrow(() -> new DomainException("State version not found: " + version));
    }

    public List<NarrativeEvent> getEventsDependingOn(SceneId sceneId) {
        return events.stream()
            .filter(e -> e.dependsOn(sceneId))
            .toList();
    }
}
```

---

### 8. Sistema de Conflitos e Contradições

Detecta incoerências antes de executar cenas.

```java
public class NarrativeConflict {

    private ConflictType type;
    private String description;
    private List<StoryEntityId> involvedEntities;
    private SceneId conflictingScene;

    public static NarrativeConflict deadCharacterInScene(StoryEntityId characterId, SceneId sceneId) {
        return new NarrativeConflict(
            ConflictType.DEAD_CHARACTER_IN_SCENE,
            "Character " + characterId + " is dead but appears in scene " + sceneId,
            List.of(characterId),
            sceneId
        );
    }
}

public enum ConflictType {
    DEAD_CHARACTER_IN_SCENE,
    CHARACTER_IN_MULTIPLE_LOCATIONS,
    CONTRADICTORY_FACTS,
    IMPOSSIBLE_CAUSALITY,
    UNIQUE_EVENT_REPEATED
}
```

---

### 9. Sistema de Regras Narrativas

Regras que evitam incoerências. São avaliadas pelo `NarrativeValidator` antes de qualquer execução.

```java
public interface NarrativeRule {
    List<NarrativeConflict> validate(Scene scene, WorldState state);
    String describe();
}

// Regra: personagem não pode estar em dois lugares ao mesmo tempo
public class SingleLocationRule implements NarrativeRule {
    public List<NarrativeConflict> validate(Scene scene, WorldState state) {
        return scene.getInvolvedCharacters().stream()
            .filter(c -> state.countLocationsOf(c) > 1)
            .map(c -> NarrativeConflict.characterInMultipleLocations(c, scene.getId()))
            .toList();
    }
}

// Regra: morto não fala / aparece
public class AlivePresenceRule implements NarrativeRule {
    public List<NarrativeConflict> validate(Scene scene, WorldState state) {
        return scene.getInvolvedCharacters().stream()
            .filter(c -> state.hasFact(c, "IS_DEAD"))
            .map(c -> NarrativeConflict.deadCharacterInScene(c, scene.getId()))
            .toList();
    }
}

// Regra: evento único não repete
public class UniqueEventRule implements NarrativeRule {
    public List<NarrativeConflict> validate(Scene scene, WorldState state) {
        if (scene.isUnique() && state.hasExecuted(scene.getId())) {
            return List.of(NarrativeConflict.uniqueEventRepeated(scene.getId()));
        }
        return List.of();
    }
}
```

---

### 10. Sistema de Escolha Narrativa (Scene Selector)

Decide qual cena executar quando múltiplas são possíveis.

```java
@DomainService
public class SceneSelector {

    public Optional<Scene> select(List<Scene> availableScenes, WorldState state, SelectionStrategy strategy) {
        var candidates = availableScenes.stream()
            .filter(s -> s.canExecuteIn(state))
            .toList();

        return switch (strategy) {
            case HIGHEST_PRIORITY -> candidates.stream()
                .max(Comparator.comparing(Scene::getPriority));
            case DRAMATIC_TENSION -> selectByDramaticTension(candidates, state);
            case WEIGHTED_RANDOM   -> selectWeightedRandom(candidates);
            case CHARACTER_GOAL    -> selectByCharacterGoals(candidates, state);
        };
    }
}

public enum SelectionStrategy {
    HIGHEST_PRIORITY,
    DRAMATIC_TENSION,
    WEIGHTED_RANDOM,
    CHARACTER_GOAL
}
```

---

### 11. Modelo de Personagem (Character Model)

Personagens possuem objetivos, crenças, emoções e relações sociais para decisões coerentes.

```java
public class CharacterModel {

    private StoryEntityId characterId;
    private List<CharacterGoal> goals;
    private List<CharacterBelief> beliefs;
    private CharacterEmotion currentEmotion;
    private List<SocialRelation> socialRelations;
    private PhysicalState physicalState;
    private MentalState mentalState;
}

public record CharacterGoal(String description, int priority, boolean achieved) {}
public record CharacterBelief(String statement, float certainty) {}
public record SocialRelation(StoryEntityId targetId, String relationType, float intensity) {}

public enum PhysicalState { HEALTHY, WOUNDED, CRITICAL, DEAD }
public enum MentalState  { STABLE, SHAKEN, TRAUMATIZED, INSANE }
```

---

### 12. Sistema de Consultas Narrativas

```java
@DomainService
public class NarrativeQueryService {

    // Quais cenas são possíveis agora?
    public List<Scene> getAvailableScenes(WorldState state, List<Scene> allScenes) {
        return allScenes.stream()
            .filter(s -> s.canExecuteIn(state))
            .sorted(Comparator.comparing(Scene::getPriority).reversed())
            .toList();
    }

    // Quais personagens estão em conflito?
    public List<StoryEntityId> getCharactersInConflict(WorldState state) { ... }

    // Quais eventos levaram a este estado?
    public List<NarrativeEvent> getCausalChain(WorldState state, WorldStateVersion from) { ... }

    // Qual personagem possui o artefato?
    public Optional<StoryEntityId> getPossessorOf(StoryEntityId artifactId, WorldState state) {
        return state.findFactsWithPredicateAndObject(artifactId, "POSSESSES")
            .findFirst()
            .map(FactPredicate::subject);
    }
}
```

---

### 13. Motor Principal (Story Engine)

Serviço de domínio que orquestra todo o ciclo narrativo.

```java
@DomainService
public class StoryEngine {

    private final NarrativeValidator validator;
    private final SceneSelector selector;
    private final NarrativeQueryService queryService;

    /**
     * Avança a história executando a próxima cena válida.
     */
    public AdvanceResult advanceStory(WorldModel world, NarrativeTimeline timeline,
                                       List<Scene> allScenes, SelectionStrategy strategy) {
        var state = world.getCurrentState();
        var available = queryService.getAvailableScenes(state, allScenes);

        if (available.isEmpty()) return AdvanceResult.noScenesAvailable();

        var selectedScene = selector.select(available, state, strategy)
            .orElseThrow(() -> new DomainException("Scene selection failed"));

        var conflicts = validator.validate(selectedScene, state);
        if (!conflicts.isEmpty()) return AdvanceResult.conflicts(conflicts);

        var newState = selectedScene.execute(state);
        world.applyState(newState);
        timeline.record(selectedScene, newState);

        return AdvanceResult.success(selectedScene, newState);
    }

    /**
     * Simula múltiplos futuros possíveis sem modificar o estado real.
     */
    public List<SimulatedFuture> simulateFutures(WorldModel world, List<Scene> allScenes, int depth) {
        // Cria cópias do estado e simula recursivamente
    }
}
```

---

### 14. Validação Narrativa

```java
@DomainService
public class NarrativeValidator {

    private final List<NarrativeRule> rules;

    public List<NarrativeConflict> validate(Scene scene, WorldState state) {
        // 1. Verifica requisitos da cena
        var unmetRequirements = scene.getRequirements().stream()
            .filter(req -> !req.isSatisfiedBy(state))
            .map(req -> NarrativeConflict.unmetRequirement(scene.getId(), req.describe()))
            .toList();

        // 2. Aplica todas as regras narrativas
        var ruleConflicts = rules.stream()
            .flatMap(rule -> rule.validate(scene, state).stream())
            .toList();

        return Stream.concat(unmetRequirements.stream(), ruleConflicts.stream()).toList();
    }
}
```

---

### 15. API da Story Engine

Endpoints expostos pela engine narrativa:

```
POST   /api/v1/stories/{worldId}/advance          → advanceStory()
GET    /api/v1/stories/{worldId}/scenes/available → getAvailableScenes()
POST   /api/v1/stories/{worldId}/events           → applyEvent()
GET    /api/v1/stories/{worldId}/state            → queryWorldState()
GET    /api/v1/stories/{worldId}/characters/{id}  → getCharacterState()
POST   /api/v1/stories/{worldId}/seed             → seedNarrative()
GET    /api/v1/stories/{worldId}/simulate         → simulateFutures()
GET    /api/v1/stories/{worldId}/timeline         → getNarrativeTimeline()
GET    /api/v1/stories/{worldId}/conflicts        → detectConflicts()
```

---

### 16. Persistência da Story Engine

```
db/migration/
├── V5__create_world_models.sql         # world_models, world_states
├── V6__create_story_entities.sql       # story_entities, entity_properties, entity_relations
├── V7__create_narrative_facts.sql      # narrative_facts
├── V8__create_scenes.sql               # scenes, scene_requirements, scene_effects
└── V9__create_narrative_timeline.sql   # narrative_events, simulated_futures
```

O estado do mundo é persistido a cada avanço narrativo. Fatos ativos, eventos executados e versões de estado são sempre recuperáveis.

---

### 17. Observabilidade da Story Engine

A engine expõe dados de observação via endpoints dedicados:

```
GET /api/v1/stories/{worldId}/observe/state     → Estado atual do mundo
GET /api/v1/stories/{worldId}/observe/timeline  → Linha do tempo narrativa
GET /api/v1/stories/{worldId}/observe/facts     → Fatos ativos
GET /api/v1/stories/{worldId}/observe/scenes    → Cenas disponíveis
GET /api/v1/stories/{worldId}/observe/decisions → Decisões tomadas pela engine
```

---

### 18. Frontend — Story Engine

```
src/app/features/
└── story-engine/
    ├── components/
    │   ├── world-state-panel/        # Painel de estado atual do mundo
    │   ├── scene-list/               # Cenas disponíveis
    │   ├── narrative-timeline/       # Visualização da timeline narrativa
    │   ├── character-card/           # Estado de um personagem
    │   └── conflict-alert/           # Conflitos e contradições detectadas
    ├── services/
    │   ├── story-engine.service.ts   # Serviço de comunicação com a API
    │   └── world-state.service.ts    # Estado reativo do mundo (Signals)
    └── domain/
        ├── world-state.model.ts
        ├── scene.model.ts
        ├── narrative-fact.model.ts
        └── character-model.model.ts
```

```typescript
// domain/world-state.model.ts
export interface WorldState {
  version: WorldStateVersion;
  entitySnapshots: EntitySnapshot[];
  activeFacts: NarrativeFact[];
  globalConditions: Record<string, unknown>;
}

export interface Scene {
  id: string;
  narrativeDescription: string;
  involvedCharacters: string[];
  priority: number;
  thematicTags: string[];
}

export interface NarrativeFact {
  id: string;
  subject: string;
  predicate: string;
  object?: string;
  validFrom: Date;
  validUntil?: Date;
}

// services/world-state.service.ts
@Injectable()
export class WorldStateService {

  private readonly _worldState = signal<WorldState | null>(null);
  private readonly _availableScenes = signal<Scene[]>([]);
  private readonly _conflicts = signal<NarrativeConflict[]>([]);

  readonly worldState = this._worldState.asReadonly();
  readonly availableScenes = this._availableScenes.asReadonly();
  readonly conflicts = this._conflicts.asReadonly();

  readonly hasConflicts = computed(() => this._conflicts().length > 0);

  loadState(worldId: string): void {
    this.storyEngineService.queryWorldState(worldId).subscribe(state => {
      this._worldState.set(state);
    });
  }
}
```

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
│   ├── timeline-connection.model.ts
│   ├── world-state.model.ts        # Story Engine
│   ├── scene.model.ts              # Story Engine
│   └── narrative-fact.model.ts     # Story Engine
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
│   ├── timeline-list/              # Listagem e busca
│   └── story-engine/               # Story Engine UI
│       ├── components/
│       ├── services/
│       └── domain/
├── shared/                         # Componentes, pipes, diretivas reutilizáveis
│   ├── components/
│   │   ├── button/
│   │   └── modal/
│   ├── pipes/
│   └── directives/
└── infrastructure/
    └── api/
        ├── timeline-api.service.ts
        ├── story-engine-api.service.ts  # Story Engine
        └── dto/
            ├── timeline.dto.ts
            ├── create-timeline.dto.ts
            └── story/
                ├── world-state.dto.ts
                ├── scene.dto.ts
                └── advance-story.dto.ts
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
| Story Engine — Regras | PascalCase + Rule | `AlivePresenceRule` |
| Story Engine — Efeitos | PascalCase + Effect | `KillCharacterEffect` |
| Story Engine — Requisitos | PascalCase + Requirement | `CharacterAliveRequirement` |

### Regras Gerais

**Backend:**
- Nunca exponha entidades JPA diretamente na API — sempre use DTOs
- Nunca injete repositórios nos controllers — passe pelos use cases
- Use `record` para Commands, Queries e DTOs imutáveis
- Prefira construtores sobre setters nas entidades de domínio
- Sempre valide no domínio, não só na camada web
- Testes unitários para domínio e aplicação, testes de integração para infraestrutura
- Na Story Engine, **nunca execute uma cena sem passar pelo `NarrativeValidator`**
- Na Story Engine, **nunca modifique `WorldState` diretamente** — use `SceneEffect` e `WorldStateBuilder`

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
│   │   ├── TimelineTest.java
│   │   ├── story/
│   │   │   ├── NarrativeFactTest.java
│   │   │   ├── SceneTest.java
│   │   │   ├── WorldStateTest.java
│   │   │   ├── AlivePresenceRuleTest.java
│   │   │   ├── SceneSelectorTest.java
│   │   │   └── StoryEngineTest.java
│   └── application/
│       ├── CreateTimelineUseCaseTest.java
│       └── story/
│           ├── AdvanceStoryUseCaseTest.java
│           └── GetAvailableScenesUseCaseTest.java
└── integration/
    ├── persistence/
    │   ├── TimelineRepositoryAdapterTest.java
    │   └── story/
    │       ├── WorldModelRepositoryAdapterTest.java
    │       └── SceneRepositoryAdapterTest.java
    └── web/
        ├── TimelineControllerTest.java
        └── story/
            └── StoryEngineControllerTest.java
```

```java
// Exemplo de teste da Story Engine — sem Spring, rápido
class StoryEngineTest {

    private StoryEngine engine;
    private NarrativeValidator validator;
    private SceneSelector selector;

    @BeforeEach
    void setUp() {
        validator = new NarrativeValidator(List.of(
            new AlivePresenceRule(),
            new UniqueEventRule(),
            new SingleLocationRule()
        ));
        selector = new SceneSelector();
        engine = new StoryEngine(validator, selector, new NarrativeQueryService());
    }

    @Test
    void shouldNotExecuteSceneWithDeadCharacter() {
        var world = WorldModel.createWithSeed(/* ... */);
        var deadCharacterId = world.findEntityByName("King").getId();
        world.getCurrentState().applyFact(deadCharacterId, "IS_DEAD");

        var sceneWithDeadKing = Scene.builder()
            .involvedCharacter(deadCharacterId)
            .build();

        var result = engine.advanceStory(world, timeline, List.of(sceneWithDeadKing), SelectionStrategy.HIGHEST_PRIORITY);

        assertThat(result.hasConflicts()).isTrue();
        assertThat(result.getConflicts()).anyMatch(c -> c.getType() == ConflictType.DEAD_CHARACTER_IN_SCENE);
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

describe('WorldStateService', () => {
  it('should compute hasConflicts correctly', () => {
    const service = new WorldStateService(mockStoryEngineService);
    expect(service.hasConflicts()).toBe(false);
    service['_conflicts'].set([mockConflict]);
    expect(service.hasConflicts()).toBe(true);
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
  story-engine:
    default-selection-strategy: HIGHEST_PRIORITY
    max-simulation-depth: 10
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
# Timeline
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

# Story Engine
POST   /api/v1/stories/{worldId}/advance           → Avança a história
GET    /api/v1/stories/{worldId}/scenes/available  → Cenas disponíveis
POST   /api/v1/stories/{worldId}/events            → Aplica evento manualmente
GET    /api/v1/stories/{worldId}/state             → Estado atual do mundo
GET    /api/v1/stories/{worldId}/characters/{id}   → Estado de um personagem
POST   /api/v1/stories/{worldId}/seed              → Inicializa história com seed
GET    /api/v1/stories/{worldId}/simulate          → Simula futuros possíveis
GET    /api/v1/stories/{worldId}/timeline          → Linha do tempo narrativa
GET    /api/v1/stories/{worldId}/conflicts         → Conflitos e contradições detectados
GET    /api/v1/stories/{worldId}/observe/state     → Observabilidade — estado
GET    /api/v1/stories/{worldId}/observe/facts     → Observabilidade — fatos ativos
GET    /api/v1/stories/{worldId}/observe/decisions → Observabilidade — decisões da engine
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
11. **Story Engine — nunca execute uma cena** sem antes chamar `NarrativeValidator.validate()`
12. **Story Engine — WorldState é imutável** — produza sempre um novo estado via `SceneEffect` e `WorldStateBuilder`
13. **Story Engine — separe motor de conteúdo** — lógica de simulação nunca deve conhecer cenas ou personagens específicos
14. **Story Engine — toda modificação de estado** deve ser rastreada na `NarrativeTimeline`

---

## Roadmap de Features

### MVP
- [ ] CRUD de timelines
- [ ] CRUD de eventos em uma timeline
- [ ] Canvas visual com uma timeline horizontal
- [ ] Zoom e pan no canvas
- [ ] Inserir texto em eventos

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

### V4 — Story Engine
- [ ] World Model CRUD (entidades, fatos, relações)
- [ ] Sistema de cenas com requisitos e efeitos
- [ ] Motor de avanço narrativo (`advanceStory`)
- [ ] Detecção de conflitos e contradições
- [ ] Painel de observabilidade narrativa no frontend
- [ ] Seeds narrativas para iniciar histórias
- [ ] Simulação de futuros possíveis
- [ ] Linha do tempo narrativa integrada ao canvas visual