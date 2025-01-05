package nova.mjs.weeklyMenu.controller;

import lombok.*;
import nova.mjs.weeklyMenu.entity.WeeklyMenu;
import nova.mjs.weeklyMenu.service.WeeklyMenuService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/mjs")
@RequiredArgsConstructor
public class WeeklyMenuController {

    private final WeeklyMenuService menuService;

    @GetMapping("/weeklymenu")
    public List<WeeklyMenu> crawlMenu() {
        return menuService.crawlWeeklyMenu();
    }
}