
interface Board
{
    int
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

    int field(int row, int col);
    int hands(int player, int kind);
}