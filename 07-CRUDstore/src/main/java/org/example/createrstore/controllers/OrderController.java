package org.example.createrstore.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.createrstore.dto.OrderRequest;
import org.example.createrstore.entities.Order;
import org.example.createrstore.services.OrderService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public Order createOrder(@Valid @RequestBody OrderRequest orderRequest) {
        return orderService.createOrder(orderRequest);
    }

    @GetMapping
    public List<Order> getAllOrders() {
        return orderService.getAllOrders();
    }
}
