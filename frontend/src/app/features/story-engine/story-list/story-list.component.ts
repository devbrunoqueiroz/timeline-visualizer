import { Component, OnInit, signal, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { StoryApiService } from '../../../infrastructure/api/story-api.service';
import { StorySummary } from '../../../domain/story.model';

@Component({
  selector: 'app-story-list',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="page">
      <header class="page-header">
        <div class="header-left">
          <a routerLink="/" class="back-btn">← Timeline Canvas</a>
          <h1 class="page-title">Story Engine</h1>
        </div>
        <button class="create-btn" (click)="showCreate.set(true)">+ New Story</button>
      </header>

      @if (showCreate()) {
        <div class="create-card">
          <h2 class="create-title">New Story</h2>
          <input class="input" placeholder="Title" [(ngModel)]="newTitle" />
          <textarea class="textarea" placeholder="Description" [(ngModel)]="newDescription" rows="3"></textarea>
          <div class="create-actions">
            <button class="btn-secondary" (click)="showCreate.set(false)">Cancel</button>
            <button class="btn-primary" (click)="createStory()" [disabled]="!newTitle.trim()">Create</button>
          </div>
        </div>
      }

      @if (loading()) {
        <div class="loading">Loading stories…</div>
      } @else if (stories().length === 0) {
        <div class="empty">
          <div class="empty-icon">📖</div>
          <p>No stories yet. Create your first narrative world.</p>
        </div>
      } @else {
        <div class="stories-grid">
          @for (story of stories(); track story.id) {
            <div class="story-card">
              <div class="story-meta">
                <span class="scene-count">{{ story.sceneCount }} {{ story.sceneCount === 1 ? 'scene' : 'scenes' }}</span>
              </div>
              <h2 class="story-title">{{ story.title }}</h2>
              @if (story.description) {
                <p class="story-desc">{{ story.description }}</p>
              }
              <div class="story-actions">
                <a class="btn-action" [routerLink]="['/stories', story.id, 'build']">Build</a>
                <a class="btn-action btn-play" [routerLink]="['/stories', story.id, 'play']">Play</a>
                <a class="btn-action btn-graph" [routerLink]="['/stories', story.id, 'graph']">Graph</a>
              </div>
            </div>
          }
        </div>
      }
    </div>
  `,
  styles: [`
    .page { min-height: 100vh; background: #0f172a; color: #e2e8f0; font-family: system-ui, sans-serif; }

    .page-header {
      display: flex; justify-content: space-between; align-items: center;
      padding: 20px 32px; border-bottom: 1px solid #1e293b;
      background: #0f172a; position: sticky; top: 0; z-index: 10;
    }
    .header-left { display: flex; align-items: center; gap: 20px; }
    .back-btn {
      color: #6366f1; font-size: 13px; font-weight: 600; text-decoration: none;
      padding: 6px 12px; border: 1px solid #312e81; border-radius: 6px;
      transition: background 0.15s;
    }
    .back-btn:hover { background: #1e1b4b; }
    .page-title { font-size: 20px; font-weight: 800; color: #f1f5f9; margin: 0; }

    .create-btn {
      padding: 8px 20px; background: #6366f1; color: white; border: none;
      border-radius: 8px; font-size: 14px; font-weight: 600; cursor: pointer;
      transition: background 0.15s;
    }
    .create-btn:hover { background: #4f46e5; }

    .create-card {
      margin: 24px 32px; padding: 24px; background: #1e293b;
      border: 1px solid #334155; border-radius: 12px; max-width: 500px;
    }
    .create-title { font-size: 16px; font-weight: 700; margin: 0 0 16px; color: #f1f5f9; }
    .input, .textarea {
      width: 100%; box-sizing: border-box; background: #0f172a; border: 1px solid #334155;
      color: #e2e8f0; border-radius: 8px; padding: 10px 12px; font-size: 14px;
      margin-bottom: 12px; outline: none;
    }
    .input:focus, .textarea:focus { border-color: #6366f1; }
    .textarea { resize: vertical; font-family: inherit; }
    .create-actions { display: flex; gap: 10px; justify-content: flex-end; }
    .btn-primary {
      padding: 8px 20px; background: #6366f1; color: white; border: none;
      border-radius: 6px; font-size: 13px; font-weight: 600; cursor: pointer;
    }
    .btn-primary:disabled { opacity: 0.5; cursor: not-allowed; }
    .btn-secondary {
      padding: 8px 20px; background: transparent; color: #94a3b8;
      border: 1px solid #334155; border-radius: 6px; font-size: 13px; cursor: pointer;
    }

    .loading { padding: 60px; text-align: center; color: #64748b; }

    .empty {
      padding: 80px 32px; text-align: center; color: #475569;
    }
    .empty-icon { font-size: 48px; margin-bottom: 16px; }
    .empty p { font-size: 15px; }

    .stories-grid {
      display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
      gap: 20px; padding: 28px 32px;
    }

    .story-card {
      background: #1e293b; border: 1px solid #334155; border-radius: 12px;
      padding: 20px; display: flex; flex-direction: column; gap: 8px;
      transition: border-color 0.15s, transform 0.1s;
    }
    .story-card:hover { border-color: #6366f1; transform: translateY(-2px); }

    .story-meta { display: flex; gap: 8px; }
    .scene-count {
      font-size: 11px; font-weight: 700; text-transform: uppercase;
      color: #6366f1; background: #1e1b4b; padding: 3px 8px; border-radius: 20px;
    }
    .story-title { font-size: 17px; font-weight: 700; color: #f1f5f9; margin: 0; }
    .story-desc {
      font-size: 13px; color: #94a3b8; margin: 0; line-height: 1.5;
      display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden;
    }

    .story-actions { display: flex; gap: 8px; margin-top: 8px; }
    .btn-action {
      flex: 1; text-align: center; padding: 7px 0; border-radius: 6px; font-size: 12px;
      font-weight: 600; text-decoration: none; background: #0f172a;
      border: 1px solid #334155; color: #94a3b8; transition: all 0.15s; cursor: pointer;
    }
    .btn-action:hover { border-color: #6366f1; color: #818cf8; }
    .btn-play { background: #166534; border-color: #15803d; color: #86efac; }
    .btn-play:hover { background: #14532d; border-color: #22c55e; color: #4ade80; }
    .btn-graph { background: #1e1b4b; border-color: #312e81; color: #a5b4fc; }
    .btn-graph:hover { background: #1e1b4b; border-color: #6366f1; color: #818cf8; }
  `]
})
export class StoryListComponent implements OnInit {
  readonly loading = signal(true);
  readonly stories = signal<StorySummary[]>([]);
  readonly showCreate = signal(false);

  newTitle = '';
  newDescription = '';

  constructor(private api: StoryApiService) {}

  ngOnInit(): void {
    this.load();
  }

  private load(): void {
    this.api.listStories().subscribe({
      next: s => { this.stories.set(s); this.loading.set(false); },
      error: () => this.loading.set(false)
    });
  }

  createStory(): void {
    if (!this.newTitle.trim()) return;
    this.api.createStory({ title: this.newTitle.trim(), description: this.newDescription.trim() }).subscribe({
      next: () => {
        this.newTitle = '';
        this.newDescription = '';
        this.showCreate.set(false);
        this.load();
      }
    });
  }
}
