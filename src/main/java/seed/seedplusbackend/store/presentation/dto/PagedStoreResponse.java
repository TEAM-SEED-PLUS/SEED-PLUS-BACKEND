package seed.seedplusbackend.store.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.springframework.data.domain.Page;
import seed.seedplusbackend.global.response.PageInfo;
import seed.seedplusbackend.store.domain.entity.Store;

@Schema(description = "점포 페이지 응답")
public record PagedStoreResponse(
    @Schema(description = "점포 목록") List<StoreResponse> content,
    @Schema(description = "페이지 정보") PageInfo pageInfo) {

  public static PagedStoreResponse from(Page<Store> page) {
    return new PagedStoreResponse(
        page.getContent().stream().map(StoreResponse::from).toList(),
        PageInfo.of(page.getNumber(), page.getSize(), page.getTotalElements()));
  }
}
