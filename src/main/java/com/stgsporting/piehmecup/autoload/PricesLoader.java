package com.stgsporting.piehmecup.autoload;

import com.stgsporting.piehmecup.entities.Price;
import com.stgsporting.piehmecup.repositories.LevelRepository;
import com.stgsporting.piehmecup.repositories.PriceRepository;
 import com.stgsporting.piehmecup.repositories.SchoolYearRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class PricesLoader implements CommandLineRunner {
    private final PriceRepository priceRepository;
    private final LevelRepository levelRepository;

    public PricesLoader(PriceRepository priceRepository, LevelRepository levelRepository) {
        this.priceRepository = priceRepository;
        this.levelRepository = levelRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (priceRepository.findAll().isEmpty()) {
            Map<String, Integer> prices = new HashMap<>(Map.of(
                    "Rating Price", 1100,
                    "Madares el Ahad", 1140,
                    "Odas", 1900,
                    "Odas El-Gom3a", 1900,
                    "Pisagi", 1140,
                    "Tasbeha", 1520
            ));

            levelRepository.findAllOrderById().forEach(level -> prices.forEach((name, price) -> {
                if (priceRepository.findPricesByNameAndLevel(name, level.getId()).isEmpty()) {
                    priceRepository.save(new Price(name, price, level));
                }
            }));
        }
    }
}
