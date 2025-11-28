package unrn.api.dto;

import java.util.List;

public record TimelineResponse(
        List<TweetResponse> tweets) {
}
