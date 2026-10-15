package seed.seedplusbackend.support.application;

import java.util.List;
import org.springframework.stereotype.Component;
import seed.seedplusbackend.support.domain.entity.Faq;
import seed.seedplusbackend.support.domain.entity.FaqCategory;
import seed.seedplusbackend.support.domain.entity.FaqGroup;

@Component
public class FaqCatalog {

  private static final List<Faq> FAQS =
      List.of(
          faq(1, FaqGroup.GENERAL, FaqCategory.SERVICE_USE, "SEED+는 어떤 서비스인가요?", "SEED+는 점포형 창업을 준비하는 사용자가 창업과 점포 선택 과정에서 필요한 정보를 데이터 기반으로 확인하고, 보다 나은 의사결정을 할 수 있도록 돕는 서비스입니다."),
          faq(2, FaqGroup.GENERAL, FaqCategory.SERVICE_USE, "SEED+는 누가 이용할 수 있나요?", "점포형 창업을 준비하고 있거나 창업과 상권, 점포 선택에 필요한 정보를 확인하고 싶은 사용자가 이용할 수 있습니다."),
          faq(3, FaqGroup.GENERAL, FaqCategory.SERVICE_USE, "SEED+에서는 어떤 정보를 확인할 수 있나요?", "SEED+에서는 서비스에서 제공하는 기능을 통해 상권과 점포 선택에 필요한 다양한 정보를 확인할 수 있습니다. 제공되는 정보와 기능은 서비스 업데이트에 따라 변경되거나 추가될 수 있습니다."),
          faq(4, FaqGroup.GENERAL, FaqCategory.SERVICE_USE, "SEED+를 이용하려면 회원가입이 필요한가요?", "네, SEED+는 회원가입 후 이용할 수 있습니다."),
          faq(5, FaqGroup.GENERAL, FaqCategory.ANALYSIS, "분석 결과는 실제 창업 성공을 보장하나요?", "SEED+에서 제공하는 분석 결과는 서비스 이용자의 의사결정을 돕기 위한 참고 정보입니다. 실제 창업 결과는 업종, 입지, 운영 방식, 시장 상황 등 다양한 요인의 영향을 받을 수 있으므로 분석 결과가 특정 결과를 보장하지는 않습니다."),
          faq(6, FaqGroup.GENERAL, FaqCategory.ANALYSIS, "수익률 관련 정보는 어떻게 활용하면 되나요?", "SEED+에서 제공하는 수익률 관련 정보는 창업과 점포 선택 과정에서 참고할 수 있는 정보입니다. 실제 투자 및 운영 환경에 따라 결과가 달라질 수 있으므로 최종 의사결정 시에는 개별 조건을 함께 검토하는 것이 필요합니다."),
          faq(7, FaqGroup.GENERAL, FaqCategory.ANALYSIS, "생존율은 무엇을 의미하나요?", "SEED+에서 제공하는 생존율 관련 정보는 업종과 사업 환경을 이해하고 창업 의사결정에 참고할 수 있도록 제공되는 분석 정보입니다. 개별 사업장의 실제 운영 결과를 직접적으로 보장하거나 예측하는 것은 아닙니다."),
          faq(8, FaqGroup.GENERAL, FaqCategory.SERVICE_USE, "내 상가 만들기 기능은 무엇인가요?", "내 상가 만들기는 사용자가 관심 있는 점포 또는 창업 조건과 관련된 정보를 입력하고 저장하여 자신의 창업 후보를 관리할 수 있도록 돕는 기능입니다."),
          faq(9, FaqGroup.GENERAL, FaqCategory.SERVICE_USE, "여러 후보지를 비교할 수 있나요?", "마이페이지에서 저장하여 1대1로 비교할 수 있습니다."),
          faq(10, FaqGroup.GENERAL, FaqCategory.ANALYSIS, "SEED+에서 제공하는 정보는 실시간 정보인가요?", "SEED+에서 제공하는 정보는 데이터 종류와 서비스 기능에 따라 갱신 주기가 다를 수 있습니다. 따라서 모든 정보가 동일한 주기로 실시간 제공되는 것은 아닙니다."),
          faq(11, FaqGroup.GENERAL, FaqCategory.ACCOUNT_AND_OTHER, "서비스 이용 중 오류가 발생했어요.", "서비스 이용 중 오류가 발생한 경우 문의하기 페이지에서 서비스 오류 신고를 선택하여 문의 내용을 작성해 주세요. 확인 후 필요한 조치를 진행합니다."),
          faq(12, FaqGroup.GENERAL, FaqCategory.ACCOUNT_AND_OTHER, "서비스에 대한 의견이나 기능을 제안하고 싶어요.", "SEED+ 서비스에 대한 의견이나 개선 사항은 문의하기를 통해 전달할 수 있습니다. 보내주신 의견은 서비스 개선을 위한 참고 자료로 활용될 수 있습니다."),
          faq(13, FaqGroup.GENERAL, FaqCategory.ACCOUNT_AND_OTHER, "개인정보는 어떻게 처리되나요?", "SEED+는 서비스 제공을 위해 필요한 범위에서 개인정보를 처리하며, 개인정보 처리와 관련된 자세한 내용은 서비스 내 개인정보 관련 정책에서 확인할 수 있습니다."),
          faq(14, FaqGroup.GENERAL, FaqCategory.ACCOUNT_AND_OTHER, "회원정보를 변경하거나 서비스 이용을 중단하려면 어떻게 하나요?", "회원정보 변경 및 서비스 이용 종료와 관련된 기능과 절차는 현재 서비스에서 제공하는 방식에 따라 진행할 수 있습니다. 필요한 기능이 제공되지 않는 경우 문의하기를 통해 문의할 수 있습니다."),
          faq(15, FaqGroup.GENERAL, FaqCategory.SERVICE_USE, "SEED+ 서비스는 계속 업데이트되나요?", "SEED+는 서비스 개선과 기능 고도화를 진행하고 있으며, 주요 변경 사항이나 서비스 관련 안내는 공지사항을 통해 확인할 수 있습니다."),
          faq(16, FaqGroup.PARTNER, FaqCategory.COOPERATION, "SEED+와 공인중개사는 어떤 방식으로 협력할 수 있나요?", "SEED+와의 협력 방식은 서비스 운영 방향과 협력 목적에 따라 달라질 수 있습니다. 공인중개사 협력 또는 제휴와 관련된 자세한 문의는 문의하기를 통해 전달할 수 있습니다."),
          faq(17, FaqGroup.PARTNER, FaqCategory.COOPERATION, "공인중개사 제휴는 어떻게 신청하나요?", "SEED+와의 제휴를 희망하는 경우 문의하기 페이지에서 제휴 관련 문의를 선택하여 기본 정보를 남겨주세요. 문의 내용을 확인한 후 필요한 경우 별도로 안내드립니다."),
          faq(18, FaqGroup.PARTNER, FaqCategory.COOPERATION, "전문가 또는 파트너로 SEED+와 협력할 수 있나요?", "전문가 및 다양한 분야의 파트너와의 협력 가능성은 서비스 운영 방향과 협력 내용에 따라 검토될 수 있습니다. 협력을 희망하는 경우 문의하기를 통해 제안 내용을 전달할 수 있습니다."),
          faq(19, FaqGroup.PARTNER, FaqCategory.COOPERATION, "SEED+와의 협력 관련 문의는 어디에서 할 수 있나요?", "SEED+ 홈페이지의 문의하기 페이지에서 협력 및 제휴 관련 문의를 남길 수 있습니다."),
          faq(20, FaqGroup.PARTNER, FaqCategory.COOPERATION, "협력 또는 제휴 문의 후 어떻게 진행되나요?", "문의 내용을 확인한 후 협력 가능 여부와 필요 사항에 따라 개별적으로 안내드립니다."));

  public List<Faq> getFaqs() {
    return FAQS;
  }

  private static Faq faq(
      long id, FaqGroup group, FaqCategory category, String question, String answer) {
    return new Faq(id, group, category, question, answer, Math.toIntExact(id));
  }
}
