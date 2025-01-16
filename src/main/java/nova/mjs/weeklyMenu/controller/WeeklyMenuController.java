package nova.mjs.weeklyMenu.controller;

import lombok.*;
import nova.mjs.community.DTO.CommunityBoardResponse;
import nova.mjs.util.response.ApiResponse;
import nova.mjs.weeklyMenu.DTO.WeeklyMenuResponse;
import nova.mjs.weeklyMenu.entity.WeeklyMenu;
import nova.mjs.weeklyMenu.service.WeeklyMenuService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/mjs")
@RequiredArgsConstructor
public class WeeklyMenuController {

    private final WeeklyMenuService menuService;

    @GetMapping("/weeklymenu")
    public ResponseEntity<ApiResponse<WeeklyMenuResponse>> crawlMenu() {
        WeeklyMenuResponse menu = (WeeklyMenuResponse) menuService.crawlWeeklyMenu();
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(menu));
    }
}