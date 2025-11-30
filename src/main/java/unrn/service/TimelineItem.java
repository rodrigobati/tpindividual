package unrn.service;

import unrn.model.ReTweet;
import unrn.model.Tweet;

import java.time.LocalDateTime;

/**
 * Representa un item del timeline que puede ser:
 * - Un tweet original
 * - Un retweet (con referencia al tweet original)
 * 
 * Permite unificar tweets y retweets en una sola colección ordenable.
 */
public class TimelineItem {

    private final Tweet tweetOriginal;
    private final ReTweet retweet; // null si es tweet original
    private final LocalDateTime fechaParaOrdenamiento;

    // Constructor para tweet original
    private TimelineItem(Tweet tweet) {
        this.tweetOriginal = tweet;
        this.retweet = null;
        this.fechaParaOrdenamiento = tweet.fechaCreacion();
    }

    // Constructor para retweet
    private TimelineItem(ReTweet retweet) {
        this.tweetOriginal = retweet.original();
        this.retweet = retweet;
        this.fechaParaOrdenamiento = retweet.fechaCreacion();
    }

    public static TimelineItem deTweet(Tweet tweet) {
        return new TimelineItem(tweet);
    }

    public static TimelineItem deRetweet(ReTweet retweet) {
        return new TimelineItem(retweet);
    }

    public boolean esRetweet() {
        return retweet != null;
    }

    public Tweet getTweetOriginal() {
        return tweetOriginal;
    }

    public ReTweet getRetweet() {
        return retweet;
    }

    public LocalDateTime getFechaParaOrdenamiento() {
        return fechaParaOrdenamiento;
    }

    /**
     * Nombre del usuario que "publica" este item en el timeline:
     * - Para tweets: el autor del tweet
     * - Para retweets: el autor del retweet (quien lo compartió)
     */
    public String getAutorEnTimeline() {
        if (esRetweet()) {
            return retweet.autor().nombreUsuario();
        }
        return tweetOriginal.autor().nombreUsuario();
    }
}
