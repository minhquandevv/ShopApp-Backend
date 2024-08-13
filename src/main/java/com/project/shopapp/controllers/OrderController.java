package com.project.shopapp.controllers;

import com.project.shopapp.dtos.OrderDTO;
import com.project.shopapp.models.Order;
import com.project.shopapp.services.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/orders")
public class OrderController {
  private final OrderService orderService;

  @PostMapping("")
  public ResponseEntity<?> createOrder(@RequestBody @Valid OrderDTO orderDTO, BindingResult result) {
    try {
      if (result.hasErrors()) {
        List<String> errorMessage = result.getFieldErrors()
            .stream()
            .map(FieldError::getDefaultMessage)
            .toList();
        return ResponseEntity.badRequest().body(errorMessage);
      }
      Order order = orderService.createOrder(orderDTO);
      return ResponseEntity.status(HttpStatus.CREATED).body(order);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    }
  }

  @GetMapping("/user/{user_id}")
  public ResponseEntity<?> getOrders(@Valid @PathVariable("user_id") Long user_id) {
    try {
      List<Order> orders = orderService.findByUserId(user_id);
      return ResponseEntity.status(HttpStatus.OK).body(orders);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    }
  }

  @GetMapping("/{id}")
  public ResponseEntity<?> getOrder(@Valid @PathVariable("id") Long orderId) {
    try {
      Order existingOrder = orderService.getOrderById(orderId);
      return ResponseEntity.status(HttpStatus.OK).body(existingOrder);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    }
  }

  @PutMapping("/{id}")
  public ResponseEntity<?> updateOrder(
      @Valid
      @PathVariable("id") Long id,
      @Valid
      @RequestBody
      OrderDTO orderDTO) {
    try {
      Order order = orderService.updateOrder(id, orderDTO);
      return ResponseEntity.status(HttpStatus.OK).body(order);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<String> deleteOrder(@Valid @PathVariable("id") Long id) {
    orderService.deleteOrder(id);
    return ResponseEntity.status(HttpStatus.OK).body("Delete order successfully");
  }
}
