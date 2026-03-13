import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import {
  ConnectionDto,
  EventGraphDto,
  TimelineDto,
  TimelineEventDto,
  TimelineSummaryDto
} from './dto/timeline.dto';
import { TimelineMapper } from './timeline-mapper';
import {
  ContentType,
  EventContent,
  EventGraphResponse,
  StoryPath,
  TemporalPosition,
  Timeline,
  TimelineConnection,
  TimelineEvent,
  TimelineSummary,
  TimelineVisibility
} from '../../domain/timeline.model';

@Injectable({ providedIn: 'root' })
export class TimelineApiService {

  private readonly baseUrl = `${environment.apiUrl}/timelines`;
  private readonly connectionsUrl = `${environment.apiUrl}/connections`;
  private readonly graphUrl = `${environment.apiUrl}/graph`;

  constructor(private http: HttpClient) {}

  listTimelines(): Observable<TimelineSummary[]> {
    return this.http.get<TimelineSummaryDto[]>(this.baseUrl).pipe(
      map(dtos => dtos.map(dto => TimelineMapper.toSummaryDomain(dto)))
    );
  }

  getTimeline(id: string): Observable<Timeline> {
    return this.http.get<TimelineDto>(`${this.baseUrl}/${id}`).pipe(
      map(dto => TimelineMapper.toDomain(dto))
    );
  }

  createTimeline(name: string, description: string, visibility: TimelineVisibility = 'PRIVATE'): Observable<Timeline> {
    const dto = TimelineMapper.toCreateDto(name, description, visibility);
    return this.http.post<TimelineDto>(this.baseUrl, dto).pipe(
      map(response => TimelineMapper.toDomain(response))
    );
  }

  updateTimeline(id: string, name: string, description: string, visibility: TimelineVisibility): Observable<Timeline> {
    const dto = TimelineMapper.toUpdateDto(name, description, visibility);
    return this.http.put<TimelineDto>(`${this.baseUrl}/${id}`, dto).pipe(
      map(response => TimelineMapper.toDomain(response))
    );
  }

  deleteTimeline(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  addEvent(timelineId: string, title: string, content: EventContent,
           temporal: TemporalPosition): Observable<TimelineEvent> {
    const dto = TimelineMapper.toCreateEventDto(title, content, temporal);
    return this.http.post<TimelineEventDto>(`${this.baseUrl}/${timelineId}/events`, dto).pipe(
      map(response => TimelineMapper.toEventDomain(response, timelineId))
    );
  }

  updateEvent(timelineId: string, eventId: string, title: string,
              content: EventContent, temporal: TemporalPosition): Observable<TimelineEvent> {
    const dto = TimelineMapper.toUpdateEventDto(title, content, temporal);
    return this.http.put<TimelineEventDto>(`${this.baseUrl}/${timelineId}/events/${eventId}`, dto).pipe(
      map(response => TimelineMapper.toEventDomain(response, timelineId))
    );
  }

  removeEvent(timelineId: string, eventId: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${timelineId}/events/${eventId}`);
  }

  createConnection(sourceEventId: string, targetEventId: string,
                   description: string, connectionType: string): Observable<TimelineConnection> {
    const dto = TimelineMapper.toCreateConnectionDto(sourceEventId, targetEventId, description, connectionType);
    return this.http.post<ConnectionDto>(this.connectionsUrl, dto).pipe(
      map(response => TimelineMapper.toConnectionDomain(response))
    );
  }

  deleteConnection(connectionId: string): Observable<void> {
    return this.http.delete<void>(`${this.connectionsUrl}/${connectionId}`);
  }

  getConnections(eventIds: string[]): Observable<TimelineConnection[]> {
    let params = new HttpParams();
    eventIds.forEach(id => params = params.append('eventIds', id));
    return this.http.get<ConnectionDto[]>(this.connectionsUrl, { params }).pipe(
      map(dtos => dtos.map(dto => TimelineMapper.toConnectionDomain(dto)))
    );
  }

  getEventGraph(timelineIds?: string[]): Observable<EventGraphResponse> {
    let params = new HttpParams();
    if (timelineIds?.length) {
      timelineIds.forEach(id => params = params.append('timelineIds', id));
    }
    return this.http.get<EventGraphDto>(this.graphUrl, { params }).pipe(
      map(dto => TimelineMapper.toGraphDomain(dto))
    );
  }

  /**
   * Finds the shortest story path between two events in the narrative graph.
   * @param fromEventId source event UUID
   * @param toEventId   target event UUID
   * @param explicitOnly if true, only traverse user-defined connections
   */
  findStoryPath(fromEventId: string, toEventId: string, explicitOnly = false): Observable<StoryPath> {
    let params = new HttpParams()
      .set('from', fromEventId)
      .set('to', toEventId)
      .set('explicitOnly', String(explicitOnly));
    return this.http.get<StoryPath>(`${this.graphUrl}/path`, { params });
  }
}
