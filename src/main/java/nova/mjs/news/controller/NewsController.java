package nova.mjs.news.controller;

import lombok.RequiredArgsConstructor;
import nova.mjs.news.DTO.NewsResponseDTO;
import nova.mjs.news.service.NewsService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/news")
@RequiredArgsConstructor
public class NewsController {
    private final NewsService newsService;

    @GetMapping
    public List<NewsResponseDTO> getNewsByCategory(@RequestParam String category) {
        return newsService.getNewsByCategory(category);
    }

    @PostMapping("/fetch")
    public List<NewsResponseDTO> crawlAndSaveNews(@RequestParam(required = false) String category) {
        return newsService.crawlAndSaveNews(category);
    }

    @DeleteMapping("/delete")
    public void deleteAllNews(@RequestParam(required = false) String category) {
        newsService.deleteAllNews(category);
    }
}
