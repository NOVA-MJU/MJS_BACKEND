package nova.mjs.admin.department.department_schedule.service;

import lombok.RequiredArgsConstructor;
import nova.mjs.util.s3.S3DomainType;
import nova.mjs.util.s3.S3ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminDepartmentScheduleImageService {

    private final S3ServiceImpl s3ServiceImpl;

    //학과 일정 이미지 업로드
    public String uploadDepartmentScheduleImage(MultipartFile file, UUID scheduleUuid) throws IOException {
        return s3ServiceImpl.uploadFile(file, S3DomainType.DEPARTMENT_SCHEDULE, scheduleUuid);
    }

}
