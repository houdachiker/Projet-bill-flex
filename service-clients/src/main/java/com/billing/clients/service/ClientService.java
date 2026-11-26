package com.billing.clients.service;

import com.billing.clients.dto.ClientRequest;
import com.billing.clients.dto.ClientDTO;
import com.billing.clients.exception.ResourceNotFoundException;
import com.billing.clients.model.Client;
import com.billing.clients.repository.ClientRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
public class ClientService {
    private final ClientRepository repository;

    public ClientService(ClientRepository repository) { this.repository = repository; }

    public List<ClientDTO> findAll() { return repository.findAll().stream().map(this::toDTO).toList(); }

    public ClientDTO findById(@Positive Long id) {
        return toDTO(findEntityById(id));
    }

    private Client findEntityById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client introuvable : " + id));
    }

    /** Mapping DTO (ClientRequest) -> entite. */
    public ClientDTO create(@Valid ClientRequest r) {
        Client c = new Client();
        apply(c, r);
        return toDTO(repository.save(c));
    }

    public ClientDTO update(@Positive Long id, @Valid ClientRequest r) {
        Client c = findEntityById(id);
        apply(c, r);
        return toDTO(repository.save(c));
    }

    public void delete(@Positive Long id) {
        repository.delete(findEntityById(id));
    }

    private ClientDTO toDTO(Client client) {
        return new ClientDTO(client.getId(), client.getNom(), client.getEmail(), client.getNumeroTva(),
                client.getModePaiement());
    }

    private void apply(Client c, ClientRequest r) {
        c.setNom(r.nom());
        c.setEmail(r.email());
        c.setNumeroTva(r.numeroTva());
        c.setModePaiement(r.modePaiement());
    }
}
