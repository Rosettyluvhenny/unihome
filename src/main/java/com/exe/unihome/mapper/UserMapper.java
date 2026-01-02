package com.exe.unihome.mapper;

import com.exe.unihome.entity.identityAndAuth.User;
import com.exe.unihome.model.response.UserResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
  UserResponse toResponse(User user);
}
