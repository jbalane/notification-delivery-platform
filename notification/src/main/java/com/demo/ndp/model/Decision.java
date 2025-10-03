package com.demo.ndp.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class Decision {
    private DecisionType type;
    private LocalDateTime delayUntil;

    public Decision(DecisionType type){
        this.type = type;
    }

    public Decision(DecisionType type, LocalDateTime delayUntil) {
        this.type = type;
        this.delayUntil = delayUntil;
    }

}
