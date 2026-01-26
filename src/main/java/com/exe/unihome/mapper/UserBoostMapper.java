package com.exe.unihome.mapper;

import com.exe.unihome.dto.subscription.request.CreateUserBoostRequest;
import com.exe.unihome.dto.subscription.response.UserBoostResponse;
import com.exe.unihome.persistence.entity.subscription.UserBoost;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring",
  uses = BoostMapper.class)
public interface UserBoostMapper {

  UserBoostResponse toResponse(UserBoost userBoost);

  UserBoost toEntity(CreateUserBoostRequest request);
}

