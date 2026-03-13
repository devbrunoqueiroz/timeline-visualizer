import { Component, OnInit, signal, computed, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { StoryApiService } from '../../../infrastructure/api/story-api.service';
import {
  Session, AvailableScene, AvailableScenesResponse,
  NarrativeContradictionView, SelectionStrategy, AdvanceStoryResult,
  NarrativeTimelineView, SimulationView
} from '../../../domain/story.model';

@Component({
  selector: 'app-story-player',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="page">
      <header class="page-header">
        <div class="header-left">
          <a routerLink="/stories" class="back-btn">← Stories</a>
          <div class="title-group">
            <h1 class="page-title">{{ storyTitle() || 'Story Player' }}</h1>
            <span class="subtitle">{{ sessionName() || 'Select a session' }}</span>
          </div>
        </div>
        <div class="header-actions">
          <a class="btn-nav" [routerLink]="['/stories', storyId, 'build']">✏ Build</a>
          <a class="btn-nav btn-graph" [routerLink]="['/stories', storyId, 'graph']">⬡ Graph</a>
          <button class="create-btn" (click)="showNewSession.set(true)">+ New Session</button>
        </div>
      </header>

      @if (showNewSession()) {
        <div class="overlay" (click)="showNewSession.set(false)">
          <div class="mini-form" (click)="$event.stopPropagation()">
            <h3 class="mini-title">New Session</h3>
            <input class="input" [(ngModel)]="newSessionName" placeholder="Session name (optional)" />
            <div class="mini-actions">
              <button class="btn-secondary" (click)="showNewSession.set(false)">Cancel</button>
              <button class="btn-primary" (click)="createSession()">Create</button>
            </div>
          </div>
        </div>
      }

      <div class="layout">
        <!-- Sessions sidebar -->
        <aside class="sidebar">
          <div class="sidebar-title">Sessions</div>
          @if (sessions().length === 0) {
            <div class="sidebar-empty">No sessions yet</div>
          }
          @for (s of sessions(); track s.id) {
            <button
              class="session-item"
              [class.active]="activeSessionId() === s.id"
              (click)="selectSession(s.id)">
              <span class="session-name">{{ s.name || 'Unnamed' }}</span>
              <span class="scene-count">{{ s.appliedSceneIds.length }} scenes</span>
            </button>
          }
        </aside>

        @if (activeSession()) {
          <div class="main">
            <!-- World State -->
            <div class="panel world-panel">
              <div class="panel-header">
                <span class="panel-title">World State</span>
                <span class="fact-count">{{ factEntries().length }} facts</span>
              </div>
              @if (factEntries().length === 0) {
                <div class="panel-empty">No facts yet — world state is empty</div>
              } @else {
                <div class="facts-grid">
                  @for (entry of factEntries(); track entry.key) {
                    <div class="fact-chip">
                      <span class="fact-key">{{ entry.key }}</span>
                      <span class="fact-value">{{ entry.value }}</span>
                    </div>
                  }
                </div>
              }
            </div>

            <!-- Auto Advance -->
            <div class="panel auto-panel">
              <div class="panel-header">
                <span class="panel-title">Auto Advance</span>
              </div>
              <div class="auto-content">
                <select class="select" [(ngModel)]="advanceStrategy">
                  <option value="HIGHEST_PRIORITY">Highest Priority</option>
                  <option value="DRAMATIC_TENSION">Dramatic Tension</option>
                  <option value="WEIGHTED_RANDOM">Weighted Random</option>
                </select>
                <button class="advance-btn" (click)="autoAdvance()" [disabled]="applying()">
                  ⚡ Auto Advance
                </button>
                @if (lastAdvanceResult()) {
                  <div class="advance-result" [class.success]="lastAdvanceResult()!.success" [class.fail]="!lastAdvanceResult()!.success">
                    {{ lastAdvanceResult()!.message }}
                  </div>
                }
              </div>
            </div>

            <!-- Available Scenes -->
            <div class="panel scenes-panel">
              <div class="panel-header">
                <span class="panel-title">Available Scenes</span>
                @if (loadingScenes()) {
                  <span class="loading-dot">loading…</span>
                }
              </div>
              @if (availableScenes().length === 0 && !loadingScenes()) {
                <div class="panel-empty">No scenes available — all requirements are locked</div>
              }
              <div class="scenes-list">
                @for (scene of availableScenes(); track scene.id) {
                  <div class="scene-card" [class.has-warnings]="hasWarnings(scene)" [class.has-errors]="hasErrors(scene)">
                    <div class="scene-top">
                      <div class="scene-info">
                        <h3 class="scene-title">{{ scene.title }}</h3>
                        @if (scene.description) {
                          <p class="scene-desc">{{ scene.description }}</p>
                        }
                      </div>
                      <button
                        class="play-btn"
                        [disabled]="hasErrors(scene) || applying()"
                        (click)="applyScene(scene.id)">
                        ▶ Play
                      </button>
                    </div>

                    @if (scene.potentialContradictions.length > 0) {
                      <div class="contradictions">
                        @for (c of scene.potentialContradictions; track c.factKey) {
                          <div class="contradiction" [class.error]="c.severity === 'ERROR'" [class.warning]="c.severity === 'WARNING'">
                            <span class="sev-badge">{{ c.severity }}</span>
                            {{ c.message }}
                          </div>
                        }
                      </div>
                    }

                    @if (scene.effects.length > 0) {
                      <div class="effects-preview">
                        @for (e of scene.effects; track e.factKey) {
                          <span class="eff-chip">
                            @if (e.type === 'ADD_FACT') { +{{ e.factKey }} }
                            @if (e.type === 'REMOVE_FACT') { -{{ e.factKey }} }
                            @if (e.type === 'SET_FACT') { {{ e.factKey }}={{ e.factValue }} }
                          </span>
                        }
                      </div>
                    }
                  </div>
                }
              </div>
            </div>

            <!-- Narrative Timeline History -->
            @if (narrativeTimeline()?.appliedScenes?.length) {
              <div class="panel history-panel">
                <div class="panel-header">
                  <span class="panel-title">Narrative Timeline</span>
                  <span class="fact-count">{{ narrativeTimeline()!.appliedScenes.length }} scenes</span>
                </div>
                <div class="history-list">
                  @for (entry of narrativeTimeline()!.appliedScenes; track entry.sceneId) {
                    <div class="history-item">
                      <span class="history-num">{{ entry.position + 1 }}</span>
                      <div class="history-info">
                        <span class="history-title">{{ entry.title }}</span>
                        @if (entry.description) {
                          <span class="history-desc">{{ entry.description }}</span>
                        }
                      </div>
                    </div>
                  }
                </div>
              </div>
            } @else if (activeSession()!.appliedSceneIds.length > 0) {
              <div class="panel history-panel">
                <div class="panel-header">
                  <span class="panel-title">Applied Scenes</span>
                  <span class="fact-count">{{ activeSession()!.appliedSceneIds.length }}</span>
                </div>
                <div class="history-list">
                  @for (id of activeSession()!.appliedSceneIds; track id; let i = $index) {
                    <div class="history-item">
                      <span class="history-num">{{ i + 1 }}</span>
                      <span class="history-id">{{ id }}</span>
                    </div>
                  }
                </div>
              </div>
            }
          </div>
        } @else {
          <div class="no-session">
            <div class="no-session-icon">🎭</div>
            <p>Select a session or create a new one to start playing.</p>
          </div>
        }
      </div>
    </div>
  `,
  styles: [`
    .page { min-height: 100vh; background: #0f172a; color: #e2e8f0; font-family: system-ui, sans-serif; display: flex; flex-direction: column; }

    .page-header {
      display: flex; justify-content: space-between; align-items: center;
      padding: 16px 28px; border-bottom: 1px solid #1e293b;
      background: #0f172a; position: sticky; top: 0; z-index: 20;
    }
    .header-left { display: flex; align-items: center; gap: 16px; }
    .back-btn {
      color: #6366f1; font-size: 13px; font-weight: 600; text-decoration: none;
      padding: 5px 10px; border: 1px solid #312e81; border-radius: 6px;
    }
    .back-btn:hover { background: #1e1b4b; }
    .title-group { display: flex; flex-direction: column; gap: 2px; }
    .page-title { font-size: 18px; font-weight: 800; color: #f1f5f9; margin: 0; }
    .subtitle { font-size: 11px; color: #64748b; text-transform: uppercase; font-weight: 600; }
    .header-actions { display: flex; gap: 10px; align-items: center; }
    .btn-nav {
      padding: 6px 14px; background: transparent; color: #94a3b8;
      border: 1px solid #334155; border-radius: 6px; font-size: 12px; font-weight: 600;
      text-decoration: none;
    }
    .btn-nav:hover { border-color: #6366f1; color: #818cf8; }
    .btn-graph { border-color: #312e81; color: #a5b4fc; }
    .create-btn {
      padding: 7px 18px; background: #166534; color: #86efac; border: 1px solid #15803d;
      border-radius: 8px; font-size: 13px; font-weight: 600; cursor: pointer;
    }
    .create-btn:hover { background: #14532d; }

    /* Mini form overlay */
    .overlay {
      position: fixed; inset: 0; background: rgba(0,0,0,0.6);
      display: flex; align-items: center; justify-content: center; z-index: 100;
    }
    .mini-form {
      background: #1e293b; border: 1px solid #334155; border-radius: 12px;
      padding: 24px; width: 360px;
    }
    .mini-title { font-size: 16px; font-weight: 700; color: #f1f5f9; margin: 0 0 16px; }
    .input {
      width: 100%; box-sizing: border-box; background: #0f172a; border: 1px solid #334155;
      color: #e2e8f0; border-radius: 8px; padding: 9px 12px; font-size: 13px; outline: none;
      margin-bottom: 12px;
    }
    .input:focus { border-color: #6366f1; }
    .mini-actions { display: flex; gap: 8px; justify-content: flex-end; }
    .btn-primary {
      padding: 7px 18px; background: #6366f1; color: white; border: none;
      border-radius: 6px; font-size: 13px; font-weight: 600; cursor: pointer;
    }
    .btn-secondary {
      padding: 7px 18px; background: transparent; color: #94a3b8;
      border: 1px solid #334155; border-radius: 6px; font-size: 13px; cursor: pointer;
    }

    /* Layout */
    .layout { display: flex; flex: 1; overflow: hidden; }

    /* Sidebar */
    .sidebar {
      width: 220px; min-width: 220px; border-right: 1px solid #1e293b;
      background: #0a1122; padding: 16px 12px; overflow-y: auto;
    }
    .sidebar-title {
      font-size: 11px; font-weight: 700; text-transform: uppercase; color: #475569;
      padding: 0 8px; margin-bottom: 10px;
    }
    .sidebar-empty { font-size: 12px; color: #334155; padding: 8px; }
    .session-item {
      width: 100%; display: flex; flex-direction: column; align-items: flex-start;
      padding: 10px 12px; background: transparent; border: 1px solid transparent;
      border-radius: 8px; cursor: pointer; color: #94a3b8; text-align: left;
      transition: all 0.15s; margin-bottom: 4px;
    }
    .session-item:hover { background: #1e293b; border-color: #334155; }
    .session-item.active { background: #1e1b4b; border-color: #4f46e5; color: #e0e7ff; }
    .session-name { font-size: 13px; font-weight: 600; }
    .scene-count { font-size: 11px; color: #64748b; margin-top: 2px; }

    /* Main content */
    .main { flex: 1; overflow-y: auto; padding: 20px 24px; display: flex; flex-direction: column; gap: 16px; }

    .no-session { flex: 1; display: flex; flex-direction: column; align-items: center; justify-content: center; color: #475569; }
    .no-session-icon { font-size: 48px; margin-bottom: 12px; }
    .no-session p { font-size: 14px; }

    /* Panels */
    .panel { background: #1e293b; border: 1px solid #334155; border-radius: 12px; overflow: hidden; }
    .panel-header {
      display: flex; align-items: center; justify-content: space-between;
      padding: 12px 16px; border-bottom: 1px solid #334155;
    }
    .panel-title { font-size: 13px; font-weight: 700; color: #94a3b8; text-transform: uppercase; }
    .fact-count { font-size: 12px; color: #475569; }
    .loading-dot { font-size: 12px; color: #6366f1; }
    .panel-empty { padding: 16px; font-size: 13px; color: #475569; }

    /* World State */
    .facts-grid { display: flex; flex-wrap: wrap; gap: 8px; padding: 12px 16px; }
    .fact-chip {
      display: flex; align-items: center; gap: 6px;
      background: #0f172a; border: 1px solid #334155; border-radius: 6px; padding: 5px 10px;
    }
    .fact-key { font-size: 12px; font-family: monospace; color: #94a3b8; }
    .fact-value { font-size: 12px; font-family: monospace; color: #4ade80; font-weight: 600; }

    /* Scenes */
    .scenes-list { display: flex; flex-direction: column; gap: 10px; padding: 12px 16px; }
    .scene-card {
      background: #0f172a; border: 1px solid #1e293b; border-radius: 10px; padding: 14px 16px;
    }
    .scene-card.has-warnings { border-color: #854d0e; }
    .scene-card.has-errors { border-color: #7f1d1d; }
    .scene-top { display: flex; align-items: flex-start; justify-content: space-between; gap: 12px; }
    .scene-info { flex: 1; }
    .scene-title { font-size: 14px; font-weight: 700; color: #f1f5f9; margin: 0 0 4px; }
    .scene-desc { font-size: 12px; color: #64748b; margin: 0; line-height: 1.5; }
    .play-btn {
      padding: 6px 16px; background: #166534; color: #86efac; border: 1px solid #15803d;
      border-radius: 6px; font-size: 12px; font-weight: 600; cursor: pointer; white-space: nowrap;
    }
    .play-btn:hover:not(:disabled) { background: #14532d; }
    .play-btn:disabled { opacity: 0.4; cursor: not-allowed; }

    .contradictions { margin-top: 10px; display: flex; flex-direction: column; gap: 6px; }
    .contradiction {
      display: flex; align-items: flex-start; gap: 8px; font-size: 12px; padding: 6px 10px; border-radius: 6px;
    }
    .contradiction.error { background: #450a0a; color: #fca5a5; border: 1px solid #7f1d1d; }
    .contradiction.warning { background: #1c1400; color: #fde68a; border: 1px solid #854d0e; }
    .sev-badge {
      font-size: 9px; font-weight: 800; text-transform: uppercase;
      padding: 1px 5px; border-radius: 3px; background: rgba(255,255,255,0.1); white-space: nowrap;
    }

    .effects-preview { display: flex; flex-wrap: wrap; gap: 6px; margin-top: 10px; }
    .eff-chip {
      font-size: 11px; font-family: monospace;
      background: #14532d; color: #86efac; border: 1px solid #166534;
      padding: 2px 8px; border-radius: 4px;
    }

    /* Auto Advance */
    .auto-content { display: flex; gap: 10px; align-items: center; padding: 12px 16px; flex-wrap: wrap; }
    .select {
      background: #0f172a; border: 1px solid #334155; color: #e2e8f0;
      border-radius: 6px; padding: 6px 10px; font-size: 12px; cursor: pointer; outline: none;
    }
    .advance-btn {
      padding: 6px 16px; background: #1e1b4b; color: #818cf8; border: 1px solid #4f46e5;
      border-radius: 6px; font-size: 12px; font-weight: 600; cursor: pointer;
    }
    .advance-btn:hover:not(:disabled) { background: #312e81; }
    .advance-btn:disabled { opacity: 0.4; cursor: not-allowed; }
    .advance-result { font-size: 12px; padding: 5px 10px; border-radius: 5px; }
    .advance-result.success { background: #14271a; color: #86efac; }
    .advance-result.fail { background: #1c1400; color: #fde68a; }

    /* History */
    .history-list { padding: 10px 16px; display: flex; flex-direction: column; gap: 4px; }
    .history-item { display: flex; gap: 10px; align-items: flex-start; padding: 6px 0; border-bottom: 1px solid #1e293b; }
    .history-item:last-child { border-bottom: none; }
    .history-num {
      width: 22px; height: 22px; background: #1e1b4b; border-radius: 50%;
      display: flex; align-items: center; justify-content: center;
      font-size: 10px; font-weight: 700; color: #818cf8; flex-shrink: 0; margin-top: 2px;
    }
    .history-id { font-size: 11px; font-family: monospace; color: #475569; }
    .history-info { display: flex; flex-direction: column; gap: 2px; }
    .history-title { font-size: 13px; font-weight: 600; color: #e2e8f0; }
    .history-desc { font-size: 11px; color: #64748b; line-height: 1.4; }
  `]
})
export class StoryPlayerComponent implements OnInit {
  readonly sessions = signal<Session[]>([]);
  readonly activeSession = signal<Session | null>(null);
  readonly activeSessionId = signal<string | null>(null);
  readonly availableScenes = signal<AvailableScene[]>([]);
  readonly loadingScenes = signal(false);
  readonly applying = signal(false);
  readonly showNewSession = signal(false);
  readonly storyTitle = signal('');
  readonly narrativeTimeline = signal<NarrativeTimelineView | null>(null);
  readonly lastAdvanceResult = signal<AdvanceStoryResult | null>(null);
  readonly sessionName = computed(() => this.activeSession()?.name ?? null);

  readonly factEntries = computed(() => {
    const facts = this.activeSession()?.worldStateFacts ?? {};
    return Object.entries(facts).map(([key, value]) => ({ key, value }));
  });

  storyId = '';
  newSessionName = '';
  advanceStrategy: SelectionStrategy = 'HIGHEST_PRIORITY';

  constructor(private route: ActivatedRoute, private api: StoryApiService) {}

  ngOnInit(): void {
    this.storyId = this.route.snapshot.paramMap.get('id') ?? '';
    this.api.getStory(this.storyId).subscribe(s => this.storyTitle.set(s.title));
    this.loadSessions();
  }

  private loadSessions(): void {
    this.api.listSessions(this.storyId).subscribe(s => this.sessions.set(s));
  }

  selectSession(id: string): void {
    this.activeSessionId.set(id);
    this.narrativeTimeline.set(null);
    this.lastAdvanceResult.set(null);
    this.api.getSession(id).subscribe(s => {
      this.activeSession.set(s);
      this.loadAvailableScenes(id);
      this.loadNarrativeTimeline(id);
    });
  }

  createSession(): void {
    this.api.createSession(this.storyId, this.newSessionName.trim()).subscribe(s => {
      this.newSessionName = '';
      this.showNewSession.set(false);
      this.sessions.update(arr => [...arr, s]);
      this.selectSession(s.id);
    });
  }

  loadAvailableScenes(sessionId: string): void {
    this.loadingScenes.set(true);
    this.api.getAvailableScenes(sessionId).subscribe({
      next: r => { this.availableScenes.set(r.availableScenes); this.loadingScenes.set(false); },
      error: () => this.loadingScenes.set(false)
    });
  }

  applyScene(sceneId: string): void {
    const sid = this.activeSessionId();
    if (!sid) return;
    this.applying.set(true);
    this.api.applyScene(sid, sceneId).subscribe({
      next: () => {
        this.applying.set(false);
        this.api.getSession(sid).subscribe(s => {
          this.activeSession.set(s);
          this.loadAvailableScenes(sid);
          this.loadNarrativeTimeline(sid);
        });
      },
      error: () => this.applying.set(false)
    });
  }

  autoAdvance(): void {
    const sid = this.activeSessionId();
    if (!sid) return;
    this.applying.set(true);
    this.api.advanceStory(sid, this.advanceStrategy).subscribe({
      next: result => {
        this.applying.set(false);
        this.lastAdvanceResult.set(result);
        if (result.success) {
          this.api.getSession(sid).subscribe(s => {
            this.activeSession.set(s);
            this.loadAvailableScenes(sid);
            this.loadNarrativeTimeline(sid);
          });
        }
      },
      error: () => this.applying.set(false)
    });
  }

  private loadNarrativeTimeline(sessionId: string): void {
    this.api.getNarrativeTimeline(sessionId).subscribe({
      next: t => this.narrativeTimeline.set(t),
      error: () => {}
    });
  }

  hasWarnings(scene: AvailableScene): boolean {
    return scene.potentialContradictions.some(c => c.severity === 'WARNING');
  }

  hasErrors(scene: AvailableScene): boolean {
    return scene.potentialContradictions.some(c => c.severity === 'ERROR');
  }
}
