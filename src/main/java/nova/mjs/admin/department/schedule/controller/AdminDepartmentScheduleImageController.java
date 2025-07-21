package nova.mjs.admin.department.schedule.controller;

import lombok.RequiredArgsConstructor;
import nova.mjs.admin.department.schedule.service.AdminDepartmentScheduleImageService;
import nova.mjs.util.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/department-schedules/images")
@RequiredArgsConstructor
public class AdminDepartmentScheduleImageController {

    private final AdminDepartmentScheduleImageService imageService;

    @PostMapping
    public ResponseEntity<ApiResponse<String>> uploadImage(
            @RequestParam MultipartFile file,
            @RequestParam UUID scheduleUuid) throws IOException {

        String imageUrl = imageService.uploadDepartmentScheduleImage(file, scheduleUuid);
        return ResponseEntity
                .ok(ApiResponse.success(imageUrl));
    }
}
