package com.vibecoding.social.domain.post;

import com.vibecoding.social.common.exception.PostAuthorMismatchException;
import com.vibecoding.social.common.exception.PostNotFoundException;
import com.vibecoding.social.domain.post.dto.CreatePostRequest;
import com.vibecoding.social.domain.post.dto.PostResponse;
import com.vibecoding.social.domain.post.entity.Post;
import com.vibecoding.social.domain.post.repository.PostRepository;
import com.vibecoding.social.domain.post.service.PostService;
import com.vibecoding.social.domain.user.entity.User;
import com.vibecoding.social.domain.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

/**
 * PART 6 — 단위 테스트 (JUnit5 + Mockito)
 * given / when / then 패턴
 * Spring Context 없이 순수 단위 테스트 — 빠름
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PostService 단위 테스트")
class PostServiceTest {

    @Mock PostRepository postRepository;
    @Mock UserService    userService;

    @InjectMocks PostService postService;

    private User  testUser;
    private Post  testPost;

    @BeforeEach
    void setUp() {
        testUser = User.create("test@test.com", "encodedPw", "testuser");
        setField(testUser, "id", 1L);

        testPost = Post.create(testUser, "테스트 게시글 내용입니다.", null);
        setField(testPost, "id", 10L);
    }

    // ── 게시글 작성 ────────────────────────────────────────────────────────
    @Nested
    @DisplayName("게시글 작성")
    class CreatePost {

        @Test
        @DisplayName("성공 — 정상 입력")
        void createPost_success() {
            // given
            CreatePostRequest request = new CreatePostRequest("새 게시글입니다.", null);
            given(userService.getUser(1L)).willReturn(testUser);
            given(postRepository.save(any(Post.class))).willReturn(testPost);

            // when
            PostResponse response = postService.createPost(1L, request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getContent()).isEqualTo("테스트 게시글 내용입니다.");
            assertThat(response.getUserId()).isEqualTo(1L);

            verify(postRepository, times(1)).save(any(Post.class));
            verify(userService, times(1)).getUser(1L);
        }

        @Test
        @DisplayName("실패 — 존재하지 않는 유저")
        void createPost_userNotFound() {
            // given
            given(userService.getUser(999L)).willThrow(new com.vibecoding.social.common.exception.UserNotFoundException(999L));

            // when & then
            assertThatThrownBy(() -> postService.createPost(999L, new CreatePostRequest("내용", null)))
                    .isInstanceOf(com.vibecoding.social.common.exception.UserNotFoundException.class);
        }
    }

    // ── 게시글 조회 ────────────────────────────────────────────────────────
    @Nested
    @DisplayName("게시글 조회")
    class GetPost {

        @Test
        @DisplayName("성공 — 존재하는 게시글")
        void getPost_success() {
            // given
            given(postRepository.findById(10L)).willReturn(Optional.of(testPost));

            // when
            PostResponse response = postService.getPost(10L);

            // then
            assertThat(response.getId()).isEqualTo(10L);
            assertThat(response.getContent()).isEqualTo("테스트 게시글 내용입니다.");
        }

        @Test
        @DisplayName("실패 — 존재하지 않는 게시글 ID")
        void getPost_notFound() {
            // given
            given(postRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> postService.getPost(999L))
                    .isInstanceOf(PostNotFoundException.class)
                    .hasMessageContaining("게시글을 찾을 수 없습니다");
        }
    }

    // ── 게시글 수정 ────────────────────────────────────────────────────────
    @Nested
    @DisplayName("게시글 수정")
    class UpdatePost {

        @Test
        @DisplayName("성공 — 작성자 본인")
        void updatePost_success() {
            // given
            given(postRepository.findById(10L)).willReturn(Optional.of(testPost));

            // when
            PostResponse response = postService.updatePost(10L, 1L,
                    new com.vibecoding.social.domain.post.dto.UpdatePostRequest("수정된 내용", null));

            // then
            assertThat(response.getContent()).isEqualTo("수정된 내용");
            // save() 호출 없음 — 변경 감지 동작 검증
            verify(postRepository, never()).save(any());
        }

        @Test
        @DisplayName("실패 — 작성자가 아닌 유저가 수정 시도")
        void updatePost_notAuthor_throwsException() {
            // given
            given(postRepository.findById(10L)).willReturn(Optional.of(testPost));

            // when & then — userId=999는 작성자(userId=1)가 아님
            assertThatThrownBy(() -> postService.updatePost(10L, 999L,
                    new com.vibecoding.social.domain.post.dto.UpdatePostRequest("수정 시도", null)))
                    .isInstanceOf(PostAuthorMismatchException.class);
        }
    }

    // ── 게시글 삭제 ────────────────────────────────────────────────────────
    @Nested
    @DisplayName("게시글 삭제")
    class DeletePost {

        @Test
        @DisplayName("성공 — 작성자 본인")
        void deletePost_success() {
            // given
            given(postRepository.findById(10L)).willReturn(Optional.of(testPost));

            // when
            postService.deletePost(10L, 1L);

            // then
            verify(postRepository, times(1)).delete(testPost);
        }

        @Test
        @DisplayName("실패 — 다른 유저가 삭제 시도")
        void deletePost_notAuthor() {
            // given
            given(postRepository.findById(10L)).willReturn(Optional.of(testPost));

            // when & then
            assertThatThrownBy(() -> postService.deletePost(10L, 999L))
                    .isInstanceOf(PostAuthorMismatchException.class);

            verify(postRepository, never()).delete(any());
        }
    }

    // 리플렉션으로 private 필드 설정 (테스트용)
    private void setField(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
