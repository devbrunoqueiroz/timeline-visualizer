package com.chronicle.infrastructure.web.rest;

import com.chronicle.application.connection.createconnection.CreateConnectionCommand;
import com.chronicle.application.connection.createconnection.CreateConnectionUseCase;
import com.chronicle.application.connection.deleteconnection.DeleteConnectionCommand;
import com.chronicle.application.connection.deleteconnection.DeleteConnectionUseCase;
import com.chronicle.application.connection.listconnections.ListConnectionsQuery;
import com.chronicle.application.connection.listconnections.ListConnectionsUseCase;
import com.chronicle.infrastructure.web.rest.dto.ConnectionResponse;
import com.chronicle.infrastructure.web.rest.dto.CreateConnectionRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/connections")
public class ConnectionController {

    private final CreateConnectionUseCase createConnection;
    private final DeleteConnectionUseCase deleteConnection;
    private final ListConnectionsUseCase listConnections;

    public ConnectionController(CreateConnectionUseCase createConnection,
                                 DeleteConnectionUseCase deleteConnection,
                                 ListConnectionsUseCase listConnections) {
        this.createConnection = createConnection;
        this.deleteConnection = deleteConnection;
        this.listConnections = listConnections;
    }

    @GetMapping
    public List<ConnectionResponse> listByEventIds(@RequestParam Set<String> eventIds) {
        return listConnections.execute(new ListConnectionsQuery(eventIds)).stream()
                .map(ConnectionResponse::from)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ConnectionResponse create(@RequestBody @Valid CreateConnectionRequest request) {
        var command = new CreateConnectionCommand(request.sourceEventId(), request.targetEventId(),
                request.description(), request.connectionType());
        var result = createConnection.execute(command);
        return ConnectionResponse.from(result);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        deleteConnection.execute(new DeleteConnectionCommand(id));
    }
}
