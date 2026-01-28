package com.exe.unihome.postAndSubscription;

import com.exe.unihome.dto.postAndComment.request.CreatePostRequest;
import com.exe.unihome.dto.postAndComment.request.UpdatePostRequest;
import com.exe.unihome.dto.postAndComment.response.PostResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface PostService {

  PostResponse createPost(CreatePostRequest request);

  PostResponse getPostById(String id);

  Page<PostResponse> getAllPosts(Pageable pageable);

  Page<PostResponse> getPostsByUserId(String userId, Pageable pageable);

  Page<PostResponse> getPostsByCategory(UUID categoryId, Pageable pageable);

  Page<PostResponse> searchPostsByTitle(String title, Pageable pageable);

  PostResponse updatePost(String id, UpdatePostRequest request);

  void disabledPost(String id);
}

