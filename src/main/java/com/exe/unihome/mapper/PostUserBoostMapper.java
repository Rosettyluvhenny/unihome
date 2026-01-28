package com.exe.unihome.mapper;

import com.exe.unihome.dto.postAndComment.response.PostUserBoostResponse;
import com.exe.unihome.persistence.entity.postAndComment.PostUserBoost;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PostUserBoostMapper {

  @Mapping(source = "userBoost.id", target = "userBoostId")
  @Mapping(source = "post.id", target = "postId")
  PostUserBoostResponse toResponse(PostUserBoost postUserBoost);

  @Mapping(source = "userBoostId", target = "userBoost.id")
  @Mapping(source = "postId", target = "post.id")
  PostUserBoost toEntity(PostUserBoostResponse response);
}

