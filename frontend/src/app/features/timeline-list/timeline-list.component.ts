import {
  Component, OnInit, signal, inject, ChangeDetectionStrategy
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { TimelineApiService } from '../../infrastructure/api/timeline-api.service';
import { TimelineSummary } from '../../domain/timeline.model';

@Component({
  selector: 'app-timeline-list',
  standalone: true,
  imports: [CommonModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="list-container">
      <div class="list-header">
        <h1>My Timelines</h1>
        <button class="btn-primary" (click)="createNew()">+ New Timeline</button>
      </div>

      @if (loading()) {
        <div class="loading">Loading timelines...</div>
      } @else if (timelines().length === 0) {
        <div class="empty">
          <div class="empty-icon">📅</div>
          <p>No timelines yet. Create your first one!</p>
          <button class="btn-primary" (click)="createNew()">Create Timeline</button>
        </div>
      } @else {
        <div class="timelines-grid">
          @for (timeline of timelines(); track timeline.id) {
            <div class="timeline-card" (click)="openTimeline(timeline.id)">
              <div class="card-header">
                <h3>{{ timeline.name }}</h3>
                <span class="visibility-badge" [class]="timeline.visibility.toLowerCase()">
                  {{ timeline.visibility }}
                </span>
              </div>
              @if (timeline.description) {
                <p class="card-description">{{ timeline.description }}</p>
              }
              <div class="card-footer">
                <span class="event-count">{{ timeline.eventCount }} events</span>
                <span class="created-date">{{ timeline.createdAt | date:'mediumDate' }}</span>
              </div>
            </div>
          }
        </div>
      }
    </div>
  `,
  styles: [`
    .list-container {
      max-width: 1200px;
      margin: 0 auto;
      padding: 32px 24px;
    }

    .list-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 32px;
    }

    h1 {
      font-size: 28px;
      font-weight: 700;
      color: #0f172a;
      margin: 0;
    }

    .btn-primary {
      padding: 10px 20px;
      background: #6366f1;
      color: white;
      border: none;
      border-radius: 8px;
      font-size: 14px;
      font-weight: 600;
      cursor: pointer;
      transition: background 0.15s;
    }

    .btn-primary:hover {
      background: #4f46e5;
    }

    .loading, .empty {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      min-height: 300px;
      color: #64748b;
      gap: 16px;
    }

    .empty-icon {
      font-size: 48px;
    }

    .timelines-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
      gap: 20px;
    }

    .timeline-card {
      background: white;
      border: 1px solid #e2e8f0;
      border-radius: 12px;
      padding: 20px;
      cursor: pointer;
      transition: box-shadow 0.15s, transform 0.15s;
    }

    .timeline-card:hover {
      box-shadow: 0 4px 16px rgba(0,0,0,0.1);
      transform: translateY(-2px);
    }

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      margin-bottom: 12px;
    }

    h3 {
      font-size: 16px;
      font-weight: 600;
      color: #0f172a;
      margin: 0;
    }

    .visibility-badge {
      font-size: 10px;
      font-weight: 600;
      padding: 2px 8px;
      border-radius: 10px;
      text-transform: uppercase;
    }

    .visibility-badge.private {
      background: #fef3c7;
      color: #92400e;
    }

    .visibility-badge.public {
      background: #dcfce7;
      color: #166534;
    }

    .visibility-badge.unlisted {
      background: #e0e7ff;
      color: #3730a3;
    }

    .card-description {
      font-size: 13px;
      color: #64748b;
      margin: 0 0 16px;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
    }

    .card-footer {
      display: flex;
      justify-content: space-between;
      align-items: center;
      font-size: 12px;
      color: #94a3b8;
      border-top: 1px solid #f1f5f9;
      padding-top: 12px;
      margin-top: 12px;
    }

    .event-count {
      font-weight: 500;
    }
  `]
})
export class TimelineListComponent implements OnInit {

  private readonly api = inject(TimelineApiService);
  private readonly router = inject(Router);

  readonly timelines = signal<TimelineSummary[]>([]);
  readonly loading = signal(true);

  ngOnInit(): void {
    this.api.listTimelines().subscribe({
      next: (timelines) => {
        this.timelines.set(timelines);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  openTimeline(id: string): void {
    this.router.navigate(['/timelines', id]);
  }

  createNew(): void {
    this.router.navigate(['/timelines', 'new']);
  }
}
