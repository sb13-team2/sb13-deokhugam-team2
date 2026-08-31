package com.deokhugam.user.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    @DisplayName("닉네임 변경 테스트")
    void updateNickname() {
        // given
        User user = User.create("test@test.com", "oldName", "password");

        // when
        user.updateNickname("newName");

        // then
        assertEquals("newName", user.getNickname());
    }

    @Test
    @DisplayName("소프트 삭제 및 복구 테스트")
    void softDeleteAndRestore() {
        // given
        User user = User.create("test@test.com", "nickname", "password");
        assertNull(user.getDeletedAt());

        // when (삭제)
        user.softDelete();

        // then
        assertTrue(user.isDeleted());
        assertNotNull(user.getDeletedAt());

        // when (복구)
        user.restore();

        // then
        assertFalse(user.isDeleted());
        assertNull(user.getDeletedAt());
    }
}