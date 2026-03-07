import {
  ConnectionDto,
  CreateConnectionDto,
  CreateEventDto,
  CreateTimelineDto,
  EventGraphDto,
  TimelineDto,
  TimelineEventDto,
  TimelineSummaryDto,
  UpdateEventDto,
  UpdateTimelineDto
} from './dto/timeline.dto';
import {
  ConnectionType,
  ContentType,
  EventContent,
  EventGraphResponse,
  GREGORIAN,
  GraphEdge,
  GraphNode,
  TemporalPosition,
  Timeline,
  TimelineConnection,
  TimelineEvent,
  TimelineSummary,
  TimelineVisibility
} from '../../domain/timeline.model';

export class TimelineMapper {

  static toDomain(dto: TimelineDto): Timeline {
    return {
      id: dto.id,
      name: dto.name,
      description: dto.description,
      visibility: dto.visibility as TimelineVisibility,
      events: dto.events.map(e => TimelineMapper.toEventDomain(e, dto.id)),
      createdAt: new Date(dto.createdAt),
      updatedAt: new Date(dto.updatedAt)
    };
  }

  static toSummaryDomain(dto: TimelineSummaryDto): TimelineSummary {
    return {
      id: dto.id,
      name: dto.name,
      description: dto.description,
      visibility: dto.visibility as TimelineVisibility,
      eventCount: dto.eventCount,
      createdAt: new Date(dto.createdAt),
      updatedAt: new Date(dto.updatedAt)
    };
  }

  static toEventDomain(dto: TimelineEventDto, timelineId: string): TimelineEvent {
    return {
      id: dto.id,
      timelineId,
      title: dto.title,
      content: {
        text: dto.contentText,
        type: dto.contentType as ContentType,
        metadata: {}
      },
      temporalPosition: {
        position: Number(dto.temporalPosition),
        label: dto.temporalLabel,
        calendarSystem: dto.calendarSystem
      },
      displayOrder: dto.displayOrder
    };
  }

  static toConnectionDomain(dto: ConnectionDto): TimelineConnection {
    return {
      id: dto.id,
      sourceEventId: dto.sourceEventId,
      targetEventId: dto.targetEventId,
      description: dto.description,
      connectionType: dto.connectionType as any
    };
  }

  static toCreateDto(name: string, description: string, visibility: TimelineVisibility): CreateTimelineDto {
    return { name, description, visibility };
  }

  static toUpdateDto(name: string, description: string, visibility: TimelineVisibility): UpdateTimelineDto {
    return { name, description, visibility };
  }

  static toCreateEventDto(title: string, content: EventContent, temporal: TemporalPosition): CreateEventDto {
    return {
      title,
      contentText: content.text,
      contentType: content.type,
      temporalPosition: temporal.position,
      temporalLabel: temporal.label,
      calendarSystem: temporal.calendarSystem
    };
  }

  static toUpdateEventDto(title: string, content: EventContent, temporal: TemporalPosition): UpdateEventDto {
    return {
      title,
      contentText: content.text,
      contentType: content.type,
      temporalPosition: temporal.position,
      temporalLabel: temporal.label,
      calendarSystem: temporal.calendarSystem
    };
  }

  static toCreateConnectionDto(sourceEventId: string, targetEventId: string,
                                description: string, connectionType: string): CreateConnectionDto {
    return { sourceEventId, targetEventId, description, connectionType };
  }

  static toGraphDomain(dto: EventGraphDto): EventGraphResponse {
    return {
      nodes: dto.nodes.map(n => ({
        id: n.id,
        title: n.title,
        contentText: n.contentText,
        contentType: n.contentType as ContentType,
        temporalLabel: n.temporalLabel,
        temporalPosition: n.temporalPosition,
        calendarSystem: n.calendarSystem,
        timelineId: n.timelineId,
        timelineName: n.timelineName
      }) as GraphNode),
      edges: dto.edges.map(e => ({
        id: e.id,
        sourceEventId: e.sourceEventId,
        targetEventId: e.targetEventId,
        connectionType: e.connectionType as ConnectionType ?? null,
        description: e.description ?? null,
        inferred: e.inferred ?? false
      }) as GraphEdge),
      validations: dto.validations ?? []
    };
  }
}
