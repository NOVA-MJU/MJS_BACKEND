package nova.mjs.util.s3;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3Service {

    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.cloudfront.url}")
    private String cloudFrontUrl;


    /**
    uploadFileAndGetUrl:	파일 업로드 + CloudFront URL 반환
    listKeys(prefix):	특정 prefix(폴더) 아래 S3 key 목록 조회
    copyFile(oldKey, newKey):	S3 객체 복사 (임시 → 실제 위치 이동)
    deleteFile(key):	단일 파일 삭제
    deleteFolder(prefix):	폴더 내 전체 파일 삭제
    moveFolder(from, to):	폴더 전체 이동 (copy + delete)
    extractKeyFromUrl:	이미지 URL로부터 S3 key 추출
    */

    public String uploadFileAndGetUrl(MultipartFile file, String key) throws IOException {
        String originalFileName = file.getOriginalFilename();
        String safeFileName = URLEncoder.encode(originalFileName, StandardCharsets.UTF_8); // URL-safe 처리
        String finalKey = key.substring(0, key.lastIndexOf("/") + 1) + safeFileName;

        log.info("[S3 업로드 요청] 원본 파일명: {}, 인코딩 파일명: {}", originalFileName, safeFileName);

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(finalKey)
                .contentType(file.getContentType())
                .build();

        s3Client.putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

        log.info("[S3 업로드 완료] key: {}", finalKey);
        return cloudFrontUrl + "/" + finalKey;
    }
    // 전체 폴더 이동
    public void moveFolder(String fromPrefix, String toPrefix) {
        List<String> keys = listKeys(fromPrefix);
        for (String oldKey : keys) {
            String newKey = oldKey.replace(fromPrefix, toPrefix);
            copyFile(oldKey, newKey);
            deleteFile(oldKey);
        }
    }


    public List<String> listKeys(String prefix) {
        log.info("[S3 Key 목록 조회] prefix: {}", prefix);
        ListObjectsV2Request request = ListObjectsV2Request.builder()
                .bucket(bucket)
                .prefix(prefix)
                .build();
        ListObjectsV2Response response = s3Client.listObjectsV2(request);
        List<String> keys = response.contents().stream()
                .map(S3Object::key)
                .toList();

        log.info("[S3 Key 목록 조회 완료] prefix: {}, keys: {}", prefix, keys);

        return keys;
    }



    public void copyFile(String oldKey, String newKey) {
        log.info("[S3 복사 시작] from: {}, to: {}", oldKey, newKey);

        CopyObjectRequest copyRequest = CopyObjectRequest.builder()
                .sourceBucket(bucket)
                .sourceKey(oldKey)
                .destinationBucket(bucket)
                .destinationKey(newKey)
                .build();

        s3Client.copyObject(copyRequest);

        log.info("[S3 복사 완료] to: {}", newKey);
    }


    public void deleteFile(String key) {
        log.info("[S3 삭제 요청] key: {}", key);

        DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        try {
            s3Client.deleteObject(deleteRequest);
            log.info("[S3 삭제 완료] key: {}", key);
        } catch (S3Exception e) {
            log.error("[S3 삭제 실패] key: {}, message: {}", key, e.awsErrorDetails().errorMessage());
            throw e;
        }
    }


    public void deleteFolder(String prefix) {
        log.info("[S3 폴더 삭제 시작] prefix: {}", prefix);
        List<String> keys = listKeys(prefix);
        log.info("[S3 폴더 내 파일 수]: {}", keys.size());

        for (String key : keys) {
            deleteFile(key);
        }

        log.info("[S3 폴더 삭제 완료] prefix: {}", prefix);
    }


    public String extractKeyFromUrl(String imageUrl) {
        log.info("[CloudFront URL → S3 key 변환 요청] imageUrl: {}", imageUrl);
        if (!imageUrl.startsWith(cloudFrontUrl)) {
            throw new IllegalArgumentException("Invalid CloudFront URL: " + imageUrl);
        }
        String key = imageUrl.replace(cloudFrontUrl + "/", "");
        log.info("[변환된 S3 key]: {}", key);

        return key;
    }


}
