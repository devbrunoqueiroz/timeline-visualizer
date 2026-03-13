package com.chronicle.infrastructure.web.exception;

import com.chronicle.application.shared.ApplicationException;
import com.chronicle.domain.character.CharacterNotFoundException;
import com.chronicle.domain.connection.ConnectionNotFoundException;
import com.chronicle.domain.shared.DomainException;
import com.chronicle.domain.story.SceneNotFoundException;
import com.chronicle.domain.story.StoryNotFoundException;
import com.chronicle.domain.story.StorySessionNotFoundException;
import com.chronicle.domain.timeline.TimelineNotFoundException;
import com.chronicle.domain.user.UserAlreadyExistsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyExists(UserAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.of("USER_ALREADY_EXISTS", ex.getMessage()));
    }

    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ErrorResponse> handleApplication(ApplicationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of("APPLICATION_ERROR", ex.getMessage()));
    }

    @ExceptionHandler(CharacterNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCharacterNotFound(CharacterNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of("CHARACTER_NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(TimelineNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleTimelineNotFound(TimelineNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of("TIMELINE_NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(ConnectionNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleConnectionNotFound(ConnectionNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of("CONNECTION_NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(StoryNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleStoryNotFound(StoryNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of("STORY_NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(SceneNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleSceneNotFound(SceneNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of("SCENE_NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(StorySessionNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleStorySessionNotFound(StorySessionNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of("SESSION_NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ErrorResponse> handleDomain(DomainException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of("DOMAIN_ERROR", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        var message = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of("VALIDATION_ERROR", message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of("INTERNAL_ERROR", "An unexpected error occurred"));
    }
}
