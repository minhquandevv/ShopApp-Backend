package com.project.shopapp.controllers;

import com.project.shopapp.dtos.UserDTO;
import com.project.shopapp.dtos.UserLoginDTO;
import com.project.shopapp.models.User;
import com.project.shopapp.responses.LoginResponse;
import com.project.shopapp.responses.RegisterResponse;
import com.project.shopapp.services.UserService;
import com.project.shopapp.components.LocalizationUtils;
import com.project.shopapp.utils.MessageKeys;
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
  private final LocalizationUtils localizationUtils;

  @PostMapping("/register")
  public ResponseEntity<RegisterResponse> register(
      @Valid
      @RequestBody
      UserDTO userDTO,
      BindingResult result
  ) {
    RegisterResponse registerResponse = new RegisterResponse();
    try {
      if (result.hasErrors()) {
        List<String> errorMessage = result.getFieldErrors()
            .stream()
            .map(FieldError::getDefaultMessage)
            .toList();
        registerResponse.setMessage(errorMessage.toString());
        return ResponseEntity.badRequest().body(registerResponse);
      }
      if (!userDTO.getPassword().equals(userDTO.getRetypePassword())) {
        registerResponse.setMessage(localizationUtils.getLocalizedMessage(MessageKeys.PASSWORD_NOT_MATCH));
        return ResponseEntity.badRequest().body(registerResponse);
      }
      User user = userService.createUser(userDTO);
      registerResponse.setMessage(localizationUtils.getLocalizedMessage(MessageKeys.REGISTER_SUCCESSFULLY));
      registerResponse.setUser(user);
      return ResponseEntity.ok(registerResponse);
    } catch (Exception e) {
      registerResponse.setMessage(e.getMessage());
      return ResponseEntity.badRequest().body(registerResponse);
    }
  }

  @PostMapping("/login")
  public ResponseEntity<LoginResponse> login(
      @Valid @RequestBody UserLoginDTO userLoginDTO
  ) {
    //Check value and create token
    try {
      String token = userService.login(
          userLoginDTO.getPhoneNumber(),
          userLoginDTO.getPassword(),
          userLoginDTO.getRoleId()
      );
      // Response token
      return ResponseEntity.ok(LoginResponse.builder()
          .message(localizationUtils.getLocalizedMessage(MessageKeys.LOGIN_SUCCESSFULLY))
          .token(token)
          .build());
    } catch (Exception e) {
      return ResponseEntity.badRequest().body(
          LoginResponse.builder()
              .message(localizationUtils.getLocalizedMessage(MessageKeys.LOGIN_FAILED, e.getMessage()))
              .build()
      );
    }
  }
}
