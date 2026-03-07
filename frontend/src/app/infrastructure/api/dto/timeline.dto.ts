export interface TimelineDto {
  id: string;
  name: string;
  description: string;
  visibility: string;
  events: TimelineEventDto[];
  createdAt: string;
  updatedAt: string;
}

export interface TimelineEventDto {
  id: string;
  title: string;
  contentText: string;
  contentType: string;
  temporalPosition: number;
  temporalLabel: string;
  calendarSystem: string;
  displayOrder: number;
}

export interface TimelineSummaryDto {
  id: string;
  name: string;
  description: string;
  visibility: string;
  eventCount: number;
  createdAt: string;
  updatedAt: string;
}

export interface ConnectionDto {
  id: string;
  sourceEventId: string;
  targetEventId: string;
  description: string;
  connectionType: string;
}

export interface CreateTimelineDto {
  name: string;
  description?: string;
  visibility?: string;
}

export interface UpdateTimelineDto {
  name: string;
  description?: string;
  visibility?: string;
}

export interface CreateEventDto {
  title: string;
  contentText?: string;
  contentType?: string;
  temporalPosition: number;
  temporalLabel: string;
  calendarSystem?: string;
}

export interface UpdateEventDto {
  title: string;
  contentText?: string;
  contentType?: string;
  temporalPosition: number;
  temporalLabel: string;
  calendarSystem?: string;
}

export interface CreateConnectionDto {
  sourceEventId: string;
  targetEventId: string;
  description?: string;
  connectionType: string;
}

export interface GraphNodeDto {
  id: string;
  title: string;
  contentText: string;
  contentType: string;
  temporalLabel: string;
  temporalPosition: number;
  calendarSystem: string;
  timelineId: string;
  timelineName: string;
}

export interface GraphEdgeDto {
  id: string;
  sourceEventId: string;
  targetEventId: string;
  connectionType: string | null;
  description: string | null;
  inferred: boolean;
}

export interface NarrativeValidationDto {
  connectionId: string;
  severity: 'INFO' | 'WARNING';
  message: string;
  suggestedFix: string | null;
}

export interface EventGraphDto {
  nodes: GraphNodeDto[];
  edges: GraphEdgeDto[];
  validations: NarrativeValidationDto[];
}
