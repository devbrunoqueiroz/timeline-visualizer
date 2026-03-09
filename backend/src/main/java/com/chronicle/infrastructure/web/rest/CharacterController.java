package com.chronicle.infrastructure.web.rest;

import com.chronicle.application.character.addevent.AddCharacterEventCommand;
import com.chronicle.application.character.addevent.AddCharacterEventUseCase;
import com.chronicle.application.character.create.CreateCharacterCommand;
import com.chronicle.application.character.create.CreateCharacterUseCase;
import com.chronicle.application.character.delete.DeleteCharacterCommand;
import com.chronicle.application.character.delete.DeleteCharacterUseCase;
import com.chronicle.application.character.get.GetCharacterQuery;
import com.chronicle.application.character.get.GetCharacterUseCase;
import com.chronicle.application.character.list.ListCharactersQuery;
import com.chronicle.application.character.list.ListCharactersUseCase;
import com.chronicle.application.character.removeevent.RemoveCharacterEventCommand;
import com.chronicle.application.character.removeevent.RemoveCharacterEventUseCase;
import com.chronicle.application.character.update.UpdateCharacterCommand;
import com.chronicle.application.character.update.UpdateCharacterUseCase;
import com.chronicle.application.character.updateevent.UpdateCharacterEventCommand;
import com.chronicle.application.character.updateevent.UpdateCharacterEventUseCase;
import com.chronicle.domain.character.CharacterId;
import com.chronicle.domain.timeline.ContentType;
import com.chronicle.domain.timeline.TemporalPosition;
import com.chronicle.domain.timeline.TimelineId;
import com.chronicle.infrastructure.web.rest.dto.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/timelines/{timelineId}/characters")
public class CharacterController {

    private final CreateCharacterUseCase createCharacter;
    private final GetCharacterUseCase getCharacter;
    private final ListCharactersUseCase listCharacters;
    private final DeleteCharacterUseCase deleteCharacter;
    private final UpdateCharacterUseCase updateCharacter;
    private final AddCharacterEventUseCase addEvent;
    private final UpdateCharacterEventUseCase updateEvent;
    private final RemoveCharacterEventUseCase removeEvent;

    public CharacterController(CreateCharacterUseCase createCharacter,
                                GetCharacterUseCase getCharacter,
                                ListCharactersUseCase listCharacters,
                                DeleteCharacterUseCase deleteCharacter,
                                UpdateCharacterUseCase updateCharacter,
                                AddCharacterEventUseCase addEvent,
                                UpdateCharacterEventUseCase updateEvent,
                                RemoveCharacterEventUseCase removeEvent) {
        this.createCharacter = createCharacter;
        this.getCharacter = getCharacter;
        this.listCharacters = listCharacters;
        this.deleteCharacter = deleteCharacter;
        this.updateCharacter = updateCharacter;
        this.addEvent = addEvent;
        this.updateEvent = updateEvent;
        this.removeEvent = removeEvent;
    }

    @GetMapping
    public List<CharacterSummaryResponse> list(@PathVariable String timelineId) {
        return listCharacters.execute(new ListCharactersQuery(TimelineId.of(timelineId))).stream()
                .map(CharacterSummaryResponse::from)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CharacterResponse create(@PathVariable String timelineId,
                                     @RequestBody @Valid CreateCharacterRequest request) {
        var result = createCharacter.execute(new CreateCharacterCommand(
                timelineId, request.name(), request.description(),
                request.startPosition(), request.endPosition()
        ));
        return CharacterResponse.from(getCharacter.execute(
                new GetCharacterQuery(CharacterId.of(result.id()))
        ));
    }

    @GetMapping("/{characterId}")
    public CharacterResponse get(@PathVariable String characterId) {
        return CharacterResponse.from(getCharacter.execute(
                new GetCharacterQuery(CharacterId.of(characterId))
        ));
    }

    @DeleteMapping("/{characterId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String characterId) {
        deleteCharacter.execute(new DeleteCharacterCommand(characterId));
    }

    @PutMapping("/{characterId}")
    public CharacterResponse update(@PathVariable String characterId,
                                     @RequestBody @Valid UpdateCharacterRequest request) {
        var view = updateCharacter.execute(new UpdateCharacterCommand(
                characterId, request.name(), request.description(),
                request.startPosition(), request.endPosition()
        ));
        return CharacterResponse.from(view);
    }

    @PostMapping("/{characterId}/events")
    @ResponseStatus(HttpStatus.CREATED)
    public CharacterEventResponse addEvent(@PathVariable String characterId,
                                            @RequestBody @Valid CreateCharacterEventRequest request) {
        var contentType = request.contentType() != null ? ContentType.valueOf(request.contentType()) : ContentType.TEXT;
        var calendarSystem = request.calendarSystem() != null ? request.calendarSystem() : TemporalPosition.CUSTOM;
        var temporal = new TemporalPosition(request.temporalPosition(), request.temporalLabel(), calendarSystem);
        var result = addEvent.execute(new AddCharacterEventCommand(
                characterId, request.title(),
                request.contentText() != null ? request.contentText() : "",
                contentType, temporal
        ));
        return new CharacterEventResponse(result.id(), result.title(), result.contentText(),
                result.contentType(), result.temporalPosition().position(),
                result.temporalPosition().label(), result.temporalPosition().calendarSystem(),
                result.displayOrder());
    }

    @PutMapping("/{characterId}/events/{eventId}")
    public CharacterEventResponse updateEvent(@PathVariable String characterId,
                                               @PathVariable String eventId,
                                               @RequestBody @Valid UpdateCharacterEventRequest request) {
        var contentType = request.contentType() != null ? ContentType.valueOf(request.contentType()) : ContentType.TEXT;
        var calendarSystem = request.calendarSystem() != null ? request.calendarSystem() : TemporalPosition.CUSTOM;
        var temporal = new TemporalPosition(request.temporalPosition(), request.temporalLabel(), calendarSystem);
        var result = updateEvent.execute(new UpdateCharacterEventCommand(
                characterId, eventId, request.title(),
                request.contentText() != null ? request.contentText() : "",
                contentType, temporal
        ));
        return new CharacterEventResponse(result.id(), result.title(), result.contentText(),
                result.contentType(), result.temporalPosition().position(),
                result.temporalPosition().label(), result.temporalPosition().calendarSystem(),
                result.displayOrder());
    }

    @DeleteMapping("/{characterId}/events/{eventId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeEvent(@PathVariable String characterId, @PathVariable String eventId) {
        removeEvent.execute(new RemoveCharacterEventCommand(characterId, eventId));
    }
}
