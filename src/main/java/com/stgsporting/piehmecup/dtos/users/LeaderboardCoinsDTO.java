package com.stgsporting.piehmecup.dtos.users;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LeaderboardCoinsDTO {
    private List<UserInLeaderboardDTO> users;
}
