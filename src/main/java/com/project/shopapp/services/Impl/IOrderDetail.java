package com.project.shopapp.services.Impl;

import com.project.shopapp.dtos.OrderDetailDTO;
import com.project.shopapp.exceptions.DataNotFoundException;
import com.project.shopapp.models.Order;
import com.project.shopapp.models.OrderDetail;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface IOrderDetail {
  OrderDetail createOrderDetail(OrderDetailDTO orderDetailDTO) throws Exception;

  OrderDetail getOrderDetail(Long id) throws Exception;

  OrderDetail updateOrderDetail(Long id, OrderDetailDTO orderDetailDTO) throws DataNotFoundException;

  void deleteOrderDetail(Long id);

  List<OrderDetail> findByOrderId(Long orderId);

}
