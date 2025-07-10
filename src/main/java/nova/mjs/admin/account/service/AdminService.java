package nova.mjs.admin.account.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import nova.mjs.admin.account.DTO.AdminDTO;
import nova.mjs.admin.account.entity.Admin;
import nova.mjs.admin.account.exception.AdminIdMismatchException;
import nova.mjs.admin.account.exception.InvalidRequestException;
import nova.mjs.admin.account.repository.AdminRepository;
import nova.mjs.admin.account.exception.PasswordIsInvalidException;
import nova.mjs.util.exception.ErrorCode;
import nova.mjs.util.jwt.JwtUtil;
import nova.mjs.util.s3.S3Service;
import nova.mjs.util.security.AuthDTO;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Log4j2
public class AdminService {
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final S3Service s3Service;


    @Transactional
    public void preRegisterAdminId(String adminId) {
        Admin admin = Admin.builder()
                .adminId(adminId)
                .uuid(UUID.randomUUID())
                .password("") // 비밀번호는 따로 설정
                .studentUnionName("")
                .department("")
                .logoImageUrl("")
                .role(Admin.Role.ADMIN)
                .build();
        adminRepository.save(admin);
    }


    @Transactional
    public void updateAdminInfoWithImage(String adminId, AdminDTO.AdminRequestDTO dto, MultipartFile file) throws IOException {
        Admin admin = adminRepository.findByAdminId(adminId)
                .orElseThrow(AdminIdMismatchException::new);

        if (!StringUtils.hasText(dto.getDepartment())) {
            throw new InvalidRequestException("학과명은 비워둘 수 없습니다.", ErrorCode.INVALID_REQUEST);
        }
        if (!StringUtils.hasText(dto.getStudentUnionName())) {
            throw new InvalidRequestException("학생회 이름은 비워둘 수 없습니다.", ErrorCode.INVALID_REQUEST);
        }

        // 이미지가 있을 경우 S3 업로드 → CloudFront URL 생성
        if (!StringUtils.hasText(file.getOriginalFilename())) {
            throw new InvalidRequestException("로고 이미지는 필수입니다.", ErrorCode.INVALID_REQUEST);
        }
        log.info("[어드민 로고 이미지 업데이트 감지] adminId = {}", adminId);
        String keyPrefix = "admin/logo/" + admin.getUuid() + "/";
        String logoImageUrl = s3Service.uploadFile(file, keyPrefix);

        admin.updateInfo(
                dto.getDepartment(),
                dto.getStudentUnionName(),
                dto.getHomepageUrl(),
                dto.getInstagramUrl(),
                dto.getIntroduction(),
                logoImageUrl
        );
    }

    @Transactional
    public AuthDTO.LoginResponseDTO updatePassword(String adminId, String rawPassword) {
        Admin admin = adminRepository.findByAdminId(adminId)
                .orElseThrow(AdminIdMismatchException::new);

        String encodedPassword = passwordEncoder.encode(rawPassword);
        admin.updatePassword(encodedPassword);

        UUID userId = admin.getUuid();
        String role = admin.getRole().name();

        String accessToken = jwtUtil.generateAccessToken(userId, adminId, role);
        String refreshToken = jwtUtil.generateRefreshToken(userId, adminId);

        return AuthDTO.LoginResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Transactional(readOnly = true)
    public AuthDTO.LoginResponseDTO login(String adminId, String rawPassword) {
        Admin admin = adminRepository.findByAdminId(adminId)
                .orElseThrow(AdminIdMismatchException::new);

        if (!passwordEncoder.matches(rawPassword, admin.getPassword())) {
            throw new InvalidRequestException("비밀번호가 일치하지 않습니다.", ErrorCode.INVALID_REQUEST);
        }

        UUID uuid = admin.getUuid();
        String role = admin.getRole().name();

        String accessToken = jwtUtil.generateAccessToken(uuid, adminId, role);
        String refreshToken = jwtUtil.generateRefreshToken(uuid, adminId);

        return AuthDTO.LoginResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Transactional
    public void deleteAdmin(String adminId, AdminDTO.PasswordRequestDTO requestPassword) {
        Admin admin = adminRepository.findByAdminId(adminId)
                .orElseThrow(AdminIdMismatchException::new);

        if (!passwordEncoder.matches(requestPassword.getPassword(), admin.getPassword())) {
            throw new PasswordIsInvalidException();
        }

        adminRepository.delete(admin);
        log.info("관리자 계정 삭제 - adminId: {}", adminId);
    }
}
