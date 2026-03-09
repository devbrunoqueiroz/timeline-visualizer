import { Component, Input, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Character } from '../../../../domain/timeline.model';

@Component({
  selector: 'app-character-track',
  standalone: true,
  imports: [CommonModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="character-track">
      <div class="track-header">
        <div class="header-left">
          <span class="character-icon">👤</span>
          <div class="header-text">
            <span class="character-name">{{ character.name }}</span>
            @if (character.startPosition !== null || character.endPosition !== null) {
              <span class="temporal-range">
                {{ rangeLabel }}
              </span>
            }
          </div>
        </div>
        <span class="event-count">{{ character.events.length }} eventos</span>
      </div>

      <div class="events-container">
        @if (sortedEvents.length === 0) {
          <div class="empty-track">Nenhum evento</div>
        } @else {
          @for (event of sortedEvents; track event.id) {
            <div class="event-card">
              <div class="event-dot"></div>
              <div class="event-body">
                <span class="event-label">{{ event.temporalLabel }}</span>
                <span class="event-title">{{ event.title }}</span>
              </div>
            </div>
          }
        }
      </div>
    </div>
  `,
  styles: [`
    .character-track {
      width: 240px;
      flex-shrink: 0;
      background: #fdf4ff;
      border: 1.5px solid #e9d5ff;
      border-radius: 10px;
      overflow: hidden;
    }

    .track-header {
      background: #f3e8ff;
      border-bottom: 1px solid #e9d5ff;
      padding: 10px 12px;
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 8px;
    }

    .header-left {
      display: flex;
      align-items: flex-start;
      gap: 8px;
      min-width: 0;
    }

    .character-icon {
      font-size: 16px;
      flex-shrink: 0;
      margin-top: 1px;
    }

    .header-text {
      display: flex;
      flex-direction: column;
      min-width: 0;
    }

    .character-name {
      font-size: 13px;
      font-weight: 700;
      color: #6b21a8;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .temporal-range {
      font-size: 10px;
      color: #9333ea;
      margin-top: 1px;
    }

    .event-count {
      font-size: 10px;
      color: #a855f7;
      white-space: nowrap;
      flex-shrink: 0;
    }

    .events-container {
      padding: 12px 10px;
      display: flex;
      flex-direction: column;
      gap: 8px;
      min-height: 60px;
    }

    .empty-track {
      font-size: 12px;
      color: #c4b5fd;
      text-align: center;
      padding: 8px 0;
    }

    .event-card {
      display: flex;
      align-items: flex-start;
      gap: 8px;
    }

    .event-dot {
      width: 8px;
      height: 8px;
      border-radius: 50%;
      background: #a855f7;
      flex-shrink: 0;
      margin-top: 4px;
    }

    .event-body {
      display: flex;
      flex-direction: column;
      min-width: 0;
    }

    .event-label {
      font-size: 10px;
      color: #a855f7;
      margin-bottom: 1px;
    }

    .event-title {
      font-size: 12px;
      font-weight: 500;
      color: #4c1d95;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  `]
})
export class CharacterTrackComponent {
  @Input({ required: true }) character!: Character;

  get sortedEvents() {
    return [...this.character.events].sort(
      (a, b) => a.temporalPosition - b.temporalPosition
    );
  }

  get rangeLabel(): string {
    const start = this.character.startPosition;
    const end = this.character.endPosition;
    if (start !== null && end !== null) return `${start} → ${end}`;
    if (start !== null) return `a partir de ${start}`;
    if (end !== null) return `até ${end}`;
    return '';
  }
}
