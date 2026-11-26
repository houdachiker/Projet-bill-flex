package com.billing.clients.controller;

import com.billing.clients.dto.ClientRequest;
import com.billing.clients.dto.ClientDTO;
import com.billing.clients.service.ClientService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/clients")
public class ClientController {
    private final ClientService service;

    public ClientController(ClientService service) { this.service = service; }

    @GetMapping
    public List<ClientDTO> findAll() { return service.findAll(); }

    @GetMapping("/{id}")
    public ClientDTO findById(@PathVariable Long id) { return service.findById(id); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClientDTO create(@Valid @RequestBody ClientRequest request) { return service.create(request); }

    @PutMapping("/{id}")
    public ClientDTO update(@PathVariable Long id, @Valid @RequestBody ClientRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) { service.delete(id); }
}
