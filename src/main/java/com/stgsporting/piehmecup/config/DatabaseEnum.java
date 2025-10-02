package com.stgsporting.piehmecup.config;

public class DatabaseEnum {
    // Base ID for all tables
    public static final String baseId = "id";

    // School Year TABLE
    public static final String schoolYearTable = "SCHOOL_YEARS";
    public static final String schoolYearName = "name";
    public static final String levelId = "levelId";

    // School Year TABLE
    public static final String levelTable = "LEVELS";
    public static final String levelName = "name";

    // USERS TABLE
    public static final String usersTable = "USERS";
    public static final String username = "username";
    public static final String password = "password";
    public static final String schoolYearId = "schoolYearId";
    public static final String lineupRating = "lineupRating";
    public static final String cardRating = "cardRating";
    public static final String confirmed = "confirmed";
    public static final String waladImgLink = "imgLink";
    public static final String leaderboardBoolean = "leaderboardBoolean";

    // ADMINS TABLE
    public static final String adminsTable = "ADMINS";
    public static final String role = "role";

    // BUTTONS VISIBILITY TABLE
    public static final String buttonsTable = "BUTTONS_VISIBILITY";
    public static final String buttonName = "name";
    public static final String visible = "visible";
    public static final String userRole = "userRole";

    // OWNED PLAYERS TABLE
    public static final String ownedPlayersTable = "OWNED_PLAYERS";
    public static final String playerId = "playerId";
    public static final String chemistry = "chemistry";

    // OWNED ICONS TABLE
    public static final String ownedIconsTable = "OWNED_ICONS";
    public static final String iconId = "iconId";

    // OWNED POSITIONS TABLE
    public static final String ownedPositionsTable = "OWNED_POSITIONS";
    public static final String positionId = "positionId";

    // Common attributes for PLAYERS, ICONS, POSITIONS TABLES
    public static final String price = "price";
    public static final String name = "name";
    public static final String available = "available";

    // PLAYERS TABLE
    public static final String playersTable = "PLAYERS";
    public static final String rating = "rating";
    public static final String playerImgLink = "imgLink";
    public static final String position = "position";
    public static final String nationality = "nationality";
    public static final String club = "club";
    public static final String league = "league";

    // ICONS TABLE
    public static final String iconsTable = "ICONS";
    public static final String iconImgLink = "imgLink";

    // POSITIONS TABLE
    public static final String positionsTable = "POSITIONS";

    // PRICES TABLE
    public static final String pricesTable = "PRICES";
    public static final String liturgyName = "name";
    public static final String coins = "coins";

    // ATTENDANCE TABLE
    public static final String attendanceTable = "ATTENDANCE";
    public static final String userId = "userId";
    public static final String createdAt = "createdAt";
    public static final String approved = "approved";
    public static final String quizId = "quizId";
    public static final String priceId = "priceId";

    // TRANSACTIONS TABLE
    public static final String transactionsTable = "TRANSACTIONS";
    public static final String amount = "amount";
    public static final String transactionType = "type";
    public static final String transactionDate = "createdAt";
    public static final String transactionDescription = "description";
    public static final String transactionUserId = "userId";

    // SELECTED ICON TABLE
    public static final String selectedIconId = "iconId";

    // SELECTED POSITION TABLE
    public static final String selectedPositionId = "positionId";
    public static final String date = "date";
}
