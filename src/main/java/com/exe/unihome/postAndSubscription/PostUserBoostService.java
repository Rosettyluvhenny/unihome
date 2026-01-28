package com.exe.unihome.postAndSubscription;

import com.exe.unihome.dto.postAndComment.request.CreatePostUserBoostRequest;
import com.exe.unihome.dto.postAndComment.request.UpdatePostUserBoostRequest;
import com.exe.unihome.dto.postAndComment.response.PostUserBoostResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PostUserBoostService {

  PostUserBoostResponse createPostUserBoost(CreatePostUserBoostRequest request);

  PostUserBoostResponse getPostUserBoostById(String id);

  List<PostUserBoostResponse> getPostUserBoostsByPostId(String postId);

  List<PostUserBoostResponse> getPostUserBoostsByUserBoostId(String userBoostId, Pageable pageable);

  Page<PostUserBoostResponse> getPostUserBoostsByStatus(String status, Pageable pageable);

  PostUserBoostResponse updatePostUserBoost(String id, UpdatePostUserBoostRequest request);

//  void deletePostUserBoost(String id);

  void updateBoostUsageStatus();

  PostUserBoostResponse cancelPostUserBoost(String id);
}

