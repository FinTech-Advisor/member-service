package org.advisor.admin.product.controller;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.apache.tomcat.jni.FileInfo;

import java.util.List;

@Data
public class RequestProduct {
    private String mode;
    private Long seq; // 상품 번호, 수정시 필요

    private boolean open; // true : 소비자페이지 상품 노출

    @NotBlank
    private String gid;

    @NotBlank
    private String name; // 상품명
    private String summary; // 상품 요약 설명
    private String description; // 상품 상세 설명

    private int consumerPrice; // 소비자가
    private int salePrice; // 판매가



    private List<FileInfo> mainImages; // 상품 상세 메인이미지

    private List<FileInfo> listImages; // 목록 이미지

    private List<FileInfo> editorImages; // 상세설명 이미지
}
