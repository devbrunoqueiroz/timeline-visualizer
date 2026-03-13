CREATE TABLE stories (
    id UUID PRIMARY KEY,
    title VARCHAR(300) NOT NULL,
    description TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);
