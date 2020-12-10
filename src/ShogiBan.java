import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import javax.microedition.lcdui.*;
import javax.microedition.lcdui.game.*;

final class ShogiBan extends GameCanvas implements GConstants
{
    private static final int
        BACKGROUND_COLOR = 0xFFB000,
        KOMA_COLOR = 0xFFE010,
        CURSOR_COLOR = CYAN,
        LINE_COLOR = BLACK,
        SPACE = 4,
        CELL_SIZE = 20,
        HANDS_CELL_WIDTH = 9*CELL_SIZE/8,
        OPPO_HAND_OFFSET_Y = 20,
        NUMBER_H_OFFSET_Y = OPPO_HAND_OFFSET_Y+CELL_SIZE+SPACE,
        BAN_OFFSET_X = (DISP_W - 9*CELL_SIZE) / 2,
        BAN_OFFSET_Y = NUMBER_H_OFFSET_Y + 12,
        MY_HAND_OFFSET_Y = BAN_OFFSET_Y + 9*CELL_SIZE + SPACE,
        NUMBER_V_OFFSET_X = BAN_OFFSET_X + 9*CELL_SIZE + SPACE;

    private static int curX, curY, selX, selY;

    private static Image backgroundImage;
    private static Sprite komaStamp, modeMark;
    private static TiledLayer komaField, colorField;

    private static int mode = 0;
    private static int state = 0;
    private static int menuMode = 0;

    private static int rangeMode = 0;

    private static Board board;

    private static Menu menu = null;

    ShogiBan()
    {
        super(false);
        makeStaticImages();

        Storage.loadAppState();

        if (isGameMode())
        {
            Storage.loadTemporaryGame();
            board = Game.getInstance();
        }
        else if (isEditMode())
        {
            Storage.loadTemporaryProblem();
            board = Problem.getInstance();
        }
        clearMovable();
        render();
    }

    static void writeTo(DataOutput out) throws IOException
    {
        out.writeInt(1); // version
        out.writeInt(mode);
        out.writeInt(rangeMode);
        out.writeInt(curX);
        out.writeInt(curY);
    }

    static void readFrom(DataInput in) throws IOException
    {
        in.readInt(); // version
        mode = in.readInt();
        rangeMode = in.readInt();
        curX = in.readInt();
        curY = in.readInt();
    }

    static boolean isGameMode()
    {
        return mode == 0;
    }

    static boolean isEditMode()
    {
        return mode == 1;
    }

    void save(String title)
    {
        if (title == null)
        {
            setTicker(new Ticker("canceled"));
            closeMenu();
            render();
            return;
        }
        if (isGameMode())
        {
            if (!title.equals(Problem.getTitle()))
            {
                Game.setTitle(title);
            }
            Storage.saveGame(false);
        }
        else if (isEditMode())
        {
            Problem.setTitle(title);
            Storage.saveProblem(false);
        }
        setTicker(new Ticker("saved"));
        closeMenu();
        render();
    }

    void menu()
    {
        if (getTicker() != null)
        {
            setTicker(null);
        }
        switch (menuMode)
        {
        case 0:
            if (isGameMode())
            {
                openMenu(2);
                render();
            }
            else if (isEditMode())
            {
                openMenu(5);
                render();
            }
            break;
        case 2:
        case 5:
        case 6:
        case 7:
        case 8:
        case 9:
        case 10:
        case 11:
        case 12:
            closeMenu();
            render();
            break;
        }
    }

    void render()
    {
        Graphics g = getGraphics();

        g.drawImage(backgroundImage, 0, 0, Graphics.LEFT|Graphics.TOP);

        renderHands(g);

        colorField.paint(g);

        for (int row = 0; row < 9; row++)
        {
            for (int col = 0; col < 9; col++)
            {
                komaField.setCell(col, row, board.field(row, col));
            }
        }
        komaField.paint(g);

        modeMark.setFrame(mode);
        modeMark.paint(g);

        g.setColor(LINE_COLOR);
        int headerOffsetX = modeMark.getWidth() + SPACE;
        if (Problem.getStepLimit() > 0)
        {
            String s = Integer.toString(Problem.getStepLimit()) + WORDS[2];
            g.drawString(
                s,
                headerOffsetX,
                0,
                Graphics.LEFT|Graphics.TOP
            );
            headerOffsetX += SMALL_FONT.stringWidth(s) + SPACE;
        }

        if (isGameMode())
        {
            renderGameInfo(g, headerOffsetX);
        }
        else if (isEditMode())
        {
            g.setColor(Problem.recordID == 0 ? GRAY : LINE_COLOR);
            g.drawString(
                Problem.getTitle(),
                headerOffsetX,
                0,
                Graphics.LEFT|Graphics.TOP
            );
        }

        renderCursor(g);

        if (menuMode != 0)
        {
            menu.paint(g);
        }

        flushGraphics();
    }

    private void renderCursor(Graphics g)
    {
        if (isGameMode())
        {
            if (state != 0)
            {
                renderBanCursor(g, selX, selY, GREEN);
            }
            renderBanCursor(g, curX, curY, CURSOR_COLOR);
        }
        else if (isEditMode())
        {
            if (state != 0)
            {
                renderBanCursor(g, selX, selY, GREEN);
                if (curY >= 9)
                {
                    renderHandsCursor(g, curY, CURSOR_COLOR);
                    return;
                }
            }
            renderBanCursor(g, curX, curY, CURSOR_COLOR);
        }
    }

    private void renderHandsCursor(Graphics g, int y, int color)
    {
        g.setColor(color);
        if (y == 9)
        {
            g.drawRect(
                BAN_OFFSET_X,
                MY_HAND_OFFSET_Y,
                9*CELL_SIZE,
                CELL_SIZE
            );
        }
        else
        {
            g.drawRect(
                BAN_OFFSET_X,
                OPPO_HAND_OFFSET_Y,
                9*CELL_SIZE,
                CELL_SIZE
            );
        }
    }

    private void renderBanCursor(Graphics g, int x, int y, int color)
    {
        g.setColor(color);
        if (y < 9)
        {
            g.drawRect(
                x*CELL_SIZE + BAN_OFFSET_X,
                y*CELL_SIZE + BAN_OFFSET_Y,
                CELL_SIZE,
                CELL_SIZE
            );
        }
        else if (y == 9)
        {
            g.drawRect(
                x*HANDS_CELL_WIDTH + BAN_OFFSET_X,
                MY_HAND_OFFSET_Y,
                HANDS_CELL_WIDTH,
                CELL_SIZE
            );
        }
        else
        {
            g.drawRect(
                x*HANDS_CELL_WIDTH + BAN_OFFSET_X,
                OPPO_HAND_OFFSET_Y,
                HANDS_CELL_WIDTH,
                CELL_SIZE
            );
        }
    }

    private void renderGameInfo(Graphics g, int headerOffsetX)
    {
        g.setFont(SMALL_FONT);
        g.setColor(LINE_COLOR);

        // ??? TE-ME
        headerOffsetX += SMALL_FONT.stringWidth("0000" + WORDS[3]);
        g.drawString(
            Integer.toString(Game.getCurrentStep()+1) + WORDS[3],
            headerOffsetX,
            0,
            Graphics.RIGHT|Graphics.TOP
        );

        // SEN-TE-BAN | GO-TE-BAN
        headerOffsetX += SPACE;
        g.drawString(
            WORDS[4 + (Game.getFirstPlayer() ^ Game.getCurrentPlayer())],
            headerOffsetX,
            0,
            Graphics.LEFT|Graphics.TOP
        );

        // OU-TE
        if (Game.isDanger())
        {
            headerOffsetX += SMALL_FONT.stringWidth(
                WORDS[4 + (Game.getFirstPlayer() ^ Game.getCurrentPlayer())]
            ) + SPACE;
            String msg = WORDS[Game.isCheckmate() ? 10 : 9];
            g.setColor(Game.isCheckmate() ? LINE_COLOR : RED);
            g.fillRect(
                headerOffsetX,
                0,
                SMALL_FONT.stringWidth(msg) + 3,
                SMALL_FONT.getHeight()
            );
            g.setColor(WHITE);
            g.drawString(msg, headerOffsetX+2, 0, Graphics.LEFT|Graphics.TOP);
            g.setColor(LINE_COLOR);
        }

        // SEN-TE GO-TE
        g.drawString(
            WORDS[1^Game.getFirstPlayer()],
            (BAN_OFFSET_X-SMALL_FONT.stringWidth(WORDS[1^Game.getFirstPlayer()]))/2,
            OPPO_HAND_OFFSET_Y+(CELL_SIZE-SMALL_FONT.getHeight())/2,
            Graphics.LEFT|Graphics.TOP
        );
        g.drawString(
            WORDS[Game.getFirstPlayer()],
            (BAN_OFFSET_X-SMALL_FONT.stringWidth(WORDS[Game.getFirstPlayer()]))/2,
            MY_HAND_OFFSET_Y+(CELL_SIZE-SMALL_FONT.getHeight())/2,
            Graphics.LEFT|Graphics.TOP
        );
    }

    private void renderHands(Graphics g)
    {
        g.setFont(SMALL_FONT);
        for (int k = 0; k < 8; k++)
        {
            if (board.hands(0,k) > 0)
            {
                komaStamp.setPosition(
                    k*HANDS_CELL_WIDTH + BAN_OFFSET_X,
                    MY_HAND_OFFSET_Y
                );
                komaStamp.setFrame(k);
                komaStamp.paint(g);
                if (board.hands(0,k) > 1)
                {
                    int wide = board.hands(0,k) > 9 ? 6 : 0;
                    g.setColor(WHITE);
                    g.fillRect(
                        (k+1)*HANDS_CELL_WIDTH - 7 - wide + BAN_OFFSET_X,
                        CELL_SIZE - 12 + MY_HAND_OFFSET_Y,
                        6 + wide,
                        11
                    );
                    g.setColor(LINE_COLOR);
                    g.drawString(
                        Integer.toString(board.hands(0,k)),
                        (k+1)*HANDS_CELL_WIDTH - 1 + BAN_OFFSET_X,
                        CELL_SIZE + MY_HAND_OFFSET_Y,
                        Graphics.RIGHT|Graphics.BOTTOM
                    );
                }
            }
            if (board.hands(1,k) > 0)
            {
                komaStamp.setPosition(
                    k*HANDS_CELL_WIDTH + BAN_OFFSET_X,
                    OPPO_HAND_OFFSET_Y
                );
                komaStamp.setFrame(k+16 + ((k/7)*8));
                komaStamp.paint(g);
                if (board.hands(1,k) > 1)
                {
                    int wide = board.hands(1,k) > 9 ? 6 : 0;
                    g.setColor(WHITE);
                    g.fillRect(
                        (k+1)*HANDS_CELL_WIDTH - 7 - wide + BAN_OFFSET_X,
                        CELL_SIZE - 12 + OPPO_HAND_OFFSET_Y,
                        6 + wide,
                        11
                    );
                    g.setColor(LINE_COLOR);
                    g.drawString(
                        Integer.toString(board.hands(1,k)),
                        (k+1)*HANDS_CELL_WIDTH - 1 + BAN_OFFSET_X,
                        CELL_SIZE + OPPO_HAND_OFFSET_Y,
                        Graphics.RIGHT|Graphics.BOTTOM
                    );

                }
            }
        }
    }

    private void openMenu(int menuId)
    {
        // push stack submenu?
        menuMode = menuId;
        switch (menuId)
        {
        case 1:
            menu = Menu.getRankUpMenu().cleanUp();
            break;
        case 2:
            menu = Menu.getGameMenu();
            menu.setEnable(1, History.hasPrev());
            menu.setEnable(2, History.hasNext());
            menu.setEnable(4, Storage.hasGame()); // load
            break;
        case 3:
            menu = Menu.getEditMenuOnBan().cleanUp();
            menu.setEnable(1, Problem.canFlip(selY, selX));
            break;
        case 4:
            menu = Menu.getEditMenuOnHand().cleanUp();
            menu.setEnable(0, !Problem.isEmptyInHands(selY-9, selX));
            menu.setEnable(1, !Problem.isFullyInHands(selY-9, selX));
            menu.setEnable(2, !Problem.isEmptyInHands(selY-9, selX));
            break;
        case 5:
            menu = Menu.getEditMenu();
            menu.setValue(Problem.getStepLimit());
            menu.setEnable(2, Storage.hasProblem()); // load
            break;
        case 6:
            menu = Menu.getSaveMenu().cleanUp();
            if (isGameMode())
            {
                menu.setEnable(1, Storage.hasGame(Game.recordID)); // overwrite
            }
            else if (isEditMode())
            {
                menu.setEnable(1, Storage.hasProblem(Problem.recordID)); // overwrite
            }
            break;
        case 7:
            menu = Menu.getListProblemMenu();
            break;
        case 8:
            menu = Menu.getLoadProblemMenu().cleanUp();
            break;
        case 9:
            menu = Menu.getNewProblemMenu().cleanUp();
            break;
        case 10:
            menu = Menu.getListGameMenu();
            break;
        case 11:
            menu = Menu.getLoadGameMenu().cleanUp();
            break;
        case 12:
            menu = Menu.getNewGameMenu();
            break;
        }
    }

    private void closeMenu()
    {
        // pop stack submenu?
        menuMode = 0;
        menu = null;
    }

    protected void keyRepeated(int keyCode)
    {
        if (getGameAction(keyCode) != FIRE)
        {
            keyPressed(keyCode);
        }
    }

    protected void keyPressed(int keyCode)
    {
        if (getTicker() != null)
        {
            setTicker(null);
        }
        int action = getGameAction(keyCode);
        if (menuMode != 0)
        {
            if (menu.keyPressed(keyCode, action))
            {
                if (menuMode == 5)
                {
                    Problem.setStepLimit(menu.getValue());
                }
                render();
                return;
            }
        }
        switch (menuMode)
        {
        case 0:
            if (isGameMode())
            {
                movePlayModeCursor(keyCode, action);
            }
            else if (isEditMode())
            {
                moveEditModeCursor(keyCode, action);
            }
            break;
        case 1:
            actRankUpMenu(keyCode, action);
            break;
        case 2:
            actGameMenu(keyCode, action);
            break;
        case 3:
            actEditMenuOnBan(keyCode, action);
            break;
        case 4:
            actEditMenuOnHand(keyCode, action);
            break;
        case 5:
            actEditMenu(keyCode, action);
            break;
        case 6:
            actSaveMenu(keyCode, action);
            break;
        case 7:
            actListProblemMenu(keyCode, action);
            break;
        case 8:
            actLoadProblemMenu(keyCode, action);
            break;
        case 9:
            actNewProblemMenu(keyCode, action);
            break;
        case 10:
            actListGameMenu(keyCode, action);
            break;
        case 11:
            actLoadGameMenu(keyCode, action);
            break;
        case 12:
            actNewGameMenu(keyCode, action);
            break;
        }
    }

    private void actNewGameMenu(int keyCode, int action)
    {
        if (action != FIRE)
        {
            return;
        }
        if (menu.canceled())
        {
            openMenu(2);
            render();
            return;
        }
        Game.clear();
        int sel = menu.getSelect();
        switch (sel)
        {
        case 0: // HON-SHOU-GI
            Problem.setNormalGame();
            break;
        case 1: // SAKU-SEI-CHU
            Storage.loadTemporaryProblem();
            break;
        default: // load
            Storage.loadProblem(sel - 2);
            break;
        }
        state = 0;
        Game.initPlay();
        clearMovable();
        closeMenu();
        render();
    }

    private void actLoadGameMenu(int keyCode, int action)
    {
        if (action != FIRE)
        {
            return;
        }
        int sel = Menu.getListGameMenu().getSelect();
        switch (menu.getSelect())
        {
        case 1: // load
            Storage.loadGame(sel);
            state = 0;
            Game.ready();
            clearMovable();
            closeMenu();
            setTicker(new Ticker("loaded"));
            break;
        case 2: // delete
            Storage.deleteGame(sel);
            closeMenu();
            setTicker(new Ticker("deleted"));
            break;
        case 3: // cancel
            openMenu(10);
            break;
        default:
            return;
        }
        render();
    }

    private void actListGameMenu(int keyCode, int action)
    {
        if (action != FIRE)
        {
            return;
        }
        if (menu.canceled())
        {
            if (isGameMode())
            {
                openMenu(2);
            }
            else if (isEditMode())
            {
                openMenu(9);
            }
            render();
            return;
        }
        if (isGameMode())
        {
            openMenu(11);
            render();
            return;
        }
        else if (isEditMode())
        {
            int sel = menu.getSelect();
            state = 0;
            Storage.loadGame(sel);
            Game.copyToProblem();
            clearMovable();
            closeMenu();
            render();
        }
    }

    private void actNewProblemMenu(int keyCode, int action)
    {
        if (action != FIRE)
        {
            return;
        }
        if (menu.canceled())
        {
            openMenu(5);
            render();
            return;
        }
        switch (menu.getSelect())
        {
        case 0: // TSUME-SHOU-GI
            Problem.setPuzzleTemplate();
            break;
        case 1: // HON-SHOU-GI
            Problem.setNormalGame();
            break;
        case 2: // playing-game SHI-KOU-CHU
            Storage.loadTemporaryGame();
            Game.copyToProblem();
            break;
        case 3: // saved-game HO-ZON-SHI-KOU
            openMenu(10);
            render();
            return;
        default:
            return;
        }
        state = 0;
        clearMovable();
        closeMenu();
        render();
    }

    private void actLoadProblemMenu(int keyCode, int action)
    {
        if (action != FIRE)
        {
            return;
        }
        int sel = Menu.getListProblemMenu().getSelect();
        switch (menu.getSelect())
        {
        case 1: // load
            Storage.loadProblem(sel);
            clearMovable();
            closeMenu();
            setTicker(new Ticker("loaded"));
            break;
        case 2: // delete
            Storage.deleteProblem(sel);
            closeMenu();
            setTicker(new Ticker("deleted"));
            break;
        case 3: // cancel
            openMenu(7);
            break;
        }
        render();
    }

    private void actListProblemMenu(int keyCode, int action)
    {
        if (action != FIRE)
        {
            return;
        }
        if (menu.canceled())
        {
            if (isEditMode())
            {
                openMenu(5);
                render();
            }
        }
        else
        {
            openMenu(8);
            render();
        }
    }

    private void actSaveMenu(int keyCode, int action)
    {
        if (action != FIRE)
        {
            return;
        }
        switch (menu.getSelect())
        {
        case 0: // create
            if (isGameMode())
            {
                ShogiBanMIDlet.showTextBox(Game.getTitle());
            }
            else if (isEditMode())
            {
                ShogiBanMIDlet.showTextBox(Problem.getTitle());
            }
            break;
        case 1: // overwrite
            if (isGameMode())
            {
                Storage.saveGame(true);
            }
            else if (isEditMode())
            {
                Storage.saveProblem(true);
            }
            setTicker(new Ticker("saved"));
            closeMenu();
            render();
            break;
        case 2: // cancel
            if (isGameMode())
            {
                openMenu(2);
            }
            else if (isEditMode())
            {
                openMenu(5);
            }
            render();
            break;
        }
    }

    private void actEditMenu(int keyCode, int action)
    {
        if (action != FIRE)
        {
            return;
        }
        switch (menu.getSelect())
        {
        case 1: // save
            openMenu(6);
            break;
        case 2: // load
            openMenu(7);
            break;
        case 3: // new
            openMenu(9);
            break;
        case 4: // change mode
            mode = 0;
            state = 0;
            Storage.saveTemporaryProblem();
            Storage.loadTemporaryGame();
            board = Game.getInstance();
            clearMovable();
            closeMenu();
            break;
        default:
            return;
        }
        render();
    }

    private void actEditMenuOnHand(int keyCode, int action)
    {
        if (action != FIRE)
        {
            return;
        }
        switch (menu.getSelect())
        {
        case 0: // move
            state = 2;
            break;
        case 1: // increment
            Problem.incrementInHands(selY-9, selX);
            break;
        case 2: // decrement
            Problem.decrementInHands(selY-9, selX);
            break;
        default:
            return;
        }
        closeMenu();
        render();
    }

    private void actEditMenuOnBan(int keyCode, int action)
    {
        if (action != FIRE)
        {
            return;
        }
        switch (menu.getSelect())
        {
        case 0: // move
            state = 1;
            colorField.setCell(selX, selY, 3);
            break;
        case 1: // flip
            Problem.flip(selY, selX);
            clearMovable();
            break;
        case 2: // change owner
            Problem.changeOwner(selY, selX);
            clearMovable();
            break;
        default:
            return;
        }
        closeMenu();
        render();
    }

    private void actGameMenu(int keyCode, int action)
    {
        if (action != FIRE)
        {
            return;
        }
        switch (menu.getSelect())
        {
        case 0: // change range view
            rangeMode = (rangeMode + 1) % 4;
            if (state == 0)
            {
                clearMovable();
            }
            else
            {
                showMovable(true);
            }
            break;
        case 1: // prev 1 step
            Game.goHistoryPrev();
            menu.setEnable(1, History.hasPrev());
            menu.setEnable(2, History.hasNext());
            state = 0;
            clearMovable();
            break;
        case 2: // next 1 step
            Game.goHistoryNext();
            menu.setEnable(1, History.hasPrev());
            menu.setEnable(2, History.hasNext());
            state = 0;
            clearMovable();
            break;
        case 3: // save
            openMenu(6);
            break;
        case 4: // load
            openMenu(10);
            break;
        case 5: // new
            openMenu(12);
            break;
        case 6: // change mode
            mode = 1;
            state = 0;
            Storage.saveTemporaryGame();
            Storage.loadTemporaryProblem();
            board = Problem.getInstance();
            clearMovable();
            closeMenu();
            break;
        default:
            return;
        }
        render();
    }

    private void actRankUpMenu(int keyCode, int action)
    {
        if (action != FIRE)
        {
            return;
        }
        if (Game.move(selY, selX, curY, curX, menu.getSelect() == 0))
        {
            state = 0;
            clearMovable();
            closeMenu();
            render();
        }
    }

    private void moveEditModeCursor(int keyCode, int action)
    {
        switch (action)
        {
        case UP:
            curY = (curY + 10) % 11;
            if (curY == 10 && curX == 8)
            {
                curX = 7;
            }
            break;
        case DOWN:
            curY = (curY + 1) % 11;
            if (curY == 9 && curX == 8)
            {
                curX = 7;
            }
            break;
        case LEFT:
            if (curY < 9)
            {
                curX = (curX + 8) % 9;
            }
            else
            {
                curX = (curX + 7) % 8;
            }
            break;
        case RIGHT:
            if (curY < 9)
            {
                curX = (curX + 1) % 9;
            }
            else
            {
                curX = (curX + 1) % 8;
            }
            break;
        case FIRE:
            if (!fireEditMode())
            {
                return;
            }
            break;
        default:
            return;
        }
        render();
    }

    private boolean fireEditMode()
    {
        switch (state)
        {
        case 0: // no state
            if (curY < 9)
            {
                if (Problem.isEmpty(curY, curX))
                {
                    return false;
                }
                selX = curX;
                selY = curY;
                openMenu(3);
            }
            else
            {
                selX = curX;
                selY = curY;
                openMenu(4);
            }
            return true;
        case 1: // move from Board
            if (curY < 9)
            {
                Problem.move(selY, selX, curY, curX);
                clearMovable();
            }
            else
            {
                Problem.moveIntoHands(selY, selX, curY - 9);
                clearMovable();
            }
            state = 0;
            colorField.setCell(selX, selY, 0);
            return true;
        case 2: // move from Hands
            if (curY < 9)
            {
                Problem.put(selY - 9, selX, curY, curX);
                clearMovable();
            }
            else
            {
                Problem.give(selY - 9, selX, curY - 9);
            }
            state = 0;
            return true;
        default:
            break;
        }
        return false;
    }

    private void movePlayModeCursor(int keyCode, int action)
    {
        switch (action)
        {
        case UP:
            curY = (curY + 10) % 11;
            if (curY == 10 && curX == 8)
            {
                curX = 7;
            }
            break;
        case DOWN:
            curY = (curY + 1) % 11;
            if (curY == 9 && curX == 8)
            {
                curX = 7;
            }
            break;
        case LEFT:
            if (curY < 9)
            {
                curX = (curX + 8) % 9;
            }
            else
            {
                curX = (curX + 7) % 8;
            }
            break;
        case RIGHT:
            if (curY < 9)
            {
                curX = (curX + 1) % 9;
            }
            else
            {
                curX = (curX + 1) % 8;
            }
            break;
        case FIRE:
            if (!firePlayeMode())
            {
                return;
            }
            break;
        default:
            return;
        }
        render();
    }

    private void clearMovable()
    {
        if (isEditMode())
        {
            for (int row = 0; row < 9; row++)
            {
                for (int col = 0; col < 9; col++)
                {
                    colorField.setCell(
                        col,
                        row,
                        Problem.isInvalid(row, col) ? 5 : 0
                    );
                }
            }
            return;
        }
        if (rangeMode == 0)
        {
            colorField.fillCells(0, 0, 9, 9, 0);
            return;
        }
        for (int row = 0; row < 9; row++)
        {
            for (int col = 0; col < 9; col++)
            {
                int cell = 0;
                if ((rangeMode&1) != 0 && Game.getRange(0, row, col) > 0)
                {
                    cell = 2;
                }
                if ((rangeMode&2) != 0 && Game.getRange(1, row, col) > 0)
                {
                    cell = Math.max(1, cell) + 4;
                }
                colorField.setCell(col, row, cell);
            }
        }
    }

    private void showMovable(boolean showCurrentPlayer)
    {
        int frame = 3;
        if (!showCurrentPlayer)
        {
            int another = 1^Game.getCurrentPlayer();
            frame = (rangeMode & (1 << another)) == 0
                ? 2 + 3*another
                : 1;
        }
        for (int row = 0; row < 9; row++)
        {
            for (int col = 0; col < 9; col++)
            {
                int cell = 0;
                if (Game.canMoveTo(row, col))
                {
                   cell = frame;
                }
                if (cell == 0 || showCurrentPlayer)
                {
                    if ((rangeMode&1) != 0 && Game.getRange(0, row, col) > 0)
                    {
                        cell = Math.max(1, cell) + 1;
                    }
                    if ((rangeMode&2) != 0 && Game.getRange(1, row, col) > 0)
                    {
                        cell = Math.max(1, cell) + 4;
                    }
                }
                colorField.setCell(col, row, cell);
            }
        }
    }

    private boolean firePlayeMode()
    {
        switch (state)
        {
        case 0: // no state
        case 4: // reset state
            if (curY >= 9)
            {
                if (Game.selectHand(curY - 9, curX))
                {
                    if (Game.getCurrentPlayer() == curY - 9)
                    {
                        state = 3;
                        selX = curX;
                        selY = curY;
                    }
                    showMovable(curY - 9 == Game.getCurrentPlayer());
                    return true;
                }
                if (state == 4)
                {
                    state = 0;
                }
                clearMovable();
                return true;
            }
            if (!Game.select(curY, curX))
            {
                if (state == 4)
                {
                    state = 0;
                }
                clearMovable();
                return true;
            }
            showMovable(Game.isCurrentPlayer(curY, curX));
            if (Game.isCurrentPlayer(curY, curX))
            {
                selX = curX;
                selY = curY;
                state = 1;
            }
            else
            {
                state = 0;
            }
            return true;
        case 1: // move from Board
            if (Game.canMoveTo(curY, curX))
            {
                if (Game.canRankUp(selY, selX, curY))
                {
                    if (!Game.needRankUp(selY, selX, curY))
                    {
                        state = 2;
                        openMenu(1);
                        return true;
                    }
                }
                if (Game.move(selY, selX, curY, curX))
                {
                    state = 0;
                    clearMovable();
                    return true;
                }
            }
            else
            {
                state = 4;
                return firePlayeMode();
            }
            break;
        case 3: // move from Hands
            if (curY >= 9 || !Game.isEmpty(curY, curX))
            {
                state = 4;
                return firePlayeMode();
            }
            if (Game.put(selX, curY, curX))
            {
                state = 0;
                clearMovable();
                return true;
            }
            break;
        }
        return false;
    }

    private static void makeStaticImages()
    {
         {
            Image img = Image.createImage(DISP_W, DISP_H);
            Graphics g = img.getGraphics();

            g.setColor(BACKGROUND_COLOR);
            g.fillRect(0, 0, DISP_W, DISP_H);

            g.setColor(LINE_COLOR);
            for (int i = 0; i < 10; i++)
            {
                g.drawLine(
                    i*CELL_SIZE + BAN_OFFSET_X,
                    BAN_OFFSET_Y,
                    i*CELL_SIZE + BAN_OFFSET_X,
                    9*CELL_SIZE + BAN_OFFSET_Y
                );

                g.drawLine(
                    BAN_OFFSET_X,
                    i*CELL_SIZE + BAN_OFFSET_Y,
                    9*CELL_SIZE + BAN_OFFSET_X,
                    i*CELL_SIZE + BAN_OFFSET_Y
                );
            }

            g.drawRect(
                BAN_OFFSET_X,
                OPPO_HAND_OFFSET_Y,
                9*CELL_SIZE,
                CELL_SIZE
            );

            g.drawRect(
                BAN_OFFSET_X,
                MY_HAND_OFFSET_Y,
                9*CELL_SIZE,
                CELL_SIZE
            );

            g.setFont(SMALL_FONT);
            g.setColor(LINE_COLOR);

            for (int i = 0; i < 9; i++)
            {
                g.drawString(
                    Integer.toString(9-i),
                    i*CELL_SIZE+CELL_SIZE/2 + BAN_OFFSET_X,
                    12 + NUMBER_H_OFFSET_Y,
                    Graphics.HCENTER|Graphics.BOTTOM
                );

                g.drawString(
                    CHARS[i],
                    NUMBER_V_OFFSET_X,
                    (i+1)*CELL_SIZE + BAN_OFFSET_Y,
                    Graphics.LEFT|Graphics.BOTTOM
                );
            }

            backgroundImage = Image.createImage(img);
        }

        // Sprite
        {
            Image img = Image.createImage(8*CELL_SIZE, 4*CELL_SIZE);
            Graphics g = img.getGraphics();

            g.setFont(SMALL_FONT);

            int invisible = 0xFFFFFF; // dummy color
            g.setColor(invisible);
            g.fillRect(0, 0, img.getWidth(), img.getHeight());

            for (int i = 0; i < 8; i++)
            {
                for (int k = 0; k < 2; k++)
                {
                    g.setColor(KOMA_COLOR);
                    g.fillTriangle(
                        i*CELL_SIZE+2,
                        k*CELL_SIZE+5,
                        i*CELL_SIZE+CELL_SIZE-2,
                        k*CELL_SIZE+5,
                        i*CELL_SIZE+CELL_SIZE/2,
                        k*CELL_SIZE+2
                    );
                    g.fillRect(
                        i*CELL_SIZE+2,
                        k*CELL_SIZE+5,
                        CELL_SIZE-4,
                        CELL_SIZE-7
                    );
                    g.setColor(LINE_COLOR);
                    g.drawLine(
                        i*CELL_SIZE+2,
                        k*CELL_SIZE+5,
                        i*CELL_SIZE+CELL_SIZE/2,
                        k*CELL_SIZE+2
                    );
                    g.drawLine(
                        i*CELL_SIZE+2,
                        k*CELL_SIZE+5,
                        i*CELL_SIZE+2,
                        k*CELL_SIZE+CELL_SIZE-2
                    );
                    g.drawLine(
                        i*CELL_SIZE+CELL_SIZE-2,
                        k*CELL_SIZE+5,
                        i*CELL_SIZE+CELL_SIZE/2,
                        k*CELL_SIZE+2
                    );
                    g.drawLine(
                        i*CELL_SIZE+CELL_SIZE-2,
                        k*CELL_SIZE+5,
                        i*CELL_SIZE+CELL_SIZE-2,
                        k*CELL_SIZE+CELL_SIZE-2
                    );
                    g.drawLine(
                        i*CELL_SIZE+2,
                        k*CELL_SIZE+CELL_SIZE-2,
                        i*CELL_SIZE+CELL_SIZE-2,
                        k*CELL_SIZE+CELL_SIZE-2
                    );

                }

                g.setColor(LINE_COLOR);

                g.drawString(
                    CHARS[i+9],
                    i*CELL_SIZE+CELL_SIZE/2+1,
                    CELL_SIZE-1,
                    Graphics.HCENTER|Graphics.BOTTOM
                );

                if (i == 4)
                {
                    continue;
                }

                if (i < 7)
                {
                    g.setColor(RED);
                }

                g.drawString(
                    CHARS[i < 4 ? (i+17) : (i+16)],
                    i*CELL_SIZE+CELL_SIZE/2+1,
                    2*CELL_SIZE-1,
                    Graphics.HCENTER|Graphics.BOTTOM
                );

            }

            int[] rgbData = new int[img.getWidth() * img.getHeight()];
            img.getRGB(rgbData, 0, img.getWidth(), 0, 0, img.getWidth(), img.getHeight());
            int mask = 0xFFFFFF;
            for (int i = 0; i < rgbData.length; i++)
            {
                if ((rgbData[i] & mask) == invisible)
                {
                    rgbData[i] = 0;
                }
                else
                {
                    rgbData[i] = rgbData[i] | 0xFF000000;
                }
            }

            for (int row = 0; row < 2; row++)
            {
                for (int col = 0; col < 8; col++)
                {
                    for (int y = 1; y < CELL_SIZE; y++)
                    {
                        int py = (row*CELL_SIZE+y) * img.getWidth();
                        int ry = ((row+2)*CELL_SIZE+(CELL_SIZE-y)) * img.getWidth();
                        for (int x = 1; x < CELL_SIZE; x++)
                        {
                            int px = col*CELL_SIZE+x;
                            int rx = col*CELL_SIZE+(CELL_SIZE-x);
                            rgbData[ry+rx] = rgbData[py+px];
                        }
                    }
                }
            }

            img = Image.createRGBImage(rgbData, img.getWidth(), img.getHeight(), true);
            komaStamp = new Sprite(img, CELL_SIZE, CELL_SIZE);
            komaField = new TiledLayer(9, 9, img, CELL_SIZE, CELL_SIZE);
            komaField.setPosition(BAN_OFFSET_X, BAN_OFFSET_Y);
        }

        // color field
        {
            Image img = Image.createImage(8*CELL_SIZE, CELL_SIZE);
            Graphics g = img.getGraphics();

            for (int i = 0; i < 8; i++)
            {
                int c = (((i>>0) & 1) * BLUE)
                      | (((i>>1) & 1) * GREEN)
                      | (((i>>2) & 1) * RED);
                g.setColor(c);
                g.fillRect(i*CELL_SIZE, 0, CELL_SIZE, CELL_SIZE);
            }

            int[] rgbData = new int[img.getWidth() * img.getHeight()];
            img.getRGB(rgbData, 0, img.getWidth(), 0, 0, img.getWidth(), img.getHeight());
            for (int i = 0; i < rgbData.length; i++)
            {
                rgbData[i] = rgbData[i] & 0x77FFFFFF;
            }

            img = Image.createRGBImage(rgbData, img.getWidth(), img.getHeight(), true);
            colorField = new TiledLayer(9, 9, img, CELL_SIZE, CELL_SIZE);
            colorField.setPosition(BAN_OFFSET_X, BAN_OFFSET_Y);
        }

        // mode mark
        {
            int w = Math.max(
                SMALL_FONT.stringWidth("PLAY"),
                SMALL_FONT.stringWidth("EDIT")
            );

            Image img = Image.createImage(w, SMALL_FONT.getHeight()*2);
            Graphics g = img.getGraphics();

            g.setFont(SMALL_FONT);

            g.setColor(GREEN);
            g.fillRect(0, 0, img.getWidth(), img.getHeight()/2);
            g.setColor(DARK_GREEN);
            g.drawString("PLAY", img.getWidth()/2, 0, Graphics.HCENTER|Graphics.TOP);

            g.setColor(MAGENTA);
            g.fillRect(0, img.getHeight()/2, img.getWidth(), img.getHeight()/2);
            g.setColor(DARK_MAGENTA);
            g.drawString("EDIT", img.getWidth()/2, img.getHeight()/2, Graphics.HCENTER|Graphics.TOP);

            img = Image.createImage(img);
            modeMark = new Sprite(img, img.getWidth(), img.getHeight()/2);
            modeMark.setPosition(0, 0);
        }
    }
}