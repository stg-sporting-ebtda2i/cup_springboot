package com.stgsporting.piehmecup.entities;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.stgsporting.piehmecup.authentication.Authenticatable;
import com.stgsporting.piehmecup.config.DatabaseEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(
        name = DatabaseEnum.usersTable,
        indexes = {
                @Index(name = "idx_schoolyear_username", columnList = DatabaseEnum.schoolYearId + ", " + DatabaseEnum.username),
        }
)
public class User extends BaseEntity implements Authenticatable {

    @Column(name = DatabaseEnum.username, nullable = false, unique = true)
    private String username;

    @Column(name = DatabaseEnum.password, nullable = false)
    private String password;

    @Column(name = DatabaseEnum.quizId, nullable = true, unique = true)
    private Long quizId;

    @ManyToOne
    @JoinColumn(name = DatabaseEnum.schoolYearId, nullable = false)
    private SchoolYear schoolYear;

    @Column(name = DatabaseEnum.coins, nullable = false)
    @ColumnDefault("0")
    private Integer coins;

    @Column(name = DatabaseEnum.cardRating, nullable = false)
    @ColumnDefault("0")
    private Integer cardRating;

    @Column(name = DatabaseEnum.confirmed, nullable = false)
    @ColumnDefault("0")
    private Boolean confirmed;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = DatabaseEnum.baseId, referencedColumnName = DatabaseEnum.userId)
    private UserRating lineupRating;

    @Column(name = DatabaseEnum.waladImgLink, unique = true)
    private String imgLink;

    @Column(name = DatabaseEnum.leaderboardBoolean, nullable = false)
    @ColumnDefault("true")
    private Boolean leaderboardBoolean;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = DatabaseEnum.baseId, referencedColumnName = DatabaseEnum.userId)
    private UserRating totalChemistry;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OwnedPlayer> ownedPlayers;

    @ManyToMany
    @JoinTable(name = DatabaseEnum.ownedIconsTable, joinColumns =  @JoinColumn(name = DatabaseEnum.userId),
            inverseJoinColumns = @JoinColumn(name = DatabaseEnum.iconId))
    private List<Icon> icons;

    @ManyToMany
    @JoinTable(name = DatabaseEnum.ownedPositionsTable, joinColumns = @JoinColumn(name = DatabaseEnum.userId),
            inverseJoinColumns = @JoinColumn(name = DatabaseEnum.positionId))
    private List<Position> positions;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Attendance> attendances;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Transaction> transactions;

    @ManyToOne
    @JoinColumn(name = DatabaseEnum.selectedIconId)
    private Icon selectedIcon;

    @ManyToOne
    @JoinColumn(name = DatabaseEnum.selectedPositionId)
    private Position selectedPosition;

    public void setPassword(String password) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
        this.password = encoder.encode(password);
    }

    public static String generatePassword() {
        Random random = new Random();
        int password = 100000 + random.nextInt(900000);
        return String.valueOf(password);
    }

    public String getRoleString() {
        return "USER";
    }

    public Double getLineupRating() {
        if (lineupRating == null) {
            return 0.0;
        }
        return lineupRating.getLineupRating();
    }

    public Integer getTotalChemistry() {
        if (totalChemistry == null) {
            return 0;
        }
        return lineupRating.getTotalChemistry();
    }

    public boolean doesntOwn(Position position) {
        return !getPositions().contains(position);
    }

    public boolean owns(Icon icon) {
        return getIcons().contains(icon);
    }

    public void addIcon(Icon icon) {
        if (icons == null) {
            icons = new ArrayList<>();
        }
        icons.add(icon);
    }

    public void addPosition(Position position) {
        if (positions == null) {
            positions = new ArrayList<>();
        }
        positions.add(position);
    }

    // Helper to return Player list (read-only snapshot)
    @Transient
    public List<Player> getPlayers() {
        if (ownedPlayers == null) return new ArrayList<>();
        return ownedPlayers.stream().map(OwnedPlayer::getPlayer).collect(Collectors.toList());
    }

    public void addPlayer(Player player, int chemistry) {
        if (ownedPlayers == null) ownedPlayers = new ArrayList<>();
        // prevent duplicates
        boolean exists = ownedPlayers.stream().anyMatch(op -> op.getPlayer().equals(player));
        if (!exists) {
            OwnedPlayer op = new OwnedPlayer(this, player, chemistry);
            ownedPlayers.add(op);
        }
    }

    public void removePlayer(Player player) {
        if (ownedPlayers != null) {
            ownedPlayers.removeIf(op -> op.getPlayer().equals(player));
        }
    }

    public Integer getChemistryFor(Player player) {
        if (ownedPlayers == null) return null;
        return ownedPlayers.stream().filter(op -> op.getPlayer().equals(player))
                .map(OwnedPlayer::getChemistry).findFirst().orElse(null);
    }
}
