package com.stgsporting.piehmecup.dtos.users;

import com.stgsporting.piehmecup.entities.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserCoinsDTO {
    private User user;
    private Long coins;
}
