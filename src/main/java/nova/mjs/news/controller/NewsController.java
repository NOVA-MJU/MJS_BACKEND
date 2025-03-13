package nova.mjs.news.controller;

import lombok.RequiredArgsConstructor;
import nova.mjs.news.DTO.NewsResponseDTO;
import nova.mjs.news.service.NewsService;
import nova.mjs.util.response.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/news")
@RequiredArgsConstructor
public class NewsController {
    private final NewsService newsService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<NewsResponseDTO>>> getNewsByCategory(
            @RequestParam String category,
            @RequestParam(defaultValue = "0") int page,  // 기본 페이지 번호
            @RequestParam(defaultValue = "10") int size  // 기본 페이지 크기 (10개)
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<NewsResponseDTO> newsPage = newsService.getNewsByCategory(category, pageable);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(newsPage));
    }

    @PostMapping("/fetch")
    public ResponseEntity<ApiResponse<List<NewsResponseDTO>>> crawlAndSaveNews(
            @RequestParam(required = false) String category) {

        List<NewsResponseDTO> savedNews = newsService.crawlAndSaveNews(category);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(savedNews));
    }

    @DeleteMapping("/delete")
    public void deleteAllNews(@RequestParam(required = false) String category) {
        newsService.deleteAllNews(category);
    }
}
