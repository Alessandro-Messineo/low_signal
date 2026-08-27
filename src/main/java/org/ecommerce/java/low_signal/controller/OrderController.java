package org.ecommerce.java.low_signal.controller;

import java.util.List;

import org.ecommerce.java.low_signal.dto.CreateOrderRequest;
import org.ecommerce.java.low_signal.dto.OrderDTO;
import org.ecommerce.java.low_signal.security.DatabaseUserDetails;
import org.ecommerce.java.low_signal.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public ResponseEntity<List<OrderDTO>> getAllOrders() {
        List<OrderDTO> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/my")
    public ResponseEntity<List<OrderDTO>> getMyOrders() {
        DatabaseUserDetails userDetails = (DatabaseUserDetails) SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();
        List<OrderDTO> orders = orderService.getMyOrders(userDetails.getId());
        return ResponseEntity.ok(orders);
    }

    @PostMapping
    public ResponseEntity<OrderDTO> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        DatabaseUserDetails userDetails = (DatabaseUserDetails) SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();
        OrderDTO order = orderService.createOrder(userDetails.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }
}