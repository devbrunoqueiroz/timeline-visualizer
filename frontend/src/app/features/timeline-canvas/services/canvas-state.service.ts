import { Injectable, computed, signal } from '@angular/core';
import { CanvasPosition, Character, Timeline, TimelineConnection, TimelineEvent } from '../../../domain/timeline.model';

export interface EventPosition {
  x: number;
  y: number;
  w: number;
  h: number;
}

export interface PendingConnection {
  source: TimelineEvent;
  target: TimelineEvent;
}

@Injectable()
export class CanvasStateService {

  private readonly _timelines = signal<Timeline[]>([]);
  private readonly _characters = signal<Character[]>([]);
  private readonly _connections = signal<TimelineConnection[]>([]);
  private readonly _selectedEvent = signal<TimelineEvent | null>(null);
  private readonly _zoom = signal<number>(1);
  private readonly _offset = signal<CanvasPosition>({ x: 0, y: 0 });
  private readonly _isPanning = signal<boolean>(false);
  private readonly _connectingMode = signal<boolean>(false);
  private readonly _pendingSource = signal<TimelineEvent | null>(null);
  private readonly _pendingConnection = signal<PendingConnection | null>(null);
  private readonly _eventPositions = signal<Map<string, EventPosition>>(new Map());

  readonly timelines = this._timelines.asReadonly();
  readonly characters = this._characters.asReadonly();
  readonly connections = this._connections.asReadonly();
  readonly selectedEvent = this._selectedEvent.asReadonly();
  readonly zoom = this._zoom.asReadonly();
  readonly offset = this._offset.asReadonly();
  readonly isPanning = this._isPanning.asReadonly();
  readonly connectingMode = this._connectingMode.asReadonly();
  readonly pendingSource = this._pendingSource.asReadonly();
  readonly pendingConnection = this._pendingConnection.asReadonly();
  readonly eventPositions = this._eventPositions.asReadonly();

  readonly visibleTimelines = computed(() =>
    this._timelines().filter(t => t.events.length >= 0)
  );

  readonly canvasTransform = computed(() => {
    const zoom = this._zoom();
    const offset = this._offset();
    return `translate(${offset.x}px, ${offset.y}px) scale(${zoom})`;
  });

  setTimelines(timelines: Timeline[]): void {
    this._timelines.set(timelines);
  }

  setCharacters(characters: Character[]): void {
    this._characters.set(characters);
  }

  addCharacter(character: Character): void {
    this._characters.update(chars => [...chars, character]);
  }

  removeCharacter(characterId: string): void {
    this._characters.update(chars => chars.filter(c => c.id !== characterId));
  }

  updateCharacter(character: Character): void {
    this._characters.update(chars =>
      chars.map(c => c.id === character.id ? character : c)
    );
  }

  addTimeline(timeline: Timeline): void {
    this._timelines.update(timelines => [...timelines, timeline]);
  }

  updateTimeline(timeline: Timeline): void {
    this._timelines.update(timelines =>
      timelines.map(t => t.id === timeline.id ? timeline : t)
    );
  }

  removeTimeline(timelineId: string): void {
    this._timelines.update(timelines => timelines.filter(t => t.id !== timelineId));
  }

  setConnections(connections: TimelineConnection[]): void {
    this._connections.set(connections);
  }

  addConnection(connection: TimelineConnection): void {
    this._connections.update(connections => [...connections, connection]);
  }

  removeConnection(connectionId: string): void {
    this._connections.update(connections => connections.filter(c => c.id !== connectionId));
  }

  selectEvent(event: TimelineEvent | null): void {
    this._selectedEvent.set(event);
  }

  setZoom(zoom: number): void {
    const clampedZoom = Math.min(Math.max(zoom, 0.1), 5);
    this._zoom.set(clampedZoom);
  }

  zoomIn(): void {
    this.setZoom(this._zoom() + 0.1);
  }

  zoomOut(): void {
    this.setZoom(this._zoom() - 0.1);
  }

  resetZoom(): void {
    this._zoom.set(1);
    this._offset.set({ x: 0, y: 0 });
  }

  pan(delta: CanvasPosition): void {
    this._offset.update(offset => ({
      x: offset.x + delta.x,
      y: offset.y + delta.y
    }));
  }

  setIsPanning(panning: boolean): void {
    this._isPanning.set(panning);
  }

  toggleConnectingMode(): void {
    const next = !this._connectingMode();
    this._connectingMode.set(next);
    if (!next) {
      this._pendingSource.set(null);
      this._pendingConnection.set(null);
    }
  }

  handleEventClickInConnectMode(event: TimelineEvent): void {
    const source = this._pendingSource();
    if (!source) {
      this._pendingSource.set(event);
    } else if (source.id !== event.id) {
      this._pendingConnection.set({ source, target: event });
    }
  }

  cancelPendingConnection(): void {
    this._pendingSource.set(null);
    this._pendingConnection.set(null);
  }

  confirmConnectionCreated(connection: TimelineConnection): void {
    this._connections.update(cs => [...cs, connection]);
    this._pendingSource.set(null);
    this._pendingConnection.set(null);
    this._connectingMode.set(false);
  }

  setEventPositions(positions: Map<string, EventPosition>): void {
    this._eventPositions.set(positions);
  }
}
