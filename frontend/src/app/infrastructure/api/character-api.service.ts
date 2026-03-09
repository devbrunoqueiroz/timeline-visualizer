import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Character, CharacterEvent, CharacterSummary, EventContent, TemporalPosition } from '../../domain/timeline.model';

@Injectable({ providedIn: 'root' })
export class CharacterApiService {
  private readonly http = inject(HttpClient);

  private url(timelineId: string) {
    return `${environment.apiUrl}/timelines/${timelineId}/characters`;
  }

  listCharacters(timelineId: string): Observable<CharacterSummary[]> {
    return this.http.get<CharacterSummary[]>(this.url(timelineId));
  }

  getCharacter(timelineId: string, characterId: string): Observable<Character> {
    return this.http.get<Character>(`${this.url(timelineId)}/${characterId}`);
  }

  createCharacter(timelineId: string, name: string, description: string,
                  startPosition?: number | null, endPosition?: number | null): Observable<Character> {
    return this.http.post<Character>(this.url(timelineId), {
      name, description,
      startPosition: startPosition ?? null,
      endPosition: endPosition ?? null
    });
  }

  updateCharacter(timelineId: string, characterId: string, name: string, description: string,
                  startPosition?: number | null, endPosition?: number | null): Observable<Character> {
    return this.http.put<Character>(`${this.url(timelineId)}/${characterId}`, {
      name, description,
      startPosition: startPosition ?? null,
      endPosition: endPosition ?? null
    });
  }

  deleteCharacter(timelineId: string, characterId: string): Observable<void> {
    return this.http.delete<void>(`${this.url(timelineId)}/${characterId}`);
  }

  addEvent(timelineId: string, characterId: string, title: string,
           contentText: string, temporal: TemporalPosition): Observable<CharacterEvent> {
    return this.http.post<CharacterEvent>(`${this.url(timelineId)}/${characterId}/events`, {
      title, contentText, contentType: 'RICH_TEXT',
      temporalPosition: temporal.position, temporalLabel: temporal.label,
      calendarSystem: temporal.calendarSystem
    });
  }

  updateEvent(timelineId: string, characterId: string, eventId: string,
              title: string, contentText: string, temporal: TemporalPosition): Observable<CharacterEvent> {
    return this.http.put<CharacterEvent>(`${this.url(timelineId)}/${characterId}/events/${eventId}`, {
      title, contentText, contentType: 'RICH_TEXT',
      temporalPosition: temporal.position, temporalLabel: temporal.label,
      calendarSystem: temporal.calendarSystem
    });
  }

  removeEvent(timelineId: string, characterId: string, eventId: string): Observable<void> {
    return this.http.delete<void>(`${this.url(timelineId)}/${characterId}/events/${eventId}`);
  }
}
