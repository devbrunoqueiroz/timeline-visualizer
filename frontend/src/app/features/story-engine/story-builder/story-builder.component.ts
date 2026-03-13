import { Component, OnInit, signal, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { StoryApiService } from '../../../infrastructure/api/story-api.service';
import { Story, Scene, AddSceneRequest, UpdateSceneRequest, Requirement, Effect, RequirementType, EffectType } from '../../../domain/story.model';


@Component({
  selector: 'app-story-builder',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="page">
      <header class="page-header">
        <div class="header-left">
          <a routerLink="/stories" class="back-btn">← Stories</a>
          <div class="title-group">
            <h1 class="page-title">{{ story()?.title ?? 'Loading…' }}</h1>
            <span class="subtitle">Scene Builder</span>
          </div>
        </div>
        <div class="header-actions">
          <a class="btn-nav" [routerLink]="['/stories', storyId, 'play']">▶ Play</a>
          <a class="btn-nav btn-graph" [routerLink]="['/stories', storyId, 'graph']">⬡ Graph</a>
          <button class="create-btn" (click)="openCreate()">+ Add Scene</button>
        </div>
      </header>

      @if (loading()) {
        <div class="loading">Loading story…</div>
      } @else if (story()) {
        <div class="content">

          @if (showForm()) {
            <div class="form-overlay" (click)="closeForm()">
              <div class="form-panel" (click)="$event.stopPropagation()">
                <h2 class="form-title">{{ editingSceneId ? 'Edit Scene' : 'New Scene' }}</h2>

                <div class="field">
                  <label class="label">Title *</label>
                  <input class="input" [(ngModel)]="form.title" placeholder="Scene title" />
                </div>
                <div class="field">
                  <label class="label">Description</label>
                  <textarea class="textarea" [(ngModel)]="form.description" rows="3" placeholder="What happens in this scene?"></textarea>
                </div>
                <div class="field-row">
                  <label class="label">
                    <input type="checkbox" [(ngModel)]="form.repeatable" />
                    Repeatable
                  </label>
                </div>
                <div class="field">
                  <label class="label">Priority ({{ form.priority }})</label>
                  <input type="range" class="range-input" [(ngModel)]="form.priority" min="0" max="100" step="1" />
                </div>
                <div class="field">
                  <label class="label">Tags <span class="hint">comma-separated</span></label>
                  <input class="input" [(ngModel)]="form.tags" placeholder="combat, twist, character-development" />
                </div>
                <div class="field">
                  <label class="label">Involved Characters <span class="hint">comma-separated IDs</span></label>
                  <input class="input" [(ngModel)]="form.involvedCharacters" placeholder="gandalf, frodo" />
                </div>

                <!-- Requirements -->
                <div class="section-header">
                  <span class="section-label">Requirements</span>
                  <button class="add-small" (click)="addRequirement()">+ Add</button>
                </div>
                @for (req of form.requirements; track $index; let i = $index) {
                  <div class="condition-row">
                    <input class="input small" [(ngModel)]="req.factKey" placeholder="fact key" />
                    <select class="select" [(ngModel)]="req.type">
                      <option value="FACT_EXISTS">EXISTS</option>
                      <option value="FACT_ABSENT">ABSENT</option>
                      <option value="FACT_EQUALS">EQUALS</option>
                    </select>
                    @if (req.type === 'FACT_EQUALS') {
                      <input class="input small" [(ngModel)]="req.expectedValue" placeholder="value" />
                    }
                    <button class="remove-btn" (click)="removeRequirement(i)">✕</button>
                  </div>
                }

                <!-- Effects -->
                <div class="section-header">
                  <span class="section-label">Effects</span>
                  <button class="add-small" (click)="addEffect()">+ Add</button>
                </div>
                @for (eff of form.effects; track $index; let i = $index) {
                  <div class="condition-row">
                    <select class="select" [(ngModel)]="eff.type">
                      <option value="ADD_FACT">ADD</option>
                      <option value="REMOVE_FACT">REMOVE</option>
                      <option value="SET_FACT">SET</option>
                    </select>
                    <input class="input small" [(ngModel)]="eff.factKey" placeholder="fact key" />
                    @if (eff.type !== 'REMOVE_FACT') {
                      <input class="input small" [(ngModel)]="eff.factValue" placeholder="value" />
                    }
                    <button class="remove-btn" (click)="removeEffect(i)">✕</button>
                  </div>
                }

                <div class="form-actions">
                  <button class="btn-secondary" (click)="closeForm()">Cancel</button>
                  <button class="btn-primary" (click)="saveScene()" [disabled]="!form.title.trim()">
                    {{ editingSceneId ? 'Update Scene' : 'Save Scene' }}
                  </button>
                </div>
              </div>
            </div>
          }

          <div class="scenes-list">
            @if (story()!.scenes.length === 0) {
              <div class="empty">
                <div class="empty-icon">🎬</div>
                <p>No scenes yet. Add your first scene to start building the narrative.</p>
              </div>
            } @else {
              @for (scene of story()!.scenes; track scene.id) {
                <div class="scene-card">
                  <div class="scene-header">
                    <h3 class="scene-title">{{ scene.title }}</h3>
                    <div class="scene-actions">
                      <button class="btn-edit-scene" (click)="openEdit(scene)">✏</button>
                      <button class="btn-del-scene" (click)="deleteScene(scene.id)">🗑</button>
                    </div>
                    <div class="scene-badges">
                      @if (scene.repeatable) { <span class="badge badge-repeat">repeatable</span> }
                      @if (scene.requirements.length > 0) {
                        <span class="badge badge-req">{{ scene.requirements.length }} req</span>
                      }
                      @if (scene.effects.length > 0) {
                        <span class="badge badge-eff">{{ scene.effects.length }} eff</span>
                      }
                      @if (scene.priority != null) {
                        <span class="badge badge-prio">P{{ scene.priority }}</span>
                      }
                    </div>
                  </div>
                  @if (scene.description) {
                    <p class="scene-desc">{{ scene.description }}</p>
                  }
                  @if (scene.requirements.length > 0) {
                    <div class="condition-section">
                      <span class="cond-label req-label">REQUIRES</span>
                      <div class="cond-list">
                        @for (r of scene.requirements; track r.factKey) {
                          <span class="cond-tag req-tag">
                            {{ r.factKey }}
                            @if (r.type === 'FACT_ABSENT') { <em>absent</em> }
                            @if (r.type === 'FACT_EQUALS') { = {{ r.expectedValue }} }
                          </span>
                        }
                      </div>
                    </div>
                  }
                  @if (scene.effects.length > 0) {
                    <div class="condition-section">
                      <span class="cond-label eff-label">SETS</span>
                      <div class="cond-list">
                        @for (e of scene.effects; track e.factKey) {
                          <span class="cond-tag eff-tag">
                            @if (e.type === 'ADD_FACT') { +{{ e.factKey }} }
                            @if (e.type === 'REMOVE_FACT') { -{{ e.factKey }} }
                            @if (e.type === 'SET_FACT') { {{ e.factKey }}={{ e.factValue }} }
                          </span>
                        }
                      </div>
                    </div>
                  }
                  @if (scene.tags && scene.tags.length > 0) {
                    <div class="condition-section">
                      <span class="cond-label tag-label">TAGS</span>
                      <div class="cond-list">
                        @for (t of scene.tags; track t) {
                          <span class="cond-tag tag-chip">{{ t }}</span>
                        }
                      </div>
                    </div>
                  }
                  @if (scene.involvedCharacters && scene.involvedCharacters.length > 0) {
                    <div class="condition-section">
                      <span class="cond-label char-label">CHARS</span>
                      <div class="cond-list">
                        @for (c of scene.involvedCharacters; track c) {
                          <span class="cond-tag char-chip">{{ c }}</span>
                        }
                      </div>
                    </div>
                  }
                </div>
              }
            }
          </div>
        </div>
      }
    </div>
  `,
  styles: [`
    .page { min-height: 100vh; background: #0f172a; color: #e2e8f0; font-family: system-ui, sans-serif; }

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
      text-decoration: none; transition: all 0.15s;
    }
    .btn-nav:hover { border-color: #6366f1; color: #818cf8; }
    .btn-graph { border-color: #312e81; color: #a5b4fc; }
    .create-btn {
      padding: 7px 18px; background: #6366f1; color: white; border: none;
      border-radius: 8px; font-size: 13px; font-weight: 600; cursor: pointer;
    }
    .create-btn:hover { background: #4f46e5; }

    .loading { padding: 60px; text-align: center; color: #64748b; }

    .content { padding: 24px 28px; }

    /* Form overlay */
    .form-overlay {
      position: fixed; inset: 0; background: rgba(0,0,0,0.7);
      display: flex; align-items: center; justify-content: center; z-index: 100;
    }
    .form-panel {
      background: #1e293b; border: 1px solid #334155; border-radius: 16px;
      padding: 28px; width: 560px; max-height: 85vh; overflow-y: auto;
    }
    .form-title { font-size: 18px; font-weight: 700; color: #f1f5f9; margin: 0 0 20px; }
    .field { margin-bottom: 14px; }
    .field-row { margin-bottom: 14px; }
    .label { display: block; font-size: 12px; font-weight: 600; color: #94a3b8; margin-bottom: 6px; text-transform: uppercase; }
    .input, .textarea, .select {
      width: 100%; box-sizing: border-box; background: #0f172a; border: 1px solid #334155;
      color: #e2e8f0; border-radius: 8px; padding: 9px 12px; font-size: 13px; outline: none;
    }
    .input:focus, .textarea:focus, .select:focus { border-color: #6366f1; }
    .textarea { resize: vertical; font-family: inherit; }
    .select { cursor: pointer; }
    .input.small { width: auto; flex: 1; min-width: 80px; }

    .section-header {
      display: flex; align-items: center; justify-content: space-between;
      margin: 18px 0 10px;
    }
    .section-label { font-size: 12px; font-weight: 700; text-transform: uppercase; color: #64748b; }
    .add-small {
      padding: 3px 10px; background: #1e1b4b; color: #818cf8; border: 1px solid #312e81;
      border-radius: 4px; font-size: 12px; cursor: pointer;
    }
    .add-small:hover { background: #312e81; }

    .condition-row {
      display: flex; gap: 8px; align-items: center; margin-bottom: 8px;
    }
    .remove-btn {
      padding: 4px 8px; background: transparent; color: #64748b;
      border: 1px solid #334155; border-radius: 4px; cursor: pointer; font-size: 12px;
    }
    .remove-btn:hover { border-color: #ef4444; color: #ef4444; }

    .form-actions { display: flex; gap: 10px; justify-content: flex-end; margin-top: 20px; }
    .btn-primary {
      padding: 8px 20px; background: #6366f1; color: white; border: none;
      border-radius: 6px; font-size: 13px; font-weight: 600; cursor: pointer;
    }
    .btn-primary:disabled { opacity: 0.4; cursor: not-allowed; }
    .btn-secondary {
      padding: 8px 20px; background: transparent; color: #94a3b8;
      border: 1px solid #334155; border-radius: 6px; font-size: 13px; cursor: pointer;
    }

    /* Scenes list */
    .scenes-list { display: flex; flex-direction: column; gap: 14px; }
    .empty { padding: 60px 0; text-align: center; color: #475569; }
    .empty-icon { font-size: 40px; margin-bottom: 12px; }
    .empty p { font-size: 14px; }

    .scene-card {
      background: #1e293b; border: 1px solid #334155; border-radius: 12px;
      padding: 18px 20px; transition: border-color 0.15s;
    }
    .scene-card:hover { border-color: #475569; }
    .scene-header { display: flex; align-items: flex-start; justify-content: space-between; margin-bottom: 8px; gap: 8px; }
    .scene-title { font-size: 15px; font-weight: 700; color: #f1f5f9; margin: 0; flex: 1; }
    .scene-actions { display: flex; gap: 4px; flex-shrink: 0; }
    .btn-edit-scene, .btn-del-scene {
      padding: 3px 8px; border-radius: 5px; font-size: 12px; cursor: pointer; border: 1px solid;
    }
    .btn-edit-scene { background: #1e1b4b; color: #818cf8; border-color: #4f46e5; }
    .btn-edit-scene:hover { background: #312e81; }
    .btn-del-scene { background: #450a0a; color: #fca5a5; border-color: #7f1d1d; }
    .btn-del-scene:hover { background: #7f1d1d; }
    .scene-badges { display: flex; gap: 6px; flex-wrap: wrap; }
    .badge {
      font-size: 10px; font-weight: 700; text-transform: uppercase;
      padding: 2px 7px; border-radius: 20px;
    }
    .badge-repeat { background: #1e3a5f; color: #7dd3fc; }
    .badge-req { background: #1a1a3e; color: #a5b4fc; }
    .badge-eff { background: #14271a; color: #86efac; }
    .badge-prio { background: #2d1a00; color: #fb923c; }

    .range-input { width: 100%; cursor: pointer; accent-color: #6366f1; }
    .hint { font-size: 10px; color: #475569; font-weight: 400; text-transform: none; margin-left: 4px; }

    .tag-label { background: #1a2a1a; color: #86efac; }
    .tag-chip { background: #1a2a1a; color: #86efac; border: 1px solid #166534; }
    .char-label { background: #1a1a2e; color: #c4b5fd; }
    .char-chip { background: #1a1a2e; color: #c4b5fd; border: 1px solid #4c1d95; }

    .scene-desc { font-size: 13px; color: #94a3b8; margin: 0 0 12px; line-height: 1.5; }

    .condition-section { display: flex; align-items: flex-start; gap: 10px; margin-top: 10px; }
    .cond-label {
      font-size: 10px; font-weight: 800; text-transform: uppercase;
      padding: 3px 7px; border-radius: 4px; white-space: nowrap; margin-top: 2px;
    }
    .req-label { background: #1a1a3e; color: #818cf8; }
    .eff-label { background: #14271a; color: #4ade80; }
    .cond-list { display: flex; flex-wrap: wrap; gap: 6px; }
    .cond-tag {
      font-size: 11px; font-family: monospace;
      padding: 3px 8px; border-radius: 4px;
    }
    .req-tag { background: #1e1b4b; color: #a5b4fc; border: 1px solid #312e81; }
    .eff-tag { background: #14532d; color: #86efac; border: 1px solid #166534; }
  `]
})
export class StoryBuilderComponent implements OnInit {
  readonly loading = signal(true);
  readonly story = signal<Story | null>(null);
  readonly showForm = signal(false);

  storyId = '';
  editingSceneId: string | null = null;

  form: {
    title: string;
    description: string;
    repeatable: boolean;
    priority: number;
    tags: string;
    involvedCharacters: string;
    requirements: Array<{ factKey: string; type: RequirementType; expectedValue: string | null }>;
    effects: Array<{ type: EffectType; factKey: string; factValue: string | null }>;
  } = this.emptyForm();

  constructor(private route: ActivatedRoute, private api: StoryApiService) {}

  ngOnInit(): void {
    this.storyId = this.route.snapshot.paramMap.get('id') ?? '';
    this.load();
  }

  private load(): void {
    this.api.getStory(this.storyId).subscribe({
      next: s => { this.story.set(s); this.loading.set(false); },
      error: () => this.loading.set(false)
    });
  }

  openCreate(): void {
    this.editingSceneId = null;
    this.form = this.emptyForm();
    this.showForm.set(true);
  }

  openEdit(scene: Scene): void {
    this.editingSceneId = scene.id;
    this.form = {
      title: scene.title,
      description: scene.description ?? '',
      repeatable: scene.repeatable,
      priority: scene.priority ?? 50,
      tags: (scene.tags ?? []).join(', '),
      involvedCharacters: (scene.involvedCharacters ?? []).join(', '),
      requirements: scene.requirements.map(r => ({ factKey: r.factKey, type: r.type, expectedValue: r.expectedValue })),
      effects: scene.effects.map(e => ({ type: e.type, factKey: e.factKey, factValue: e.factValue }))
    };
    this.showForm.set(true);
  }

  closeForm(): void {
    this.showForm.set(false);
    this.editingSceneId = null;
  }

  deleteScene(sceneId: string): void {
    if (!confirm('Delete this scene?')) return;
    this.api.deleteScene(this.storyId, sceneId).subscribe({ next: () => this.load() });
  }

  addRequirement(): void {
    this.form.requirements.push({ factKey: '', type: 'FACT_EXISTS', expectedValue: null });
  }

  removeRequirement(i: number): void {
    this.form.requirements.splice(i, 1);
  }

  addEffect(): void {
    this.form.effects.push({ type: 'ADD_FACT', factKey: '', factValue: null });
  }

  removeEffect(i: number): void {
    this.form.effects.splice(i, 1);
  }

  saveScene(): void {
    if (!this.form.title.trim()) return;
    const requirements = this.form.requirements
      .filter(r => r.factKey.trim())
      .map(r => ({ factKey: r.factKey.trim(), type: r.type, expectedValue: r.type === 'FACT_EQUALS' ? (r.expectedValue ?? '') : null }));
    const effects = this.form.effects
      .filter(e => e.factKey.trim())
      .map(e => ({ type: e.type, factKey: e.factKey.trim(), factValue: e.type !== 'REMOVE_FACT' ? (e.factValue ?? '') : null }));
    const tags = this.form.tags.split(',').map(t => t.trim()).filter(t => t.length > 0);
    const involvedCharacters = this.form.involvedCharacters.split(',').map(c => c.trim()).filter(c => c.length > 0);
    const payload = { title: this.form.title.trim(), description: this.form.description.trim(), repeatable: this.form.repeatable, requirements, effects, priority: this.form.priority, tags, involvedCharacters };

    const obs = this.editingSceneId
      ? this.api.updateScene(this.storyId, this.editingSceneId, payload as UpdateSceneRequest)
      : this.api.addScene(this.storyId, payload as AddSceneRequest);

    obs.subscribe({ next: () => { this.closeForm(); this.load(); } });
  }

  private emptyForm() {
    return { title: '', description: '', repeatable: false, priority: 50, tags: '', involvedCharacters: '', requirements: [] as any[], effects: [] as any[] };
  }
}
