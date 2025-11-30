package unrn.api.dto;

public record RetweetResponse(
        Long id,
        String autorRetweet,
        TweetResponse tweetOriginal,
        String fechaRetweet) {
}
