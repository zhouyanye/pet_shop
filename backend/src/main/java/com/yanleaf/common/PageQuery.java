package com.yanleaf.common;

import lombok.Data;

@Data

public class PageQuery {
    /**
     分页
     */
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private String keyword;
    private Integer categoryId;
    private Integer status;
    private String sortBy;
    private String order;
    private String tag;
    private java.math.BigDecimal minPrice;
    private java.math.BigDecimal maxPrice;

}