package com.project.shopapp.services.Impl;

import com.project.shopapp.dtos.OrderDTO;
import com.project.shopapp.exceptions.DataNotFoundException;
import com.project.shopapp.models.Order;

import java.util.List;

public interface IOrderService {
  Order createOrder(OrderDTO orderDTO) throws Exception;

  Order getOrderById(Long id);

  Order updateOrder(Long id, OrderDTO orderDTO) throws DataNotFoundException;

  void deleteOrder(Long id);

  List<Order> findByUserId(Long userId);
}
