package nova.mjs.domain.weeklyMenu.controller;

import lombok.*;
import nova.mjs.util.response.ApiResponse;
import nova.mjs.domain.weeklyMenu.DTO.WeeklyMenuResponseDTO;
import nova.mjs.domain.weeklyMenu.service.WeeklyMenuService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/menus")
@RequiredArgsConstructor
public class WeeklyMenuController {

    private final WeeklyMenuService menuService;

    @PostMapping("/crawling")
    public ResponseEntity<ApiResponse<List<WeeklyMenuResponseDTO>>> crawlMenu() {
        List<WeeklyMenuResponseDTO> menu = menuService.crawlWeeklyMenu();
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(menu));
    }

    @GetMapping()
    public ResponseEntity<ApiResponse<List<WeeklyMenuResponseDTO>>> getAllMenu() {
        List<WeeklyMenuResponseDTO> menu = menuService.getAllWeeklyMenus();
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(menu));
    }
}