package com.stgsporting.piehmecup.dtos.players;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class PlayerUploadDTO {
    private Long id;
    private String name;
    private String position;
    private Integer rating;
    private Boolean available;
    private MultipartFile image;
    private Integer price;
    private String club;
    private String league;
    private String nationality;
}
