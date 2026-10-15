package seed.seedplusbackend.support.domain.entity;

public record Faq(
    Long id, FaqGroup group, FaqCategory category, String question, String answer, int displayOrder) {}
