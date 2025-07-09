package nova.mjs.admin.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import nova.mjs.admin.DTO.AdminDTO;
import nova.mjs.admin.entity.Admin;
import nova.mjs.admin.exception.AdminIdMismatchException;
import nova.mjs.admin.repository.AdminRepository;
import nova.mjs.util.jwt.JwtUtil;
import nova.mjs.util.s3.S3Service;
import nova.mjs.util.security.AuthDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Log4j2
public class AdminService {
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final S3Service s3Service;

    @Value("${s3.path.custom.admin-temp}")
    private String adminTempPrefix;

    @Value("${s3.path.custom.admin-logo}")
    private String adminPostPrefix;


    @Transactional
    public AuthDTO.LoginResponseDTO createAdmin(AdminDTO.AdminRequestDTO requestDTO) {

        // adminId 일치 여부
        adminRepository.findByAdminId(requestDTO.getAdminId())
                .orElseThrow(() -> {
                    log.warn("[사전 등록된 Admin ID와 사용자가 제출한 정보가 일치하지 않음] AdminId: {}", requestDTO.getAdminId());
                    return new AdminIdMismatchException();
                });

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(requestDTO.getPassword());

        // admin 회원가입 정보 생성
        Admin admin = Admin.create(
                requestDTO.getAdminId(),
                requestDTO.getDepartment(),
                requestDTO.getIntroduction(),
                requestDTO.getHomepageUrl(),
                requestDTO.getInstagramUrl(),
                requestDTO.getLogoImageUrl(),
                requestDTO.getStudentUnionName()
        );
        adminRepository.save(admin);

        // 이미지 이동 처리 (temp → post)
        log.info("[이미지 이동 시작] 총 이미지 수: {}", requestDTO.getLogoImageUrl());

        String logoImageUrl = requestDTO.getLogoImageUrl();
        String tempImageKey = s3Service.extractKeyFromUrl(logoImageUrl);

        if (tempImageKey.startsWith(adminTempPrefix)) {
            String fileName = tempImageKey.substring(tempImageKey.lastIndexOf('/') + 1);
            String realKey = adminPostPrefix + admin.getUuid() + "/" + fileName;

            log.info("[로고 이미지 복사] from: {}, to: {}", tempImageKey, realKey);

            s3Service.copyFile(tempImageKey, realKey);
        }

        log.info("[이미지 이동 완료] 회원 UUID: {}", admin.getUuid());
        return null;
    }
}
