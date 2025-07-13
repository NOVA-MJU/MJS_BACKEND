package nova.mjs.domain.community.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import nova.mjs.util.s3.S3DomainType;
import nova.mjs.util.s3.S3ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * 커뮤니티 게시판 이미지 전용 서비스
 *
 * 게시글의 이미지 처리 (업로드, 삭제, 이동 등)을 담당합니다.
 * 게시판 서비스(CommunityBoardServiceImpl)와의 책임 분리를 통해 가독성과 유지보수성을 높입니다.
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class CommunityBoardImageService {

    private final S3ServiceImpl s3ServiceImpl;

    // 게시판 이미지용 S3 prefix (Enum 기반으로 일관성 있게 관리)
    private static final String TEMP_PREFIX = S3DomainType.COMMUNITY_TEMP.getPrefix();
    private static final String POST_PREFIX = S3DomainType.COMMUNITY_POST.getPrefix();


    public String uploadCommunityBoardImage(MultipartFile file, UUID folderUuid) throws IOException {
        return s3ServiceImpl.uploadFile(file, S3DomainType.COMMUNITY_POST, folderUuid);
    }
    /**
     * [게시글 작성] 시 temp 이미지 → post 이미지로 이동 (S3 내부 복사)
     *
     * @param imageUrls 업로드된 이미지 URL 목록
     * @param boardUuid 게시글 UUID (이미지 폴더명으로 사용)
     */
    public void moveTempImagesToPost(List<String> imageUrls, UUID boardUuid) {
        imageUrls.stream()
                .filter(url -> s3ServiceImpl.replaceCloudfrontUrlToS3Url(url).startsWith(TEMP_PREFIX))
                .forEach(tempImageUrl -> {
                    String tempKey = s3ServiceImpl.replaceCloudfrontUrlToS3Url(tempImageUrl);
                    String fileName = tempKey.substring(tempKey.lastIndexOf('/') + 1);
                    String realKey = POST_PREFIX + boardUuid + "/" + fileName;

                    log.info("[이미지 이동] from: {}, to: {}", tempKey, realKey);
                    s3ServiceImpl.copyFile(tempKey, realKey);
                    // s3Service.deleteFile(tempKey); // 필요시 주석 해제
                });
    }

    /**
     * [게시글 수정] 시 삭제된 이미지 S3에서도 제거
     *
     * @param oldImages 기존 이미지 목록
     * @param newImages 수정 후 이미지 목록
     */
    public void deleteRemovedImages(List<String> oldImages, List<String> newImages) {
        oldImages.stream()
                .filter(old -> !newImages.contains(old))
                .forEach(imageUrl -> {
                    String key = s3ServiceImpl.replaceCloudfrontUrlToS3Url(imageUrl);
                    log.info("[삭제 대상 이미지] key: {}", key);
                    s3ServiceImpl.deleteFile(key);
                });
    }

    /**
     * [게시글 수정] 시 새로 추가된 temp 이미지 → post로 복사 + temp 삭제
     *
     * @param newImages 수정 후 이미지 목록
     * @param oldImages 기존 이미지 목록
     * @param boardUuid 게시글 UUID
     */
    public void copyNewTempImages(List<String> newImages, List<String> oldImages, UUID boardUuid) {
        newImages.stream()
                .filter(newImg -> !oldImages.contains(newImg))
                .filter(newImg -> s3ServiceImpl.replaceCloudfrontUrlToS3Url(newImg).startsWith(TEMP_PREFIX))
                .forEach(tempImageUrl -> {
                    String tempKey = s3ServiceImpl.replaceCloudfrontUrlToS3Url(tempImageUrl);
                    String fileName = tempKey.substring(tempKey.lastIndexOf('/') + 1);
                    String realKey = POST_PREFIX + boardUuid + "/" + fileName;

                    log.info("[신규 이미지 복사] from: {}, to: {}", tempKey, realKey);
                    s3ServiceImpl.copyFile(tempKey, realKey);
                    s3ServiceImpl.deleteFile(tempKey);
                });
    }

    /**
     * [게시글 삭제] 시 게시글 이미지 폴더 삭제
     *
     * @param boardUuid 게시글 UUID
     */
    public void deletePostFolder(UUID boardUuid) {
        String postFolder = POST_PREFIX + boardUuid + "/";
        log.info("[이미지 폴더 삭제] path: {}", postFolder);
        s3ServiceImpl.deleteFolder(postFolder);
    }


}
