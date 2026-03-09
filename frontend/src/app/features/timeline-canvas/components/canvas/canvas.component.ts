import {
  Component, inject, ChangeDetectionStrategy,
  AfterViewInit, OnDestroy, ViewChild, ElementRef,
  NgZone, ChangeDetectorRef, effect, computed, signal
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { CanvasStateService, EventPosition } from '../../services/canvas-state.service';
import { TimelineTrackComponent } from '../timeline-track/timeline-track.component';
import { CharacterTrackComponent } from '../character-track/character-track.component';
import { TimelineApiService } from '../../../../infrastructure/api/timeline-api.service';
import { TimelineConnection, TimelineEvent } from '../../../../domain/timeline.model';

const CONNECTION_COLORS: Record<string, string> = {
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

const CONNECTION_LABELS: Record<string, string> = {
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
  selector: 'app-canvas',
  standalone: true,
  imports: [CommonModule, FormsModule, TimelineTrackComponent, CharacterTrackComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="canvas-wrapper">
      <div class="canvas-toolbar">
        <button class="toolbar-btn" (click)="canvasState.zoomIn()" title="Zoom In">+</button>
        <span class="zoom-label">{{ (canvasState.zoom() * 100).toFixed(0) }}%</span>
        <button class="toolbar-btn" (click)="canvasState.zoomOut()" title="Zoom Out">−</button>
        <button class="toolbar-btn reset-btn" (click)="canvasState.resetZoom()" title="Reset View">⌂</button>
        <div class="toolbar-divider"></div>
        <button class="toolbar-btn connect-btn"
                [class.active]="canvasState.connectingMode()"
                (click)="canvasState.toggleConnectingMode()"
                title="Connect events">
          ⟷ Connect
        </button>
        @if (canvasState.connectingMode()) {
          <span class="connect-hint">
            @if (!canvasState.pendingSource()) {
              Click source event
            } @else {
              Click target event
            }
          </span>
        }
        <div class="toolbar-divider"></div>
        <button class="toolbar-btn export-btn"
                [disabled]="isExporting()"
                (click)="exportAsImage()"
                title="Export as PNG">
          {{ isExporting() ? '...' : '↓ Export' }}
        </button>
        <button class="toolbar-btn graph-btn"
                (click)="openGraphMode()"
                title="Open Graph Mode">
          ⬡ Graph
        </button>
      </div>

      @if (canvasState.pendingConnection()) {
        <div class="connection-form-bar">
          <span class="conn-form-label">
            "{{ canvasState.pendingConnection()!.source.title }}"
            → "{{ canvasState.pendingConnection()!.target.title }}"
          </span>
          <select class="conn-type-select" [(ngModel)]="newConnType">
            <optgroup label="Original">
              <option value="CAUSAL">Causal</option>
              <option value="TEMPORAL">Temporal</option>
              <option value="REFERENCE">Reference</option>
              <option value="CONTRAST">Contrast</option>
            </optgroup>
            <optgroup label="Narrative">
              <option value="PREREQUISITE">Prerequisite</option>
              <option value="FORESHADOW">Foreshadow</option>
              <option value="REVEAL">Reveal</option>
              <option value="ESCALATION">Escalation</option>
              <option value="RESOLUTION">Resolution</option>
              <option value="PARALLEL">Parallel</option>
            </optgroup>
          </select>
          <input class="conn-desc-input" [(ngModel)]="newConnDesc" placeholder="Description (optional)" />
          <button class="btn-confirm" (click)="confirmConnection()">Connect</button>
          <button class="btn-cancel" (click)="canvasState.cancelPendingConnection()">Cancel</button>
        </div>
      }

      <div class="canvas-container"
           [class.panning]="canvasState.isPanning() && !canvasState.connectingMode()"
           [class.connecting]="canvasState.connectingMode()"
           (wheel)="onWheel($event)"
           (mousedown)="onMouseDown($event)"
           (mousemove)="onMouseMove($event)"
           (mouseup)="onMouseUp($event)"
           (mouseleave)="onMouseUp($event)">

        <div #canvasContent
             class="canvas-content"
             [style.transform]="canvasState.canvasTransform()">

          <svg class="connections-svg">
            <defs>
              @for (type of connectionTypes; track type) {
                <marker [attr.id]="'arrow-' + type"
                        markerWidth="8" markerHeight="8"
                        refX="6" refY="3" orient="auto">
                  <path d="M0,0 L0,6 L8,3 z"
                        [attr.fill]="getColorByType(type)" />
                </marker>
              }
            </defs>
            @for (cp of connectionPaths(); track cp.conn.id) {
              <g>
                <path [attr.d]="cp.path"
                      class="conn-hit"
                      (click)="onConnectionClick(cp.conn, $event)" />
                <path [attr.d]="cp.path"
                      class="conn-line"
                      [attr.stroke]="cp.color"
                      [attr.marker-end]="cp.markerUrl" />
                <text [attr.x]="cp.midX"
                      [attr.y]="cp.midY - 6"
                      class="conn-label">{{ connLabel(cp.conn.connectionType) }}</text>
              </g>
            }
          </svg>

          @if (canvasState.visibleTimelines().length === 0) {
            <div class="empty-state">
              <div class="empty-icon">📅</div>
              <p class="empty-title">No timelines yet</p>
              <p class="empty-subtitle">Create a timeline to get started</p>
            </div>
          } @else {
            <div class="timelines-container">
              @for (timeline of canvasState.visibleTimelines(); track timeline.id) {
                <div class="timeline-row">
                  <app-timeline-track
                    [timeline]="timeline"
                    [selectedEventId]="canvasState.selectedEvent()?.id ?? null"
                    [connectingMode]="canvasState.connectingMode()"
                    [pendingSourceId]="canvasState.pendingSource()?.id ?? null"
                    (eventSelected)="onEventSelected($event)"
                  />
                </div>
              }
              @for (character of canvasState.characters(); track character.id) {
                <div class="timeline-row">
                  <app-character-track [character]="character" />
                </div>
              }
            </div>
          }
        </div>
      </div>
    </div>
  `,
  styles: [`
    :host {
      display: flex;
      flex: 1;
      flex-direction: column;
      min-height: 0;
    }

    .canvas-wrapper {
      display: flex;
      flex-direction: column;
      flex: 1;
      min-height: 0;
      background: #f8fafc;
      border-radius: 12px;
      overflow: hidden;
      border: 1px solid #e2e8f0;
    }

    .canvas-toolbar {
      display: flex;
      align-items: center;
      gap: 4px;
      padding: 8px 12px;
      background: white;
      border-bottom: 1px solid #e2e8f0;
      flex-shrink: 0;
    }

    .toolbar-btn {
      height: 28px;
      padding: 0 8px;
      border: 1px solid #e2e8f0;
      border-radius: 6px;
      background: white;
      cursor: pointer;
      font-size: 13px;
      display: flex;
      align-items: center;
      gap: 4px;
      transition: background 0.15s;
      white-space: nowrap;
    }

    .toolbar-btn:hover { background: #f1f5f9; }
    .toolbar-btn:disabled { opacity: 0.5; cursor: default; }

    .toolbar-btn.active {
      background: #ede9fe;
      border-color: #6366f1;
      color: #6366f1;
      font-weight: 600;
    }

    .export-btn { color: #0f172a; }
    .graph-btn { color: #6366f1; font-weight: 700; }

    .toolbar-divider {
      width: 1px;
      height: 20px;
      background: #e2e8f0;
      margin: 0 4px;
    }

    .zoom-label {
      font-size: 12px;
      color: #64748b;
      min-width: 40px;
      text-align: center;
    }

    .connect-hint {
      font-size: 12px;
      color: #6366f1;
      font-weight: 500;
      margin-left: 4px;
    }

    .connection-form-bar {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 8px 12px;
      background: #ede9fe;
      border-bottom: 1px solid #c4b5fd;
      flex-shrink: 0;
      flex-wrap: wrap;
    }

    .conn-form-label {
      font-size: 12px;
      font-weight: 600;
      color: #4c1d95;
      flex-shrink: 0;
    }

    .conn-type-select, .conn-desc-input {
      height: 28px;
      padding: 0 8px;
      border: 1px solid #c4b5fd;
      border-radius: 6px;
      font-size: 12px;
      background: white;
    }

    .conn-desc-input { flex: 1; min-width: 150px; }

    .btn-confirm {
      height: 28px;
      padding: 0 12px;
      background: #6366f1;
      color: white;
      border: none;
      border-radius: 6px;
      font-size: 12px;
      font-weight: 600;
      cursor: pointer;
    }

    .btn-cancel {
      height: 28px;
      padding: 0 12px;
      background: white;
      color: #64748b;
      border: 1px solid #e2e8f0;
      border-radius: 6px;
      font-size: 12px;
      cursor: pointer;
    }

    .canvas-container {
      flex: 1;
      overflow: hidden;
      cursor: grab;
      position: relative;
    }

    .canvas-container.panning { cursor: grabbing; }
    .canvas-container.connecting { cursor: crosshair; }

    .canvas-content {
      position: absolute;
      top: 0;
      left: 0;
      transform-origin: 0 0;
      min-width: 100%;
      min-height: 100%;
      padding: 40px;
    }

    .connections-svg {
      position: absolute;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      overflow: visible;
      pointer-events: none;
    }

    .conn-hit {
      fill: none;
      stroke: transparent;
      stroke-width: 16px;
      pointer-events: stroke;
      cursor: pointer;
    }

    .conn-line {
      fill: none;
      stroke-width: 2px;
      stroke-dasharray: none;
    }

    .conn-label {
      font-size: 10px;
      font-weight: 600;
      fill: #475569;
      text-anchor: middle;
      pointer-events: none;
      background: white;
    }

    .empty-state {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      height: 300px;
      gap: 8px;
    }

    .empty-icon { font-size: 48px; }

    .empty-title {
      font-size: 18px;
      font-weight: 600;
      color: #475569;
      margin: 0;
    }

    .empty-subtitle {
      font-size: 14px;
      color: #94a3b8;
      margin: 0;
    }

    .timelines-container {
      display: flex;
      flex-direction: row;
      align-items: flex-start;
      gap: 60px;
    }

    .timeline-row { display: flex; }
  `]
})
export class CanvasComponent implements AfterViewInit, OnDestroy {

  protected readonly canvasState = inject(CanvasStateService);
  private readonly api = inject(TimelineApiService);
  private readonly router = inject(Router);
  private readonly ngZone = inject(NgZone);
  private readonly cdr = inject(ChangeDetectorRef);

  @ViewChild('canvasContent') canvasContentRef!: ElementRef<HTMLDivElement>;

  private panStart: { x: number; y: number } | null = null;
  private rafId?: number;

  readonly connectionTypes = [
    'CAUSAL', 'TEMPORAL', 'REFERENCE', 'CONTRAST',
    'PREREQUISITE', 'FORESHADOW', 'REVEAL', 'ESCALATION', 'RESOLUTION', 'PARALLEL'
  ];
  newConnType = 'REFERENCE';
  newConnDesc = '';
  readonly isExporting = signal(false);

  readonly connectionPaths = computed(() => {
    const positions = this.canvasState.eventPositions();
    return this.canvasState.connections()
      .map(conn => {
        const src = positions.get(conn.sourceEventId);
        const tgt = positions.get(conn.targetEventId);
        if (!src || !tgt) return null;
        // Connect from the edge facing the other event (handles both directions)
        const srcOnLeft = src.x <= tgt.x;
        const x1 = srcOnLeft ? src.x + src.w : src.x;
        const y1 = src.y + src.h / 2;
        const x2 = srcOnLeft ? tgt.x : tgt.x + tgt.w;
        const y2 = tgt.y + tgt.h / 2;
        const dx = Math.abs(x2 - x1) * 0.5 + 40;
        const cp1x = srcOnLeft ? x1 + dx : x1 - dx;
        const cp2x = srcOnLeft ? x2 - dx : x2 + dx;
        const path = `M ${x1} ${y1} C ${cp1x} ${y1} ${cp2x} ${y2} ${x2} ${y2}`;
        return {
          conn,
          path,
          midX: (x1 + x2) / 2,
          midY: (y1 + y2) / 2,
          color: CONNECTION_COLORS[conn.connectionType] ?? '#94a3b8',
          markerUrl: `url(#arrow-${conn.connectionType})`,
        };
      })
      .filter((cp): cp is NonNullable<typeof cp> => cp !== null);
  });

  constructor() {
    effect(() => {
      this.canvasState.visibleTimelines();
      this.canvasState.connections();
      this.canvasState.zoom();
      this.canvasState.offset();
      this.scheduleMeasure();
    });
  }

  ngAfterViewInit(): void {
    this.scheduleMeasure();
  }

  ngOnDestroy(): void {
    if (this.rafId) cancelAnimationFrame(this.rafId);
  }

  private scheduleMeasure(): void {
    if (this.rafId) cancelAnimationFrame(this.rafId);
    this.ngZone.runOutsideAngular(() => {
      this.rafId = requestAnimationFrame(() => {
        this.rafId = undefined;
        this.ngZone.run(() => {
          this.measureEventPositions();
          this.cdr.markForCheck();
        });
      });
    });
  }

  private measureEventPositions(): void {
    const container = this.canvasContentRef?.nativeElement;
    if (!container) return;
    const containerRect = container.getBoundingClientRect();
    const zoom = this.canvasState.zoom();
    const positions = new Map<string, EventPosition>();
    container.querySelectorAll<HTMLElement>('[data-event-id]').forEach(el => {
      const rect = el.getBoundingClientRect();
      positions.set(el.dataset['eventId']!, {
        x: (rect.left - containerRect.left) / zoom,
        y: (rect.top - containerRect.top) / zoom,
        w: rect.width / zoom,
        h: rect.height / zoom,
      });
    });
    this.canvasState.setEventPositions(positions);
  }

  getColorByType(type: string): string {
    return CONNECTION_COLORS[type] ?? '#94a3b8';
  }

  connLabel(type: string): string {
    return CONNECTION_LABELS[type] ?? type;
  }

  onConnectionClick(conn: TimelineConnection, event: MouseEvent): void {
    event.stopPropagation();
    if (confirm(`Remove connection "${conn.connectionType}"?`)) {
      this.api.deleteConnection(conn.id).subscribe(() => {
        this.canvasState.removeConnection(conn.id);
      });
    }
  }

  onWheel(event: WheelEvent): void {
    event.preventDefault();
    const delta = event.deltaY > 0 ? -0.1 : 0.1;
    this.canvasState.setZoom(this.canvasState.zoom() + delta);
  }

  onMouseDown(event: MouseEvent): void {
    if (event.button === 0 && !this.canvasState.connectingMode()) {
      this.panStart = { x: event.clientX, y: event.clientY };
      this.canvasState.setIsPanning(true);
    }
  }

  onMouseMove(event: MouseEvent): void {
    if (this.panStart && this.canvasState.isPanning()) {
      const delta = {
        x: event.clientX - this.panStart.x,
        y: event.clientY - this.panStart.y
      };
      this.canvasState.pan(delta);
      this.panStart = { x: event.clientX, y: event.clientY };
    }
  }

  onMouseUp(event: MouseEvent): void {
    this.panStart = null;
    this.canvasState.setIsPanning(false);
  }

  onEventSelected(event: TimelineEvent): void {
    if (this.canvasState.connectingMode()) {
      this.canvasState.handleEventClickInConnectMode(event);
    } else {
      this.canvasState.selectEvent(event);
    }
  }

  async exportAsImage(): Promise<void> {
    const el = this.canvasContentRef.nativeElement;
    this.isExporting.set(true);
    this.cdr.markForCheck();

    // Save current transform and reset so the full content is captured
    const savedTransform = el.style.transform;
    el.style.transform = 'translate(0px, 0px) scale(1)';

    // Wait for the browser to apply the layout change
    await new Promise<void>(resolve => requestAnimationFrame(() => { requestAnimationFrame(() => resolve()); }));

    try {
      const { default: html2canvas } = await import('html2canvas');
      const canvas = await html2canvas(el, {
        backgroundColor: '#f8fafc',
        scale: 2,
        useCORS: true,
        logging: false,
        width: el.offsetWidth,
        height: el.offsetHeight,
      });

      const timelines = this.canvasState.visibleTimelines();
      const name = timelines.length === 1
        ? timelines[0].name
        : `timeline-export`;
      const link = document.createElement('a');
      link.download = `${name.replace(/\s+/g, '-').toLowerCase()}.png`;
      link.href = canvas.toDataURL('image/png');
      link.click();
    } finally {
      el.style.transform = savedTransform;
      this.isExporting.set(false);
      this.cdr.markForCheck();
    }
  }

  confirmConnection(): void {
    const pending = this.canvasState.pendingConnection();
    if (!pending) return;
    this.api.createConnection(
      pending.source.id, pending.target.id, this.newConnDesc, this.newConnType
    ).subscribe(conn => {
      this.canvasState.confirmConnectionCreated(conn);
      this.newConnDesc = '';
      this.newConnType = 'REFERENCE';
    });
  }

  openGraphMode(): void {
    const timelineIds = this.canvasState.timelines().map(t => t.id);
    this.router.navigate(['/graph'], {
      queryParams: { timelineIds },
    });
  }
}
