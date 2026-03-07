export interface Timeline {
  id: string;
  name: string;
  description: string;
  events: TimelineEvent[];
  visibility: TimelineVisibility;
  createdAt: Date;
  updatedAt: Date;
}

export interface TemporalPosition {
  position: number;
  label: string;
  calendarSystem: string;
}

export interface TimelineEvent {
  id: string;
  timelineId: string;
  title: string;
  content: EventContent;
  temporalPosition: TemporalPosition;
  displayOrder: number;
  position?: CanvasPosition;
}

export const GREGORIAN = 'GREGORIAN';
export const CUSTOM = 'CUSTOM';

export function temporalFromDate(date: Date): TemporalPosition {
  return { position: date.getTime(), label: date.toISOString(), calendarSystem: GREGORIAN };
}

export function temporalCustom(position: number, label: string): TemporalPosition {
  return { position, label, calendarSystem: CUSTOM };
}

export interface EventContent {
  text: string;
  type: ContentType;
  metadata: Record<string, string>;
}

export interface TimelineSummary {
  id: string;
  name: string;
  description: string;
  visibility: TimelineVisibility;
  eventCount: number;
  createdAt: Date;
  updatedAt: Date;
}

export interface TimelineConnection {
  id: string;
  sourceEventId: string;
  targetEventId: string;
  description: string;
  connectionType: ConnectionType;
}

export interface CanvasPosition {
  x: number;
  y: number;
}

export type TimelineVisibility = 'PUBLIC' | 'PRIVATE' | 'UNLISTED';
export type ContentType = 'TEXT' | 'RICH_TEXT' | 'MARKDOWN';
export type ConnectionType =
  // Original types
  | 'CAUSAL' | 'TEMPORAL' | 'REFERENCE' | 'CONTRAST'
  // Narrative enrichment types
  | 'PREREQUISITE' | 'FORESHADOW' | 'REVEAL' | 'ESCALATION' | 'RESOLUTION' | 'PARALLEL';

export interface GraphNode {
  id: string;
  title: string;
  contentText: string;
  contentType: ContentType;
  temporalLabel: string;
  temporalPosition: number;
  calendarSystem: string;
  timelineId: string;
  timelineName: string;
}

export interface GraphEdge {
  id: string;
  sourceEventId: string;
  targetEventId: string;
  connectionType: ConnectionType;
  description: string;
}

export interface EventGraphResponse {
  nodes: GraphNode[];
  edges: GraphEdge[];
}
