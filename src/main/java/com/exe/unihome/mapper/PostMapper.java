package com.exe.unihome.mapper;

import com.exe.unihome.dto.postAndComment.response.PostResponse;
import com.exe.unihome.persistence.entity.postAndComment.Post;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {PostDetailMapper.class, CommentMapper.class, PostUserBoostMapper.class})
public interface PostMapper {

  @Mapping(source = "user.id", target = "userId")
  @Mapping(source = "user.fullName", target = "userName")
  @Mapping(source = "user.image", target = "userImage")
  @Mapping(source = "category.categoryId", target = "categoryId")
  @Mapping(source = "category.name", target = "categoryName")
  @Mapping(source = "postDetail", target = "postDetail")
  @Mapping(source = "comments", target = "comments")
//  @Mapping(source = "postUserBoosts", target = "postUserBoosts")
  PostResponse toResponse(Post post);

  @Mapping(source = "userId", target = "user.id")
  @Mapping(source = "categoryId", target = "category.categoryId")
  Post toEntity(PostResponse response);
}

