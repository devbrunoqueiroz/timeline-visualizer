import {
  Component, OnInit, signal, inject, ChangeDetectionStrategy, computed
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RichTextEditorComponent } from '../../shared/components/rich-text-editor/rich-text-editor.component';
import { ActivatedRoute, Router } from '@angular/router';
import { TimelineApiService } from '../../infrastructure/api/timeline-api.service';
import { CanvasStateService } from '../timeline-canvas/services/canvas-state.service';
import { CanvasComponent } from '../timeline-canvas/components/canvas/canvas.component';
import {
  CUSTOM, GREGORIAN, temporalCustom, temporalFromDate,
  TemporalPosition, Timeline, TimelineEvent, TimelineSummary, TimelineVisibility
} from '../../domain/timeline.model';

@Component({
  selector: 'app-timeline-editor',
  standalone: true,
  imports: [CommonModule, FormsModule, CanvasComponent, RichTextEditorComponent],
  providers: [CanvasStateService],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="editor-layout">
      <!-- Sidebar -->
      <aside class="sidebar">
        <div class="sidebar-header">
          <button class="back-btn" (click)="goBack()">← Back</button>
          <h2>{{ isNew() ? 'New Timeline' : (timeline()?.name ?? 'Loading...') }}</h2>
        </div>

        @if (isNew() || editingMeta()) {
          <div class="meta-form">
            <label>Name</label>
            <input [(ngModel)]="formName" placeholder="Timeline name..." class="input" />
            <label>Description</label>
            <textarea [(ngModel)]="formDescription" placeholder="Optional description..." class="input textarea"></textarea>
            <label>Visibility</label>
            <select [(ngModel)]="formVisibility" class="input">
              <option value="PRIVATE">Private</option>
              <option value="PUBLIC">Public</option>
              <option value="UNLISTED">Unlisted</option>
            </select>
            <div class="form-actions">
              <button class="btn-primary" (click)="saveTimeline()">
                {{ isNew() ? 'Create' : 'Save' }}
              </button>
              @if (!isNew()) {
                <button class="btn-secondary" (click)="editingMeta.set(false)">Cancel</button>
              }
            </div>
          </div>
        } @else {
          <div class="timeline-info">
            <p class="description">{{ timeline()?.description }}</p>
            <button class="btn-secondary btn-sm" (click)="editingMeta.set(true)">Edit Info</button>
            <button class="btn-danger btn-sm" (click)="deleteTimeline()">Delete</button>
          </div>

          <div class="events-section">
            <div class="section-header">
              <h3>Other Timelines</h3>
              <button class="btn-icon" (click)="showOtherTimelines.set(!showOtherTimelines())">
                {{ showOtherTimelines() ? '▲' : '▼' }}
              </button>
            </div>

            @if (showOtherTimelines()) {
              <div class="other-timelines-list">
                @for (t of otherTimelines(); track t.id) {
                  <div class="other-timeline-item">
                    <span class="other-timeline-name">{{ t.name }}</span>
                    @if (isInCanvas(t.id)) {
                      <button class="btn-remove-canvas" (click)="removeFromCanvas(t.id)">× Remove</button>
                    } @else {
                      <button class="btn-add-canvas" (click)="addToCanvas(t.id)">+ Add</button>
                    }
                  </div>
                }
                @if (otherTimelines().length === 0) {
                  <p class="no-other">No other timelines</p>
                }
              </div>
            }
          </div>

          @if (canvasState.timelines().length > 1) {
            <div class="timeline-tabs-section">
              <div class="timeline-tabs">
                @for (t of canvasState.timelines(); track t.id) {
                  <button class="timeline-tab"
                          [class.active]="activeEditTimelineId() === t.id"
                          (click)="switchEditingTimeline(t.id)">
                    {{ t.name }}
                  </button>
                }
              </div>
            </div>
          }

          <div class="events-section">
            <div class="section-header">
              <h3>Events</h3>
              <button class="btn-primary btn-sm" (click)="showAddEvent.set(true)">+ Add Event</button>
            </div>

            @if (showAddEvent()) {
              <div class="event-form">
                <input [(ngModel)]="eventTitle" placeholder="Event title..." class="input" />
                <label>Date type</label>
                <select [(ngModel)]="eventDateType" class="input">
                  <option value="GREGORIAN">Gregorian (real date)</option>
                  <option value="CUSTOM">Custom date</option>
                </select>
                @if (eventDateType === 'GREGORIAN') {
                  <label>Date</label>
                  <input type="datetime-local" [(ngModel)]="eventDate" class="input" />
                } @else {
                  <label>Numeric position (for ordering)</label>
                  <input type="number" [(ngModel)]="eventPosition" placeholder="e.g. 1203" class="input" />
                  <label>Label (displayed on timeline)</label>
                  <input [(ngModel)]="eventLabel" placeholder="e.g. Era 2, Ano 1203" class="input" />
                }
                <label>Description</label>
                <app-rich-text-editor
                  [content]="eventContent"
                  (contentChange)="eventContent = $event"
                  placeholder="Optional description...">
                </app-rich-text-editor>
                <div class="form-actions">
                  <button class="btn-primary btn-sm" (click)="addEvent()">Add</button>
                  <button class="btn-secondary btn-sm" (click)="showAddEvent.set(false)">Cancel</button>
                </div>
              </div>
            }

            <div class="events-list">
              @for (event of editingTimeline()?.events ?? []; track event.id) {
                @if (editingEvent()?.id === event.id) {
                  <div class="event-form">
                    <label>Title</label>
                    <input [(ngModel)]="editEventTitle" placeholder="Event title..." class="input" />
                    <label>Date type</label>
                    <select [(ngModel)]="editEventDateType" class="input">
                      <option value="GREGORIAN">Gregorian (real date)</option>
                      <option value="CUSTOM">Custom date</option>
                    </select>
                    @if (editEventDateType === 'GREGORIAN') {
                      <label>Date</label>
                      <input type="datetime-local" [(ngModel)]="editEventDate" class="input" />
                    } @else {
                      <label>Numeric position</label>
                      <input type="number" [(ngModel)]="editEventPosition" class="input" />
                      <label>Label</label>
                      <input [(ngModel)]="editEventLabel" placeholder="e.g. Era 2, Ano 1203" class="input" />
                    }
                    <label>Description</label>
                    <app-rich-text-editor
                      [content]="editEventContent"
                      (contentChange)="editEventContent = $event"
                      placeholder="Optional description...">
                    </app-rich-text-editor>
                    <div class="form-actions">
                      <button class="btn-primary btn-sm" (click)="saveEditEvent()">Save</button>
                      <button class="btn-secondary btn-sm" (click)="editingEvent.set(null)">Cancel</button>
                    </div>
                  </div>
                } @else {
                  <div class="event-item"
                       [class.selected]="canvasState.selectedEvent()?.id === event.id"
                       (click)="canvasState.selectEvent(event)">
                    <div class="event-item-header">
                      <strong>{{ event.title }}</strong>
                      <div class="event-item-actions">
                        <button class="edit-btn" (click)="startEditEvent(event, $event)">✎</button>
                        <button class="delete-btn" (click)="removeEvent(event, $event)">×</button>
                      </div>
                    </div>
                    <span class="event-item-date">{{ event.temporalPosition.label }}</span>
                  </div>
                }
              }
            </div>
          </div>
        }
      </aside>

      <!-- Canvas -->
      <main class="canvas-area">
        @if (!isNew() && timeline()) {
          <app-canvas />
        } @else if (!isNew()) {
          <div class="loading-canvas">Loading...</div>
        }
      </main>
    </div>
  `,
  styles: [`
    .editor-layout {
      display: flex;
      height: 100vh;
      background: #f8fafc;
    }

    .sidebar {
      width: 280px;
      background: white;
      border-right: 1px solid #e2e8f0;
      display: flex;
      flex-direction: column;
      overflow-y: auto;
      flex-shrink: 0;
    }

    .sidebar-header {
      padding: 20px 16px 16px;
      border-bottom: 1px solid #f1f5f9;
    }

    .back-btn {
      background: none;
      border: none;
      color: #6366f1;
      cursor: pointer;
      font-size: 13px;
      padding: 0;
      margin-bottom: 8px;
    }

    h2 {
      font-size: 16px;
      font-weight: 700;
      color: #0f172a;
      margin: 0;
    }

    h3 {
      font-size: 14px;
      font-weight: 600;
      color: #1e293b;
      margin: 0;
    }

    .meta-form, .timeline-info, .events-section {
      padding: 16px;
    }

    label {
      display: block;
      font-size: 12px;
      font-weight: 600;
      color: #64748b;
      margin-bottom: 4px;
      margin-top: 12px;
    }

    .input {
      width: 100%;
      padding: 8px 10px;
      border: 1px solid #e2e8f0;
      border-radius: 6px;
      font-size: 13px;
      color: #1e293b;
      background: white;
      box-sizing: border-box;
      outline: none;
      transition: border-color 0.15s;
    }

    .input:focus {
      border-color: #6366f1;
    }

    .textarea {
      min-height: 80px;
      resize: vertical;
      font-family: inherit;
    }

    .form-actions {
      display: flex;
      gap: 8px;
      margin-top: 12px;
    }

    .btn-primary {
      padding: 8px 16px;
      background: #6366f1;
      color: white;
      border: none;
      border-radius: 6px;
      font-size: 13px;
      font-weight: 600;
      cursor: pointer;
    }

    .btn-primary:hover { background: #4f46e5; }

    .btn-secondary {
      padding: 8px 16px;
      background: #f1f5f9;
      color: #475569;
      border: 1px solid #e2e8f0;
      border-radius: 6px;
      font-size: 13px;
      font-weight: 600;
      cursor: pointer;
    }

    .btn-danger {
      padding: 8px 16px;
      background: #fee2e2;
      color: #dc2626;
      border: 1px solid #fecaca;
      border-radius: 6px;
      font-size: 13px;
      font-weight: 600;
      cursor: pointer;
    }

    .btn-sm {
      padding: 5px 10px;
      font-size: 12px;
    }

    .description {
      font-size: 13px;
      color: #64748b;
      margin: 0 0 12px;
    }

    .section-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 12px;
    }

    .event-form {
      background: #f8fafc;
      border: 1px solid #e2e8f0;
      border-radius: 8px;
      padding: 12px;
      margin-bottom: 12px;
    }

    .event-form .input { margin-bottom: 8px; }

    .events-list {
      display: flex;
      flex-direction: column;
      gap: 8px;
    }

    .event-item {
      background: #f8fafc;
      border: 1px solid #e2e8f0;
      border-radius: 6px;
      padding: 10px 12px;
      cursor: pointer;
      transition: border-color 0.15s;
    }

    .event-item:hover, .event-item.selected {
      border-color: #6366f1;
      background: #eef2ff;
    }

    .event-item-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      font-size: 13px;
      color: #1e293b;
    }

    .event-item-date {
      font-size: 11px;
      color: #94a3b8;
      margin-top: 2px;
    }

    .event-item-actions {
      display: flex;
      gap: 4px;
      align-items: center;
    }

    .edit-btn {
      background: none;
      border: none;
      color: #94a3b8;
      cursor: pointer;
      font-size: 14px;
      line-height: 1;
      padding: 0 2px;
    }

    .edit-btn:hover { color: #6366f1; }

    .delete-btn {
      background: none;
      border: none;
      color: #94a3b8;
      cursor: pointer;
      font-size: 16px;
      line-height: 1;
      padding: 0 2px;
    }

    .delete-btn:hover { color: #dc2626; }

    .canvas-area {
      flex: 1;
      padding: 24px;
      overflow: hidden;
      display: flex;
      flex-direction: column;
    }

    .loading-canvas {
      display: flex;
      align-items: center;
      justify-content: center;
      flex: 1;
      color: #94a3b8;
      font-size: 14px;
    }

    .events-section {
      border-top: 1px solid #f1f5f9;
    }

    .btn-icon {
      background: none;
      border: none;
      cursor: pointer;
      color: #94a3b8;
      font-size: 11px;
      padding: 2px 4px;
    }

    .other-timelines-list {
      display: flex;
      flex-direction: column;
      gap: 6px;
      margin-bottom: 8px;
    }

    .other-timeline-item {
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 8px;
      padding: 6px 8px;
      background: #f8fafc;
      border: 1px solid #e2e8f0;
      border-radius: 6px;
      font-size: 12px;
    }

    .other-timeline-name {
      flex: 1;
      color: #1e293b;
      font-weight: 500;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .btn-add-canvas {
      padding: 3px 8px;
      background: #ede9fe;
      color: #6366f1;
      border: 1px solid #c4b5fd;
      border-radius: 4px;
      font-size: 11px;
      font-weight: 600;
      cursor: pointer;
      white-space: nowrap;
    }

    .btn-remove-canvas {
      padding: 3px 8px;
      background: #fee2e2;
      color: #dc2626;
      border: 1px solid #fecaca;
      border-radius: 4px;
      font-size: 11px;
      font-weight: 600;
      cursor: pointer;
      white-space: nowrap;
    }

    .no-other {
      font-size: 12px;
      color: #94a3b8;
      text-align: center;
      margin: 8px 0;
    }

    .timeline-tabs-section {
      border-top: 1px solid #f1f5f9;
      padding: 8px 16px 0;
    }

    .timeline-tabs {
      display: flex;
      gap: 4px;
      flex-wrap: wrap;
    }

    .timeline-tab {
      padding: 4px 10px;
      border: 1px solid #e2e8f0;
      border-radius: 6px;
      background: white;
      color: #475569;
      font-size: 11px;
      font-weight: 500;
      cursor: pointer;
      transition: all 0.15s;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
      max-width: 120px;
    }

    .timeline-tab:hover {
      background: #f1f5f9;
      border-color: #cbd5e1;
    }

    .timeline-tab.active {
      background: #ede9fe;
      border-color: #6366f1;
      color: #6366f1;
      font-weight: 600;
    }
  `]
})
export class TimelineEditorComponent implements OnInit {

  private readonly api = inject(TimelineApiService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  protected readonly canvasState = inject(CanvasStateService);

  readonly timeline = signal<Timeline | null>(null);
  readonly isNew = signal(false);
  readonly editingMeta = signal(false);
  readonly showAddEvent = signal(false);
  readonly editingEvent = signal<TimelineEvent | null>(null);
  readonly allTimelines = signal<TimelineSummary[]>([]);
  readonly showOtherTimelines = signal(false);
  readonly activeEditTimelineId = signal<string>('');

  readonly editingTimeline = computed(() =>
    this.canvasState.timelines().find(t => t.id === this.activeEditTimelineId()) ?? this.timeline()
  );

  readonly otherTimelines = () =>
    this.allTimelines().filter(t => t.id !== this.timeline()?.id);

  formName = '';
  formDescription = '';
  formVisibility: TimelineVisibility = 'PRIVATE';
  eventTitle = '';
  eventDate = '';
  eventContent = '';
  eventDateType: string = GREGORIAN;
  eventPosition: number = 0;
  eventLabel: string = '';

  editEventTitle = '';
  editEventDate = '';
  editEventContent = '';
  editEventDateType: string = GREGORIAN;
  editEventPosition: number = 0;
  editEventLabel: string = '';

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id === 'new') {
      this.isNew.set(true);
    } else if (id) {
      this.loadTimeline(id);
    }
    this.api.listTimelines().subscribe(ts => this.allTimelines.set(ts));
  }

  private loadTimeline(id: string): void {
    this.api.getTimeline(id).subscribe(t => {
      this.timeline.set(t);
      this.canvasState.setTimelines([
        ...this.canvasState.timelines().filter(ct => ct.id !== t.id),
        t,
      ]);
      if (!this.activeEditTimelineId()) {
        this.activeEditTimelineId.set(t.id);
      }
      this.loadConnections();
    });
  }

  private reloadEditingTimeline(): void {
    const id = this.activeEditTimelineId();
    this.api.getTimeline(id).subscribe(t => {
      this.canvasState.updateTimeline(t);
      if (id === this.timeline()?.id) {
        this.timeline.set(t);
      }
      this.loadConnections();
    });
  }

  switchEditingTimeline(id: string): void {
    this.activeEditTimelineId.set(id);
    this.showAddEvent.set(false);
    this.editingEvent.set(null);
  }

  private loadConnections(): void {
    const allEventIds = this.canvasState.timelines()
      .flatMap(t => t.events.map(e => e.id));
    if (allEventIds.length === 0) {
      this.canvasState.setConnections([]);
      return;
    }
    this.api.getConnections(allEventIds).subscribe(conns => {
      this.canvasState.setConnections(conns);
    });
  }

  saveTimeline(): void {
    if (this.isNew()) {
      this.api.createTimeline(this.formName, this.formDescription, this.formVisibility)
        .subscribe(t => {
          this.router.navigate(['/timelines', t.id]);
        });
    } else {
      const id = this.timeline()!.id;
      this.api.updateTimeline(id, this.formName, this.formDescription, this.formVisibility)
        .subscribe(t => {
          this.timeline.set(t);
          this.canvasState.updateTimeline(t);
          this.editingMeta.set(false);
        });
    }
  }

  deleteTimeline(): void {
    if (!confirm('Delete this timeline?')) return;
    this.api.deleteTimeline(this.timeline()!.id).subscribe(() => {
      this.router.navigate(['/']);
    });
  }

  addEvent(): void {
    if (!this.eventTitle) return;
    const temporal = this.buildTemporalPosition();
    if (!temporal) return;
    const tId = this.activeEditTimelineId();
    const content = { text: this.eventContent, type: 'RICH_TEXT' as const, metadata: {} };
    this.api.addEvent(tId, this.eventTitle, content, temporal)
      .subscribe(() => {
        this.reloadEditingTimeline();
        this.eventTitle = '';
        this.eventDate = '';
        this.eventContent = '';
        this.eventPosition = 0;
        this.eventLabel = '';
        this.showAddEvent.set(false);
      });
  }

  private buildTemporalPosition(): TemporalPosition | null {
    if (this.eventDateType === GREGORIAN) {
      if (!this.eventDate) return null;
      return temporalFromDate(new Date(this.eventDate));
    } else {
      if (!this.eventLabel) return null;
      return temporalCustom(this.eventPosition, this.eventLabel);
    }
  }

  startEditEvent(event: TimelineEvent, domEvent: MouseEvent): void {
    domEvent.stopPropagation();
    this.editingEvent.set(event);
    this.editEventTitle = event.title;
    this.editEventContent = event.content.text;
    this.editEventDateType = event.temporalPosition.calendarSystem;
    if (event.temporalPosition.calendarSystem === GREGORIAN) {
      const d = new Date(event.temporalPosition.position);
      this.editEventDate = d.toISOString().slice(0, 16);
    } else {
      this.editEventPosition = event.temporalPosition.position;
      this.editEventLabel = event.temporalPosition.label;
    }
  }

  saveEditEvent(): void {
    const ev = this.editingEvent();
    if (!ev || !this.editEventTitle) return;
    let temporal: TemporalPosition | null;
    if (this.editEventDateType === GREGORIAN) {
      if (!this.editEventDate) return;
      temporal = temporalFromDate(new Date(this.editEventDate));
    } else {
      if (!this.editEventLabel) return;
      temporal = temporalCustom(this.editEventPosition, this.editEventLabel);
    }
    const tId = this.activeEditTimelineId();
    const content = { text: this.editEventContent, type: 'RICH_TEXT' as const, metadata: {} };
    this.api.updateEvent(tId, ev.id, this.editEventTitle, content, temporal)
      .subscribe(() => {
        this.editingEvent.set(null);
        this.reloadEditingTimeline();
      });
  }

  removeEvent(event: TimelineEvent, domEvent: MouseEvent): void {
    domEvent.stopPropagation();
    if (!confirm(`Remove event "${event.title}"?`)) return;
    const tId = this.activeEditTimelineId();
    this.api.removeEvent(tId, event.id).subscribe(() => this.reloadEditingTimeline());
  }

  isInCanvas(timelineId: string): boolean {
    return this.canvasState.timelines().some(t => t.id === timelineId);
  }

  addToCanvas(timelineId: string): void {
    this.api.getTimeline(timelineId).subscribe(t => {
      this.canvasState.addTimeline(t);
      this.loadConnections();
    });
  }

  removeFromCanvas(timelineId: string): void {
    if (timelineId === this.timeline()?.id) return;
    if (this.activeEditTimelineId() === timelineId) {
      this.activeEditTimelineId.set(this.timeline()!.id);
      this.showAddEvent.set(false);
      this.editingEvent.set(null);
    }
    this.canvasState.removeTimeline(timelineId);
    this.loadConnections();
  }

  goBack(): void {
    this.router.navigate(['/']);
  }
}
