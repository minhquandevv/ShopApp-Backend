package com.project.shopapp.controllers;

import com.project.shopapp.dtos.UserDTO;
import com.project.shopapp.dtos.UserLoginDTO;
import com.project.shopapp.models.User;
import com.project.shopapp.services.UserService;
import jakarta.validation.Valid;
import lombok.*;
import org.springframework.http.*;
import org.springframework.validation.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/users")
@RequiredArgsConstructor
public class UserController {
  private final UserService userService;

  @PostMapping("/register")
  public ResponseEntity<?> register(
      @Valid
      @RequestBody
      UserDTO userDTO,
      BindingResult result
  ) {
    try {
      if (result.hasErrors()) {
        List<String> errorMessage = result.getFieldErrors()
            .stream()
            .map(FieldError::getDefaultMessage)
            .toList();
        return ResponseEntity.badRequest().body(errorMessage);
      }
      if (!userDTO.getPassword().equals(userDTO.getRetypePassword())) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Passwords do not match");
      }
      User user = userService.createUser(userDTO);
      return ResponseEntity.status(HttpStatus.OK).body("Register successfully");
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }
  }

  @PostMapping("/login")
  public ResponseEntity<String> login(@Valid @RequestBody UserLoginDTO userLoginDTO) {
    try {
      String token = userService.loginUser(userLoginDTO.getPhoneNumber(), userLoginDTO.getPassword());
      return ResponseEntity.ok().body("Login user successfully with token: " + token);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }
  }
}
