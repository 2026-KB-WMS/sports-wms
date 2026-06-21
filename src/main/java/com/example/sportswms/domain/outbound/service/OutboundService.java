package com.example.sportswms.domain.outbound.service;

import com.example.sportswms.domain.order.entity.StockOrder;
import com.example.sportswms.domain.order.entity.StockOrderDetail;
import com.example.sportswms.domain.outbound.entity.Outbound;
import com.example.sportswms.domain.outbound.entity.OutboundDetail;
import com.example.sportswms.domain.outbound.repository.OutboundDetailRepository;
import com.example.sportswms.domain.outbound.repository.OutboundRepository;
import com.example.sportswms.domain.warehouse.entity.Warehouse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OutboundService {
    private final OutboundRepository outboundRepository;
    private final OutboundDetailRepository outboundDetailRepository;

    /**
     * 본사관리자가 발주 상세를 창고에 위임(StockOrder)할 때, 동일 트랜잭션에서
     * StockOrder/StockOrderDetail에 대응하는 출고 요청(Outbound/OutboundDetail)을 함께 생성
     *
     * 실물 출고는 지점(Store) 단위로 나가야 하므로, 데이터도 지점별로 분리
     * 여러 지점의 StockOrderDetail이 한 번에 위임되더라도, Outbound는 지점마다 하나씩 생성
     * (창고에서의 Batch Picking 총량 계산은 한 StockOrder에 딸린 Outbound들의 OutboundDetail을 SKU 기준으로 합산)
     */
    @Transactional
    public void createOutboundFromStockOrder(Warehouse warehouse, StockOrder stockOrder, List<StockOrderDetail> stockOrderDetails) {
        // Store.equals/hashCode 기본 동작에 의존하지 않도록 store_id 기준으로 그룹핑
        Map<Long, List<StockOrderDetail>> detailsByStoreId = stockOrderDetails.stream()
                .collect(Collectors.groupingBy(detail -> detail.getStore().getId()));

        detailsByStoreId.forEach((storeId, detailsForStore) -> {
            Outbound outbound = Outbound.create(warehouse, stockOrder);
            outboundRepository.save(outbound);

            List<OutboundDetail> outboundDetails = detailsForStore.stream()
                    .map(detail -> OutboundDetail.from(outbound, detail))
                    .collect(Collectors.toList());

            outboundDetailRepository.saveAll(outboundDetails);
        });
    }
}
