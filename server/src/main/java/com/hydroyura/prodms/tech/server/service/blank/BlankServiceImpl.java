package com.hydroyura.prodms.tech.server.service.blank;

import com.hydroyura.prodms.tech.client.enums.BlankSortCode;
import com.hydroyura.prodms.tech.client.req.BlankCreateReq;
import com.hydroyura.prodms.tech.client.req.BlankListReq;
import com.hydroyura.prodms.tech.client.res.BlankCreateRes;
import com.hydroyura.prodms.tech.client.res.BlankListRes;
import com.hydroyura.prodms.tech.client.res.SingleBlankRes;
import com.hydroyura.prodms.tech.server.db.repository.BlankRepository;
import com.hydroyura.prodms.tech.server.props.DefaultParams;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BlankServiceImpl implements BlankService {

    private final BlankRepository blankRepository;
    private final DefaultParams defaultParams;


    @Override
    public Optional<SingleBlankRes> get(String number) {
        return blankRepository.get(number);
    }

    @Override
    public BlankListRes list(BlankListReq filter) {
        populateWithDefaultsEquipmentListFilter(filter);
        return blankRepository.list(filter);
    }

    @Override
    public BlankCreateRes create(BlankCreateReq equipment) {
        Integer id = blankRepository.create(equipment);
        return new BlankCreateRes(id);
    }

    private void populateWithDefaultsEquipmentListFilter(final BlankListReq filter) {
        Optional
            .ofNullable(filter.getItemsPerPage())
            .ifPresentOrElse(v -> {}, () -> filter.setItemsPerPage(defaultParams.getBlankList().getItemsPerPage()));
        Optional
            .ofNullable(filter.getPage())
            .ifPresentOrElse(v -> {}, () -> filter.setPage(defaultParams.getBlankList().getPage()));
        Optional
            .ofNullable(filter.getSortCode())
            .ifPresentOrElse(
                v -> {},
                () -> filter.setSortCode(BlankSortCode.getByCode(defaultParams.getBlankList().getSortCode())));
    }

}
