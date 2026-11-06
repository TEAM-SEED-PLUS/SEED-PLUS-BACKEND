package seed.seedplusbackend.support.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import seed.seedplusbackend.support.domain.entity.Notice;
import seed.seedplusbackend.support.domain.entity.NoticeCategory;

@Schema(description = "공지사항 목록 항목")
public record NoticeResponse(
    Long id, NoticeCategory category, String title, boolean pinned, Instant publishedAt) {

  public static NoticeResponse from(Notice notice) {
    return new NoticeResponse(
        notice.getId(),
        notice.getCategory(),
        notice.getTitle(),
        notice.isPinned(),
        notice.getPublishedAt());
  }
}
