package com.muscat.Collabus.User.mapper;

import com.muscat.Collabus.User.entity.User;
import com.muscat.Collabus.User.model.UserDto;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

  public User mapToUser(User user, UserDto userDto) {
    user.setEmail(userDto.getEmail());
    user.setNickname(userDto.getNickname());
    user.setPassword(userDto.getPassword());
    user.setRole(userDto.getRole());
    return user;
  }

  public UserDto mapToUserDto(User user) {
    UserDto userDto = new UserDto();
    userDto.setId(user.getId());
    userDto.setEmail(user.getEmail());
    userDto.setNickname(user.getNickname());
    userDto.setPassword(user.getPassword());
    userDto.setRole(user.getRole());
    userDto.setDisplayName(user.getDisplayName());
    return userDto;
  }
}
