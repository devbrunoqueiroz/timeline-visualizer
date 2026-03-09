-- Add linked_timeline_id to characters: each character owns a sub-timeline
-- where its events are stored as regular timeline events (connectable to any other events)
ALTER TABLE characters ADD COLUMN linked_timeline_id UUID REFERENCES timelines(id) ON DELETE SET NULL;

-- Create linked timelines for all existing characters and migrate their events
DO $$
DECLARE
    char_record RECORD;
    new_timeline_id UUID;
BEGIN
    FOR char_record IN SELECT * FROM characters WHERE linked_timeline_id IS NULL LOOP
        new_timeline_id := gen_random_uuid();

        INSERT INTO timelines (id, name, description, visibility, owner_id, created_at, updated_at)
        VALUES (new_timeline_id, char_record.name, char_record.description, 'PRIVATE', NULL, NOW(), NOW());

        UPDATE characters SET linked_timeline_id = new_timeline_id WHERE id = char_record.id;

        -- Migrate existing character_events into timeline_events
        INSERT INTO timeline_events (id, timeline_id, title, content_text, content_type,
                                     temporal_position, temporal_label, calendar_system, display_order)
        SELECT gen_random_uuid(), new_timeline_id, title, content_text, content_type,
               temporal_position, temporal_label, calendar_system, display_order
        FROM character_events
        WHERE character_id = char_record.id;
    END LOOP;
END $$;
