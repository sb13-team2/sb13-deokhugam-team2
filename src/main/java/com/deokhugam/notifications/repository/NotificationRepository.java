package com.deokhugam.notifications.repository;

import com.deokhugam.notifications.entity.Notification;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {


}
