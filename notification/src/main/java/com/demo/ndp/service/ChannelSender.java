package com.demo.ndp.service;

import com.demo.ndp.model.Notification;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class ChannelSender {

    public void send(Notification notification) throws Exception{
        //mock 20% failure rate
        if(Math.random() < 0.2){
            throw new RuntimeException("Sample Gateway failure");
        }

        System.out.println("Sent " + notification.getChannel() + " to user " + notification.getUserId() + ": " + notification.getMessage());
    }
}
