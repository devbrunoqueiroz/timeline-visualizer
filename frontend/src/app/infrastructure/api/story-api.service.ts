import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  AddSceneRequest,
  UpdateSceneRequest,
  ApplySceneResult,
  AvailableScenesResponse,
  CreateStoryRequest,
  Session,
  Story,
  StorySummary,
  StoryGraphView,
  SelectionStrategy,
  AdvanceStoryResult,
  SimulationView,
  NarrativeTimelineView,
  NarrativeConflict
} from '../../domain/story.model';

@Injectable({ providedIn: 'root' })
export class StoryApiService {

  private readonly base = `${environment.apiUrl}/stories`;
  private readonly sessions = `${environment.apiUrl}/sessions`;

  constructor(private http: HttpClient) {}

  // ── Stories ────────────────────────────────────────────────────────────────

  listStories(): Observable<StorySummary[]> {
    return this.http.get<StorySummary[]>(this.base);
  }

  getStory(id: string): Observable<Story> {
    return this.http.get<Story>(`${this.base}/${id}`);
  }

  createStory(req: CreateStoryRequest): Observable<Story> {
    return this.http.post<Story>(this.base, req);
  }

  getStoryGraph(storyId: string): Observable<StoryGraphView> {
    return this.http.get<StoryGraphView>(`${this.base}/${storyId}/graph`);
  }

  addScene(storyId: string, req: AddSceneRequest): Observable<any> {
    return this.http.post(`${this.base}/${storyId}/scenes`, req);
  }

  updateScene(storyId: string, sceneId: string, req: UpdateSceneRequest): Observable<void> {
    return this.http.put<void>(`${this.base}/${storyId}/scenes/${sceneId}`, req);
  }

  deleteScene(storyId: string, sceneId: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/${storyId}/scenes/${sceneId}`);
  }

  // ── Sessions ───────────────────────────────────────────────────────────────

  listSessions(storyId: string): Observable<Session[]> {
    return this.http.get<Session[]>(`${this.base}/${storyId}/sessions`);
  }

  createSession(storyId: string, name: string): Observable<Session> {
    return this.http.post<Session>(`${this.base}/${storyId}/sessions`, { name });
  }

  getSession(sessionId: string): Observable<Session> {
    return this.http.get<Session>(`${this.sessions}/${sessionId}`);
  }

  getAvailableScenes(sessionId: string): Observable<AvailableScenesResponse> {
    return this.http.get<AvailableScenesResponse>(`${this.sessions}/${sessionId}/available-scenes`);
  }

  applyScene(sessionId: string, sceneId: string): Observable<ApplySceneResult> {
    return this.http.post<ApplySceneResult>(`${this.sessions}/${sessionId}/apply/${sceneId}`, {});
  }

  advanceStory(sessionId: string, strategy: SelectionStrategy): Observable<AdvanceStoryResult> {
    return this.http.post<AdvanceStoryResult>(`${this.sessions}/${sessionId}/advance`, { strategy });
  }

  simulateFutures(sessionId: string, depth: number = 3): Observable<SimulationView[]> {
    return this.http.get<SimulationView[]>(`${this.sessions}/${sessionId}/simulate?depth=${depth}`);
  }

  getConflicts(sessionId: string, sceneId: string): Observable<NarrativeConflict[]> {
    return this.http.get<NarrativeConflict[]>(`${this.sessions}/${sessionId}/conflicts/${sceneId}`);
  }

  getNarrativeTimeline(sessionId: string): Observable<NarrativeTimelineView> {
    return this.http.get<NarrativeTimelineView>(`${this.sessions}/${sessionId}/timeline`);
  }
}
