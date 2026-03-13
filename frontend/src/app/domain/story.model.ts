// ── Enums ────────────────────────────────────────────────────────────────────

export type RequirementType = 'FACT_EXISTS' | 'FACT_ABSENT' | 'FACT_EQUALS';
export type EffectType      = 'ADD_FACT' | 'REMOVE_FACT' | 'SET_FACT';
export type NodeType        = 'SCENE' | 'FACT';
export type EdgeType        = 'REQUIREMENT' | 'EFFECT' | 'TRANSITION';
export type ContradictionSeverity = 'WARNING' | 'ERROR';
export type SelectionStrategy = 'HIGHEST_PRIORITY' | 'DRAMATIC_TENSION' | 'WEIGHTED_RANDOM';
export type ConflictType = 'DEAD_CHARACTER_IN_SCENE' | 'CHARACTER_IN_MULTIPLE_LOCATIONS' | 'CONTRADICTORY_FACTS' | 'IMPOSSIBLE_CAUSALITY' | 'UNIQUE_EVENT_REPEATED' | 'UNMET_REQUIREMENT';

// ── Core domain objects ───────────────────────────────────────────────────────

export interface Requirement {
  factKey: string;
  type: RequirementType;
  expectedValue: string | null;
}

export interface Effect {
  type: EffectType;
  factKey: string;
  factValue: string | null;
}

export interface Scene {
  id: string;
  title: string;
  description: string;
  repeatable: boolean;
  displayOrder: number;
  requirements: Requirement[];
  effects: Effect[];
  priority: number;
  tags: string[];
  involvedCharacters: string[];
}

export interface Story {
  id: string;
  title: string;
  description: string;
  createdAt: string;
  scenes: Scene[];
}

export interface StorySummary {
  id: string;
  title: string;
  description: string;
  sceneCount: number;
  createdAt: string;
}

// ── Session / WorldState ─────────────────────────────────────────────────────

export interface Session {
  id: string;
  storyId: string;
  name: string | null;
  worldStateFacts: Record<string, string>;
  appliedSceneIds: string[];
  createdAt: string;
  updatedAt: string;
}

// ── Available Scenes (with consistency info) ─────────────────────────────────

export interface NarrativeContradictionView {
  sceneId: string;
  factKey: string;
  message: string;
  severity: ContradictionSeverity;
}

export interface AvailableScene {
  id: string;
  title: string;
  description: string;
  repeatable: boolean;
  requirements: Requirement[];
  effects: Effect[];
  potentialContradictions: NarrativeContradictionView[];
}

export interface AvailableScenesResponse {
  sessionId: string;
  availableScenes: AvailableScene[];
  worldStateFacts: Record<string, string>;
}

// ── Apply result ──────────────────────────────────────────────────────────────

export interface ApplySceneResult {
  sessionId: string;
  appliedSceneTitle: string;
  newWorldStateFacts: Record<string, string>;
  appliedSceneIds: string[];
}

// ── Narrative Graph ───────────────────────────────────────────────────────────

export interface StoryGraphNode {
  id: string;
  label: string;
  nodeType: NodeType;
}

export interface StoryGraphEdge {
  id: string;
  sourceId: string;
  targetId: string;
  edgeType: EdgeType;
}

export interface StoryGraphView {
  nodes: StoryGraphNode[];
  edges: StoryGraphEdge[];
}

// ── Request helpers ───────────────────────────────────────────────────────────

export interface CreateStoryRequest {
  title: string;
  description: string;
}

export interface AddSceneRequest {
  title: string;
  description: string;
  repeatable: boolean;
  requirements: Requirement[];
  effects: Effect[];
  priority?: number;
  tags?: string[];
  involvedCharacters?: string[];
}

export interface UpdateSceneRequest {
  title: string;
  description: string;
  repeatable: boolean;
  requirements: Requirement[];
  effects: Effect[];
  priority?: number;
  tags?: string[];
  involvedCharacters?: string[];
}

// ── Advance / Simulate / Conflicts ───────────────────────────────────────────

export interface NarrativeConflict {
  type: ConflictType;
  description: string;
  conflictingSceneId: string | null;
}

export interface AdvanceStoryResult {
  success: boolean;
  sceneId: string | null;
  sceneTitle: string | null;
  newWorldState: Record<string, string> | null;
  conflicts: NarrativeConflict[];
  message: string;
}

export interface SimulationView {
  sceneIds: string[];
  sceneTitles: string[];
  finalWorldState: Record<string, string>;
  depth: number;
}

export interface NarrativeTimelineView {
  sessionId: string;
  sessionName: string;
  appliedScenes: { sceneId: string; title: string; description: string; position: number }[];
}
