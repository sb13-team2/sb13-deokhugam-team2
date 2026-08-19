package com.deokhugam.user.repository;

import com.deokhugam.user.entity.Period;
import com.deokhugam.user.dto.response.PowerUserDto;
import org.springframework.data.domain.Slice;

import java.time.LocalDateTime;

public interface UserRepositoryCustom {
    Slice<PowerUserDto> findPowerUsers(Period period, String direction, String cursor, LocalDateTime after, int limit);
}