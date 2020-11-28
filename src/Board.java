
abstract class Board
{
    static final int
        OPPONENT = 16,
        FU = 1,
        KYO = 2,
        KEI = 3,
        GIN = 4,
        KIN = 5,
        KAKU = 6,
        HI = 7,
        GYOKU = 8,
        TO = 9,
        NKYO = 10,
        NKEI = 11,
        NGIN = 12,
        UMA = 14,
        RYU = 15,
        OU = 16;

    public abstract int field(int row, int col);
    public abstract int hands(int player, int kind);
    public abstract int getStepLimit();
    public abstract String getTitle();

    public final int kind(int row, int col)
    {
        return isOpponent(row, col)
            ? field(row, col) - OPPONENT
            : field(row, col);
    }

    public final boolean isOpponent(int row, int col)
    {
        return field(row, col) > OPPONENT;
    }

    public final boolean isMine(int row, int col)
    {
        return 0 < field(row, col) && !isOpponent(row, col);
    }

    public final boolean inField(int row, int col)
    {
        return 0 <= row && row < 9 && 0 <= col && col < 9;
    }
}