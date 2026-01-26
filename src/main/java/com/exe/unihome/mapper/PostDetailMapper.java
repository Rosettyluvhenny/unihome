package com.exe.unihome.mapper;

import com.exe.unihome.dto.postAndComment.request.CreatePostDetailRequest;
import com.exe.unihome.dto.postAndComment.response.PostDetailResponse;
import com.exe.unihome.persistence.entity.postAndComment.PostDetail;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PostDetailMapper {

  PostDetailResponse toResponse(PostDetail postDetail);

  PostDetail toEntity(CreatePostDetailRequest rq);
}

