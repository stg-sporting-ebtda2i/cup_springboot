package com.stgsporting.piehmecup.services;

import com.stgsporting.piehmecup.dtos.UserCardDTO;
import com.stgsporting.piehmecup.entities.SchoolYear;
import com.stgsporting.piehmecup.entities.User;
import com.stgsporting.piehmecup.exceptions.MinRatingException;
import com.stgsporting.piehmecup.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CardRatingService {
    private final PriceService priceService;
    private final UserService userService;
    private final WalletService walletService;
    private final FileService fileService;
    private static final int MIN_RATING = 50;
    private final UserRepository userRepository;

    public CardRatingService(PriceService priceService, UserService userService, WalletService walletService, FileService fileService, UserRepository userRepository) {
        this.priceService = priceService;
        this.userService = userService;
        this.walletService = walletService;
        this.fileService = fileService;
        this.userRepository = userRepository;
    }

    public Integer getCardRating() {
        long id = userService.getAuthenticatableId();
        User user = userService.getAuthenticatableById(id);

        return user.getCardRating();
    }

    @Transactional
    public void updateRating(Integer delta) {
        if (delta == null || delta == 0) {
            return;
        }

        long id = userService.getAuthenticatableId();
        User user = userRepository.findUserByIdWithLock(id)
                .orElseThrow(() -> new IllegalStateException("User not found: " + id));

        Integer deltaPrice = priceService.getPrice("Rating Price", user.getSchoolYear().getLevel()).getCoins();

        if (user.getCardRating() + delta < MIN_RATING)
            throw new MinRatingException("Rating cannot be negative");

        int amount = deltaPrice * Math.abs(delta);

        if (delta < 0) {
            walletService.credit(user, amount, "Rating changed by " + delta);
        } else {
            walletService.debit(user, amount, "Rating changed by " + delta);
        }
        
        user.setCardRating(user.getCardRating() + delta);

        userService.save(user);
    }

    public UserCardDTO getUserCardDTO() {
        long id = userService.getAuthenticatableId();
        User user = userService.getAuthenticatableById(id);

        return new UserCardDTO(
                user.getId(),
                user.getUsername(),
                user.getCardRating(),
                user.getLineupRating(),
                user.getTotalChemistry(),
                fileService.generateSignedUrl(user.getImgLink()),
                user.getImgLink(),
                fileService.generateSignedUrl(user.getSelectedIcon().getImgLink()),
                user.getSelectedIcon().getImgLink(),
                user.getSelectedPosition().getName()
        );
    }

    public UserCardDTO getUserCardDTO(Long userId) {
        User user = userService.getAuthenticatableById(userId);

        return new UserCardDTO(
                user.getId(),
                user.getUsername(),
                user.getCardRating(),
                user.getLineupRating(),
                user.getTotalChemistry(),
                fileService.generateSignedUrl(user.getImgLink()),
                user.getImgLink(),
                fileService.generateSignedUrl(user.getSelectedIcon().getImgLink()),
                user.getSelectedIcon().getImgLink(),
                user.getSelectedPosition().getName()
        );
    }

    public void resetAllCards(SchoolYear schoolYear) {
        int oldPrice = priceService.getPrice("Rating Price", schoolYear.getLevel()).getCoins();
        userRepository.findAllBySchoolYear(schoolYear)
                .forEach(user -> {
                    int oldRating = user.getCardRating();
                    user.setCardRating(50);
                    walletService.credit(user, (oldRating-50) * oldPrice, "Card reset");
                    userService.save(user);
                });
    }
}
