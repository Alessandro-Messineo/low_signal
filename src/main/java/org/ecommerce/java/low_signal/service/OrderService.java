package org.ecommerce.java.low_signal.service;

import java.util.List;
import java.util.stream.Collectors;

import org.ecommerce.java.low_signal.dto.OrderDTO;
import org.ecommerce.java.low_signal.repository.OrderRepository;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Transactional
    public List<OrderDTO> getAllOrders() {
        return orderRepository.findAllWithDetails()
                .stream()
                .map(OrderDTO::fromEntity)
                .collect(Collectors.toList());
    }
}
