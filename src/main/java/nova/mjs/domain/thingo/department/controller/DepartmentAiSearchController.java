package nova.mjs.domain.thingo.department.controller;

import lombok.RequiredArgsConstructor;
import nova.mjs.domain.thingo.department.search.DepartmentAiSearchDTO;
import nova.mjs.domain.thingo.department.search.DepartmentAiSearchService;
import nova.mjs.util.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/ai/departments")
public class DepartmentAiSearchController {

    private final DepartmentAiSearchService searchService;

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<DepartmentAiSearchDTO.Response>> search(
            @RequestParam String query,
            @RequestParam(defaultValue = "AUTO") DepartmentAiSearchDTO.Category category
    ) {
        return ResponseEntity.ok(ApiResponse.success(searchService.search(query, category)));
    }
}
