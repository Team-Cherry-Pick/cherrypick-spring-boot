package com.cherrypick.backend.domain.linkprice.service;

import com.cherrypick.backend.domain.auth.domain.vo.AuthenticatedUser;
import com.cherrypick.backend.domain.linkprice.dto.request.LinkPriceRequest;
import com.cherrypick.backend.domain.linkprice.dto.response.LinkPriceResponse;
import com.cherrypick.backend.domain.user.enums.Role;
import com.cherrypick.backend.global.exception.BaseException;
import com.cherrypick.backend.global.exception.enums.GlobalErrorCode;
import com.cherrypick.backend.global.exception.enums.LinkPriceErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class LinkPriceTransactionService {

    private final WebClient linkPriceWebClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${linkprice.affiliate-id}")
    private String aId;

    @Value("${linkprice.affiliate-auth-key}")
    private String authKey;

    // 실적 조회 API
    public LinkPriceResponse getTransactions(LinkPriceRequest request) {

//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        Object principal = (authentication == null) ? null : authentication.getPrincipal();
//
//        if (!(principal instanceof AuthenticatedUser userDetails)) {
//            throw new BaseException(GlobalErrorCode.UNAUTHORIZED);
//        }
//        boolean isAdmin = userDetails.role() == Role.ADMIN;
//        if (!isAdmin) {
//            throw new BaseException(GlobalErrorCode.FORBIDDEN);
//        }

        String raw = linkPriceWebClient.get()
                .uri(uriBuilder -> {
                    var b = uriBuilder.path("/translist.php")
                            .queryParam("a_id", aId)
                            .queryParam("auth_key", authKey)
                            .queryParam("yyyymmdd", request.yyyymmdd());
                    request.cancelFlagOpt().ifPresent(v -> b.queryParam("cancel_flag", v));
                    request.currencyOpt().ifPresent(v -> b.queryParam("currency", v));
                    request.merchantIdOpt().ifPresent(v -> b.queryParam("merchant_id", v));
                    request.pageOpt().ifPresent(v -> b.queryParam("page", v));
                    request.perPageOpt().ifPresent(v -> b.queryParam("per_page", v));
                    request.testOpt().ifPresent(v -> b.queryParam("test", v));
                    return b.build();
                })
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError,
                        r -> r.bodyToMono(String.class)
                                .then(Mono.error(new BaseException(LinkPriceErrorCode.TRANSLIST_HTTP_CLIENT_ERROR))))
                .onStatus(HttpStatusCode::is5xxServerError,
                        r -> r.bodyToMono(String.class)
                                .then(Mono.error(new BaseException(LinkPriceErrorCode.TRANSLIST_HTTP_SERVER_ERROR))))
                .bodyToMono(String.class)
                .block();

        try {
            LinkPriceResponse body = objectMapper.readValue(raw, LinkPriceResponse.class); // ← JSON 파싱
            if (body == null || body.result() == null || !"0".equals(body.result())) {
                throw new BaseException(LinkPriceErrorCode.TRANSLIST_RESULT_NOT_SUCCESS);
            }
            return body;
        } catch (Exception e) {
            throw new BaseException(LinkPriceErrorCode.TRANSLIST_RESULT_NOT_SUCCESS);
        }
    }
}
