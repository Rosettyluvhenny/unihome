package com.exe.unihome.auth.mapper;

import com.exe.unihome.auth.model.UserResponse;
import com.exe.unihome.auth.model.UserSummary;
import com.exe.unihome.persistence.entity.identityAndAuth.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
  UserResponse toResponse(User user);

  UserSummary ResponsetoSummary(UserResponse user);

}
