package com.demo.ndp.service;

import com.demo.ndp.config.Rule;
import com.demo.ndp.config.RulesConfig;
import com.demo.ndp.model.Decision;
import com.demo.ndp.model.DecisionType;
import com.demo.ndp.model.Notification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CEPService {

    private final RulesConfig rulesConfig;
    private final Map<String, List<Notification>> cache = new HashMap<>();

    public CEPService(RulesConfig rulesConfig){
        this.rulesConfig = rulesConfig;
    }

    public Decision evaluate(Notification notification){
        String key = notification.getUserId() + ":" + notification.getChannel();
        LocalDateTime now = LocalDateTime.now();
        cache.computeIfAbsent(key, k -> new ArrayList<>());

        for(Rule rule: rulesConfig.getRules()){
            switch (rule.getType()) {
                case "RATE_LIMIT":
                    //check for limiting frequency of messages for a specific channel
                    if(rule.getChannel() != null && rule.getChannel().equalsIgnoreCase(notification.getChannel())){
                        long count = cache.get(key).stream()
                                .filter(n -> n.getCreatedAt().isAfter(now.minusMinutes(rule.getWindowMinutes())))
                                .count();
                        if(count >= rule.getLimit()) return new Decision(DecisionType.DROP);
                    }
                    break;
                case "DEDUP":
                    //check for duplicate messages
                    boolean duplicate = cache.get(key).stream().anyMatch(n -> n.getMessage().equals(notification.getMessage()))
                            && notification.getCreatedAt().isAfter(now.minusMinutes(rule.getWindowMinutes()));
                    if(duplicate) return new Decision(DecisionType.DROP);
                    break;
                case "NIGHT_BLOCK":
                    //check for snooze time based on country
                    if(rule.getCountry() != null && rule.getCountry().equalsIgnoreCase(notification.getCountry())){
                        int hour = now.getHour();
                        if(hour >= rule.getStartHour() || hour < rule.getEndHour()){
                            LocalDateTime nextMorning = now.withHour(rule.getEndHour()).withMinute(0).withSecond(0);
                            if(hour >= rule.getStartHour()){
                                nextMorning = nextMorning.plusDays(1);
                            }
                            return new Decision(DecisionType.DELAY, nextMorning);
                        }
                    }
                    break;
            }

        }
        cache.get(key).add(notification);
        return new Decision(DecisionType.ALLOW);
    }
}
