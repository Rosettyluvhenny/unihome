package com.exe.unihome.mapper;

import com.exe.unihome.dto.subscription.response.BoostResponse;
import com.exe.unihome.persistence.entity.subscription.Boost;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BoostMapper {

  BoostResponse toResponse(Boost boost);

  Boost toEntity(BoostResponse response);
}

