package com.stgsporting.piehmecup.entities;

import com.stgsporting.piehmecup.config.DatabaseEnum;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = DatabaseEnum.ownedPlayersTable,
        uniqueConstraints = @UniqueConstraint(name = "uk_owned_player_user_player", columnNames = {"user_id", "player_id"}))
public class OwnedPlayer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    /* Chemistry ranges from 0 to 3
     * 0 means no chemistry
     * 3 means full chemistry
     *
     * +1 point for every player can link with another player in the lineup
     * */
    @Column(name = DatabaseEnum.chemistry, nullable = false)
    private Integer chemistry = 0;

    public OwnedPlayer(User user, Player player, Integer chemistry) {
        this.user = user;
        this.player = player;
        this.chemistry = chemistry == null ? 0 : chemistry;
    }
}
