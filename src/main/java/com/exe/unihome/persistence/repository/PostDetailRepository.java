package com.exe.unihome.persistence.repository;

import com.exe.unihome.persistence.entity.postAndComment.PostDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostDetailRepository extends JpaRepository<PostDetail, String> {

  PostDetail findByPostId(String postId);
}

