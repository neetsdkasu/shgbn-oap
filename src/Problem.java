
class Problem extends Board
{
    private static Problem NORMAL_GAME = null;

    static Problem getNormalGame()
    {
        if (NORMAL_GAME == null)
        {
            NORMAL_GAME = makeNormalGame();
        }

        return NORMAL_GAME;
    }

    static Problem makeNormalGame()
    {
        Problem p = new Problem();

        for (int i = 0; i < 4; i++)
        {
            p.initialField[0][i] = KYO + i + OPPONENT;
            p.initialField[0][8-i] = KYO + i + OPPONENT;
            p.initialField[8][i] = KYO + i;
            p.initialField[8][8-i] = KYO + i;
        }

        p.initialField[0][4] = OU + OPPONENT;
        p.initialField[1][1] = HI + OPPONENT;
        p.initialField[1][7] = KAKU + OPPONENT;
        p.initialField[8][4] = GYOKU;
        p.initialField[7][7] = HI;
        p.initialField[7][1] = KAKU;

        for (int i = 0; i < 9; i++)
        {
            p.initialField[2][i] = FU + OPPONENT;
            p.initialField[6][i] = FU;
        }

        return p;
    }

    static Problem makeFromGame(Game game)
    {
        Problem p = new Problem();
        p.stepLimit = game.getStepLimit();
        p.title = game.getTitle();
        for (int i = 0; i < 9; i++)
        {
            System.arraycopy(
                game.currentField[i],
                0,
                p.initialField[i],
                0,
                p.initialField[i].length
            );
        }
        for (int i = 0; i < 2; i++)
        {
            System.arraycopy(
                game.currentHands[i],
                0,
                p.initialHands[i],
                0,
                p.initialHands[i].length
            );
        }
        return p;
    }

    static Problem make()
    {
        Problem p = new Problem();

        p.initialHands[1][FU-1] = 18;
        p.initialHands[1][KYO-1] = 4;
        p.initialHands[1][KEI-1] = 4;
        p.initialHands[1][GIN-1] = 4;
        p.initialHands[1][KIN-1] = 4;
        p.initialHands[1][HI-1] = 2;
        p.initialHands[1][KAKU-1] = 2;

        p.initialField[1][6] = OU + OPPONENT;

        return p;
    }

    int stepLimit;
    String title = "";

    final int[][] initialField, initialHands;

    Problem()
    {
        initialField = new int[9][9];
        initialHands = new int[2][8];
    }

    public String getTitle()
    {
        return title;
    }

    public int field(int row, int col)
    {
        return initialField[row][col];
    }

    public int hands(int player, int kind)
    {
        return initialHands[player][kind];
    }

    public int getStepLimit()
    {
        return stepLimit;
    }
}