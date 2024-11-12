package com.example.orderservice.order;

import com.example.orderservice.order.domain.Order;
import com.example.orderservice.order.domain.OrderDto;
import com.example.orderservice.order.domain.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final UserClient userClient;
    private final OrderRepository orderRepository;


    public OrderDto createOrder(OrderDto orderDto) {
        // Order ID와 총 가격 설정
        orderDto.setOrderId(UUID.randomUUID().toString());
        orderDto.setTotalPrice(orderDto.getQty() * orderDto.getUnitPrice());

        // OrderDto -> OrderEntity 수동 매핑
        Order orderEntity = new Order();
        orderEntity.setOrderId(orderDto.getOrderId());
        orderEntity.setProductId(orderDto.getProductId());
        orderEntity.setQty(orderDto.getQty());
        orderEntity.setUnitPrice(orderDto.getUnitPrice());
        orderEntity.setTotalPrice(orderDto.getTotalPrice());
        orderEntity.setUserId(orderDto.getUserId());

        // 데이터베이스에 저장
        orderRepository.save(orderEntity);

        // OrderEntity -> OrderDto 수동 매핑 (저장 후 반환값 설정)
        OrderDto returnValue = new OrderDto();
        returnValue.setOrderId(orderEntity.getOrderId());
        returnValue.setProductId(orderEntity.getProductId());
        returnValue.setQty(orderEntity.getQty());
        returnValue.setUnitPrice(orderEntity.getUnitPrice());
        returnValue.setTotalPrice(orderEntity.getTotalPrice());
        returnValue.setUserId(orderEntity.getUserId());

        return returnValue;
    }

    public OrderDto getOrder(String orderId) {

        // OrderEntity 조회
        Order orderEntity = orderRepository.findByOrderId(orderId).orElseThrow(() -> new RuntimeException("Order not found"));

        // OrderEntity -> OrderDto 수동 매핑
        OrderDto orderDto = new OrderDto();
        orderDto.setOrderId(orderEntity.getOrderId());
        orderDto.setProductId(orderEntity.getProductId());
        orderDto.setQty(orderEntity.getQty());
        orderDto.setUnitPrice(orderEntity.getUnitPrice());
        orderDto.setTotalPrice(orderEntity.getTotalPrice());
        orderDto.setUserId(orderEntity.getUserId());

        return orderDto;
    }

    public List<Order> getOrders(Long userId) {
        return orderRepository.findByUserId(userId);
    }

}
