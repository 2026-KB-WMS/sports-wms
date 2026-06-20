package com.example.sportswms.domain.inbound.repository;

import com.example.sportswms.domain.inbound.entity.Inbound;
import com.example.sportswms.domain.inbound.entity.InboundDetail;
import com.example.sportswms.domain.inbound.entity.InboundStatus;
import com.example.sportswms.domain.warehouse.entity.Section;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InboundDetailRepository extends JpaRepository<InboundDetail, Long> {
    List<InboundDetail> findByInboundId(Long inboundId);
    boolean existsByInboundAndSectionIsNull(Inbound inbound);

    // 특정 구역에 검수 중인 입고들에서 이미 배정된 수량의 합계를 반환
    // currentUsage와 별개로, 아직 완료되지 않은 배정 수량을 실시간으로 파악하기 위해 사용
    @Query("SELECT COALESCE(SUM(d.quantity), 0) FROM InboundDetail d " +
           "WHERE d.section = :section AND d.inbound.status = :status")
    int sumQuantityBySectionAndInboundStatus(@Param("section") Section section,
                                             @Param("status") InboundStatus status);
}
