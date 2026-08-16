package nova.mjs.domain.thingo.department.search;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import nova.mjs.domain.thingo.department.entity.enumList.College;
import nova.mjs.domain.thingo.department.entity.enumList.DepartmentName;

import java.time.LocalDateTime;
import java.util.List;

public class DepartmentAiSearchDTO {

    public enum Category {
        AUTO,
        BASIC,
        FOUNDATION,
        MAJOR,
        EVENT
    }

    public enum BlockType {
        PROFILE_CARD,
        TEXT_ANSWER,
        COURSE_LIST,
        EVENT_LIST,
        SOURCE_LIST
    }

    @Getter
    @Builder
    public static class Response {
        private String query;
        private Category category;
        private EntityRef entity;
        private List<Block> blocks;
    }

    @Getter
    @Builder
    public static class EntityRef {
        private String id;
        private College college;
        private DepartmentName departmentName;
        private String collegeLabel;
        private String departmentLabel;
        private String displayName;
    }

    @Getter
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Block {
        private BlockType type;
        private String title;
        private String text;
        private ProfileCard profile;
        private List<Item> items;
    }

    @Getter
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ProfileCard {
        private String academicOfficePhone;
        private String instagramUrl;
        private String homepageUrl;
        private String collectionStatus;
        private LocalDateTime verifiedAt;
    }

    @Getter
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Item {
        private String title;
        private String description;
        private String url;
        private LocalDateTime date;
        private String sourceType;
    }
}
