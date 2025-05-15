package com.food.ordering.system.order.service.domain;

import com.food.ordering.system.order.service.domain.dto.create.CreateOrderCommand;
import com.food.ordering.system.order.service.domain.dto.create.CreateOrderResponse;
import com.food.ordering.system.order.service.domain.entity.Customer;
import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.entity.Restaurant;
import com.food.ordering.system.order.service.domain.event.OrderCreatedEvent;
import com.food.ordering.system.order.service.domain.exception.OrderDomainException;
import com.food.ordering.system.order.service.domain.mapper.OrderDataMapper;
import com.food.ordering.system.order.service.domain.ports.output.repository.CustomerRepository;
import com.food.ordering.system.order.service.domain.ports.output.repository.OrderRepository;
import com.food.ordering.system.order.service.domain.ports.output.repository.RestaurantRepository;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
public class OrderCreateCommandHandler {
    private final OrderDomainService orderDomainService;
    private final OrderRepository orderRepository;
    private final OrderDataMapper orderDataMapper;
    private final CustomerRepository customerRepository;
    private final RestaurantRepository restaurantRepository;

    public OrderCreateCommandHandler(OrderDomainService orderDomainService,
                                     OrderRepository orderRepository,
                                     OrderDataMapper orderDataMapper,
                                     CustomerRepository customerRepository,
                                     RestaurantRepository restaurantRepository) {
        this.orderDomainService = orderDomainService;
        this.orderRepository = orderRepository;
        this.orderDataMapper = orderDataMapper;
        this.customerRepository = customerRepository;
        this.restaurantRepository = restaurantRepository;
    }

    @Transactional
    public CreateOrderResponse createOrder(CreateOrderCommand createOrderCommand) {
        checkCustomer(createOrderCommand.getCustomerId());
        Restaurant restaurant = checkRestaurant(createOrderCommand);
        Order order = orderDataMapper.createOrderCommandToOrder(createOrderCommand);
        OrderCreatedEvent orderCreatedEvent = orderDomainService.validateAndInitiateOrder(order, restaurant);
        Order orderResult = saveOrder(order);
        log.info("Order is created with id {} ", orderResult.getId().getValue());
        return orderDataMapper.orderToCreateOrderResponse(orderResult,
                "Order created successfully with tracking id " + orderResult.getTrackingId().getValue());
    }

    private Restaurant checkRestaurant(CreateOrderCommand createOrderCommand) {
        Restaurant restaurant = orderDataMapper.createOrderCommandToRestaurant(createOrderCommand);
        Optional<Restaurant> restaurantOptional = restaurantRepository.findRestaurantInformation(restaurant);
        if (restaurantOptional.isEmpty()) {
            log.warn("Restaurant with id {} not found", createOrderCommand.getRestaurantId());
            throw new OrderDomainException("Restaurant not found with id " + createOrderCommand.getRestaurantId());
        }
        return restaurantOptional.get();
    }

    private void checkCustomer(@NotNull UUID customerId) {
        Optional<Customer> customer = customerRepository.findCustomer(customerId);
        if(customer.isEmpty()) {
            log.error("Customer with id {} not found", customerId);
            throw new OrderDomainException("Customer not found find customer with id " + customer);
        }
    }

    private Order saveOrder(Order order) {
        Order orderResult = orderRepository.save(order);
        if(orderResult == null) {
            log.error("Could not save order with id {}", order.getId().getValue());
            throw new OrderDomainException("Could not save order with id " + order.getId().getValue());
        }
        log.info("Order with id {} is saved", orderResult.getId().getValue());
        return orderResult;
    }
}
