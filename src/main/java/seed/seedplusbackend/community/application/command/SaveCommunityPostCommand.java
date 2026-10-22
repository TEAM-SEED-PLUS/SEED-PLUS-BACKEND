package seed.seedplusbackend.community.application.command;

import java.util.List;

public record SaveCommunityPostCommand(
    String title, String content, String location, List<Long> regionIds, List<Long> industryIds) {}
