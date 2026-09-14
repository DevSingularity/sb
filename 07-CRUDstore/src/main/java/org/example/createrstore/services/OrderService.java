package org.example.createrstore.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.createrstore.dto.OrderItemRequest;
import org.example.createrstore.dto.OrderRequest;
import org.example.createrstore.entities.Order;
import org.example.createrstore.entities.OrderItem;
import org.example.createrstore.entities.Product;
import org.example.createrstore.repositories.OrderRepository;
import org.example.createrstore.repositories.ProductRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    @Transactional
    public Order createOrder(OrderRequest orderRequest) {
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalPrice = BigDecimal.ZERO;
        Order order = new Order();

        order.setCustomerEmail(orderRequest.getCustomerEmail());
        order.setCustomerName(orderRequest.getCustomerName());
        order.setStatus("CONFIRMED");

        for (OrderItemRequest itemRequest : orderRequest.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId()).orElseThrow(() -> new RuntimeException("Product not found" + itemRequest.getProductId()));

            // check if in quantity
            if (product.getStockQuantity() < itemRequest.getQuantity()) {
                throw new RuntimeException("Stock quantity exceeded");
            }

            // calculate total price
            BigDecimal priceOfItem = product.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity()));

            totalPrice = totalPrice.add(priceOfItem);

            // update product table with latest stock quantity
            product.setStockQuantity(
                    product.getStockQuantity() - itemRequest.getQuantity()
            );

            productRepository.save(product);

            // Builder pattern to make object
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(itemRequest.getQuantity())
                    .priceAtPurchase(product.getPrice())
                    .build();

            orderItems.add(orderItem);
        }

        order.setTotalPrice(totalPrice);
        order.setOrderItems(orderItems);
        return orderRepository.save(order);
    }

    public List<Order> getAllOrders() {
        List<Order> orders = new ArrayList<>();

        for (Order order : orderRepository.findAll()) {
            orders.add(order);
        }

        return orders;
    }
}
