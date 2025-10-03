package com.demo.ndp.config;

import lombok.Data;

@Data
public class Rule {
    private String id;
    private String type;
    private String channel;
    private Integer limit;
    private Integer windowMinutes;
    private String country;
    private Integer startHour;
    private Integer endHour;
}
