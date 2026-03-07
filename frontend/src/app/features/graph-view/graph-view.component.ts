import {
  Component, OnInit, OnDestroy, ElementRef, ViewChild, AfterViewInit,
  signal, computed, ChangeDetectionStrategy, inject
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { TimelineApiService } from '../../infrastructure/api/timeline-api.service';
import { ConnectionType, EventGraphResponse, GraphEdge, GraphNode } from '../../domain/timeline.model';

const TIMELINE_COLORS = ['#6366f1', '#10b981', '#f59e0b', '#ef4444', '#8b5cf6', '#06b6d4', '#f97316', '#84cc16'];

const EDGE_COLORS: Record<ConnectionType, string> = {
  // Original
  CAUSAL:       '#6366f1',
  TEMPORAL:     '#3b82f6',
  REFERENCE:    '#10b981',
  CONTRAST:     '#f59e0b',
  // Narrative enrichment
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
                <div class="edge-item" [style.border-left-color]="edgeColor(edge.connectionType)">
                  <span class="edge-type" [style.color]="edgeColor(edge.connectionType)">{{ edgeLabel(edge.connectionType) }}</span>
                  <span class="edge-target">→ {{ nodeTitle(edge.targetEventId) }}</span>
                  @if (edge.description) {
                    <div class="edge-desc">{{ edge.description }}</div>
                  }
                </div>
              }
            </div>
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

    .graph-body { flex: 1; position: relative; overflow: hidden; display: flex; }

    .cy-container { flex: 1; height: 100%; }

    .detail-panel {
      width: 300px; background: white; border-left: 1px solid #e2e8f0;
      padding: 20px; overflow-y: auto; position: relative; flex-shrink: 0;
    }

    .close-btn {
      position: absolute; top: 12px; right: 12px; background: none; border: none;
      font-size: 16px; cursor: pointer; color: #94a3b8; padding: 2px 6px;
    }
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
    .edge-type { font-size: 10px; font-weight: 700; text-transform: uppercase; display: block; }
    .edge-target { font-size: 12px; color: #0f172a; }
    .edge-desc { font-size: 11px; color: #64748b; margin-top: 3px; font-style: italic; }

    .loading-overlay, .error-overlay {
      position: absolute; inset: 0; display: flex; align-items: center; justify-content: center;
      font-size: 15px; font-weight: 600;
    }
    .loading-overlay { background: rgba(248,250,252,0.8); color: #6366f1; }
    .error-overlay { background: rgba(254,242,242,0.9); color: #ef4444; }

    .legend {
      display: flex; gap: 16px; flex-wrap: wrap; padding: 8px 20px;
      background: white; border-top: 1px solid #e2e8f0; flex-shrink: 0;
    }
    .legend-item { display: flex; align-items: center; gap: 6px; font-size: 12px; color: #475569; }
    .legend-dot { width: 10px; height: 10px; border-radius: 50%; flex-shrink: 0; }
  `]
})
export class GraphViewComponent implements OnInit, AfterViewInit, OnDestroy {

  @ViewChild('cytoscapeContainer') containerRef!: ElementRef<HTMLDivElement>;

  private readonly api = inject(TimelineApiService);
  private readonly route = inject(ActivatedRoute);

  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly selectedNode = signal<GraphNode | null>(null);
  readonly activeTypes = signal<Set<ConnectionType>>(new Set([
    'CAUSAL', 'TEMPORAL', 'REFERENCE', 'CONTRAST',
    'PREREQUISITE', 'FORESHADOW', 'REVEAL', 'ESCALATION', 'RESOLUTION', 'PARALLEL'
  ] as ConnectionType[]));

  private graph: EventGraphResponse = { nodes: [], edges: [] };
  private cy: any = null;
  private timelineColorMap = new Map<string, string>();

  readonly connectionTypes: ConnectionType[] = [
    'CAUSAL', 'TEMPORAL', 'REFERENCE', 'CONTRAST',
    'PREREQUISITE', 'FORESHADOW', 'REVEAL', 'ESCALATION', 'RESOLUTION', 'PARALLEL'
  ];

  readonly timelineLegend = signal<{ id: string; name: string; color: string }[]>([]);

  readonly edgesForSelected = computed(() => {
    const node = this.selectedNode();
    if (!node) return [];
    return this.graph.edges.filter(
      e => e.sourceEventId === node.id && this.activeTypes().has(e.connectionType)
    );
  });

  ngOnInit(): void {
    const ids = this.route.snapshot.queryParamMap.getAll('timelineIds');
    this.api.getEventGraph(ids.length ? ids : undefined).subscribe({
      next: g => {
        this.graph = g;
        this.buildColorMap();
        this.loading.set(false);
        this.renderGraph();
      },
      error: () => {
        this.error.set('Failed to load graph data.');
        this.loading.set(false);
      }
    });
  }

  ngAfterViewInit(): void {
    // cy is initialized after data loads in renderGraph()
  }

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
    const visibleEdges = this.graph.edges.filter(e => active.has(e.connectionType));

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
      ...visibleEdges.map(e => ({
        data: {
          id: e.id,
          source: e.sourceEventId,
          target: e.targetEventId,
          label: EDGE_LABELS[e.connectionType] ?? e.connectionType,
          color: EDGE_COLORS[e.connectionType]
        }
      }))
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
      this.selectedNode.set(node);
    });

    this.cy.on('tap', (evt: any) => {
      if (evt.target === this.cy) {
        this.selectedNode.set(null);
      }
    });
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

  edgeColor(type: ConnectionType): string {
    return EDGE_COLORS[type] ?? '#94a3b8';
  }

  edgeLabel(type: ConnectionType): string {
    return EDGE_LABELS[type] ?? type;
  }

  nodeColor(timelineId: string): string {
    return this.timelineColorMap.get(timelineId) ?? '#6366f1';
  }

  nodeTitle(eventId: string): string {
    return this.graph.nodes.find(n => n.id === eventId)?.title ?? eventId;
  }
}
