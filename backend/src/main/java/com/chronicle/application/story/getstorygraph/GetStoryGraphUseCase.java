package com.chronicle.application.story.getstorygraph;

import com.chronicle.domain.story.RequirementType;
import com.chronicle.domain.story.Scene;
import com.chronicle.domain.story.StoryId;
import com.chronicle.domain.story.StoryNotFoundException;
import com.chronicle.domain.story.StoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class GetStoryGraphUseCase {

    private final StoryRepository storyRepository;

    public GetStoryGraphUseCase(StoryRepository storyRepository) {
        this.storyRepository = storyRepository;
    }

    public StoryGraphView execute(String storyIdStr) {
        var storyId = StoryId.of(storyIdStr);
        var story = storyRepository.findById(storyId)
                .orElseThrow(() -> new StoryNotFoundException(storyId));

        var nodes = new ArrayList<StoryGraphNodeView>();
        var edges = new ArrayList<StoryGraphEdgeView>();
        var factKeys = new HashSet<String>();

        // Collect all fact keys
        for (var scene : story.getScenes()) {
            scene.getRequirements().forEach(r -> factKeys.add(r.factKey()));
            scene.getEffects().forEach(e -> factKeys.add(e.factKey()));
        }

        // Add scene nodes
        for (var scene : story.getScenes()) {
            nodes.add(new StoryGraphNodeView(scene.getId().toString(), scene.getTitle(), "SCENE"));
        }

        // Add fact nodes
        for (var factKey : factKeys) {
            var factNodeId = "fact:" + factKey;
            nodes.add(new StoryGraphNodeView(factNodeId, factKey, "FACT"));
        }

        // Add requirement edges (fact → scene) and effect edges (scene → fact)
        for (var scene : story.getScenes()) {
            var sceneId = scene.getId().toString();

            for (var req : scene.getRequirements()) {
                var factNodeId = "fact:" + req.factKey();
                edges.add(new StoryGraphEdgeView(
                        UUID.randomUUID().toString(), factNodeId, sceneId, "REQUIREMENT"));
            }

            for (var eff : scene.getEffects()) {
                var factNodeId = "fact:" + eff.factKey();
                edges.add(new StoryGraphEdgeView(
                        UUID.randomUUID().toString(), sceneId, factNodeId, "EFFECT"));
            }
        }

        // Add transition edges (scene A → scene B when A produces a fact that B requires)
        var scenes = story.getScenes();
        for (var sceneA : scenes) {
            var producedFacts = new HashSet<String>();
            sceneA.getEffects().forEach(e -> producedFacts.add(e.factKey()));

            for (var sceneB : scenes) {
                if (sceneA.getId().equals(sceneB.getId())) continue;
                boolean hasTransition = sceneB.getRequirements().stream()
                        .filter(r -> r.type() == RequirementType.FACT_EXISTS || r.type() == RequirementType.FACT_EQUALS)
                        .anyMatch(r -> producedFacts.contains(r.factKey()));
                if (hasTransition) {
                    edges.add(new StoryGraphEdgeView(
                            UUID.randomUUID().toString(),
                            sceneA.getId().toString(),
                            sceneB.getId().toString(),
                            "TRANSITION"));
                }
            }
        }

        return new StoryGraphView(nodes, edges);
    }
}
