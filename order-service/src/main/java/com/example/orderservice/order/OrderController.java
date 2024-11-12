package com.example.orderservice.order;

import com.example.orderservice.order.domain.Order;
import com.example.orderservice.order.domain.OrderDto;
import com.example.orderservice.order.domain.RequestOrder;
import com.example.orderservice.order.domain.ResponseOrder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RequestMapping("/orders")
@RestController
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/{userId}")
    public ResponseEntity<ResponseOrder> createOrder(@PathVariable Long userId, @RequestBody RequestOrder orderDetails) {

        // RequestOrder -> OrderDto 수동 매핑
        OrderDto orderDto = new OrderDto();
        orderDto.setProductId(orderDetails.getProductId());
        orderDto.setQty(orderDetails.getQty());
        orderDto.setUnitPrice(orderDetails.getUnitPrice());
        orderDto.setTotalPrice(orderDetails.getQty() * orderDetails.getUnitPrice());
        orderDto.setUserId(userId);

        // Order 생성
        OrderDto createdOrder = orderService.createOrder(orderDto);

        // OrderDto -> ResponseOrder 수동 매핑
        ResponseOrder responseOrder = new ResponseOrder();
        responseOrder.setOrderId(createdOrder.getOrderId());
        responseOrder.setProductId(createdOrder.getProductId());
        responseOrder.setQty(createdOrder.getQty());
        responseOrder.setUnitPrice(createdOrder.getUnitPrice());
        responseOrder.setTotalPrice(createdOrder.getTotalPrice());

        return ResponseEntity.status(HttpStatus.CREATED).body(responseOrder);
    }


    @GetMapping("/{userId}")
    public ResponseEntity<List<ResponseOrder>> getOrders(@PathVariable Long userId) {
        List<Order> orders = orderService.getOrders(userId);

        // Order -> ResponseOrder로 수동 매핑
        List<ResponseOrder> result = new ArrayList<>();
        for (Order order : orders) {
            ResponseOrder responseOrder = new ResponseOrder();
            responseOrder.setOrderId(order.getOrderId());
            responseOrder.setProductId(order.getProductId());
            responseOrder.setQty(order.getQty());
            responseOrder.setUnitPrice(order.getUnitPrice());
            responseOrder.setTotalPrice(order.getTotalPrice());

            result.add(responseOrder);
        }

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

}
