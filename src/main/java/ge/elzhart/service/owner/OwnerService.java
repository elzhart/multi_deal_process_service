package ge.elzhart.service.owner;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import ge.elzhart.api.dto.owner.OwnerDto;
import ge.elzhart.api.dto.transaction.TransactionGraphDto;
import ge.elzhart.api.mapper.OwnerMapper;
import ge.elzhart.exception.AlreadyExistException;
import ge.elzhart.exception.NotFoundException;
import ge.elzhart.model.domain.Order;
import ge.elzhart.model.domain.OrderStatus;
import ge.elzhart.model.domain.Owner;
import ge.elzhart.model.repository.OwnerRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OwnerService {
    private final OwnerRepository ownerRepository;
    private final OwnerMapper ownerMapper;

    public OwnerDto create(String ownerName) {
        ownerRepository.findByName(ownerName).ifPresent(owner -> {
            throw new AlreadyExistException(Owner.class, owner.getName());
        });
        Owner saved = ownerRepository.save(new Owner().withName(ownerName).withCreatedDate(LocalDateTime.now()));
        return ownerMapper.toDto(saved);
    }

    public Owner findByName(String ownerName) {
        return ownerRepository.findByName(ownerName).orElseThrow(() -> new NotFoundException(Owner.class, ownerName));
    }

    public Owner save(Owner owner) {
        return ownerRepository.save(owner);
    }

    public void transactionOrders(List<TransactionGraphDto> transactionGraph) {
        Map<String, TransactionGraphDto> ownerNames = transactionGraph.stream()
                .collect(Collectors.toMap(TransactionGraphDto::getOwnerName, Function.identity()));
        Map<String, TransactionGraphDto> orderIds = transactionGraph.stream()
                .collect(Collectors.toMap(TransactionGraphDto::getOrderId, Function.identity()));

        Map<String, Order> transactionPair = new HashMap<>();
        Map<String, String> detachPair = new HashMap<>();

        List<Owner> owners = ownerRepository.findAllByNameIn(ownerNames.keySet());

        owners.forEach(owner -> {
            Iterator<Order> iterator = owner.getCreated().iterator();
            while (iterator.hasNext()) {
                Order next = iterator.next();
                TransactionGraphDto transaction = orderIds.get(next.getId());
                if (transaction != null) {
                    detachPair.put(owner.getName(), next.getId());
                    transactionPair.put(transaction.getOwnerName(), next);
                }
            }
        });

        owners.forEach(owner -> {
            if (transactionPair.containsKey(owner.getName())) {
                Order order = transactionPair.get(owner.getName());
                order.setStatus(OrderStatus.DRAFT);
                owner.getCreated().add(order);
            }
        });

        ownerRepository.saveAll(owners);
        detachPair.forEach(ownerRepository::detachOrderFromOwner);
    }

    public void deleteAll(Set<String> owners) {
        ownerRepository.deleteAll(ownerRepository.findAllByNameIn(owners));
    }
}
