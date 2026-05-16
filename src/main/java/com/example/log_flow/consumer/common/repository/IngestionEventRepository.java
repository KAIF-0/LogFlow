package com.example.log_flow.consumer.common.repository;

import com.example.log_flow.consumer.common.entity.IngestionEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IngestionEventRepository extends JpaRepository<IngestionEvent, Long> {
}