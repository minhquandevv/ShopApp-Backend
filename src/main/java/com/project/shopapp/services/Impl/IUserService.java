package com.project.shopapp.services.Impl;

import com.project.shopapp.dtos.UserDTO;
import com.project.shopapp.exceptions.DataNotFoundException;
import com.project.shopapp.models.User;

public interface IUserService {
  User createUser(UserDTO userDTO) throws Exception;
  String login(String phoneNumber, String password, Long roleId) throws Exception;
}
