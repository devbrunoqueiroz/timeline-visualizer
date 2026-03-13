package com.chronicle.infrastructure.web.rest;

import com.chronicle.application.story.advancestory.AdvanceStoryCommand;
import com.chronicle.application.story.advancestory.AdvanceStoryResult;
import com.chronicle.application.story.advancestory.AdvanceStoryUseCase;
import com.chronicle.application.story.applyscene.ApplySceneCommand;
import com.chronicle.application.story.applyscene.ApplySceneResult;
import com.chronicle.application.story.applyscene.ApplySceneUseCase;
import com.chronicle.application.story.getavailablescenes.GetAvailableScenesQuery;
import com.chronicle.application.story.getavailablescenes.GetAvailableScenesUseCase;
import com.chronicle.application.story.getconflicts.ConflictView;
import com.chronicle.application.story.getconflicts.GetConflictsQuery;
import com.chronicle.application.story.getconflicts.GetConflictsUseCase;
import com.chronicle.application.story.getnarrativetimeline.GetNarrativeTimelineUseCase;
import com.chronicle.application.story.getnarrativetimeline.NarrativeTimelineView;
import com.chronicle.application.story.getsession.GetSessionQuery;
import com.chronicle.application.story.getsession.GetSessionUseCase;
import com.chronicle.application.story.simulatefutures.SimulateFuturesQuery;
import com.chronicle.application.story.simulatefutures.SimulateFuturesUseCase;
import com.chronicle.application.story.simulatefutures.SimulationView;
import com.chronicle.domain.story.SelectionStrategy;
import com.chronicle.domain.story.SessionId;
import com.chronicle.infrastructure.web.rest.dto.AvailableScenesResponse;
import com.chronicle.infrastructure.web.rest.dto.SessionResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sessions")
public class StorySessionController {

    private final GetSessionUseCase getSession;
    private final GetAvailableScenesUseCase getAvailableScenes;
    private final ApplySceneUseCase applyScene;
    private final AdvanceStoryUseCase advanceStory;
    private final SimulateFuturesUseCase simulateFutures;
    private final GetConflictsUseCase getConflicts;
    private final GetNarrativeTimelineUseCase getNarrativeTimeline;

    public StorySessionController(GetSessionUseCase getSession,
                                   GetAvailableScenesUseCase getAvailableScenes,
                                   ApplySceneUseCase applyScene,
                                   AdvanceStoryUseCase advanceStory,
                                   SimulateFuturesUseCase simulateFutures,
                                   GetConflictsUseCase getConflicts,
                                   GetNarrativeTimelineUseCase getNarrativeTimeline) {
        this.getSession = getSession;
        this.getAvailableScenes = getAvailableScenes;
        this.applyScene = applyScene;
        this.advanceStory = advanceStory;
        this.simulateFutures = simulateFutures;
        this.getConflicts = getConflicts;
        this.getNarrativeTimeline = getNarrativeTimeline;
    }

    @GetMapping("/{id}")
    public SessionResponse getById(@PathVariable String id) {
        var view = getSession.execute(new GetSessionQuery(SessionId.of(id)));
        return SessionResponse.from(view);
    }

    @GetMapping("/{id}/available-scenes")
    public AvailableScenesResponse getAvailableScenes(@PathVariable String id) {
        var scenes = getAvailableScenes.execute(new GetAvailableScenesQuery(SessionId.of(id)));
        return AvailableScenesResponse.from(scenes);
    }

    @PostMapping("/{id}/apply/{sceneId}")
    public ApplySceneResult applyScene(@PathVariable String id, @PathVariable String sceneId) {
        return applyScene.execute(new ApplySceneCommand(id, sceneId));
    }

    @PostMapping("/{id}/advance")
    public AdvanceStoryResult advance(@PathVariable String id,
                                      @RequestBody AdvanceRequest request) {
        var strategy = request.strategy() != null ? request.strategy() : SelectionStrategy.HIGHEST_PRIORITY;
        return advanceStory.execute(new AdvanceStoryCommand(id, strategy));
    }

    @GetMapping("/{id}/simulate")
    public List<SimulationView> simulate(@PathVariable String id,
                                          @RequestParam(defaultValue = "3") int depth) {
        return simulateFutures.execute(new SimulateFuturesQuery(id, depth));
    }

    @GetMapping("/{id}/conflicts/{sceneId}")
    public List<ConflictView> conflicts(@PathVariable String id, @PathVariable String sceneId) {
        return getConflicts.execute(new GetConflictsQuery(id, sceneId));
    }

    @GetMapping("/{id}/timeline")
    public NarrativeTimelineView timeline(@PathVariable String id) {
        return getNarrativeTimeline.execute(id);
    }

    public record AdvanceRequest(SelectionStrategy strategy) {}
}
