import {
  Component, input, output, ChangeDetectionStrategy, computed
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { Timeline, TimelineEvent } from '../../../../domain/timeline.model';
import { EventNodeComponent } from '../event-node/event-node.component';

@Component({
  selector: 'app-timeline-track',
  standalone: true,
  imports: [CommonModule, EventNodeComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="timeline-track">
      <div class="track-header">
        <span class="track-name">{{ timeline().name }}</span>
        <span class="track-count">{{ timeline().events.length }} eventos</span>
      </div>

      @if (sortedEvents().length === 0) {
        <div class="empty-track">Nenhum evento ainda</div>
      } @else {
        <div class="track-body">
          @for (event of sortedEvents(); track event.id) {
            <app-event-node
              [event]="event"
              [selected]="selectedEventId() === event.id"
              [connectingMode]="connectingMode()"
              [isPendingSource]="pendingSourceId() === event.id"
              (eventClick)="eventSelected.emit($event)"
            />
            @if (!$last) {
              <div class="gap-connector" [style.minHeight.px]="getGapHeight($index)">
                <div class="gap-line"></div>
                <div class="gap-badge">{{ formatGap(sortedEvents()[$index], sortedEvents()[$index + 1]) }}</div>
                <div class="gap-line"></div>
                <div class="gap-arrow"></div>
              </div>
            }
          }
        </div>
      }
    </div>
  `,
  styles: [`
    .timeline-track {
      width: 360px;
      flex-shrink: 0;
    }

    .track-header {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 12px 16px;
      background: #1e293b;
      border-radius: 10px 10px 0 0;
      margin-bottom: 0;
    }

    .track-name {
      font-size: 14px;
      font-weight: 700;
      color: white;
    }

    .track-count {
      font-size: 11px;
      color: #94a3b8;
      background: rgba(255,255,255,0.1);
      padding: 2px 8px;
      border-radius: 10px;
    }

    .empty-track {
      padding: 40px 16px;
      text-align: center;
      color: #94a3b8;
      font-size: 13px;
      background: white;
      border-radius: 0 0 10px 10px;
      border: 1px solid #e2e8f0;
      border-top: none;
    }

    .track-body {
      display: flex;
      flex-direction: column;
      align-items: stretch;
      padding: 16px;
      background: #f8fafc;
      border: 1px solid #e2e8f0;
      border-top: none;
      border-radius: 0 0 10px 10px;
    }

    .gap-connector {
      display: flex;
      flex-direction: column;
      align-items: center;
      min-height: 80px;
      padding: 4px 0;
    }

    .gap-line {
      flex: 1;
      width: 2px;
      background: repeating-linear-gradient(
        to bottom,
        #cbd5e1 0px,
        #cbd5e1 5px,
        transparent 5px,
        transparent 10px
      );
      min-height: 12px;
    }

    .gap-badge {
      padding: 3px 12px;
      background: white;
      border: 1px solid #e2e8f0;
      border-radius: 12px;
      font-size: 11px;
      color: #64748b;
      font-weight: 600;
      white-space: nowrap;
      margin: 5px 0;
      box-shadow: 0 1px 3px rgba(0,0,0,0.05);
    }

    .gap-arrow {
      width: 0;
      height: 0;
      border-left: 6px solid transparent;
      border-right: 6px solid transparent;
      border-top: 8px solid #94a3b8;
      margin-top: 3px;
      flex-shrink: 0;
    }
  `]
})
export class TimelineTrackComponent {
  readonly timeline = input.required<Timeline>();
  readonly selectedEventId = input<string | null>(null);
  readonly connectingMode = input<boolean>(false);
  readonly pendingSourceId = input<string | null>(null);
  readonly eventSelected = output<TimelineEvent>();

  readonly sortedEvents = computed(() =>
    [...this.timeline().events].sort(
      (a, b) => a.temporalPosition.position - b.temporalPosition.position
    )
  );

  getGapHeight(index: number): number {
    const events = this.sortedEvents();
    const totalRange =
      events[events.length - 1].temporalPosition.position -
      events[0].temporalPosition.position;
    if (totalRange === 0) return 80;
    const gap =
      events[index + 1].temporalPosition.position -
      events[index].temporalPosition.position;
    return Math.max(80, (gap / totalRange) * 500);
  }

  formatGap(a: TimelineEvent, b: TimelineEvent): string {
    const diff = b.temporalPosition.position - a.temporalPosition.position;
    if (a.temporalPosition.calendarSystem === 'GREGORIAN') {
      const days = diff / 86_400_000;
      if (days < 1)   return `${Math.round(diff / 3_600_000)}h`;
      if (days < 2)   return '1 dia';
      if (days < 30)  return `${Math.round(days)} dias`;
      if (days < 60)  return '1 mês';
      if (days < 365) return `${Math.round(days / 30)} meses`;
      if (days < 730) return '1 ano';
      return `${Math.round(days / 365)} anos`;
    }
    return `+${Math.round(diff * 10) / 10}`;
  }
}
