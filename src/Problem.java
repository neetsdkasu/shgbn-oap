
final class Problem implements Board
{
    private static Problem instance = null;

    static Problem getInstance()
    {
        if (instance == null)
        {
            instance = new Problem();
        }

        return instance;
    }

    private Problem() {}

    public int field(int row, int col)
    {
        return initialField[row][col];
    }

    public int hands(int player, int kind)
    {
        return initialHands[player][kind];
    }

    private static final int[] zero = new int[9];

    private static int stepLimit = 0;
    private static String title = "";

    static final int[][]
        initialField = new int[9][9],
        initialHands = new int[2][8];

    static String getTitle()
    {
        return title;
    }

    static int getStepLimit()
    {
        return stepLimit;
    }

    static void clear()
    {
        System.arraycopy(zero, 0, initialHands[0], 0, 8);
        System.arraycopy(zero, 0, initialHands[1], 0, 8);
        for (int i = 0; i < 9; i++)
        {
            System.arraycopy(zero, 0, initialField[i], 0, 9);
        }
    }

    static void setNormalGame()
    {
        clear();

        stepLimit = 0;
        title = "";

        for (int i = 0; i < 4; i++)
        {
            initialField[0][i] = KYO + i + OPPONENT;
            initialField[0][8-i] = KYO + i + OPPONENT;
            initialField[8][i] = KYO + i;
            initialField[8][8-i] = KYO + i;
        }

        initialField[0][4] = OU + OPPONENT;
        initialField[1][1] = HI + OPPONENT;
        initialField[1][7] = KAKU + OPPONENT;
        initialField[8][4] = GYOKU;
        initialField[7][7] = HI;
        initialField[7][1] = KAKU;

        for (int i = 0; i < 9; i++)
        {
            initialField[2][i] = FU + OPPONENT;
            initialField[6][i] = FU;
        }
    }

    static void setPuzzleTemplate()
    {
        clear();

        stepLimit = 0;
        title = "";

        initialHands[1][FU-1] = 18;
        initialHands[1][KYO-1] = 4;
        initialHands[1][KEI-1] = 4;
        initialHands[1][GIN-1] = 4;
        initialHands[1][KIN-1] = 4;
        initialHands[1][HI-1] = 2;
        initialHands[1][KAKU-1] = 2;

        initialField[1][7] = OU + OPPONENT;
    }
}