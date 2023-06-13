package ge.elzhart.integration.data;

import org.springframework.stereotype.Service;

import java.util.Set;

import ge.elzhart.api.dto.owner.OwnerDto;
import ge.elzhart.service.owner.OwnerService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OwnerTestDataFactory {

    private final OwnerService ownerService;

    public OwnerDto createOwner(String username) {
        return ownerService.create(username);
    }

    public void deleteAll(Set<String> owners) {
        ownerService.deleteAll(owners);
    }
}
