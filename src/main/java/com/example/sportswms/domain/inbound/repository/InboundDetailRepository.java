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

    @Query("SELECT d FROM InboundDetail d JOIN FETCH d.productSKU WHERE d.inbound.id = :inboundId")
    List<InboundDetail> findByInboundIdWithSku(@Param("inboundId") Long inboundId);

    List<InboundDetail> findByInboundId(Long inboundId);
    boolean existsByInboundAndSectionIsNull(Inbound inbound);

    @Query("SELECT COALESCE(SUM(d.quantity), 0) FROM InboundDetail d " +
           "WHERE d.section = :section AND d.inbound.status = :status")
    int sumQuantityBySectionAndInboundStatus(@Param("section") Section section,
                                             @Param("status") InboundStatus status);

    /** 여러 구역의 pending 합계를 한 번에 조회 (N+1 해결용) */
    @Query("SELECT d.section.id, COALESCE(SUM(d.quantity), 0) FROM InboundDetail d " +
           "WHERE d.section IN :sections AND d.inbound.status = :status " +
           "GROUP BY d.section.id")
    List<Object[]> sumQuantityBySectionsAndInboundStatus(@Param("sections") List<Section> sections,
                                                          @Param("status") InboundStatus status);
}
