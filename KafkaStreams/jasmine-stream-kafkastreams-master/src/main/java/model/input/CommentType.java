package model.input;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * CommentType enum
 * */
public enum CommentType {
    @JsonProperty("comment")
    COMMENT,
    @JsonProperty("userReply")
    USER_REPLY
    ;
}
