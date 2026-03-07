import { Component, Input, Output, EventEmitter, ChangeDetectionStrategy, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TimelineEvent } from '../../../../domain/timeline.model';

@Component({
  selector: 'app-event-node',
  standalone: true,
  imports: [CommonModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="event-card"
         [class.selected]="selected"
         [class.connect-source]="isPendingSource"
         [class.connect-target]="connectingMode && !isPendingSource"
         [class.expanded]="expanded()"
         [attr.data-event-id]="event.id"
         (click)="eventClick.emit(event)">
      <div class="event-marker"></div>
      <div class="event-body">
        <div class="event-date">{{ event.temporalPosition.label }}</div>
        <div class="event-title">{{ event.title }}</div>
        @if (event.content.text) {
          @if (event.content.type === 'RICH_TEXT') {
            <div class="event-text rich" [class.collapsed]="!expanded()" [innerHTML]="event.content.text"></div>
          } @else {
            <div class="event-text" [class.collapsed]="!expanded()">{{ event.content.text }}</div>
          }
          <button class="expand-btn"
                  (click)="toggle($event)"
                  [title]="expanded() ? 'Collapse' : 'Expand'">
            {{ expanded() ? '▲' : '▼' }}
          </button>
        }
      </div>
    </div>
  `,
  styles: [`
    .event-card {
      display: flex;
      align-items: stretch;
      width: 100%;
      background: white;
      border: 2px solid #e2e8f0;
      border-radius: 10px;
      overflow: hidden;
      cursor: pointer;
      transition: border-color 0.15s, box-shadow 0.15s;
      box-sizing: border-box;
    }

    .event-card:hover {
      border-color: #6366f1;
      box-shadow: 0 2px 8px rgba(99, 102, 241, 0.15);
    }

    .event-card.selected {
      border-color: #6366f1;
      box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.2);
    }

    .event-marker {
      width: 5px;
      background: linear-gradient(to bottom, #6366f1, #8b5cf6);
      flex-shrink: 0;
    }

    .event-card.selected .event-marker {
      background: linear-gradient(to bottom, #4f46e5, #7c3aed);
    }

    .event-body {
      padding: 12px 16px;
      flex: 1;
      min-width: 0;
    }

    .event-date {
      font-size: 10px;
      color: #6366f1;
      font-weight: 700;
      margin-bottom: 4px;
      text-transform: uppercase;
      letter-spacing: 0.5px;
    }

    .event-title {
      font-size: 14px;
      font-weight: 700;
      color: #0f172a;
      margin-bottom: 4px;
    }

    .event-text {
      font-size: 12px;
      color: #64748b;
      line-height: 1.6;
    }

    .event-text.collapsed {
      overflow: hidden;
      display: -webkit-box;
      -webkit-line-clamp: 3;
      -webkit-box-orient: vertical;
    }

    .event-text.rich :is(p, h1, h2, ul, ol, blockquote, pre) { margin: 0 0 4px; }
    .event-text.rich strong { font-weight: 700; }
    .event-text.rich em { font-style: italic; }
    .event-text.rich h1 { font-size: 13px; font-weight: 700; color: #1e293b; }
    .event-text.rich h2 { font-size: 12px; font-weight: 700; color: #1e293b; }
    .event-text.rich ul, .event-text.rich ol { padding-left: 16px; }
    .event-text.rich li > p { margin: 0; }
    .event-text.rich blockquote { border-left: 3px solid #6366f1; padding-left: 8px; font-style: italic; }
    .event-text.rich code { background: #f1f5f9; border-radius: 2px; padding: 0 3px; font-family: monospace; font-size: 11px; }
    .event-text.rich pre { background: #1e293b; color: #e2e8f0; border-radius: 5px; padding: 8px 10px; overflow-x: auto; }
    .event-text.rich pre code { background: none; padding: 0; color: inherit; }

    .expand-btn {
      display: block;
      margin-top: 6px;
      background: none;
      border: none;
      cursor: pointer;
      font-size: 9px;
      color: #94a3b8;
      padding: 0;
      line-height: 1;
      transition: color 0.15s;
    }

    .expand-btn:hover { color: #6366f1; }

    .event-card.connect-source {
      border-color: #10b981;
      box-shadow: 0 0 0 3px rgba(16, 185, 129, 0.25);
    }

    .event-card.connect-source .event-marker {
      background: linear-gradient(to bottom, #10b981, #059669);
    }

    .event-card.connect-target:hover {
      border-color: #f59e0b;
      box-shadow: 0 0 0 3px rgba(245, 158, 11, 0.25);
    }
  `]
})
export class EventNodeComponent {
  @Input({ required: true }) event!: TimelineEvent;
  @Input() selected = false;
  @Input() connectingMode = false;
  @Input() isPendingSource = false;
  @Output() eventClick = new EventEmitter<TimelineEvent>();

  readonly expanded = signal(false);

  toggle(domEvent: MouseEvent): void {
    domEvent.stopPropagation();
    this.expanded.update(v => !v);
  }
}
