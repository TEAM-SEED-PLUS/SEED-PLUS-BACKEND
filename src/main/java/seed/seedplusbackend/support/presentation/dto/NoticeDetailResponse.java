package seed.seedplusbackend.support.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import seed.seedplusbackend.support.domain.entity.Notice;
import seed.seedplusbackend.support.domain.entity.NoticeCategory;

@Schema(description = "공지사항 상세")
public record NoticeDetailResponse(
    Long id, NoticeCategory category, String title, String content, boolean pinned, Instant publishedAt) {

  public static NoticeDetailResponse from(Notice notice) {
    return new NoticeDetailResponse(
        notice.getId(), notice.getCategory(), notice.getTitle(), notice.getContent(), notice.isPinned(), notice.getPublishedAt());
  }
}
