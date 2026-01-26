package com.exe.unihome.mapper;

import com.exe.unihome.dto.postAndComment.response.CommentResponse;
import com.exe.unihome.persistence.entity.postAndComment.Comment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CommentMapper {

  @Mapping(source = "user.id", target = "userId")
  @Mapping(source = "user.fullName", target = "userName")
  @Mapping(source = "post.id", target = "postId")
  @Mapping(source = "reply.id", target = "replyId")
  @Mapping(source = "replies", target = "replies")
  CommentResponse toResponse(Comment comment);

  @Mapping(source = "userId", target = "user.id")
  @Mapping(source = "postId", target = "post.id")
  @Mapping(source = "replyId", target = "reply.id")
  Comment toEntity(CommentResponse response);
}

