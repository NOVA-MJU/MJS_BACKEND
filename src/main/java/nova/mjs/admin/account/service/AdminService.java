//package nova.mjs.admin.account.service;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.log4j.Log4j2;
//import nova.mjs.admin.account.DTO.AdminDTO;
//import nova.mjs.admin.account.entity.StudentCouncilAdmin;
//import nova.mjs.admin.account.exception.AdminIdMismatchException;
//import nova.mjs.admin.account.exception.InvalidRequestException;
//import nova.mjs.admin.account.exception.PasswordIsInvalidException;
//import nova.mjs.admin.account.repository.AdminRepository;
//import nova.mjs.domain.department.entity.enumList.College;
//import nova.mjs.domain.department.entity.Department;
//import nova.mjs.domain.department.repository.DepartmentRepository;
//import nova.mjs.util.exception.ErrorCode;
//import nova.mjs.util.jwt.JwtUtil;
//import nova.mjs.util.s3.S3ServiceImpl;
//import nova.mjs.util.security.AuthDTO;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.IOException;
//import java.util.UUID;
//
//@Service
//@RequiredArgsConstructor
//@Log4j2
//public class AdminService {
//    private final AdminRepository adminRepository;
//    private final DepartmentRepository departmentRepository;
//    private final PasswordEncoder passwordEncoder;
//    private final JwtUtil jwtUtil;
//    private final S3ServiceImpl s3Service;
//
//    @Value("${s3.path.custom.admin-logo}")
//    private String adminLogo;
//
//    @Transactional
//    public void preRegisterAdminId(String adminId) {
//        Department department = Department.builder()
//                .departmentUuid(UUID.randomUUID())
//                .departmentName("")
//                .studentCouncilName("")
//                .studentCouncilLogo("")
//                .instagramUrl("")
//                .homepageUrl("")
//                .slogan("")
//                .description("")
//                .college(College.OTHER)
//                .build();
//
//        departmentRepository.save(department);
//
//        StudentCouncilAdmin admin = StudentCouncilAdmin.builder()
//                .adminId(adminId)
//                .uuid(UUID.randomUUID())
//                .password("")
//                .role(StudentCouncilAdmin.Role.ADMIN)
//                .department(department)
//                .build();
//
//        adminRepository.save(admin);
//    }
//
//    @Transactional
//    public void updateAdminInfoWithImage(String adminId, AdminDTO.AdminRequestDTO dto, MultipartFile file) throws IOException {
//        StudentCouncilAdmin admin = adminRepository.findByAdminId(adminId)
//                .orElseThrow(AdminIdMismatchException::new);
//
//        Department department = admin.getDepartment();
//
//        log.info("[어드민 로고 이미지 업데이트 감지] adminId = {}", adminId);
//        String keyPrefix = adminLogo + admin.getUuid() + "/";
//        String logoImageUrl = s3Service.uploadFile(file, keyPrefix);
//
//        department.updateInfo(
//                dto.getDepartmentName(),
//                dto.getStudentCouncilName(),
//                dto.getHomepageUrl(),
//                dto.getInstagramUrl(),
//                dto.getIntroduction(),
//                logoImageUrl,
//                dto.getSlogan(),
//                dto.getCollege()
//        );
//    }
//
//    @Transactional
//    public AuthDTO.LoginResponseDTO updatePassword(String adminId, String rawPassword) {
//        StudentCouncilAdmin admin = adminRepository.findByAdminId(adminId)
//                .orElseThrow(AdminIdMismatchException::new);
//
//        String encodedPassword = passwordEncoder.encode(rawPassword);
//        admin.updatePassword(encodedPassword);
//
//        String accessToken = jwtUtil.generateAccessToken(admin.getUuid(), adminId, admin.getRole().name());
//        String refreshToken = jwtUtil.generateRefreshToken(admin.getUuid(), adminId);
//
//        return AuthDTO.LoginResponseDTO.builder()
//                .accessToken(accessToken)
//                .refreshToken(refreshToken)
//                .build();
//    }
//
//    @Transactional(readOnly = true)
//    public AuthDTO.LoginResponseDTO login(String adminId, String rawPassword) {
//        StudentCouncilAdmin admin = adminRepository.findByAdminId(adminId)
//                .orElseThrow(AdminIdMismatchException::new);
//
//        if (!passwordEncoder.matches(rawPassword, admin.getPassword())) {
//            throw new InvalidRequestException("비밀번호가 일치하지 않습니다.", ErrorCode.INVALID_REQUEST);
//        }
//
//        String accessToken = jwtUtil.generateAccessToken(admin.getUuid(), adminId, admin.getRole().name());
//        String refreshToken = jwtUtil.generateRefreshToken(admin.getUuid(), adminId);
//
//        return AuthDTO.LoginResponseDTO.builder()
//                .accessToken(accessToken)
//                .refreshToken(refreshToken)
//                .build();
//    }
//
//    @Transactional
//    public void deleteAdmin(String adminId, AdminDTO.PasswordRequestDTO requestPassword) {
//        StudentCouncilAdmin admin = adminRepository.findByAdminId(adminId)
//                .orElseThrow(AdminIdMismatchException::new);
//
//        if (!passwordEncoder.matches(requestPassword.getPassword(), admin.getPassword())) {
//            throw new PasswordIsInvalidException();
//        }
//
//        adminRepository.delete(admin);
//        log.info("관리자 계정 삭제 - adminId: {}", adminId);
//    }
//}