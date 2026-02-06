package com.stgsporting.piehmecup.entities;

import com.stgsporting.piehmecup.config.DatabaseEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.minidev.json.annotate.JsonIgnore;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = DatabaseEnum.playersTable)
public class Player extends BaseEntity {

    @Column(name = DatabaseEnum.rating, nullable = false)
    private Integer rating;

    @Column(name = DatabaseEnum.available, nullable = false)
    private Boolean available;

    @Column(name = DatabaseEnum.playerImgLink, nullable = false)
    private String imgLink;

    @Column(name = DatabaseEnum.price, nullable = false)
    private Integer price;

    @Column(name = DatabaseEnum.name, nullable = false)
    private String name;

    @Column(name = DatabaseEnum.nationality, nullable = false)
    private String Nationality;

    @Column(name = DatabaseEnum.club, nullable = false)
    private String Club;

    @Column(name = DatabaseEnum.league, nullable = false)
    private String League;

    @ManyToOne
    @JoinColumn(name = DatabaseEnum.positionId, nullable = false)
    private Position position;

    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @OnDelete(action = OnDeleteAction.CASCADE)
    private List<OwnedPlayer> ownedPlayers;

    @ManyToOne
    @JoinColumn(name = DatabaseEnum.levelId, nullable = false)
    private Level level;

}
