package com.chronicle.infrastructure.config;

import com.chronicle.application.auth.PasswordHasher;
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
import com.chronicle.application.graph.geteventgraph.GetEventGraphUseCase;
import com.chronicle.application.narrative.NarrativeValidationService;
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
}
