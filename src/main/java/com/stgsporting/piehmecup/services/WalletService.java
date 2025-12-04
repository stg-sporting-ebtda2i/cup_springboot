package com.stgsporting.piehmecup.services;

import com.stgsporting.piehmecup.entities.User;
import com.stgsporting.piehmecup.enums.TransactionType;
import com.stgsporting.piehmecup.exceptions.InsufficientCoinsException;
import com.stgsporting.piehmecup.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WalletService {

    private final UserService userService;
    private final UserRepository userRepository;
    private final TransactionService transactionService;

    public WalletService(UserService userService, UserRepository userRepository, TransactionService transactionService) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.transactionService = transactionService;
    }

    @Transactional
    public void debit(User user, Integer amount) {
        debit(user, amount, null, false);
    }

    @Transactional
    public void debit(User user, Integer amount, String description) {
        debit(user, amount, description, false);
    }

    @Transactional
    public void debit(User user, Integer amount, String description, boolean ignoreCoins) {
        Long userId = user.getId();
        if (userId == null) {
            throw new IllegalArgumentException("User ID must not be null for wallet operations");
        }

        // Use database pessimistic lock to prevent concurrent modifications
        User freshUser = userRepository.findUserByIdWithLock(userId)
            .orElseThrow(() -> new IllegalStateException("User not found: " + userId));
        
        if (!ignoreCoins && freshUser.getCoins() < amount) {
            throw new InsufficientCoinsException("Not enough coins");
        }

        freshUser.setCoins(freshUser.getCoins() - amount);
        user.setCoins(freshUser.getCoins());

        userService.save(freshUser);

        transactionService.makeTransaction(freshUser, amount, TransactionType.DEBIT, description);
    }

    @Transactional
    public void forceDebit(User user, Integer amount, String description) {
        debit(user, amount, description, true);
    }

    @Transactional
    public void credit(User user, Integer amount) {
        credit(user, amount, null);
    }

    @Transactional
    public void credit(User user, Integer amount, String description) {
        Long userId = user.getId();
        if (userId == null) {
            throw new IllegalArgumentException("User ID must not be null for wallet operations");
        }

        // Use database pessimistic lock to prevent concurrent modifications
        User freshUser = userRepository.findUserByIdWithLock(userId)
            .orElseThrow(() -> new IllegalStateException("User not found: " + userId));
        
        freshUser.setCoins(freshUser.getCoins() + amount);
        user.setCoins(freshUser.getCoins());

        userService.save(freshUser);

        transactionService.makeTransaction(freshUser, amount, TransactionType.CREDIT, description);
    }
}
