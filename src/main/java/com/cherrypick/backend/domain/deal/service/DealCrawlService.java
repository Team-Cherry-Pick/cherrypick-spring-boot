package com.cherrypick.backend.domain.deal.service;

import com.cherrypick.backend.domain.deal.dto.request.DealCreateRequestDTO;
import com.cherrypick.backend.domain.deal.enums.PriceType;
import com.cherrypick.backend.domain.deal.enums.ShippingType;
import com.cherrypick.backend.domain.deal.vo.Price;
import com.cherrypick.backend.domain.deal.vo.Shipping;
import com.cherrypick.backend.domain.hashtag.service.HashTagService;
import com.cherrypick.backend.domain.image.service.ImageService;
import com.cherrypick.backend.domain.user.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DealCrawlService {

    private final DealService dealService;
    private final UserRepository userRepository;
    private final HashTagService hashTagService;

    private static final Random random = new Random();
    private final ImageService imageService;

    private WebDriver driver;

    @Value("${openai.api-key}")
    private String openAiApiKey;

    @Value("${openai.api-url}")
    private String openAiApiUrl;

    // 크롤링
    public void crawlAndSaveBoard(int count) throws IOException, InterruptedException {

        setupDriver();
        randomSleep();

        for (int i = 1; i <= count; i++) {
            try
            {
                String url = "https://www.fmkorea.com/index.php?mid=hotdeal&page=" + i;

                // 셀레니움으로 페이지 로드하고, 이걸 JSoup가 파싱
                driver.get(url);
                Document pageDoc = Jsoup.parse(driver.getPageSource());

                // JSoup가 해당 페이지 내 게시물 링크들을 받아옴.
                var links = pageDoc.select("div.fm_best_widget._bd_pc a");

                // 게시물 링크들을 Set에 저장(중복 제거 효과).
                HashSet<String> hrefSet = new HashSet<>();
                for (var l : links) {
                    String href = l.attr("href").trim();
                    if (href.matches("^/\\d+$")) {
                        hrefSet.add(href);
                    }
                }

                // 페이지 내에 있던 게시물들을 순회하여 저장.
                for (String href : hrefSet) {

                    driver.get("https://www.fmkorea.com" + href);
                    randomSleep();

                    // 게시물 HTML을 받아옴.
                    Document postDoc = Jsoup.parse(driver.getPageSource());
                    var article = postDoc.getElementsByTag("article").get(0);

                    var title = postDoc.getElementsByClass("np_18px_span").get(0).text();
                    var content = article.text();
                    var storeName = postDoc.select("table.hotdeal_table td .xe_content").get(1).text();
                    var discountedPrice = Double.parseDouble(postDoc.select("table.hotdeal_table td .xe_content").get(3).text().replaceAll("[^0-9]", ""));

                    // 게시물 내 이미지 링크 불러오기
                    List<WebElement> images = driver.findElements(By.cssSelector("article img"));

                    var imgList = new ArrayList<String>();
                    for (WebElement img : images) {
                        String imgSrc = img.getAttribute("data-original");
                        if (imgSrc == null || imgSrc.isEmpty()) {
                            imgSrc = img.getAttribute("src");
                        }
                        if (imgSrc != null && !imgSrc.isEmpty()) {
                            imgList.add(imgSrc);
                        }
                    }

                    Shipping shipping = Shipping.builder()
                            .shippingType(ShippingType.FREE)
                            .shippingRule("없음.")
                            .shippingPrice(0)
                            .build();

                    Price price = Price.builder()
                            .discountedPrice(discountedPrice) // 이건 넣어줘야함
                            .regularPrice(discountedPrice)
                            .priceType(PriceType.KRW)
                            .build();

                    var createDto = DealCreateRequestDTO.builder()
                            .title(title)
                            .content(content)
                            .shipping(shipping)
                            .storeName(storeName)
                            .price(price)
                            .imageIds(imageService.saveImageUrlsForCrawling(imgList))
                            .discountNames(List.of("현대카드", "쿠팡와우", "이벤트"))
                            .categoryId(1L)
                            .build();

                    log.info(createDto.toString());

                    Long dealId = dealService.createDeal(createDto).dealId();
                    hashTagService.saveHashTags(dealId, getChatGPTResponse(createDto.title(), createDto.content()));

                    randomSleep();
                }
            } catch (RuntimeException e) {
                log.error(e.getMessage());
            }
        }

        driver.quit(); // 크롤링 끝나면 브라우저 닫기
    }

    // WebDriver 초기 세팅
    private void setupDriver() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--disable-blink-features=AutomationControlled"); // 봇 탐지 방지
        options.addArguments("--start-maximized");
        //options.addArguments("--headless=new"); // 백그라운드에서 돌릴때는 headless해줘야함

        driver = new ChromeDriver(options);
    }

    private void randomSleep() throws InterruptedException {
        int sleepTime = 1000 + random.nextInt(2000); // 1~3초 랜덤
        Thread.sleep(sleepTime);
    }

    private boolean containsForbiddenWord(String title, List<String> forbiddenWords) {
        for (String forbiddenWord : forbiddenWords) {
            if (title.contains(forbiddenWord)) {
                return true;
            }
        }
        return false;
    }

    // OpenAI API 호출
    private Set<String> getChatGPTResponse(String title, String content) {

        RestTemplate restTemplate = new RestTemplate();

        //String prompt = title + "\n 이 제목을 보고 가격과 해시태그 10개를 뽑아줘. " +
        //        "해시태그는 전자기기, 가전제품, 패션, 뷰티/헬스, 홈/리빙, 식품, 스포츠/레저, 자동차, 책/문구, 취미/완구, 게임, 영화, 음악, 여행, 애완동물, 유아용품, 정원용품, 세차용품, 컴퓨터 부품, 사진/영상, 오피스 용품, 의료기기, DIY 용품, 시계, 쥬얼리, 음향기기, 스마트홈, 교육용품, 선물, 공구, 텔레비전, 스마트폰 액세서리, 인테리어 소품, 화장품 중에서 최대한 골라서." +
        //        "특가, 무료배송 이런거 말고 제목을 보고 최대한 포괄적인 단어로만 해시태그 뽑아줘." +
        //        "가격이나 해시태그를 못 뽑겠으면 정확히 '유추 불가'라고 답해줘." +
        //        "형식: 가격:10000, 해시태그: 과일, 식품, 사과, 유기농, 건강식품, 선물";

        String prompt = String.format("""
                {제목 : %s  내용 : %s} \n
                이 핫딜 정보를 보고 해시태그 10개 뽑아줘. 해시태그는 이 정보를 본 유저가 관심있어할만한 키워드 목록이야.
                해시태그는 전자기기, 가전제품, 패션, 뷰티/헬스, 홈/리빙, 식품, 스포츠/레저, 자동차, 책/문구, 취미/완구, 게임, 영화, 음악, 여행, 애완동물, 유아용품, 정원용품, 세차용품, 컴퓨터 부품, 사진/영상, 오피스 용품, 의료기기, DIY 용품, 시계, 쥬얼리, 음향기기, 스마트홈, 교육용품, 선물, 공구, 텔레비전, 스마트폰 액세서리, 인테리어 소품, 화장품
                중에 골라서 만들어줘.\n
                상품에 대한 정보만 취급하고 그 외적인건(무료배송, 특가 등) 취급하지 말아줘.\n
                형식 : {과일 식품 사과 유기농 건강식품 선물}
                문자열을 분리할때는 반드시 띄워쓰기를 사용해줘.
                """, title, content);

        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("model", "gpt-4-turbo");
        requestMap.put("max_tokens", 300);
        requestMap.put("messages", List.of(Map.of("role", "user", "content", prompt)));

        ObjectMapper objectMapper = new ObjectMapper();
        String requestBody = "";
        try {
            requestBody = objectMapper.writeValueAsString(requestMap);
        } catch (JsonProcessingException e) {
            System.err.println("요청 본문 직렬화 오류: " + e.getMessage());
        }

        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("Authorization", "Bearer " + openAiApiKey);

        org.springframework.http.HttpEntity<String> request = new org.springframework.http.HttpEntity<>(requestBody, headers);

        try {

            org.springframework.http.ResponseEntity<String> responseEntity =
                    restTemplate.exchange(openAiApiUrl, org.springframework.http.HttpMethod.POST, request, String.class);

            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                String response = responseEntity.getBody();
                JsonNode rootNode = objectMapper.readTree(response);
                var rawText = rootNode.path("choices").get(0).path("message").path("content").asText();
                return Arrays.stream(rawText.replaceAll("[{}]", "").split(" ")).collect(Collectors.toSet());

            } else {
                System.err.println("OpenAI 요청 실패: " + responseEntity.getStatusCode());
                return null;
            }
        } catch (IOException e) {
            System.err.println("OpenAI 호출 오류: " + e.getMessage());
            return null;
        }
    }

}
