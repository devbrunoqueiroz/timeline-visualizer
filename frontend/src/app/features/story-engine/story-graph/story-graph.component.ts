import {
  Component, OnInit, OnDestroy, ElementRef, ViewChild,
  signal, computed, ChangeDetectionStrategy, ChangeDetectorRef
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { StoryApiService } from '../../../infrastructure/api/story-api.service';
import {
  Story, Scene, StoryGraphView,
  Requirement, Effect, RequirementType, EffectType, UpdateSceneRequest
} from '../../../domain/story.model';
import cytoscape, { Core } from 'cytoscape';

type EditForm = {
  title: string;
  description: string;
  repeatable: boolean;
  requirements: Array<{ factKey: string; type: RequirementType; expectedValue: string | null }>;
  effects: Array<{ type: EffectType; factKey: string; factValue: string | null }>;
};

@Component({
  selector: 'app-story-graph',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="page">
      <header class="page-header">
        <div class="header-left">
          <a routerLink="/stories" class="back-btn">← Stories</a>
          <h1 class="page-title">Narrative Graph</h1>
        </div>
        <div class="header-actions">
          <a class="btn-nav" [routerLink]="['/stories', storyId, 'build']">✏ Build</a>
          <a class="btn-nav btn-play" [routerLink]="['/stories', storyId, 'play']">▶ Play</a>
          <div class="legend">
            <span class="leg-item leg-scene">Scene</span>
            <span class="leg-item leg-fact">Fact</span>
            <span class="leg-item leg-req">── req</span>
            <span class="leg-item leg-eff">── effect</span>
          </div>
        </div>
      </header>

      @if (loading()) {
        <div class="loading">Building graph…</div>
      }

      <div #cy class="cy-container"></div>

      <!-- Scene info panel -->
      @if (selectedScene()) {
        <div class="info-panel">
          <div class="info-header">
            <span class="info-type type-scene">SCENE</span>
            <div class="info-btns">
              <button class="btn-edit" (click)="openEditScene()">✏ Edit</button>
              <button class="btn-delete" (click)="confirmDeleteScene()">🗑 Delete</button>
              <button class="info-close" (click)="clearSelection()">✕</button>
            </div>
          </div>
          <div class="info-label">{{ selectedScene()!.title }}</div>
          @if (selectedScene()!.description) {
            <p class="info-desc">{{ selectedScene()!.description }}</p>
          }
          <div class="info-stats">
            <span class="stat">{{ selectedScene()!.requirements.length }} requirements</span>
            <span class="stat">{{ selectedScene()!.effects.length }} effects</span>
          </div>
        </div>
      }

      <!-- Edge info panel -->
      @if (selectedEdge()) {
        <div class="info-panel">
          <div class="info-header">
            <span class="info-type" [class]="edgeTypeClass()">{{ selectedEdge()!.edgeType }}</span>
            <div class="info-btns">
              @if (selectedEdge()!.edgeType !== 'TRANSITION') {
                <button class="btn-delete" (click)="removeRelation()">✕ Remove</button>
              }
              <button class="info-close" (click)="clearSelection()">✕</button>
            </div>
          </div>
          <div class="info-edge-detail">
            <span class="edge-from">{{ selectedEdge()!.sourceLabel }}</span>
            <span class="edge-arrow">→</span>
            <span class="edge-to">{{ selectedEdge()!.targetLabel }}</span>
          </div>
          @if (selectedEdge()!.edgeType === 'REQUIREMENT') {
            <p class="info-hint">This scene requires this fact to be present before it can be played.</p>
          } @else if (selectedEdge()!.edgeType === 'EFFECT') {
            <p class="info-hint">Playing this scene sets this fact in the world state.</p>
          } @else {
            <p class="info-hint">Transition inferred from matching requirements and effects.</p>
          }
        </div>
      }

      <!-- Edit scene modal -->
      @if (showEdit()) {
        <div class="overlay" (click)="closeEdit()">
          <div class="edit-panel" (click)="$event.stopPropagation()">
            <h2 class="edit-title">Edit Scene</h2>

            <div class="field">
              <label class="label">Title *</label>
              <input class="input" [(ngModel)]="editForm.title" />
            </div>
            <div class="field">
              <label class="label">Description</label>
              <textarea class="textarea" [(ngModel)]="editForm.description" rows="2"></textarea>
            </div>
            <div class="field-row">
              <label class="label">
                <input type="checkbox" [(ngModel)]="editForm.repeatable" /> Repeatable
              </label>
            </div>

            <div class="section-header">
              <span class="section-label">Requirements</span>
              <button class="add-small" (click)="addReq()">+ Add</button>
            </div>
            @for (req of editForm.requirements; track $index; let i = $index) {
              <div class="cond-row">
                <input class="input small" [(ngModel)]="req.factKey" placeholder="fact key" />
                <select class="select" [(ngModel)]="req.type">
                  <option value="FACT_EXISTS">EXISTS</option>
                  <option value="FACT_ABSENT">ABSENT</option>
                  <option value="FACT_EQUALS">EQUALS</option>
                </select>
                @if (req.type === 'FACT_EQUALS') {
                  <input class="input small" [(ngModel)]="req.expectedValue" placeholder="value" />
                }
                <button class="rm-btn" (click)="removeReq(i)">✕</button>
              </div>
            }

            <div class="section-header">
              <span class="section-label">Effects</span>
              <button class="add-small" (click)="addEff()">+ Add</button>
            </div>
            @for (eff of editForm.effects; track $index; let i = $index) {
              <div class="cond-row">
                <select class="select" [(ngModel)]="eff.type">
                  <option value="ADD_FACT">ADD</option>
                  <option value="REMOVE_FACT">REMOVE</option>
                  <option value="SET_FACT">SET</option>
                </select>
                <input class="input small" [(ngModel)]="eff.factKey" placeholder="fact key" />
                @if (eff.type !== 'REMOVE_FACT') {
                  <input class="input small" [(ngModel)]="eff.factValue" placeholder="value" />
                }
                <button class="rm-btn" (click)="removeEff(i)">✕</button>
              </div>
            }

            <div class="edit-actions">
              <button class="btn-secondary" (click)="closeEdit()">Cancel</button>
              <button class="btn-primary" (click)="saveEdit()" [disabled]="!editForm.title.trim() || saving()">
                {{ saving() ? 'Saving…' : 'Save' }}
              </button>
            </div>
          </div>
        </div>
      }

      <!-- Confirm delete -->
      @if (showConfirmDelete()) {
        <div class="overlay" (click)="showConfirmDelete.set(false)">
          <div class="confirm-panel" (click)="$event.stopPropagation()">
            <h3 class="confirm-title">Delete Scene?</h3>
            <p class="confirm-msg">
              "<strong>{{ selectedScene()?.title }}</strong>" will be permanently removed from this story.
            </p>
            <div class="confirm-actions">
              <button class="btn-secondary" (click)="showConfirmDelete.set(false)">Cancel</button>
              <button class="btn-danger" (click)="executeDeleteScene()" [disabled]="saving()">
                {{ saving() ? 'Deleting…' : 'Delete Scene' }}
              </button>
            </div>
          </div>
        </div>
      }
    </div>
  `,
  styles: [`
    .page { height: 100vh; display: flex; flex-direction: column; background: #0f172a; color: #e2e8f0; font-family: system-ui, sans-serif; position: relative; }

    .page-header {
      display: flex; justify-content: space-between; align-items: center;
      padding: 14px 24px; border-bottom: 1px solid #1e293b;
      background: #0f172a; z-index: 10; flex-shrink: 0;
    }
    .header-left { display: flex; align-items: center; gap: 16px; }
    .back-btn {
      color: #6366f1; font-size: 13px; font-weight: 600; text-decoration: none;
      padding: 5px 10px; border: 1px solid #312e81; border-radius: 6px;
    }
    .back-btn:hover { background: #1e1b4b; }
    .page-title { font-size: 18px; font-weight: 800; color: #f1f5f9; margin: 0; }
    .header-actions { display: flex; align-items: center; gap: 12px; }
    .btn-nav {
      padding: 6px 14px; background: transparent; color: #94a3b8;
      border: 1px solid #334155; border-radius: 6px; font-size: 12px; font-weight: 600; text-decoration: none;
    }
    .btn-nav:hover { border-color: #6366f1; color: #818cf8; }
    .btn-play { border-color: #166534; color: #86efac; }
    .legend { display: flex; gap: 10px; align-items: center; margin-left: 4px; }
    .leg-item { font-size: 11px; font-weight: 600; padding: 2px 8px; border-radius: 4px; }
    .leg-scene { background: #1e1b4b; color: #a5b4fc; border: 1px solid #4f46e5; }
    .leg-fact { background: #14271a; color: #86efac; border: 1px solid #166534; }
    .leg-req { color: #fbbf24; }
    .leg-eff { color: #34d399; }

    .loading { position: absolute; top: 80px; left: 50%; transform: translateX(-50%); color: #64748b; }
    .cy-container { flex: 1; width: 100%; }

    /* Info panel */
    .info-panel {
      position: absolute; bottom: 24px; right: 24px;
      background: #1e293b; border: 1px solid #334155; border-radius: 12px;
      padding: 16px 18px; min-width: 260px; max-width: 340px; z-index: 50;
    }
    .info-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 10px; }
    .info-type { font-size: 10px; font-weight: 700; text-transform: uppercase; padding: 3px 8px; border-radius: 4px; }
    .type-scene { background: #1e1b4b; color: #a5b4fc; }
    .type-fact { background: #14271a; color: #86efac; }
    .type-req { background: #1c1400; color: #fbbf24; }
    .type-eff { background: #022c22; color: #34d399; }
    .type-transition { background: #1e1b4b; color: #818cf8; }
    .info-btns { display: flex; gap: 6px; align-items: center; }
    .btn-edit {
      padding: 4px 10px; background: #1e1b4b; color: #818cf8; border: 1px solid #4f46e5;
      border-radius: 5px; font-size: 11px; font-weight: 600; cursor: pointer;
    }
    .btn-edit:hover { background: #312e81; }
    .btn-delete {
      padding: 4px 10px; background: #450a0a; color: #fca5a5; border: 1px solid #7f1d1d;
      border-radius: 5px; font-size: 11px; font-weight: 600; cursor: pointer;
    }
    .btn-delete:hover { background: #7f1d1d; }
    .info-close { background: transparent; border: none; color: #64748b; cursor: pointer; font-size: 14px; }
    .info-label { font-size: 15px; font-weight: 700; color: #f1f5f9; margin-bottom: 4px; }
    .info-desc { font-size: 12px; color: #94a3b8; margin: 0 0 8px; line-height: 1.5; }
    .info-stats { display: flex; gap: 10px; }
    .stat { font-size: 11px; color: #475569; }

    .info-edge-detail { display: flex; align-items: center; gap: 8px; margin-bottom: 8px; }
    .edge-from, .edge-to { font-size: 12px; font-weight: 600; color: #e2e8f0; font-family: monospace; }
    .edge-arrow { color: #475569; }
    .info-hint { font-size: 11px; color: #64748b; margin: 0; line-height: 1.4; }

    /* Overlay */
    .overlay {
      position: fixed; inset: 0; background: rgba(0,0,0,0.7);
      display: flex; align-items: center; justify-content: center; z-index: 200;
    }

    /* Edit panel */
    .edit-panel {
      background: #1e293b; border: 1px solid #334155; border-radius: 16px;
      padding: 28px; width: 560px; max-height: 85vh; overflow-y: auto;
    }
    .edit-title { font-size: 17px; font-weight: 700; color: #f1f5f9; margin: 0 0 20px; }
    .field { margin-bottom: 14px; }
    .field-row { margin-bottom: 14px; }
    .label { display: block; font-size: 11px; font-weight: 700; color: #64748b; text-transform: uppercase; margin-bottom: 5px; }
    .input, .textarea, .select {
      width: 100%; box-sizing: border-box; background: #0f172a; border: 1px solid #334155;
      color: #e2e8f0; border-radius: 7px; padding: 8px 11px; font-size: 13px; outline: none;
    }
    .input:focus, .textarea:focus, .select:focus { border-color: #6366f1; }
    .textarea { resize: vertical; font-family: inherit; }
    .input.small { flex: 1; min-width: 80px; width: auto; }
    .section-header { display: flex; align-items: center; justify-content: space-between; margin: 16px 0 8px; }
    .section-label { font-size: 11px; font-weight: 700; text-transform: uppercase; color: #475569; }
    .add-small {
      padding: 3px 10px; background: #1e1b4b; color: #818cf8; border: 1px solid #312e81;
      border-radius: 4px; font-size: 12px; cursor: pointer;
    }
    .cond-row { display: flex; gap: 7px; align-items: center; margin-bottom: 7px; }
    .rm-btn {
      padding: 4px 8px; background: transparent; color: #64748b;
      border: 1px solid #334155; border-radius: 4px; cursor: pointer; font-size: 12px; flex-shrink: 0;
    }
    .rm-btn:hover { border-color: #ef4444; color: #ef4444; }
    .edit-actions { display: flex; gap: 10px; justify-content: flex-end; margin-top: 20px; }
    .btn-primary {
      padding: 8px 20px; background: #6366f1; color: white; border: none;
      border-radius: 6px; font-size: 13px; font-weight: 600; cursor: pointer;
    }
    .btn-primary:disabled { opacity: 0.4; cursor: not-allowed; }
    .btn-secondary {
      padding: 8px 20px; background: transparent; color: #94a3b8;
      border: 1px solid #334155; border-radius: 6px; font-size: 13px; cursor: pointer;
    }

    /* Confirm delete */
    .confirm-panel {
      background: #1e293b; border: 1px solid #7f1d1d; border-radius: 12px; padding: 24px; width: 380px;
    }
    .confirm-title { font-size: 16px; font-weight: 700; color: #fca5a5; margin: 0 0 10px; }
    .confirm-msg { font-size: 13px; color: #94a3b8; margin: 0 0 20px; line-height: 1.5; }
    .confirm-actions { display: flex; gap: 10px; justify-content: flex-end; }
    .btn-danger {
      padding: 8px 20px; background: #7f1d1d; color: #fca5a5; border: 1px solid #ef4444;
      border-radius: 6px; font-size: 13px; font-weight: 600; cursor: pointer;
    }
    .btn-danger:hover { background: #991b1b; }
    .btn-danger:disabled { opacity: 0.4; cursor: not-allowed; }
  `]
})
export class StoryGraphComponent implements OnInit, OnDestroy {
  @ViewChild('cy', { static: true }) cyEl!: ElementRef;

  readonly loading = signal(true);
  readonly saving = signal(false);
  readonly showEdit = signal(false);
  readonly showConfirmDelete = signal(false);

  readonly selectedScene = signal<Scene | null>(null);
  readonly selectedEdge = signal<{ edgeType: string; sourceLabel: string; targetLabel: string; sceneId: string; itemIndex: number; itemKind: 'req' | 'eff' | null } | null>(null);

  readonly edgeTypeClass = computed(() => {
    const t = this.selectedEdge()?.edgeType;
    if (t === 'REQUIREMENT') return 'info-type type-req';
    if (t === 'EFFECT') return 'info-type type-eff';
    return 'info-type type-transition';
  });

  editForm: EditForm = this.emptyForm();
  storyId = '';

  private cy: Core | null = null;
  private story: Story | null = null;
  // maps nodeId → label (for edge label display)
  private nodeLabels = new Map<string, string>();

  constructor(
    private route: ActivatedRoute,
    private api: StoryApiService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.storyId = this.route.snapshot.paramMap.get('id') ?? '';
    this.reload();
  }

  ngOnDestroy(): void {
    this.cy?.destroy();
  }

  private reload(): void {
    this.loading.set(true);
    // Load both story (for scene data) and graph (for visualization)
    this.api.getStory(this.storyId).subscribe(story => {
      this.story = story;
      this.api.getStoryGraph(this.storyId).subscribe(graph => {
        this.loading.set(false);
        this.buildNodeLabels(graph);
        if (this.cy) {
          this.cy.destroy();
          this.cy = null;
        }
        this.initGraph(graph);
        this.cdr.markForCheck();
      });
    });
  }

  private buildNodeLabels(graph: StoryGraphView): void {
    this.nodeLabels.clear();
    graph.nodes.forEach(n => this.nodeLabels.set(n.id, n.label));
  }

  private initGraph(graph: StoryGraphView): void {
    const elements: any[] = [];
    graph.nodes.forEach(n => elements.push({ data: { id: n.id, label: n.label, nodeType: n.nodeType } }));
    graph.edges.forEach(e => elements.push({ data: { id: e.id, source: e.sourceId, target: e.targetId, edgeType: e.edgeType } }));

    this.cy = cytoscape({
      container: this.cyEl.nativeElement,
      elements,
      layout: { name: 'breadthfirst', directed: true, padding: 40, spacingFactor: 1.4 } as any,
      style: [
        {
          selector: 'node[nodeType="SCENE"]',
          style: {
            'background-color': '#312e81', 'border-color': '#6366f1', 'border-width': 2,
            'label': 'data(label)', 'color': '#e0e7ff', 'font-size': '11px',
            'text-wrap': 'wrap', 'text-max-width': '100px', 'width': 110, 'height': 40,
            'shape': 'round-rectangle', 'text-valign': 'center', 'text-halign': 'center', 'padding': '8px'
          }
        },
        {
          selector: 'node[nodeType="FACT"]',
          style: {
            'background-color': '#14532d', 'border-color': '#22c55e', 'border-width': 1,
            'label': 'data(label)', 'color': '#86efac', 'font-size': '10px', 'font-family': 'monospace',
            'text-wrap': 'wrap', 'text-max-width': '80px', 'width': 90, 'height': 30,
            'shape': 'diamond', 'text-valign': 'center', 'text-halign': 'center'
          }
        },
        { selector: 'node:selected', style: { 'border-width': 3, 'border-color': '#f59e0b' } },
        {
          selector: 'edge[edgeType="REQUIREMENT"]',
          style: {
            'line-color': '#fbbf24', 'target-arrow-color': '#fbbf24', 'target-arrow-shape': 'triangle',
            'curve-style': 'bezier', 'width': 1.5, 'line-style': 'dashed',
            'label': 'req', 'font-size': '9px', 'color': '#fbbf24', 'text-rotation': 'autorotate'
          }
        },
        {
          selector: 'edge[edgeType="EFFECT"]',
          style: {
            'line-color': '#34d399', 'target-arrow-color': '#34d399', 'target-arrow-shape': 'triangle',
            'curve-style': 'bezier', 'width': 2,
            'label': 'effect', 'font-size': '9px', 'color': '#34d399', 'text-rotation': 'autorotate'
          }
        },
        {
          selector: 'edge[edgeType="TRANSITION"]',
          style: {
            'line-color': '#6366f1', 'target-arrow-color': '#6366f1', 'target-arrow-shape': 'triangle',
            'curve-style': 'bezier', 'width': 1.5,
            'label': 'next', 'font-size': '9px', 'color': '#818cf8', 'text-rotation': 'autorotate'
          }
        },
        { selector: 'edge:selected', style: { 'width': 3, 'line-color': '#f59e0b', 'target-arrow-color': '#f59e0b' } }
      ]
    });

    this.cy.on('tap', 'node[nodeType="SCENE"]', (evt) => {
      const sceneId = evt.target.id();
      const scene = this.story?.scenes.find(s => s.id === sceneId) ?? null;
      this.selectedScene.set(scene);
      this.selectedEdge.set(null);
      this.cdr.markForCheck();
    });

    this.cy.on('tap', 'edge', (evt) => {
      const edgeType: string = evt.target.data('edgeType');
      const sourceId: string = evt.target.data('source');
      const targetId: string = evt.target.data('target');

      // Determine sceneId and itemIndex for REQUIREMENT/EFFECT edges
      let sceneId = '';
      let itemIndex = -1;
      let itemKind: 'req' | 'eff' | null = null;

      if (edgeType === 'REQUIREMENT' && this.story) {
        // sourceId = sceneId, targetId = factKey node
        const scene = this.story.scenes.find(s => s.id === sourceId);
        if (scene) {
          const factLabel = this.nodeLabels.get(targetId) ?? '';
          itemIndex = scene.requirements.findIndex(r => r.factKey === factLabel);
          sceneId = scene.id;
          itemKind = 'req';
        }
      } else if (edgeType === 'EFFECT' && this.story) {
        const scene = this.story.scenes.find(s => s.id === sourceId);
        if (scene) {
          const factLabel = this.nodeLabels.get(targetId) ?? '';
          itemIndex = scene.effects.findIndex(e => e.factKey === factLabel);
          sceneId = scene.id;
          itemKind = 'eff';
        }
      }

      this.selectedEdge.set({
        edgeType,
        sourceLabel: this.nodeLabels.get(sourceId) ?? sourceId,
        targetLabel: this.nodeLabels.get(targetId) ?? targetId,
        sceneId, itemIndex, itemKind
      });
      this.selectedScene.set(null);
      this.cdr.markForCheck();
    });

    this.cy.on('tap', (evt) => {
      if (evt.target === this.cy) {
        this.clearSelection();
      }
    });
  }

  clearSelection(): void {
    this.selectedScene.set(null);
    this.selectedEdge.set(null);
  }

  // ── Scene editing ──────────────────────────────────────────────────────────

  openEditScene(): void {
    const scene = this.selectedScene();
    if (!scene) return;
    this.editForm = {
      title: scene.title,
      description: scene.description ?? '',
      repeatable: scene.repeatable,
      requirements: scene.requirements.map(r => ({ factKey: r.factKey, type: r.type, expectedValue: r.expectedValue })),
      effects: scene.effects.map(e => ({ type: e.type, factKey: e.factKey, factValue: e.factValue }))
    };
    this.showEdit.set(true);
  }

  closeEdit(): void { this.showEdit.set(false); }

  saveEdit(): void {
    const scene = this.selectedScene();
    if (!scene || !this.editForm.title.trim()) return;
    this.saving.set(true);
    const req: UpdateSceneRequest = {
      title: this.editForm.title.trim(),
      description: this.editForm.description.trim(),
      repeatable: this.editForm.repeatable,
      requirements: this.editForm.requirements.filter(r => r.factKey.trim()).map(r => ({
        factKey: r.factKey.trim(), type: r.type,
        expectedValue: r.type === 'FACT_EQUALS' ? (r.expectedValue ?? '') : null
      })),
      effects: this.editForm.effects.filter(e => e.factKey.trim()).map(e => ({
        type: e.type, factKey: e.factKey.trim(),
        factValue: e.type !== 'REMOVE_FACT' ? (e.factValue ?? '') : null
      }))
    };
    this.api.updateScene(this.storyId, scene.id, req).subscribe({
      next: () => { this.saving.set(false); this.showEdit.set(false); this.clearSelection(); this.reload(); },
      error: () => this.saving.set(false)
    });
  }

  confirmDeleteScene(): void { this.showConfirmDelete.set(true); }

  executeDeleteScene(): void {
    const scene = this.selectedScene();
    if (!scene) return;
    this.saving.set(true);
    this.api.deleteScene(this.storyId, scene.id).subscribe({
      next: () => { this.saving.set(false); this.showConfirmDelete.set(false); this.clearSelection(); this.reload(); },
      error: () => this.saving.set(false)
    });
  }

  // ── Relation editing ───────────────────────────────────────────────────────

  removeRelation(): void {
    const edge = this.selectedEdge();
    if (!edge || !edge.sceneId || edge.itemIndex < 0 || !this.story) return;

    const scene = this.story.scenes.find(s => s.id === edge.sceneId);
    if (!scene) return;

    const requirements = [...scene.requirements];
    const effects = [...scene.effects];

    if (edge.itemKind === 'req') requirements.splice(edge.itemIndex, 1);
    if (edge.itemKind === 'eff') effects.splice(edge.itemIndex, 1);

    this.saving.set(true);
    const req: UpdateSceneRequest = {
      title: scene.title,
      description: scene.description ?? '',
      repeatable: scene.repeatable,
      requirements,
      effects
    };
    this.api.updateScene(this.storyId, scene.id, req).subscribe({
      next: () => { this.saving.set(false); this.clearSelection(); this.reload(); },
      error: () => this.saving.set(false)
    });
  }

  // ── Form helpers ───────────────────────────────────────────────────────────

  addReq(): void { this.editForm.requirements.push({ factKey: '', type: 'FACT_EXISTS', expectedValue: null }); }
  removeReq(i: number): void { this.editForm.requirements.splice(i, 1); }
  addEff(): void { this.editForm.effects.push({ type: 'ADD_FACT', factKey: '', factValue: null }); }
  removeEff(i: number): void { this.editForm.effects.splice(i, 1); }

  private emptyForm(): EditForm {
    return { title: '', description: '', repeatable: false, requirements: [], effects: [] };
  }
}
