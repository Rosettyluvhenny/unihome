package com.exe.unihome.service.postAndSubscription;

import com.exe.unihome.dto.postAndComment.request.CreatePostRequest;
import com.exe.unihome.dto.postAndComment.request.UpdatePostRequest;
import com.exe.unihome.dto.postAndComment.response.PostResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface PostService {

  PostResponse createPost(CreatePostRequest request, String userId);

  PostResponse getPostById(String id);

  Page<PostResponse> getAllPosts(Pageable pageable);

  Page<PostResponse> getPostsByUserId(String userId, Pageable pageable);

  Page<PostResponse> getPostsByCategory(UUID categoryId, Pageable pageable);

  Page<PostResponse> searchPostsByTitle(String title, Pageable pageable);

  PostResponse updatePost(String id, UpdatePostRequest request, String userId);

  void disabledPost(String id, String userId);
}

