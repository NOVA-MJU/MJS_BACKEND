package nova.mjs.util.s3;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
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
import java.util.Objects;
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
    listKeys(prefix):	특정 prefix(폴더) 아래 S3 key 목록 조회
    copyFile(oldKey, newKey):	S3 객체 복사 (임시 → 실제 위치 이동)
    deleteFile(key):	단일 파일 삭제
    deleteFolder(prefix):	폴더 내 전체 파일 삭제l
    moveFolder(from, to):	폴더 전체 이동 (copy + delete)
    extractKeyFromUrl:	이미지 URL로부터 S3 key 추출
    */

    public String uploadFile(MultipartFile file, String keyPrefix) throws IOException {
        // 해시 기반 파일명 생성
        String extension = getExtension(Objects.requireNonNull(file.getOriginalFilename()));
        String fileHash = DigestUtils.sha256Hex(file.getInputStream()); // apache commons-codec 필요
        String finalKey = keyPrefix + fileHash + extension;

        log.info("[S3 업로드 요청] 파일 해시: {}, key: {}", fileHash, finalKey);

        // 이미 존재하는지 확인
        if (!doesObjectExist(finalKey)) {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(finalKey)
                    .contentType(file.getContentType())
                    .build();
            s3Client.putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            log.info("[S3 업로드 완료] key: {}", finalKey);
        } else {
            log.info("[S3에 동일한 파일 존재. 업로드 생략] key: {}", finalKey);
        }

        return cloudFrontUrl + "/" + finalKey;
    }

    public boolean doesObjectExist(String key) {
        try {
            HeadObjectRequest headRequest = HeadObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();
            s3Client.headObject(headRequest);
            return true;
        } catch (S3Exception e) {
            return false;
        }
    }


    public String uploadCommunityBoardImage(MultipartFile file, UUID tempFolderUuid) throws IOException {
        String keyPrefix = "boards/temp/" + tempFolderUuid + "/";
        return uploadFile(file, keyPrefix);
    }

    private String getExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf(".");
        return dotIndex != -1 ? fileName.substring(dotIndex) : "";
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
