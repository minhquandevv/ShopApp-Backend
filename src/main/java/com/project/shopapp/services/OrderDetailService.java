package com.project.shopapp.services;

import com.project.shopapp.dtos.OrderDetailDTO;
import com.project.shopapp.exceptions.DataNotFoundException;
import com.project.shopapp.models.Order;
import com.project.shopapp.models.OrderDetail;
import com.project.shopapp.models.Product;
import com.project.shopapp.repositories.OrderDetailRepository;
import com.project.shopapp.repositories.OrderRepository;
import com.project.shopapp.repositories.ProductRepository;
import com.project.shopapp.services.Impl.IOrderDetail;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderDetailService implements IOrderDetail {
  private final OrderRepository orderRepository;
  private final OrderDetailRepository orderDetailRepository;
  private final ProductRepository productRepository;


  @Override
  @Transactional
  public OrderDetail createOrderDetail(OrderDetailDTO orderDetailDTO) throws Exception {
    //Check orderId
    Order order = orderRepository.findById(orderDetailDTO.getOrderId())
        .orElseThrow(() ->
            new DataNotFoundException("Cannot find order with id " + orderDetailDTO.getOrderId()));
    //Check productId
    Product product = productRepository.findById(orderDetailDTO.getProductId())
        .orElseThrow(() ->
            new DataNotFoundException("Cannot find product with id " + orderDetailDTO.getProductId()));
    OrderDetail orderDetail = OrderDetail.builder()
        .order(order)
        .product(product)
        .numberOfProducts(orderDetailDTO.getNumberOfProducts())
        .price(orderDetailDTO.getPrice())
        .totalMoney(orderDetailDTO.getTotalMoney())
        .color(orderDetailDTO.getColor())
        .build();
    return orderDetailRepository.save(orderDetail);
  }

  @Override
  public OrderDetail getOrderDetail(Long id) throws Exception {
    return orderDetailRepository.findById(id)
        .orElseThrow(() -> new DataNotFoundException("Cannot find order detail with id " + id));
  }

  @Override
  @Transactional
  public OrderDetail updateOrderDetail(Long id, OrderDetailDTO orderDetailDTO) throws DataNotFoundException {
    //Check orderId
    OrderDetail existingOrderDetail = orderDetailRepository.findById(id)
        .orElseThrow(() -> new DataNotFoundException("Cannot find order detail with id: " + id));
    Order existingOrder = orderRepository.findById(orderDetailDTO.getOrderId())
        .orElseThrow(() -> new DataNotFoundException("Cannot find order with id: " + orderDetailDTO.getOrderId()));
    Product existingProduct = productRepository.findById(orderDetailDTO.getProductId())
        .orElseThrow(() -> new DataNotFoundException("Cannot find product with id: " + orderDetailDTO.getProductId()));
    existingOrderDetail.setPrice(orderDetailDTO.getPrice());
    existingOrderDetail.setTotalMoney(orderDetailDTO.getTotalMoney());
    existingOrderDetail.setColor(orderDetailDTO.getColor());
    existingOrderDetail.setNumberOfProducts(orderDetailDTO.getNumberOfProducts());
    existingOrderDetail.setOrder(existingOrder);
    existingOrderDetail.setProduct(existingProduct);
    return orderDetailRepository.save(existingOrderDetail);
  }

  @Override
  @Transactional
  public void deleteOrderDetail(Long id) {
    orderDetailRepository.deleteById(id);
  }

  @Override
  public List<OrderDetail> findByOrderId(Long orderId) {
    return orderDetailRepository.findByOrderId(orderId);
  }

}
