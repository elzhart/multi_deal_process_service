package ge.elzhart.service;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import ge.elzhart.api.dto.TransactionGraphDto;
import ge.elzhart.model.Order;
import ge.elzhart.model.OrderStatus;
import ge.elzhart.model.Owner;
import ge.elzhart.repository.OwnerRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OwnerService {
    private final OwnerRepository ownerRepository;

    public Owner findByName(String ownerName) {
        return ownerRepository.findByName(ownerName).orElseThrow(() -> new RuntimeException(""));
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
}
