package com.deokhugam.user.repository;

import com.deokhugam.user.entity.Period;
import com.deokhugam.user.entity.User;
import com.deokhugam.user.dto.response.PowerUserDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepositoryCustom {

    private final EntityManager em;

    @Override
    public Slice<PowerUserDto> findPowerUsers(Period period, String direction, String cursor, LocalDateTime after, int limit) {
        boolean isDesc = "DESC".equalsIgnoreCase(direction);
        StringBuilder jpql = new StringBuilder("SELECT u FROM User u WHERE u.deletedAt IS NULL ");

        // 커서(시간) 기반 조건
        if (after != null) {
            jpql.append(isDesc ? "AND u.createdAt < :after " : "AND u.createdAt > :after ");
        }

        // 정렬
        jpql.append(isDesc ? "ORDER BY u.createdAt DESC" : "ORDER BY u.createdAt ASC");

        TypedQuery<User> query = em.createQuery(jpql.toString(), User.class);
        if (after != null) {
            query.setParameter("after", after);
        }

        // 다음 페이지 여부를 확인하기 위해 limit + 1개 조회
        query.setMaxResults(limit + 1);
        List<User> users = query.getResultList();

        boolean hasNext = users.size() > limit;
        if (hasNext) {
            users.remove(limit);
        }

        // Entity -> DTO 변환 (현재는 score 등이 없으므로 더미 값 매핑)
        List<PowerUserDto> dtos = users.stream()
                .map(u -> new PowerUserDto(
                        u.getId(), u.getNickname(), period.name(), u.getCreatedAt(),
                        1L, 0.0, 0.0, 0L, 0L
                ))
                .collect(Collectors.toList());

        return new SliceImpl<>(dtos, PageRequest.of(0, limit > 0 ? limit : 50), hasNext);
    }
}