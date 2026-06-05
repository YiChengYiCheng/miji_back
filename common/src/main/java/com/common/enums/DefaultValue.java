package com.common.enums;

import java.math.BigDecimal;

public class DefaultValue {

    // 占位默认值
    public static final Integer DEFAULT_ID = 1;
    // 杜绝魔法值
    public static final Integer NUM_ONE = 1;
    public static final Integer NUM_ZERO = 0;
    public static final Integer NUM_TWO = 2;
    public static final Integer NUM_ONE_HUNDRED = 100;
    public static final Integer NUM_NEGATIVE_ONE = -1;
    public static final String STR_DEFAULT = "DEFAULT";
    public static final Integer NUM_PAGE = 1;
    public static final Integer NUM_SIZE = 10;

    public static final Integer DEFAULT_WHITE_BALANCE_RED = 1500;
    public static final Integer DEFAULT_WHITE_BALANCE_GREEN = 1000;
    public static final Integer DEFAULT_WHITE_BALANCE_BLUE = 2500;

    public static final Integer DEFAULT_EXPOSURE_TIME = 50000;
//    public static final Integer DEFAULT_EXPOSURE_TIME = -1;
    public static final Integer DEFAULT_GAIN = 22;
//    public static final Integer DEFAULT_GAIN = -1;

    public static final Integer DEFAULT_FOCAL_LENGTH = 24;
    public static final Integer DEFAULT_OBJECT_DISTANCE = 200;
    //初始密码
    public static final String DEFAULT_PASSWORD = "123456";
    //初始状态
    public static final Integer DEFAULT_STATUS = 1;
    public static final String USER_PRE = "user_";

    public static final BigDecimal VIEW_SCORE_WEIGHT = BigDecimal.valueOf(0.2);
    public static final BigDecimal LIKE_SCORE_WEIGHT = BigDecimal.valueOf(0.4);
    public static final BigDecimal COLLECT_SCORE_WEIGHT = BigDecimal.valueOf(0.3);
    public static final BigDecimal COMMENT_SCORE_WEIGHT = BigDecimal.valueOf(0.1);

}
