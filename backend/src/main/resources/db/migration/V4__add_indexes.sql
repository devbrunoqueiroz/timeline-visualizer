CREATE INDEX idx_timelines_visibility ON timelines(visibility);
CREATE INDEX idx_timelines_name ON timelines(name);
CREATE INDEX idx_timeline_events_timeline_id ON timeline_events(timeline_id);
CREATE INDEX idx_timeline_events_occurred_at ON timeline_events(occurred_at);
CREATE INDEX idx_connections_source_event ON timeline_connections(source_event_id);
CREATE INDEX idx_connections_target_event ON timeline_connections(target_event_id);
