package seed.seedplusbackend.builderstore.application.command;

public record CreateBuilderStoreCommentCommand(Long parentCommentId, String content) {}
