package com.example.backend.service;

import com.example.backend.entity.Bot;
import com.example.backend.repository.BotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class BotService {

    @Autowired
    private BotRepository botRepository;

    public void saveBot(Bot bot) {
        botRepository.save(bot);
    }

    public Optional<Bot> findBot(Long id) {
        return botRepository.findById(id);
    }

    public Optional<Bot> findBot(long id) {
        return botRepository.findById(Long.valueOf(id));
    }

    public Optional<Bot> findBotName(String name) {
        return botRepository.findByName(name);
    }

    public Long exist(String name) {
        var bot = findBotName(name);
        if (bot.isPresent()) return bot.get().getId();
        return null;
    }

}
