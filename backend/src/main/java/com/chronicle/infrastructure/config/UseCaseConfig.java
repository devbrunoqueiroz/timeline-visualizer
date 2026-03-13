package com.chronicle.infrastructure.config;

import com.chronicle.application.auth.PasswordHasher;
import com.chronicle.application.story.addscene.AddSceneUseCase;
import com.chronicle.application.story.applyscene.ApplySceneUseCase;
import com.chronicle.application.story.createstory.CreateStoryUseCase;
import com.chronicle.application.story.createsession.CreateSessionUseCase;
import com.chronicle.application.story.getavailablescenes.GetAvailableScenesUseCase;
import com.chronicle.application.story.getstory.GetStoryUseCase;
import com.chronicle.application.story.getstorygraph.GetStoryGraphUseCase;
import com.chronicle.application.story.getsession.GetSessionUseCase;
import com.chronicle.application.story.liststories.ListStoriesUseCase;
import com.chronicle.application.story.updatescene.UpdateSceneUseCase;
import com.chronicle.application.story.deletescene.DeleteSceneUseCase;
import com.chronicle.application.story.advancestory.AdvanceStoryUseCase;
import com.chronicle.application.story.getconflicts.GetConflictsUseCase;
import com.chronicle.application.story.simulatefutures.SimulateFuturesUseCase;
import com.chronicle.application.story.getnarrativetimeline.GetNarrativeTimelineUseCase;
import com.chronicle.domain.story.StoryEngine;
import com.chronicle.domain.story.StoryRepository;
import com.chronicle.domain.story.StorySessionRepository;
import com.chronicle.infrastructure.persistence.jpa.StoryMapper;
import com.chronicle.application.character.addevent.AddCharacterEventUseCase;
import com.chronicle.application.character.create.CreateCharacterUseCase;
import com.chronicle.application.character.delete.DeleteCharacterUseCase;
import com.chronicle.application.character.get.GetCharacterUseCase;
import com.chronicle.application.character.list.ListCharactersUseCase;
import com.chronicle.application.character.removeevent.RemoveCharacterEventUseCase;
import com.chronicle.application.character.update.UpdateCharacterUseCase;
import com.chronicle.application.character.updateevent.UpdateCharacterEventUseCase;
import com.chronicle.domain.character.CharacterRepository;
import com.chronicle.application.auth.login.LoginUseCase;
import com.chronicle.application.auth.register.RegisterUserUseCase;
import com.chronicle.application.connection.createconnection.CreateConnectionUseCase;
import com.chronicle.application.connection.deleteconnection.DeleteConnectionUseCase;
import com.chronicle.application.connection.listconnections.ListConnectionsUseCase;
import com.chronicle.application.graph.findstorypath.FindStoryPathUseCase;
import com.chronicle.application.graph.geteventgraph.GetEventGraphUseCase;
import com.chronicle.application.narrative.NarrativeValidationService;
import com.chronicle.domain.graph.GraphTraversalService;
import com.chronicle.application.shared.DomainEventPublisher;
import com.chronicle.application.timeline.addeventtotimeline.AddEventToTimelineUseCase;
import com.chronicle.application.timeline.createtimeline.CreateTimelineUseCase;
import com.chronicle.application.timeline.deletetimeline.DeleteTimelineUseCase;
import com.chronicle.application.timeline.gettimeline.GetTimelineUseCase;
import com.chronicle.application.timeline.listtimelines.ListTimelinesUseCase;
import com.chronicle.application.timeline.removeevent.RemoveEventUseCase;
import com.chronicle.application.timeline.updateevent.UpdateEventUseCase;
import com.chronicle.application.timeline.updatetimeline.UpdateTimelineUseCase;
import com.chronicle.domain.connection.ConnectionRepository;
import com.chronicle.domain.timeline.TimelineRepository;
import com.chronicle.domain.user.UserRepository;
import com.chronicle.infrastructure.persistence.jpa.TimelineMapper;
import com.chronicle.infrastructure.security.BcryptPasswordHasher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

    @Bean
    public TimelineMapper timelineMapper() {
        return new TimelineMapper();
    }

    @Bean
    public PasswordHasher passwordHasher() {
        return new BcryptPasswordHasher();
    }

    @Bean
    public RegisterUserUseCase registerUserUseCase(UserRepository userRepository, PasswordHasher passwordHasher) {
        return new RegisterUserUseCase(userRepository, passwordHasher);
    }

    @Bean
    public LoginUseCase loginUseCase(UserRepository userRepository, PasswordHasher passwordHasher) {
        return new LoginUseCase(userRepository, passwordHasher);
    }

    @Bean
    public NarrativeValidationService narrativeValidationService() {
        return new NarrativeValidationService();
    }

    @Bean
    public CreateTimelineUseCase createTimelineUseCase(TimelineRepository repo, DomainEventPublisher publisher) {
        return new CreateTimelineUseCase(repo, publisher);
    }

    @Bean
    public GetTimelineUseCase getTimelineUseCase(TimelineRepository repo) {
        return new GetTimelineUseCase(repo);
    }

    @Bean
    public ListTimelinesUseCase listTimelinesUseCase(TimelineRepository repo) {
        return new ListTimelinesUseCase(repo);
    }

    @Bean
    public UpdateTimelineUseCase updateTimelineUseCase(TimelineRepository repo) {
        return new UpdateTimelineUseCase(repo);
    }

    @Bean
    public DeleteTimelineUseCase deleteTimelineUseCase(TimelineRepository repo) {
        return new DeleteTimelineUseCase(repo);
    }

    @Bean
    public AddEventToTimelineUseCase addEventToTimelineUseCase(TimelineRepository repo, DomainEventPublisher publisher) {
        return new AddEventToTimelineUseCase(repo, publisher);
    }

    @Bean
    public UpdateEventUseCase updateEventUseCase(TimelineRepository repo) {
        return new UpdateEventUseCase(repo);
    }

    @Bean
    public RemoveEventUseCase removeEventUseCase(TimelineRepository repo) {
        return new RemoveEventUseCase(repo);
    }

    @Bean
    public CreateConnectionUseCase createConnectionUseCase(ConnectionRepository connectionRepo,
                                                            TimelineRepository timelineRepo,
                                                            NarrativeValidationService validationService) {
        return new CreateConnectionUseCase(connectionRepo, timelineRepo, validationService);
    }

    @Bean
    public DeleteConnectionUseCase deleteConnectionUseCase(ConnectionRepository repo) {
        return new DeleteConnectionUseCase(repo);
    }

    @Bean
    public ListConnectionsUseCase listConnectionsUseCase(ConnectionRepository repo) {
        return new ListConnectionsUseCase(repo);
    }

    @Bean
    public GetEventGraphUseCase getEventGraphUseCase(TimelineRepository timelineRepo,
                                                      ConnectionRepository connectionRepo,
                                                      NarrativeValidationService validationService) {
        return new GetEventGraphUseCase(timelineRepo, connectionRepo, validationService);
    }

    @Bean
    public GraphTraversalService graphTraversalService() {
        return new GraphTraversalService();
    }

    @Bean
    public FindStoryPathUseCase findStoryPathUseCase(TimelineRepository timelineRepo,
                                                      ConnectionRepository connectionRepo,
                                                      GraphTraversalService graphTraversalService) {
        return new FindStoryPathUseCase(timelineRepo, connectionRepo, graphTraversalService);
    }

    @Bean
    public CreateCharacterUseCase createCharacterUseCase(CharacterRepository characterRepo,
                                                          TimelineRepository timelineRepo) {
        return new CreateCharacterUseCase(characterRepo, timelineRepo);
    }

    @Bean
    public GetCharacterUseCase getCharacterUseCase(CharacterRepository characterRepo, TimelineRepository timelineRepo) {
        return new GetCharacterUseCase(characterRepo, timelineRepo);
    }

    @Bean
    public ListCharactersUseCase listCharactersUseCase(CharacterRepository characterRepo) {
        return new ListCharactersUseCase(characterRepo);
    }

    @Bean
    public DeleteCharacterUseCase deleteCharacterUseCase(CharacterRepository characterRepo, TimelineRepository timelineRepo) {
        return new DeleteCharacterUseCase(characterRepo, timelineRepo);
    }

    @Bean
    public UpdateCharacterUseCase updateCharacterUseCase(CharacterRepository characterRepo, TimelineRepository timelineRepo) {
        return new UpdateCharacterUseCase(characterRepo, timelineRepo);
    }

    @Bean
    public AddCharacterEventUseCase addCharacterEventUseCase(CharacterRepository characterRepo, TimelineRepository timelineRepo) {
        return new AddCharacterEventUseCase(characterRepo, timelineRepo);
    }

    @Bean
    public UpdateCharacterEventUseCase updateCharacterEventUseCase(CharacterRepository characterRepo, TimelineRepository timelineRepo) {
        return new UpdateCharacterEventUseCase(characterRepo, timelineRepo);
    }

    @Bean
    public RemoveCharacterEventUseCase removeCharacterEventUseCase(CharacterRepository characterRepo, TimelineRepository timelineRepo) {
        return new RemoveCharacterEventUseCase(characterRepo, timelineRepo);
    }

    @Bean
    public StoryMapper storyMapper() {
        return new StoryMapper();
    }

    @Bean
    public StoryEngine storyEngine() {
        return new StoryEngine();
    }

    @Bean
    public CreateStoryUseCase createStoryUseCase(StoryRepository storyRepository) {
        return new CreateStoryUseCase(storyRepository);
    }

    @Bean
    public AddSceneUseCase addSceneUseCase(StoryRepository storyRepository) {
        return new AddSceneUseCase(storyRepository);
    }

    @Bean
    public GetStoryUseCase getStoryUseCase(StoryRepository storyRepository) {
        return new GetStoryUseCase(storyRepository);
    }

    @Bean
    public ListStoriesUseCase listStoriesUseCase(StoryRepository storyRepository) {
        return new ListStoriesUseCase(storyRepository);
    }

    @Bean
    public CreateSessionUseCase createSessionUseCase(StoryRepository storyRepository,
                                                      StorySessionRepository sessionRepository) {
        return new CreateSessionUseCase(storyRepository, sessionRepository);
    }

    @Bean
    public GetSessionUseCase getSessionUseCase(StorySessionRepository sessionRepository) {
        return new GetSessionUseCase(sessionRepository);
    }

    @Bean
    public GetAvailableScenesUseCase getAvailableScenesUseCase(StorySessionRepository sessionRepository,
                                                                StoryRepository storyRepository,
                                                                StoryEngine storyEngine) {
        return new GetAvailableScenesUseCase(sessionRepository, storyRepository, storyEngine);
    }

    @Bean
    public ApplySceneUseCase applySceneUseCase(StorySessionRepository sessionRepository,
                                                StoryRepository storyRepository) {
        return new ApplySceneUseCase(sessionRepository, storyRepository);
    }

    @Bean
    public GetStoryGraphUseCase getStoryGraphUseCase(StoryRepository storyRepository) {
        return new GetStoryGraphUseCase(storyRepository);
    }

    @Bean
    public UpdateSceneUseCase updateSceneUseCase(StoryRepository storyRepository) {
        return new UpdateSceneUseCase(storyRepository);
    }

    @Bean
    public DeleteSceneUseCase deleteSceneUseCase(StoryRepository storyRepository) {
        return new DeleteSceneUseCase(storyRepository);
    }

    @Bean
    public AdvanceStoryUseCase advanceStoryUseCase(StorySessionRepository sessionRepository,
                                                    StoryRepository storyRepository,
                                                    StoryEngine storyEngine) {
        return new AdvanceStoryUseCase(sessionRepository, storyRepository, storyEngine);
    }

    @Bean
    public GetConflictsUseCase getConflictsUseCase(StorySessionRepository sessionRepository,
                                                    StoryRepository storyRepository) {
        return new GetConflictsUseCase(sessionRepository, storyRepository);
    }

    @Bean
    public SimulateFuturesUseCase simulateFuturesUseCase(StorySessionRepository sessionRepository,
                                                          StoryRepository storyRepository,
                                                          StoryEngine storyEngine) {
        return new SimulateFuturesUseCase(sessionRepository, storyRepository, storyEngine);
    }

    @Bean
    public GetNarrativeTimelineUseCase getNarrativeTimelineUseCase(StorySessionRepository sessionRepository,
                                                                    StoryRepository storyRepository) {
        return new GetNarrativeTimelineUseCase(sessionRepository, storyRepository);
    }
}
