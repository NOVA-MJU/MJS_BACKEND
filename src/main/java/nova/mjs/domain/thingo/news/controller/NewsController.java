package nova.mjs.domain.thingo.news.controller;

import lombok.RequiredArgsConstructor;
import nova.mjs.domain.thingo.news.DTO.NewsResponseDTO;
import nova.mjs.domain.thingo.news.service.NewsService;
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
    public ResponseEntity<ApiResponse<Page<NewsResponseDTO>>> getNews(
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<NewsResponseDTO> newsPage = newsService.getNewsByCategory(category, pageable);
        return ResponseEntity.ok(ApiResponse.success(newsPage));
    }


    @PostMapping
    public ResponseEntity<ApiResponse<List<NewsResponseDTO>>> crawlAndSaveNews(
            @RequestParam(required = false) String category) {

        List<NewsResponseDTO> savedNews = newsService.crawlAndSaveNews(category);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(savedNews));
    }

    @DeleteMapping
    public void deleteAllNews(@RequestParam(required = false) String category) {
        newsService.deleteAllNews(category);
    }
}
