package com.project.shopapp.services;

import com.project.shopapp.dtos.OrderDTO;
import com.project.shopapp.exceptions.DataNotFoundException;
import com.project.shopapp.models.Order;
import com.project.shopapp.models.OrderStatus;
import com.project.shopapp.models.User;
import com.project.shopapp.repositories.OrderRepository;
import com.project.shopapp.repositories.UserRepository;
import com.project.shopapp.responses.OrderResponse;
import com.project.shopapp.services.Impl.IOrderService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService implements IOrderService {
  private final OrderRepository orderRepository;
  private final UserRepository userRepository;
  private final ModelMapper modelMapper;

  @Override
  public Order createOrder(OrderDTO orderDTO) throws Exception {
    User user = userRepository
        .findById(orderDTO.getUserId())
        .orElseThrow(() -> new DataNotFoundException("Cannot find user with id: " + orderDTO.getUserId()));
    //Convert orderDTO -> Order
    //Use library ModelMapper
    //Tao luong anh xa rieng de kiem soat viec anh xa
    modelMapper.typeMap(OrderDTO.class, Order.class).addMappings(mapper -> mapper.skip(Order::setId));
    Order order = new Order();
    // Cap nhat cac truong cua Order tu OrderDTO
    modelMapper.map(orderDTO, order);
    order.setUser(user);
    order.setOrderDate(new Date());
    order.setStatus(OrderStatus.PENDING);
    LocalDate shippingDate = orderDTO.getShippingDate() == null
        ? LocalDate.now() : orderDTO.getShippingDate();
    if (shippingDate.isBefore(LocalDate.now())) {
      throw new DataNotFoundException("Date must be at least today !");
    }
    order.setShippingDate(shippingDate);
    order.setActive(true);
    orderRepository.save(order);
    return order;
  }

  @Override
  public Order getOrderById(Long id) {
    return orderRepository.findById(id).orElse(null);
  }

  @Override
  public List<Order> findByUserId(Long userId) {
    return orderRepository.findByUserId(userId);
  }

  @Override
  public Order updateOrder(Long id, OrderDTO orderDTO) throws DataNotFoundException {
    Order order = orderRepository.findById(id).orElseThrow(() -> new DataNotFoundException("Cannot find order with id: " + id));
    User existingUser = userRepository.findById(orderDTO.getUserId()).orElseThrow(() -> new DataNotFoundException("Cannot find user with id: " + id));

    modelMapper.typeMap(OrderDTO.class, Order.class)
        .addMappings(mapper -> mapper.skip(Order::setId));

    modelMapper.map(orderDTO, order);
    order.setUser(existingUser);
    return orderRepository.save(order);
  }

  @Override
  public void deleteOrder(Long id) {
    Order order = orderRepository.findById(id).orElse(null);
    if (order != null) {
      order.setActive(false);
      orderRepository.save(order);
    }
  }
}