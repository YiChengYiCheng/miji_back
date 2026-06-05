package com.common.QO.note;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

@Data
public class UpdateNoteQO implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotNull(message = "id cannot be null")
    private Long id;

    private String title;

    private String content;

    private String cover;

    private List<String> images;

    private Integer viewCount;

    private Integer likeCount;

    private Integer collectCount;

    private Integer commentCount;
}
