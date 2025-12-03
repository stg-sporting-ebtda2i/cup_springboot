package com.stgsporting.piehmecup.services;

import com.stgsporting.piehmecup.entities.User;
import com.stgsporting.piehmecup.enums.TransactionType;
import com.stgsporting.piehmecup.exceptions.InsufficientCoinsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class WalletService {

    private final UserService userService;
    private final TransactionService transactionService;
    private final TransactionTemplate transactionTemplate;
    private final Map<Long, Object> userLocks = new ConcurrentHashMap<>();

    public WalletService(UserService userService, TransactionService transactionService, TransactionTemplate transactionTemplate) {
        this.userService = userService;
        this.transactionService = transactionService;
        this.transactionTemplate = transactionTemplate;
    }

    public void debit(User user, Integer amount) {
        debit(user, amount, null, false);
    }

    public void debit(User user, Integer amount, String description) {
        debit(user, amount, description, false);
    }

    public void debit(User user, Integer amount, String description, boolean ignoreCoins) {
        Long userId = user.getId();
        if (userId == null) {
            throw new IllegalArgumentException("User ID must not be null for wallet operations");
        }

        Object lock = userLocks.computeIfAbsent(userId, id -> new Object());
        synchronized (lock) {
            transactionTemplate.executeWithoutResult(status -> {
                User freshUser = userService.getUserById(userId)
                        .orElseThrow(() -> new IllegalArgumentException("User not found"));

                if (!ignoreCoins && freshUser.getCoins() < amount) {
                    throw new InsufficientCoinsException("Not enough coins");
                }

                freshUser.setCoins(freshUser.getCoins() - amount);

                userService.save(freshUser);

                transactionService.makeTransaction(freshUser, amount, TransactionType.DEBIT, description);
            });
        }
    }

    public void forceDebit(User user, Integer amount, String description) {
        debit(user, amount, description, true);
    }

    public void credit(User user, Integer amount) {
        credit(user, amount, null);
    }

    public void credit(User user, Integer amount, String description) {
        Long userId = user.getId();
        if (userId == null) {
            throw new IllegalArgumentException("User ID must not be null for wallet operations");
        }

        Object lock = userLocks.computeIfAbsent(userId, id -> new Object());
        synchronized (lock) {
            transactionTemplate.executeWithoutResult(status -> {
                User freshUser = userService.getUserById(userId)
                        .orElseThrow(() -> new IllegalArgumentException("User not found"));

                freshUser.setCoins(freshUser.getCoins() + amount);

                userService.save(freshUser);

                transactionService.makeTransaction(freshUser, amount, TransactionType.CREDIT, description);
            });
        }
    }
}
