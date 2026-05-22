package com.vibecoding.social.domain.follow;

import com.vibecoding.social.common.exception.AlreadyFollowingException;
import com.vibecoding.social.common.exception.CannotFollowSelfException;
import com.vibecoding.social.common.exception.NotFollowingException;
import com.vibecoding.social.domain.follow.entity.Follow;
import com.vibecoding.social.domain.follow.repository.FollowRepository;
import com.vibecoding.social.domain.follow.service.FollowService;
import com.vibecoding.social.domain.user.entity.User;
import com.vibecoding.social.domain.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FollowService 단위 테스트")
class FollowServiceTest {

    @Mock FollowRepository followRepository;
    @Mock UserService      userService;

    @InjectMocks FollowService followService;

    private User userA;
    private User userB;

    @BeforeEach
    void setUp() {
        userA = User.create("a@test.com", "pw", "userA");
        userB = User.create("b@test.com", "pw", "userB");
        setField(userA, "id", 1L);
        setField(userB, "id", 2L);
    }

    @Test
    @DisplayName("팔로우 성공")
    void follow_success() {
        // given
        given(followRepository.existsByFollowerIdAndFollowingId(1L, 2L)).willReturn(false);
        given(userService.getUser(1L)).willReturn(userA);
        given(userService.getUser(2L)).willReturn(userB);
        given(followRepository.save(any(Follow.class))).willAnswer(inv -> inv.getArgument(0));

        // when
        followService.follow(1L, 2L);

        // then
        verify(followRepository, times(1)).save(any(Follow.class));
    }

    @Test
    @DisplayName("팔로우 실패 — 자기 자신 팔로우")
    void follow_self_throwsException() {
        assertThatThrownBy(() -> followService.follow(1L, 1L))
                .isInstanceOf(CannotFollowSelfException.class);
        verify(followRepository, never()).save(any());
    }

    @Test
    @DisplayName("팔로우 실패 — 이미 팔로우 중")
    void follow_alreadyFollowing_throwsException() {
        // given
        given(followRepository.existsByFollowerIdAndFollowingId(1L, 2L)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> followService.follow(1L, 2L))
                .isInstanceOf(AlreadyFollowingException.class);
    }

    @Test
    @DisplayName("언팔로우 성공")
    void unfollow_success() {
        // given
        Follow follow = Follow.create(userA, userB);
        given(followRepository.findByFollowerIdAndFollowingId(1L, 2L))
                .willReturn(Optional.of(follow));

        // when
        followService.unfollow(1L, 2L);

        // then
        verify(followRepository, times(1)).delete(follow);
    }

    @Test
    @DisplayName("언팔로우 실패 — 팔로우 중이지 않음")
    void unfollow_notFollowing_throwsException() {
        // given
        given(followRepository.findByFollowerIdAndFollowingId(1L, 2L))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> followService.unfollow(1L, 2L))
                .isInstanceOf(NotFollowingException.class);
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) { throw new RuntimeException(e); }
    }
}
