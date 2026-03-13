import {
  Component, OnInit, OnDestroy, ElementRef, ViewChild, AfterViewInit,
  signal, computed, ChangeDetectionStrategy, inject
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { TimelineApiService } from '../../infrastructure/api/timeline-api.service';
import {
  ConnectionType, EventGraphResponse, GraphEdge, GraphNode,
  NarrativeValidation, StoryPath, StoryPathNode
} from '../../domain/timeline.model';

const TIMELINE_COLORS = ['#6366f1', '#10b981', '#f59e0b', '#ef4444', '#8b5cf6', '#06b6d4', '#f97316', '#84cc16'];

const EDGE_COLORS: Record<ConnectionType, string> = {
  CAUSAL:       '#6366f1',
  TEMPORAL:     '#3b82f6',
  REFERENCE:    '#10b981',
  CONTRAST:     '#f59e0b',
  PREREQUISITE: '#8b5cf6',
  FORESHADOW:   '#06b6d4',
  REVEAL:       '#ec4899',
  ESCALATION:   '#ef4444',
  RESOLUTION:   '#22c55e',
  PARALLEL:     '#a16207',
};

const EDGE_LABELS: Record<ConnectionType, string> = {
  CAUSAL:       'Causal',
  TEMPORAL:     'Temporal',
  REFERENCE:    'Reference',
  CONTRAST:     'Contrast',
  PREREQUISITE: 'Prerequisite',
  FORESHADOW:   'Foreshadow',
  REVEAL:       'Reveal',
  ESCALATION:   'Escalation',
  RESOLUTION:   'Resolution',
  PARALLEL:     'Parallel',
};

const INFERRED_COLOR = '#64748b';
const WARNING_COLOR  = '#f59e0b';

@Component({
  selector: 'app-graph-view',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="graph-page">
      <header class="graph-header">
        <a routerLink="/" class="back-btn">← Back to Canvas</a>
        <h1 class="graph-title">Graph View</h1>
        <div class="graph-controls">
          <label class="filter-label">Filter by type:</label>
          @for (type of connectionTypes; track type) {
            <label class="type-chip" [style.border-color]="edgeColor(type)">
              <input type="checkbox"
                     [checked]="activeTypes().has(type)"
                     (change)="toggleType(type)">
              <span [style.color]="edgeColor(type)">{{ edgeLabel(type) }}</span>
            </label>
          }
          <button class="fit-btn" (click)="fitGraph()">Fit</button>
          <button class="path-btn"
                  [class.active]="pathMode()"
                  (click)="togglePathMode()">
            ⟶ Find Path
          </button>
          <button class="warnings-btn"
                  [class.active]="showWarnings()"
                  (click)="toggleWarnings()">
            ⚠ Inconsistencies
            @if (warningCount > 0) {
              <span class="badge">{{ warningCount }}</span>
            }
          </button>
        </div>
      </header>

      <div class="graph-body">
        <div #cytoscapeContainer class="cy-container"></div>

        @if (selectedNode()) {
          <aside class="detail-panel">
            <button class="close-btn" (click)="selectedNode.set(null)">✕</button>
            <div class="detail-timeline" [style.color]="nodeColor(selectedNode()!.timelineId)">
              {{ selectedNode()!.timelineName }}
            </div>
            <div class="detail-date">{{ selectedNode()!.temporalLabel }}</div>
            <h2 class="detail-title">{{ selectedNode()!.title }}</h2>
            @if (selectedNode()!.contentText) {
              @if (selectedNode()!.contentType === 'RICH_TEXT') {
                <div class="detail-content rich" [innerHTML]="selectedNode()!.contentText"></div>
              } @else {
                <div class="detail-content">{{ selectedNode()!.contentText }}</div>
              }
            }
            <div class="detail-connections">
              <strong>Connections:</strong>
              @for (edge of edgesForSelected(); track edge.id) {
                <div class="edge-item" [style.border-left-color]="edgeColor(edge.connectionType!)"
                     [class.has-warning]="edgeWarnings(edge.id).length > 0">
                  <div class="edge-header">
                    <span class="edge-type" [style.color]="edgeColor(edge.connectionType!)">
                      {{ edgeLabel(edge.connectionType!) }}
                    </span>
                    @if (edgeWarnings(edge.id).length > 0) {
                      <span class="warning-badge">⚠</span>
                    }
                  </div>
                  <span class="edge-target">→ {{ nodeTitle(edge.targetEventId) }}</span>
                  @if (edge.description) {
                    <div class="edge-desc">{{ edge.description }}</div>
                  }
                  @for (w of edgeWarnings(edge.id); track w.connectionId + w.message) {
                    <div class="edge-warning">
                      <div class="warning-msg">{{ w.message }}</div>
                      @if (w.suggestedFix) {
                        <div class="warning-fix">💡 {{ w.suggestedFix }}</div>
                      }
                    </div>
                  }
                </div>
              }
            </div>
          </aside>
        }

        @if (showWarnings() && warningCount > 0) {
          <aside class="warnings-panel">
            <div class="warnings-header">
              <span>⚠ Narrative Inconsistencies ({{ warningCount }})</span>
              <button class="close-btn" (click)="showWarnings.set(false)">✕</button>
            </div>
            @for (v of allValidations; track v.connectionId + v.message) {
              <div class="validation-item" [class.severity-warning]="v.severity === 'WARNING'">
                <div class="validation-conn">
                  <span class="conn-from">{{ nodeTitle(edgeById(v.connectionId)?.sourceEventId ?? '') }}</span>
                  <span class="conn-arrow"> → </span>
                  <span class="conn-type" [style.color]="edgeColor(edgeById(v.connectionId)?.connectionType!)">
                    {{ edgeLabel(edgeById(v.connectionId)?.connectionType!) }}
                  </span>
                  <span class="conn-arrow"> → </span>
                  <span class="conn-to">{{ nodeTitle(edgeById(v.connectionId)?.targetEventId ?? '') }}</span>
                </div>
                <div class="validation-msg">{{ v.message }}</div>
                @if (v.suggestedFix) {
                  <div class="validation-fix">💡 {{ v.suggestedFix }}</div>
                }
              </div>
            }
          </aside>
        }

        @if (pathMode()) {
          <aside class="path-panel">
            <div class="path-panel-header">
              <span>⟶ Story Path Finder</span>
              <button class="close-btn" (click)="togglePathMode()">✕</button>
            </div>
            <div class="path-instructions">
              @if (!pathFrom()) {
                <span class="path-hint">Click the <strong>start event</strong> on the graph</span>
              } @else if (!pathTo()) {
                <div class="path-step">
                  <span class="path-step-label from-label">From:</span>
                  <span class="path-step-title">{{ pathFrom()!.title }}</span>
                </div>
                <span class="path-hint">Now click the <strong>end event</strong></span>
              } @else {
                <div class="path-step">
                  <span class="path-step-label from-label">From:</span>
                  <span class="path-step-title">{{ pathFrom()!.title }}</span>
                </div>
                <div class="path-step">
                  <span class="path-step-label to-label">To:</span>
                  <span class="path-step-title">{{ pathTo()!.title }}</span>
                </div>
              }
            </div>

            <div class="path-options">
              <label class="path-option-label">
                <input type="checkbox"
                       [checked]="pathExplicitOnly()"
                       (change)="toggleExplicitOnly()">
                Explicit connections only
              </label>
            </div>

            @if (pathLoading()) {
              <div class="path-loading">Searching…</div>
            }
            @if (pathError()) {
              <div class="path-error">{{ pathError() }}</div>
            }
            @if (pathResult(); as result) {
              @if (result.found) {
                <div class="path-found">
                  <div class="path-summary">
                    Path found — <strong>{{ result.hopCount }}</strong> {{ result.hopCount === 1 ? 'hop' : 'hops' }}
                  </div>
                  <ol class="path-steps-list">
                    @for (node of result.nodes; track node.id; let i = $index) {
                      <li class="path-list-item" [class.first]="i === 0" [class.last]="i === result.nodes.length - 1">
                        @if (node.temporalLabel) {
                          <span class="path-item-date">{{ node.temporalLabel }}</span>
                        }
                        <span class="path-item-title">{{ node.title }}</span>
                        @if (i < result.edges.length) {
                          <span class="path-item-edge"
                                [style.color]="edgeColor(result.edges[i].connectionType)">
                            {{ result.edges[i].inferred ? '→ (temporal)' : ('→ ' + edgeLabel(result.edges[i].connectionType)) }}
                          </span>
                        }
                      </li>
                    }
                  </ol>
                </div>
              } @else {
                <div class="path-not-found">No path found between these events.</div>
              }
            }

            @if (pathFrom()) {
              <button class="reset-path-btn" (click)="resetPath()">Reset</button>
            }
          </aside>
        }

        @if (loading()) {
          <div class="loading-overlay">Loading graph…</div>
        }
        @if (error()) {
          <div class="error-overlay">{{ error() }}</div>
        }
      </div>

      <div class="legend">
        @for (entry of timelineLegend(); track entry.id) {
          <div class="legend-item">
            <span class="legend-dot" [style.background]="entry.color"></span>
            {{ entry.name }}
          </div>
        }
        <div class="legend-item">
          <span class="legend-line inferred-line"></span>
          Timeline order
        </div>
        @if (warningCount > 0) {
          <div class="legend-item">
            <span class="legend-line warning-line"></span>
            Inconsistency
          </div>
        }
      </div>
    </div>
  `,
  styles: [`
    :host { display: flex; flex-direction: column; height: 100vh; background: #f8fafc; }

    .graph-page { display: flex; flex-direction: column; height: 100vh; }

    .graph-header {
      display: flex; align-items: center; gap: 16px; flex-wrap: wrap;
      padding: 12px 20px; background: white; border-bottom: 1px solid #e2e8f0;
      flex-shrink: 0;
    }

    .back-btn {
      text-decoration: none; color: #6366f1; font-weight: 600; font-size: 13px;
      padding: 6px 12px; border: 1px solid #c7d2fe; border-radius: 6px;
      transition: background 0.15s;
    }
    .back-btn:hover { background: #eef2ff; }

    .graph-title { font-size: 16px; font-weight: 700; color: #0f172a; margin: 0; }

    .graph-controls { display: flex; align-items: center; gap: 10px; margin-left: auto; flex-wrap: wrap; }

    .filter-label { font-size: 12px; color: #64748b; font-weight: 600; }

    .type-chip {
      display: flex; align-items: center; gap: 5px; cursor: pointer;
      padding: 4px 10px; border: 1.5px solid; border-radius: 20px; font-size: 12px;
    }
    .type-chip input { accent-color: currentColor; }

    .fit-btn {
      padding: 6px 14px; background: #6366f1; color: white; border: none;
      border-radius: 6px; font-size: 12px; font-weight: 600; cursor: pointer;
      transition: background 0.15s;
    }
    .fit-btn:hover { background: #4f46e5; }

    .warnings-btn {
      padding: 6px 14px; background: #fff7ed; color: #92400e; border: 1.5px solid #fcd34d;
      border-radius: 6px; font-size: 12px; font-weight: 600; cursor: pointer;
      display: flex; align-items: center; gap: 6px; transition: background 0.15s;
    }
    .warnings-btn.active { background: #fef3c7; border-color: #f59e0b; }
    .warnings-btn:hover { background: #fef3c7; }

    .badge {
      background: #f59e0b; color: white; border-radius: 10px;
      padding: 1px 6px; font-size: 11px; font-weight: 700;
    }

    .graph-body { flex: 1; position: relative; overflow: hidden; display: flex; }

    .cy-container { flex: 1; height: 100%; }

    .detail-panel {
      width: 300px; background: white; border-left: 1px solid #e2e8f0;
      padding: 20px; overflow-y: auto; position: relative; flex-shrink: 0;
    }

    .warnings-panel {
      width: 340px; background: #fffbeb; border-left: 1px solid #fcd34d;
      padding: 0; overflow-y: auto; position: relative; flex-shrink: 0;
    }
    .warnings-header {
      display: flex; justify-content: space-between; align-items: center;
      padding: 12px 16px; background: #fef3c7; border-bottom: 1px solid #fcd34d;
      font-size: 13px; font-weight: 700; color: #92400e; position: sticky; top: 0;
    }

    .validation-item {
      padding: 12px 16px; border-bottom: 1px solid #fde68a;
    }
    .validation-conn {
      font-size: 11px; color: #78350f; margin-bottom: 4px; display: flex; flex-wrap: wrap; gap: 2px; align-items: center;
    }
    .conn-from, .conn-to { font-weight: 600; }
    .conn-arrow { color: #92400e; }
    .conn-type { font-weight: 700; font-size: 10px; }
    .validation-msg { font-size: 12px; color: #451a03; line-height: 1.5; margin-bottom: 4px; }
    .validation-fix { font-size: 11px; color: #92400e; font-style: italic; }

    .close-btn {
      position: absolute; top: 12px; right: 12px; background: none; border: none;
      font-size: 16px; cursor: pointer; color: #94a3b8; padding: 2px 6px;
    }
    .warnings-panel .close-btn { position: static; }
    .close-btn:hover { color: #475569; }

    .detail-timeline { font-size: 11px; font-weight: 700; text-transform: uppercase; letter-spacing: 0.5px; margin-bottom: 4px; }
    .detail-date { font-size: 11px; color: #64748b; margin-bottom: 8px; }
    .detail-title { font-size: 16px; font-weight: 700; color: #0f172a; margin: 0 0 12px; }

    .detail-content { font-size: 13px; color: #475569; line-height: 1.6; margin-bottom: 16px; }
    .detail-content.rich :is(p, h1, h2, ul, ol) { margin: 0 0 6px; }
    .detail-content.rich strong { font-weight: 700; }
    .detail-content.rich em { font-style: italic; }

    .detail-connections { margin-top: 12px; }
    .detail-connections strong { font-size: 12px; color: #64748b; display: block; margin-bottom: 8px; }

    .edge-item {
      border-left: 3px solid; padding: 6px 10px; margin-bottom: 6px;
      background: #f8fafc; border-radius: 0 4px 4px 0;
    }
    .edge-item.has-warning { background: #fffbeb; border-left-color: #f59e0b !important; }
    .edge-header { display: flex; align-items: center; gap: 6px; }
    .edge-type { font-size: 10px; font-weight: 700; text-transform: uppercase; }
    .edge-target { font-size: 12px; color: #0f172a; display: block; }
    .edge-desc { font-size: 11px; color: #64748b; margin-top: 3px; font-style: italic; }
    .warning-badge { font-size: 12px; color: #f59e0b; }

    .edge-warning {
      margin-top: 6px; padding: 6px 8px; background: #fef3c7;
      border-radius: 4px; border-left: 2px solid #f59e0b;
    }
    .warning-msg { font-size: 11px; color: #78350f; line-height: 1.4; }
    .warning-fix { font-size: 10px; color: #92400e; margin-top: 3px; font-style: italic; }

    .loading-overlay, .error-overlay {
      position: absolute; inset: 0; display: flex; align-items: center; justify-content: center;
      font-size: 15px; font-weight: 600;
    }
    .loading-overlay { background: rgba(248,250,252,0.8); color: #6366f1; }
    .error-overlay { background: rgba(254,242,242,0.9); color: #ef4444; }

    .path-btn {
      padding: 6px 14px; background: #f0fdf4; color: #15803d;
      border: 1.5px solid #86efac; border-radius: 6px; font-size: 12px;
      font-weight: 600; cursor: pointer; transition: background 0.15s;
    }
    .path-btn:hover { background: #dcfce7; }
    .path-btn.active { background: #dcfce7; border-color: #22c55e; color: #166534; }

    .path-panel {
      width: 280px; background: #f0fdf4; border-left: 1px solid #86efac;
      padding: 0; overflow-y: auto; position: relative; flex-shrink: 0;
      display: flex; flex-direction: column;
    }
    .path-panel-header {
      display: flex; justify-content: space-between; align-items: center;
      padding: 12px 16px; background: #dcfce7; border-bottom: 1px solid #86efac;
      font-size: 13px; font-weight: 700; color: #14532d; position: sticky; top: 0;
    }
    .path-instructions { padding: 12px 16px; border-bottom: 1px solid #bbf7d0; }
    .path-hint { font-size: 12px; color: #166534; }
    .path-step { display: flex; align-items: baseline; gap: 6px; margin-bottom: 6px; }
    .path-step-label { font-size: 10px; font-weight: 700; text-transform: uppercase; padding: 2px 6px; border-radius: 10px; }
    .from-label { background: #c7d2fe; color: #3730a3; }
    .to-label { background: #a7f3d0; color: #065f46; }
    .path-step-title { font-size: 12px; font-weight: 600; color: #0f172a; }

    .path-options { padding: 8px 16px; border-bottom: 1px solid #bbf7d0; }
    .path-option-label { font-size: 12px; color: #166534; display: flex; align-items: center; gap: 6px; cursor: pointer; }

    .path-loading { padding: 12px 16px; font-size: 12px; color: #166534; font-style: italic; }
    .path-error { padding: 12px 16px; font-size: 12px; color: #dc2626; background: #fee2e2; }
    .path-not-found { padding: 12px 16px; font-size: 12px; color: #92400e; background: #fef3c7; }

    .path-found { padding: 12px 16px; }
    .path-summary { font-size: 12px; color: #166534; margin-bottom: 10px; }
    .path-steps-list { list-style: none; padding: 0; margin: 0; }
    .path-list-item {
      position: relative; padding: 8px 10px; margin-bottom: 4px;
      background: white; border-radius: 6px; border: 1px solid #bbf7d0;
    }
    .path-list-item.first { border-left: 3px solid #6366f1; }
    .path-list-item.last { border-left: 3px solid #10b981; }
    .path-item-date { display: block; font-size: 10px; color: #64748b; margin-bottom: 2px; }
    .path-item-title { display: block; font-size: 12px; font-weight: 600; color: #0f172a; }
    .path-item-edge { display: block; font-size: 10px; font-weight: 700; margin-top: 4px; }

    .reset-path-btn {
      margin: 12px 16px; padding: 6px 14px; background: white;
      border: 1px solid #86efac; border-radius: 6px; font-size: 12px;
      color: #166534; cursor: pointer; font-weight: 600;
    }
    .reset-path-btn:hover { background: #dcfce7; }

    .legend {
      display: flex; gap: 16px; flex-wrap: wrap; padding: 8px 20px;
      background: white; border-top: 1px solid #e2e8f0; flex-shrink: 0;
    }
    .legend-item { display: flex; align-items: center; gap: 6px; font-size: 12px; color: #475569; }
    .legend-dot { width: 10px; height: 10px; border-radius: 50%; flex-shrink: 0; }
    .legend-line { display: inline-block; width: 24px; height: 2px; border-radius: 1px; }
    .inferred-line { border-top: 2px dashed #64748b; background: transparent; }
    .warning-line { background: #f59e0b; }
  `]
})
export class GraphViewComponent implements OnInit, AfterViewInit, OnDestroy {

  @ViewChild('cytoscapeContainer') containerRef!: ElementRef<HTMLDivElement>;

  private readonly api = inject(TimelineApiService);
  private readonly route = inject(ActivatedRoute);

  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly selectedNode = signal<GraphNode | null>(null);
  readonly showWarnings = signal(false);
  readonly activeTypes = signal<Set<ConnectionType>>(new Set([
    'CAUSAL', 'TEMPORAL', 'REFERENCE', 'CONTRAST',
    'PREREQUISITE', 'FORESHADOW', 'REVEAL', 'ESCALATION', 'RESOLUTION', 'PARALLEL'
  ] as ConnectionType[]));

  // ── Story Path finding ────────────────────────────────────────────────────
  readonly pathMode = signal(false);
  readonly pathFrom = signal<GraphNode | null>(null);
  readonly pathTo = signal<GraphNode | null>(null);
  readonly pathResult = signal<StoryPath | null>(null);
  readonly pathLoading = signal(false);
  readonly pathError = signal<string | null>(null);
  readonly pathExplicitOnly = signal(false);

  private graph: EventGraphResponse = { nodes: [], edges: [], validations: [] };
  private cy: any = null;
  private timelineColorMap = new Map<string, string>();
  private validationMap = new Map<string, NarrativeValidation[]>();

  readonly connectionTypes: ConnectionType[] = [
    'CAUSAL', 'TEMPORAL', 'REFERENCE', 'CONTRAST',
    'PREREQUISITE', 'FORESHADOW', 'REVEAL', 'ESCALATION', 'RESOLUTION', 'PARALLEL'
  ];

  readonly timelineLegend = signal<{ id: string; name: string; color: string }[]>([]);

  get warningCount(): number {
    return this.graph.validations?.length ?? 0;
  }

  get allValidations(): NarrativeValidation[] {
    return this.graph.validations ?? [];
  }

  readonly edgesForSelected = computed(() => {
    const node = this.selectedNode();
    if (!node) return [];
    return this.graph.edges.filter(
      e => !e.inferred && e.sourceEventId === node.id && this.activeTypes().has(e.connectionType!)
    );
  });

  ngOnInit(): void {
    const ids = this.route.snapshot.queryParamMap.getAll('timelineIds');
    this.api.getEventGraph(ids.length ? ids : undefined).subscribe({
      next: g => {
        this.graph = g;
        this.buildColorMap();
        this.buildValidationMap();
        this.loading.set(false);
        this.renderGraph();
        if (this.warningCount > 0) {
          this.showWarnings.set(true);
        }
      },
      error: () => {
        this.error.set('Failed to load graph data.');
        this.loading.set(false);
      }
    });
  }

  ngAfterViewInit(): void {}

  ngOnDestroy(): void {
    this.cy?.destroy();
  }

  private buildColorMap(): void {
    const timelineIds = [...new Set(this.graph.nodes.map(n => n.timelineId))];
    timelineIds.forEach((id, i) => {
      this.timelineColorMap.set(id, TIMELINE_COLORS[i % TIMELINE_COLORS.length]);
    });
    const legend = timelineIds.map(id => ({
      id,
      name: this.graph.nodes.find(n => n.timelineId === id)?.timelineName ?? id,
      color: this.timelineColorMap.get(id)!
    }));
    this.timelineLegend.set(legend);
  }

  private buildValidationMap(): void {
    this.validationMap.clear();
    for (const v of this.graph.validations ?? []) {
      const list = this.validationMap.get(v.connectionId) ?? [];
      list.push(v);
      this.validationMap.set(v.connectionId, list);
    }
  }

  private async renderGraph(): Promise<void> {
    if (!this.containerRef) return;

    const [cytoscapeModule, dagreModule, cytoscapeDagreModule] = await Promise.all([
      import('cytoscape'),
      import('dagre'),
      import('cytoscape-dagre')
    ]);

    const cytoscape = cytoscapeModule.default;
    const dagre = dagreModule.default;
    const cytoscapeDagre = cytoscapeDagreModule.default;

    cytoscape.use(cytoscapeDagre as any);

    const active = this.activeTypes();
    // Explicit connections filtered by active types
    const explicitEdges = this.graph.edges.filter(
      e => !e.inferred && e.connectionType != null && active.has(e.connectionType)
    );

    const elements = [
      ...this.graph.nodes.map(n => ({
        data: {
          id: n.id,
          label: n.title,
          temporalLabel: n.temporalLabel,
          timelineName: n.timelineName,
          color: this.timelineColorMap.get(n.timelineId) ?? '#6366f1'
        }
      })),
      // Inferred temporal backbone — computed from nodes, always visible
      ...this.buildInferredElements(),
      ...explicitEdges.map(e => {
        const hasWarning = this.validationMap.has(e.id);
        const color = hasWarning
          ? WARNING_COLOR
          : (EDGE_COLORS[e.connectionType!] ?? '#94a3b8');
        return {
          data: {
            id: e.id,
            source: e.sourceEventId,
            target: e.targetEventId,
            label: EDGE_LABELS[e.connectionType!] ?? e.connectionType ?? '',
            color,
            inferred: false,
            hasWarning
          }
        };
      })
    ];

    this.cy?.destroy();

    this.cy = cytoscape({
      container: this.containerRef.nativeElement,
      elements,
      style: [
        {
          selector: 'node',
          style: {
            'background-color': 'data(color)',
            'label': 'data(label)',
            'color': '#fff',
            'text-valign': 'center',
            'text-halign': 'center',
            'font-size': '11px',
            'font-weight': 'bold',
            'width': 120,
            'height': 50,
            'shape': 'round-rectangle',
            'text-wrap': 'wrap',
            'text-max-width': '110px',
            'border-width': 0
          }
        },
        {
          selector: 'node:selected',
          style: {
            'border-width': 3,
            'border-color': '#0f172a'
          }
        },
        {
          selector: 'edge',
          style: {
            'width': 2,
            'line-color': 'data(color)',
            'target-arrow-color': 'data(color)',
            'target-arrow-shape': 'triangle',
            'curve-style': 'bezier',
            'label': 'data(label)',
            'font-size': '9px',
            'color': 'data(color)',
            'text-background-color': '#fff',
            'text-background-opacity': 1,
            'text-background-padding': '2px'
          }
        },
        {
          selector: 'edge[?inferred]',
          style: {
            'line-style': 'dashed' as any,
            'line-dash-pattern': [8, 4] as any,
            'width': 2.5,
            'opacity': 0.85,
            'target-arrow-shape': 'triangle' as any,
            'font-size': 0
          }
        },
        {
          selector: 'edge[?hasWarning]',
          style: {
            'width': 2.5,
            'line-style': 'solid' as any
          }
        },
        {
          selector: 'node.path-from',
          style: {
            'border-width': 4,
            'border-color': '#6366f1',
            'border-style': 'solid' as any
          }
        },
        {
          selector: 'node.path-to',
          style: {
            'border-width': 4,
            'border-color': '#10b981',
            'border-style': 'solid' as any
          }
        },
        {
          selector: 'node.path-node',
          style: {
            'border-width': 3,
            'border-color': '#f59e0b',
            'border-style': 'solid' as any,
            'opacity': 1
          }
        },
        {
          selector: 'edge.path-edge',
          style: {
            'width': 4,
            'line-color': '#f59e0b',
            'target-arrow-color': '#f59e0b',
            'line-style': 'solid' as any,
            'z-index': 10
          }
        }
      ],
      layout: {
        name: 'dagre',
        rankDir: 'LR',
        nodeSep: 60,
        rankSep: 120,
        padding: 40,
        animate: false
      } as any
    });

    this.cy.on('tap', 'node', (evt: any) => {
      const nodeId = evt.target.id();
      const node = this.graph.nodes.find(n => n.id === nodeId) ?? null;
      if (this.pathMode()) {
        if (node) this.handlePathNodeClick(node);
      } else {
        this.selectedNode.set(node);
      }
    });

    this.cy.on('tap', (evt: any) => {
      if (evt.target === this.cy) {
        this.selectedNode.set(null);
      }
    });
  }

  /**
   * Computes inferred temporal edges from nodes — groups by timelineId,
   * sorts by temporalPosition, and connects consecutive pairs.
   * Runs entirely client-side so it works without a backend rebuild.
   */
  private buildInferredElements(): any[] {
    const byTimeline = new Map<string, GraphNode[]>();
    for (const node of this.graph.nodes) {
      const list = byTimeline.get(node.timelineId) ?? [];
      list.push(node);
      byTimeline.set(node.timelineId, list);
    }

    const elements: any[] = [];
    for (const [, nodes] of byTimeline) {
      const sorted = [...nodes].sort((a, b) => a.temporalPosition - b.temporalPosition);
      for (let i = 0; i < sorted.length - 1; i++) {
        const src = sorted[i];
        const tgt = sorted[i + 1];
        elements.push({
          data: {
            id: `inferred:${src.id}:${tgt.id}`,
            source: src.id,
            target: tgt.id,
            label: '',
            color: INFERRED_COLOR,
            inferred: true,
            hasWarning: false
          }
        });
      }
    }
    return elements;
  }

  // ── Story Path methods ────────────────────────────────────────────────────

  togglePathMode(): void {
    this.pathMode.update(v => {
      if (v) {
        // Exiting path mode — clear state
        this.pathFrom.set(null);
        this.pathTo.set(null);
        this.pathResult.set(null);
        this.pathError.set(null);
        this.clearPathHighlight();
      }
      return !v;
    });
  }

  /** Called when a node is tapped while in path-finding mode. */
  private handlePathNodeClick(node: GraphNode): void {
    if (!this.pathFrom()) {
      this.pathFrom.set(node);
      this.highlightNode(node.id, 'path-from');
    } else if (!this.pathTo() && node.id !== this.pathFrom()!.id) {
      this.pathTo.set(node);
      this.highlightNode(node.id, 'path-to');
      this.executeFindPath();
    }
  }

  private executeFindPath(): void {
    const from = this.pathFrom();
    const to = this.pathTo();
    if (!from || !to) return;

    this.pathLoading.set(true);
    this.pathResult.set(null);
    this.pathError.set(null);

    this.api.findStoryPath(from.id, to.id, this.pathExplicitOnly()).subscribe({
      next: path => {
        this.pathResult.set(path);
        this.pathLoading.set(false);
        if (path.found) {
          this.highlightPath(path);
        }
      },
      error: () => {
        this.pathError.set('Failed to find path.');
        this.pathLoading.set(false);
      }
    });
  }

  toggleExplicitOnly(): void {
    this.pathExplicitOnly.update(v => !v);
    if (this.pathFrom() && this.pathTo()) {
      this.executeFindPath();
    }
  }

  resetPath(): void {
    this.pathFrom.set(null);
    this.pathTo.set(null);
    this.pathResult.set(null);
    this.pathError.set(null);
    this.clearPathHighlight();
  }

  pathNodeTitle(node: StoryPathNode): string {
    return node.title;
  }

  private highlightNode(nodeId: string, cls: string): void {
    if (!this.cy) return;
    this.cy.$(`#${nodeId}`).addClass(cls);
  }

  private highlightPath(path: StoryPath): void {
    if (!this.cy) return;
    this.clearPathHighlight();
    path.nodes.forEach(n => this.cy.$(`#${n.id}`).addClass('path-node'));
    path.edges.forEach(e => {
      if (!e.inferred) this.cy.$(`#${e.id}`).addClass('path-edge');
    });
  }

  private clearPathHighlight(): void {
    if (!this.cy) return;
    this.cy.$('.path-from, .path-to, .path-node, .path-edge').removeClass('path-from path-to path-node path-edge');
  }

  toggleWarnings(): void {
    this.showWarnings.update(v => !v);
  }

  toggleType(type: ConnectionType): void {
    this.activeTypes.update(set => {
      const next = new Set(set);
      if (next.has(type)) next.delete(type);
      else next.add(type);
      return next;
    });
    this.renderGraph();
  }

  fitGraph(): void {
    this.cy?.fit(undefined, 40);
  }

  edgeColor(type: ConnectionType | null): string {
    return type ? (EDGE_COLORS[type] ?? '#94a3b8') : INFERRED_COLOR;
  }

  edgeLabel(type: ConnectionType | null): string {
    return type ? (EDGE_LABELS[type] ?? type) : '';
  }

  nodeColor(timelineId: string): string {
    return this.timelineColorMap.get(timelineId) ?? '#6366f1';
  }

  nodeTitle(eventId: string): string {
    return this.graph.nodes.find(n => n.id === eventId)?.title ?? eventId;
  }

  edgeWarnings(edgeId: string): NarrativeValidation[] {
    return this.validationMap.get(edgeId) ?? [];
  }

  edgeById(connectionId: string): GraphEdge | undefined {
    return this.graph.edges.find(e => e.id === connectionId);
  }
}
