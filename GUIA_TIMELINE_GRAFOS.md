q# Guia: Como Criar e Organizar uma Timeline de Grafos no Chronicle

Este guia explica passo a passo como usar o **Motor Narrativo** do Chronicle para construir e navegar uma timeline de grafos — onde cada cena é um nó, as dependências (requisitos e efeitos) formam as arestas, e o estado de mundo evolui à medida que a história avança.

---

## Conceitos Fundamentais

### O que é uma Timeline de Grafos?

No Chronicle, uma **história narrativa** é modelada como um **grafo dirigido acíclico (DAG)**:

```
[Cena A] ──efeito→ estado_mundo ──requisito→ [Cena B] ──efeito→ estado_mundo ──requisito→ [Cena C]
                                          ↘ requisito→ [Cena D]
```

- **Nós** = Cenas (eventos narrativos)
- **Arestas** = Dependências causais (efeitos de uma cena habilitam requisitos de outra)
- **Estado de Mundo** = Conjunto de fatos (`chave=valor`) que representa o contexto atual
- **Sessão** = Um percurso específico pelo grafo — cada sessão tem seu próprio histórico e estado

### Os Três Pilares

| Conceito | O que é | Exemplo |
|---|---|---|
| **Requisito** | Condição que deve ser verdadeira para uma cena estar disponível | `frodo.temAnel = true` |
| **Efeito** | Mudança no estado de mundo causada pela cena | `frodo.naDestruidora = true` |
| **Sessão** | Um playthrough — registra quais cenas foram aplicadas e o estado atual | "Caminho Canônico" |

---

## Passo 1: Criar uma História

### Via API

```http
POST /api/v1/stories
Content-Type: application/json

{
  "title": "Minha Jornada Épica",
  "description": "Uma história sobre escolhas e consequências"
}
```

### Via Interface (Story Builder)

1. Acesse o menu lateral → **Story Engine**
2. Clique em **"New Story"**
3. Preencha título e descrição
4. Clique em **"Create Story"**

A história criada aparece na lista com o status **"0 scenes"**. O próximo passo é adicionar cenas.

---

## Passo 2: Modelar as Cenas (Nós do Grafo)

Cada cena é um **nó do grafo**. Ao criar uma cena, você define:

### Campos Básicos

| Campo | Obrigatório | Descrição |
|---|---|---|
| `title` | Sim | Nome da cena |
| `description` | Sim | Descrição narrativa do que acontece |
| `repeatable` | Não | Se a cena pode ocorrer mais de uma vez na mesma sessão |
| `displayOrder` | Não | Ordem visual no editor (não afeta disponibilidade) |

### Campos de Grafo

| Campo | Tipo | Descrição |
|---|---|---|
| `priority` | `0–100` | Peso da cena na seleção automática (100 = máxima prioridade) |
| `tags` | `string[]` | Categorias narrativas (ex: `["batalha", "revelacao"]`) |
| `involvedCharacters` | `string[]` | IDs dos personagens presentes (ex: `["frodo", "aragorn"]`) |

### Exemplo de Criação

```http
POST /api/v1/stories/{storyId}/scenes
Content-Type: application/json

{
  "title": "A Sociedade se Forma",
  "description": "Nove companheiros se unem em Valfenda para levar o Um Anel à Montanha da Perdição.",
  "repeatable": false,
  "displayOrder": 1,
  "priority": 90,
  "tags": ["formacao", "alianca"],
  "involvedCharacters": ["frodo", "gandalf", "aragorn", "legolas", "gimli"],
  "requirements": [],
  "effects": [
    { "type": "SET_FACT", "factKey": "sociedade.formada", "factValue": "true" },
    { "type": "SET_FACT", "factKey": "frodo.membro_da_sociedade", "factValue": "true" }
  ]
}
```

---

## Passo 3: Definir Requisitos e Efeitos (Arestas do Grafo)

As **arestas** do grafo são definidas pelos `requirements` e `effects` de cada cena.

### Tipos de Requisito

| Tipo | Significado | Exemplo |
|---|---|---|
| `EQUALS` | O fato deve ter exatamente este valor | `frodo.temAnel = "true"` |
| `NOT_EQUALS` | O fato NÃO deve ter este valor | `gandalf.morto ≠ "true"` |
| `EXISTS` | O fato deve existir (qualquer valor) | `aragorn.localAtual existe` |
| `NOT_EXISTS` | O fato NÃO deve existir | `sauron.derrotado não existe` |
| `GREATER_THAN` | O fato deve ser numericamente maior | `exercito.tamanho > "100"` |
| `LESS_THAN` | O fato deve ser numericamente menor | `frodo.saude < "50"` |

### Tipos de Efeito

| Tipo | Significado | Exemplo |
|---|---|---|
| `SET_FACT` | Define ou sobrescreve um fato | `frodo.naDestruidora = "true"` |
| `REMOVE_FACT` | Remove um fato do estado de mundo | Remove `nazgul.presente` |
| `ADD_FACT` | Alias de SET_FACT (semântica aditiva) | `frodo.ferido = "true"` |

### Padrão de Nomenclatura de Fatos

Use o padrão `{sujeito}.{predicado}` para organizar o estado de mundo:

```
# Estado de personagem
frodo.temAnel = "true"
frodo.naDestruidora = "false"
frodo.morto = "false"          ← convenção especial: regra AlivePresenceRule usa "{id}.dead"

# Estado de localização
sociedade.localAtual = "moria"

# Estado de evento
batalha.helmsDeep = "concluida"
sauron.derrotado = "true"

# Contadores
exercito.baixas = "450"
```

> **Convenção importante:** Para marcar personagens como mortos (e acionar a `AlivePresenceRule`), use a chave `{characterId}.dead = "true"`.

### Exemplo de Cadeia Causal

```
Cena 1: "Frodo recebe o Anel"
  Efeitos: frodo.temAnel = "true"

Cena 2: "Partida do Shire"
  Requisito: frodo.temAnel == "true"
  Efeitos: frodo.deixouShire = "true", sociedade.iniciada = "true"

Cena 3: "Passagem por Moria"
  Requisito: frodo.deixouShire == "true"
  Efeitos: gandalf.nasMinas = "true", sociedade.emMoria = "true"

Cena 4: "Gandalf cai na Ponte"
  Requisito: gandalf.nasMinas == "true"
  Efeitos: gandalf.dead = "true", sociedade.emLuto = "true"
```

Isso cria o grafo: `C1 → C2 → C3 → C4`

---

## Passo 4: Organizar a Estrutura do Grafo

### Tipos de Estrutura Narrativa

#### Linear (Sequência)

Cada cena tem exatamente um predecessor e um sucessor:

```
[C1] → [C2] → [C3] → [C4] → [C5]
```

Útil para: histórias com caminho único, tutoriais, prólogos.

#### Ramificação (Branching)

Uma cena tem efeitos que habilitam múltiplos caminhos paralelos:

```
         ┌→ [C3: Gandalf escapa]
[C1] → [C2]
         └→ [C4: Gandalf cai]
```

Implemente usando **efeitos alternativos** em cenas diferentes que compartilham os mesmos requisitos mas produzem efeitos distintos.

#### Convergência (Merge)

Múltiplos caminhos confluem para uma mesma cena:

```
[C2: Caminho A] ─┐
                 ├→ [C5: A Destruição do Anel]
[C3: Caminho B] ─┘
```

Implemente usando requisitos com `OR` implícito — cenas diferentes que todas levam ao mesmo efeito (`anel.destruido = "true"`), habilitando a cena convergente.

#### Paralelo com Sincronização

Dois arcos narrativos progridem independentemente e se juntam:

```
[Frodo: C1 → C2 → C3]─┐
                       ├→ [Batalha Final]
[Aragorn: C4 → C5]────┘
```

Use múltiplos requisitos na cena de convergência:
```json
"requirements": [
  { "type": "EQUALS", "factKey": "frodo.naDestruidora", "expectedValue": "true" },
  { "type": "EQUALS", "factKey": "aragorn.emPortasNegras", "expectedValue": "true" }
]
```

---

## Passo 5: Criar uma Sessão (Iniciar um Percurso)

Uma sessão representa um playthrough específico pela história. Cada sessão:
- Começa com um estado de mundo vazio (ou pré-populado)
- Registra quais cenas foram aplicadas
- Mantém seu próprio estado de mundo independente

```http
POST /api/v1/sessions
Content-Type: application/json

{
  "storyId": "{storyId}",
  "name": "Percurso Canônico",
  "initialFacts": {
    "frodo.temAnel": "true",
    "gandalf.vivo": "true"
  }
}
```

---

## Passo 6: Navegar pelo Grafo

### Verificar Cenas Disponíveis

O sistema avalia automaticamente quais cenas têm todos os requisitos satisfeitos pelo estado atual:

```http
GET /api/v1/sessions/{sessionId}/available-scenes
```

Retorna apenas as cenas cujos **todos os requisitos** são verdadeiros no estado atual.

### Aplicar uma Cena Manualmente

```http
POST /api/v1/sessions/{sessionId}/apply-scene
Content-Type: application/json

{ "sceneId": "{sceneId}" }
```

Após aplicar, os efeitos da cena são gravados no estado de mundo da sessão.

### Avanço Automático (Auto-Advance)

O motor narrativo seleciona e aplica automaticamente a próxima cena com base em uma estratégia:

```http
POST /api/v1/sessions/{sessionId}/advance
Content-Type: application/json

{ "strategy": "HIGHEST_PRIORITY" }
```

#### Estratégias de Seleção

| Estratégia | Comportamento |
|---|---|
| `HIGHEST_PRIORITY` | Seleciona a cena disponível com maior `priority` (0–100) |
| `DRAMATIC_TENSION` | Favorece cenas com mais requisitos satisfeitos (maior tensão acumulada) |
| `WEIGHTED_RANDOM` | Seleção aleatória ponderada pelo `priority` de cada cena |

---

## Passo 7: Usar o Validador Narrativo

Antes de aplicar uma cena, o sistema roda automaticamente as **Regras Narrativas**:

### Regras Ativas por Padrão

| Regra | O que verifica |
|---|---|
| `AlivePresenceRule` | Personagens com `{id}.dead = "true"` não podem aparecer em novas cenas |
| `UniqueEventRule` | Cenas não-repetíveis não podem ser aplicadas duas vezes na mesma sessão |
| `CausalityRule` | Efeitos não devem contradizer fatos já estabelecidos (ex: ressuscitar um morto sem lógica) |

### Verificar Conflitos de uma Cena

```http
GET /api/v1/sessions/{sessionId}/conflicts/{sceneId}
```

Retorna a lista de conflitos narrativos, se houver. Use isso para validar a consistência antes de construir novos arcos.

---

## Passo 8: Simular Futuros

O sistema pode explorar automaticamente os caminhos possíveis a partir do estado atual:

```http
GET /api/v1/sessions/{sessionId}/simulate?depth=3
```

Retorna até 20 futuros possíveis com:
- A sequência de cenas de cada caminho
- O estado de mundo final de cada caminho
- A profundidade (número de cenas) de cada caminho

Use isso para:
- Verificar se um grafo tem caminhos para o fim
- Detectar "becos sem saída" (estados sem cenas disponíveis)
- Planejar a estrutura narrativa antes de publicar

---

## Passo 9: Visualizar o Grafo

Acesse a view de grafos no frontend para ver a estrutura visual da história:

1. Abra o **Story Engine** no menu lateral
2. Clique na sua história
3. Selecione a aba **"Graph View"**

### Legenda do Grafo

| Elemento Visual | Significado |
|---|---|
| Nó cinza | Cena ainda não aplicada |
| Nó verde | Cena já aplicada nesta sessão |
| Nó azul | Cena disponível (requisitos satisfeitos) |
| Nó vermelho | Cena com conflito narrativo |
| Aresta sólida | Dependência direta (efeito → requisito) |
| Aresta tracejada | Dependência inferida |
| Espessura da aresta | Força da conexão causal |

---

## Passo 10: Ver a Timeline Narrativa

Veja o histórico de cenas aplicadas na sessão como uma timeline linear:

```http
GET /api/v1/sessions/{sessionId}/timeline
```

No frontend, a **Narrative Timeline** mostra cada cena aplicada em ordem, com título, descrição e posição no percurso.

---

## Exemplo Completo: Construindo "A Jornada do Herói"

### 1. Criar a história

```
POST /api/v1/stories
{ "title": "A Jornada do Herói", "description": "Ciclo clássico de transformação" }
```

### 2. Adicionar os atos (cenas com prioridades)

```
Ato 1 — "O Chamado" (priority: 100, sem requisitos)
  Efeitos: heroi.chamado = "true"

Ato 2A — "Recusa do Chamado" (priority: 60)
  Requisito: heroi.chamado == "true"
  Efeitos: heroi.recusou = "true", heroi.chamado = "false"

Ato 2B — "Aceitar o Chamado" (priority: 80)
  Requisito: heroi.chamado == "true"
  Efeitos: heroi.aceitou = "true"

Ato 3 — "Cruzar o Limiar" (priority: 85)
  Requisito: heroi.aceitou == "true"
  Efeitos: heroi.emJornada = "true"

Ato 4 — "Provação Suprema" (priority: 90)
  Requisito: heroi.emJornada == "true"
  Efeitos: heroi.transformado = "true", proacao.superada = "true"

Ato 5 — "Retorno com o Elixir" (priority: 100)
  Requisito: proacao.superada == "true"
  Efeitos: jornada.completa = "true"
```

### 3. O grafo resultante

```
[Ato 1: O Chamado]
      |
      ├──→ [Ato 2A: Recusa] → (beco sem saída)
      |
      └──→ [Ato 2B: Aceitar]
                 |
           [Ato 3: Limiar]
                 |
           [Ato 4: Provação]
                 |
           [Ato 5: Retorno] ← FIM
```

### 4. Simular para verificar

```
GET /api/v1/sessions/{id}/simulate?depth=5
```

Resultado esperado: dois futuros — um completo (5 atos) e um truncado (2A sem continuação).

---

## Boas Práticas

### Nomenclatura de Fatos

- Use `{entidade}.{propriedade}` sempre (ex: `gandalf.dead`, `anel.destruido`)
- Prefira valores explícitos como `"true"` / `"false"` em vez de ausência do fato
- Use `{personagem}.dead = "true"` para ativar a `AlivePresenceRule`

### Estrutura do Grafo

- Toda história deve ter pelo menos **uma cena raiz** (sem requisitos)
- Toda cena não-final deve ter **pelo menos um efeito** que habilite cenas subsequentes
- Evite ciclos: o grafo deve ser acíclico (não deve ser possível voltar a uma cena já aplicada via efeitos)
- Use `repeatable: true` com cuidado — pode criar loops infinitos no simulador

### Prioridades

- Reserve `priority: 90–100` para cenas de **pontos de virada** críticos
- Use `priority: 50–70` para cenas de desenvolvimento
- Use `priority: 10–30` para cenas opcionais ou de cor narrativa
- Cenas com `priority: 0` nunca serão selecionadas por `HIGHEST_PRIORITY` se houver alternativas

### Tags

Use tags para categorizar e filtrar cenas:
- `batalha`, `dialogo`, `revelacao`, `transicao`
- `ato1`, `ato2`, `climax`, `epilogo`
- `opcional`, `obrigatorio`, `secreto`

---

## Referência Rápida da API

| Endpoint | Descrição |
|---|---|
| `POST /api/v1/stories` | Criar história |
| `GET /api/v1/stories` | Listar histórias |
| `GET /api/v1/stories/{id}` | Buscar história com cenas |
| `POST /api/v1/stories/{id}/scenes` | Adicionar cena |
| `PUT /api/v1/stories/{storyId}/scenes/{sceneId}` | Atualizar cena |
| `DELETE /api/v1/stories/{storyId}/scenes/{sceneId}` | Remover cena |
| `POST /api/v1/sessions` | Criar sessão |
| `GET /api/v1/sessions/{id}` | Ver estado da sessão |
| `GET /api/v1/sessions/{id}/available-scenes` | Cenas disponíveis agora |
| `POST /api/v1/sessions/{id}/apply-scene` | Aplicar cena manualmente |
| `POST /api/v1/sessions/{id}/advance` | Avanço automático |
| `GET /api/v1/sessions/{id}/timeline` | Timeline narrativa |
| `GET /api/v1/sessions/{id}/simulate?depth=N` | Simular futuros |
| `GET /api/v1/sessions/{id}/conflicts/{sceneId}` | Verificar conflitos |
