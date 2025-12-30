package com.stgsporting.piehmecup.controllers;

import com.stgsporting.piehmecup.dtos.PaginationDTO;
import com.stgsporting.piehmecup.dtos.UserRegisterDTO;
import com.stgsporting.piehmecup.dtos.users.UserChangeImageDTO;
import com.stgsporting.piehmecup.dtos.users.UserDetailsDTO;
import com.stgsporting.piehmecup.dtos.users.UserInListDTO;
import com.stgsporting.piehmecup.entities.Admin;
import com.stgsporting.piehmecup.entities.User;
import com.stgsporting.piehmecup.exceptions.UnknownPositionException;
import com.stgsporting.piehmecup.exceptions.UserNotFoundException;
import com.stgsporting.piehmecup.exceptions.UserNotInSameSchoolYearException;
import com.stgsporting.piehmecup.services.*;
import lombok.val;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/ostaz/users")
public class UserController {
    private final UserService userService;
    private final AdminService adminService;
    private final WalletService walletService;
    private final FileService fileService;

    public UserController(UserService userService, AdminService adminService, WalletService walletService, FileService fileService) {
        this.userService = userService;
        this.adminService = adminService;
        this.walletService = walletService;
        this.fileService = fileService;
    }

    private User getUser(String userId) {
        User user = userService.getUserByIdOrUsername(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Admin admin = (Admin) adminService.getAuthenticatable();

        if (!Objects.equals(user.getSchoolYear().getId(), admin.getSchoolYear().getId())) {
            throw new UserNotInSameSchoolYearException();
        }

        return user;
    }

    @GetMapping("")
    public ResponseEntity<Object> index(@RequestParam @Nullable Integer page, @RequestParam @Nullable String search) {
        Admin admin = (Admin) adminService.getAuthenticatable();

        Pageable pageable = PageRequest.of(page == null ? 0 : page, 10);

        Page<UserInListDTO> users = userService
                .getUsersBySchoolYear(admin.getSchoolYear(), search, pageable)
                .map((user) -> new UserInListDTO(user, fileService, 0));

        return ResponseEntity.ok(new PaginationDTO<>(users));
    }

    @GetMapping("coins")
    public ResponseEntity<Object> coinsIndex(@RequestParam @Nullable Integer page, @RequestParam @Nullable String search) {
        Admin admin = (Admin) adminService.getAuthenticatable();

        Pageable pageable = PageRequest.of(page == null ? 0 : page, 10);

        Page<UserInListDTO> users = userService
                .getUsersBySchoolYearAndCoins(admin.getSchoolYear(), search, pageable)
                .map((userCoinsDTO) -> new UserInListDTO(userCoinsDTO.getUser(), fileService, userCoinsDTO.getCoins().intValue()));

        return ResponseEntity.ok(new PaginationDTO<>(users));
    }

    @GetMapping("{userId}")
    public ResponseEntity<Object> show(@PathVariable String userId) {
        User user = getUser(userId);

        return ResponseEntity.ok().body(new UserDetailsDTO(user, fileService));
    }

    @DeleteMapping("{userId}")
    public ResponseEntity<Object> delete(@PathVariable String userId) {
        User user = getUser(userId);

        userService.delete(user);

        return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
    }

    @PostMapping("{userId}/coins/add")
    public ResponseEntity<Object> addCoins(@PathVariable String userId, @RequestBody JSONObject body) {
        User user = getUser(userId);
        Admin admin = (Admin) adminService.getAuthenticatable();

        Integer coins = (Integer) body.get("coins");

        walletService.credit(user, coins, "By Admin: " + admin.getUsername());

        return ResponseEntity.ok(user.getCoins());
    }

    @PostMapping("{userId}/coins/remove")
    public ResponseEntity<Object> removeCoins(@PathVariable String userId, @RequestBody JSONObject body) {
        User user = getUser(userId);
        Admin admin = (Admin) adminService.getAuthenticatable();

        Integer coins = (Integer) body.get("coins");

        walletService.debit(user, coins, "By Admin: " + admin.getUsername());

        return ResponseEntity.ok(user.getCoins());
    }

    @PostMapping("{userId}/change-password")
    public ResponseEntity<Object> changePassword(@PathVariable String userId, @RequestBody JSONObject body) {
        User user = getUser(userId);

        String password = (String) body.get("password");

        userService.changePassword(user, password);

        return ResponseEntity.ok().build();
    }

    @PostMapping("")
    public ResponseEntity<Object> create(@RequestBody JSONObject body) {
        Admin admin = (Admin) adminService.getAuthenticatable();

        UserRegisterDTO userDTO = new UserRegisterDTO();

        userDTO.setUsername((String) body.get("username"));
        userDTO.setPassword((String) body.get("password"));
        userDTO.setSchoolYear(admin.getSchoolYear().getName());

        User user = userService.createUser(userDTO, true);

        return ResponseEntity.ok(new UserDetailsDTO(user, fileService));
    }

    @PostMapping("/bulk")
    public ResponseEntity<Object> createBulk(@RequestBody JSONObject body) {
        Admin admin = (Admin) adminService.getAuthenticatable();

        List<String> usernames = (List<String>) body.get("users");
        List<String> users = new ArrayList<>(usernames);

        return ResponseEntity.ok(
                userService.createUsersBulk(users, admin.getSchoolYear())
        );
    }

    @PostMapping("/{userId}/confirm")
    public ResponseEntity<Object> confirmUser(@PathVariable String userId) {
        Admin admin = (Admin) adminService.getAuthenticatable();
        User user = userService.getUserByIdOrUsername(userId).orElseThrow(UserNotFoundException::new);
        if(!admin.hasAccessTo(user)) {
            throw new UserNotInSameSchoolYearException();
        }

        userService.confirmUser(user);

        return ResponseEntity.ok(Map.of("message", "User confirmed successfully"));
    }

    @PutMapping("/{userId}/change-image")
    public ResponseEntity<Object> changeImage(@ModelAttribute UserChangeImageDTO changeImageDTO, @PathVariable String userId) {
        Admin admin = (Admin) adminService.getAuthenticatable();
        User user = userService.getUserByIdOrUsername(userId).orElseThrow(UserNotFoundException::new);

        if (!admin.hasAccessTo(user)) {
            throw new UserNotInSameSchoolYearException();
        }

        try {
            userService.changeImage(user, changeImageDTO.getImage().getBytes());
        } catch (IOException e) {
            throw new UnknownPositionException("Failed to change image");
        }

        return ResponseEntity.ok(Map.of("message", "Image changed successfully"));
    }

    @PatchMapping("/leaderboard/show/{userId}")
    public ResponseEntity<Object> showInLeaderboard(@PathVariable Long userId) {
        User user = getUser(userId);

        userService.showUserInLeaderboard(user);

        return ResponseEntity.ok().build();
    }

    @PatchMapping("/leaderboard/hide/{userId}")
    public ResponseEntity<Object> hideInLeaderboard(@PathVariable Long userId) {
        User user = getUser(userId);

        userService.hideUserInLeaderboard(user);

        return ResponseEntity.ok().build();
    }

    @NotNull
    private User getUser(Long userId) {
        Admin admin = (Admin) adminService.getAuthenticatable();
        User user = userService.getUserById(userId).orElseThrow(UserNotFoundException::new);

        if (!admin.hasAccessTo(user)) {
            throw new UserNotInSameSchoolYearException();
        }
        return user;
    }
}
