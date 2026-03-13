package com.chronicle.infrastructure.web.rest;

import com.chronicle.application.story.addscene.AddSceneCommand;
import com.chronicle.application.story.addscene.AddSceneResult;
import com.chronicle.application.story.addscene.AddSceneUseCase;
import com.chronicle.application.story.updatescene.UpdateSceneCommand;
import com.chronicle.application.story.updatescene.UpdateSceneUseCase;
import com.chronicle.application.story.deletescene.DeleteSceneUseCase;
import com.chronicle.application.story.createstory.CreateStoryCommand;
import com.chronicle.application.story.createstory.CreateStoryUseCase;
import com.chronicle.application.story.createsession.CreateSessionCommand;
import com.chronicle.application.story.createsession.CreateSessionUseCase;
import com.chronicle.application.story.getstory.GetStoryQuery;
import com.chronicle.application.story.getstory.GetStoryUseCase;
import com.chronicle.application.story.getstorygraph.GetStoryGraphUseCase;
import com.chronicle.application.story.getstorygraph.StoryGraphView;
import com.chronicle.application.story.getsession.GetSessionQuery;
import com.chronicle.application.story.getsession.GetSessionUseCase;
import com.chronicle.application.story.getsession.SessionView;
import com.chronicle.application.story.liststories.ListStoriesUseCase;
import com.chronicle.application.story.liststories.StorySummaryView;
import com.chronicle.domain.story.SessionId;
import com.chronicle.domain.story.StoryId;
import com.chronicle.domain.story.StorySessionRepository;
import com.chronicle.infrastructure.web.rest.dto.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/stories")
public class StoryController {

    private final CreateStoryUseCase createStory;
    private final GetStoryUseCase getStory;
    private final ListStoriesUseCase listStories;
    private final AddSceneUseCase addScene;
    private final CreateSessionUseCase createSession;
    private final GetSessionUseCase getSession;
    private final GetStoryGraphUseCase getStoryGraph;
    private final UpdateSceneUseCase updateScene;
    private final DeleteSceneUseCase deleteScene;
    private final StorySessionRepository sessionRepository;

    public StoryController(CreateStoryUseCase createStory,
                           GetStoryUseCase getStory,
                           ListStoriesUseCase listStories,
                           AddSceneUseCase addScene,
                           CreateSessionUseCase createSession,
                           GetSessionUseCase getSession,
                           GetStoryGraphUseCase getStoryGraph,
                           UpdateSceneUseCase updateScene,
                           DeleteSceneUseCase deleteScene,
                           StorySessionRepository sessionRepository) {
        this.createStory = createStory;
        this.getStory = getStory;
        this.listStories = listStories;
        this.addScene = addScene;
        this.createSession = createSession;
        this.getSession = getSession;
        this.getStoryGraph = getStoryGraph;
        this.updateScene = updateScene;
        this.deleteScene = deleteScene;
        this.sessionRepository = sessionRepository;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StoryResponse create(@RequestBody @Valid CreateStoryRequest request) {
        var command = new CreateStoryCommand(request.title(), request.description());
        var result = createStory.execute(command);
        var view = getStory.execute(new GetStoryQuery(StoryId.of(result.id())));
        return StoryResponse.from(view);
    }

    @GetMapping
    public List<StorySummaryView> list() {
        return listStories.execute();
    }

    @GetMapping("/{id}")
    public StoryResponse getById(@PathVariable String id) {
        var view = getStory.execute(new GetStoryQuery(StoryId.of(id)));
        return StoryResponse.from(view);
    }

    @GetMapping("/{id}/graph")
    public StoryGraphView getGraph(@PathVariable String id) {
        return getStoryGraph.execute(id);
    }

    @PostMapping("/{id}/scenes")
    @ResponseStatus(HttpStatus.CREATED)
    public AddSceneResult addScene(@PathVariable String id,
                                   @RequestBody @Valid AddSceneRequest request) {
        var requirements = request.requirements() == null ? List.<AddSceneCommand.RequirementDto>of() :
                request.requirements().stream()
                        .map(r -> new AddSceneCommand.RequirementDto(r.factKey(), r.type(), r.expectedValue()))
                        .toList();
        var effects = request.effects() == null ? List.<AddSceneCommand.EffectDto>of() :
                request.effects().stream()
                        .map(e -> new AddSceneCommand.EffectDto(e.type(), e.factKey(), e.factValue()))
                        .toList();
        var command = new AddSceneCommand(id, request.title(), request.description(),
                requirements, effects, request.repeatable(),
                request.priority(), request.tags(), request.involvedCharacters());
        return addScene.execute(command);
    }

    @PutMapping("/{id}/scenes/{sceneId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateScene(@PathVariable String id,
                            @PathVariable String sceneId,
                            @RequestBody @Valid AddSceneRequest request) {
        var requirements = request.requirements() == null ? List.<UpdateSceneCommand.RequirementDto>of() :
                request.requirements().stream()
                        .map(r -> new UpdateSceneCommand.RequirementDto(r.factKey(), r.type(), r.expectedValue()))
                        .toList();
        var effects = request.effects() == null ? List.<UpdateSceneCommand.EffectDto>of() :
                request.effects().stream()
                        .map(e -> new UpdateSceneCommand.EffectDto(e.type(), e.factKey(), e.factValue()))
                        .toList();
        updateScene.execute(new UpdateSceneCommand(id, sceneId, request.title(), request.description(),
                requirements, effects, request.repeatable(),
                request.priority(), request.tags(), request.involvedCharacters()));
    }

    @DeleteMapping("/{id}/scenes/{sceneId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteScene(@PathVariable String id, @PathVariable String sceneId) {
        deleteScene.execute(id, sceneId);
    }

    @PostMapping("/{id}/sessions")
    @ResponseStatus(HttpStatus.CREATED)
    public SessionResponse createSession(@PathVariable String id,
                                          @RequestBody CreateSessionRequest request) {
        var result = createSession.execute(new CreateSessionCommand(id, request.name()));
        var view = getSession.execute(new GetSessionQuery(SessionId.of(result.id())));
        return SessionResponse.from(view);
    }

    @GetMapping("/{id}/sessions")
    public List<SessionResponse> listSessions(@PathVariable String id) {
        return sessionRepository.findByStoryId(StoryId.of(id)).stream()
                .map(session -> SessionResponse.from(SessionView.from(session)))
                .toList();
    }
}
