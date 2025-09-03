package com.cherrypick.backend.domain.linkprice.dto.response;

import java.util.List;

public record LinkPriceResponse(
        String result,
        int list_count,
        List<Order> order_list
) {
    public record Order(
            String trlog_id,
            String m_id,
            String o_cd,
            String p_cd,
            String p_nm,
            String it_cnt,
            String user_id,
            String status,
            String c_cd,
            String create_time_stamp,
            String applied_pgm_id,
            String yyyymmdd,
            String hhmiss,
            String trans_comment,
            double sales,
            int commission,
            String pgm_name,
            String is_pc,
            String pur_rate
    ) {}
}
