import java.util.Scanner;

public class Context {

    // state ids
    public static final int ST_OPENING      = 0;
    public static final int ST_CLIENT_MENU  = 1;
    public static final int ST_CLERK_MENU   = 2;
    public static final int ST_MANAGER_MENU = 3;

    // event ids (column)
    public static final int EV_LOGIN_CLIENT  = 0;
    public static final int EV_LOGIN_CLERK   = 1;
    public static final int EV_LOGIN_MANAGER = 2;
    public static final int EV_BECOME_CLIENT = 3;
    public static final int EV_BECOME_CLERK  = 4;
    public static final int EV_LOGOUT        = 5;

    private int currentState;
    private boolean running = true;

    // remembering active client and how client menu was entered
    private String currentClientId = null;
    // tracking entry source for client (opening vs clerk)
    private int clientEntrySource = ST_OPENING;

    // sharing single scanner instance across all states
    private final Scanner scanner = new Scanner(System.in);
    private final Warehouse warehouse;

    // pre-creating concrete states for reuse
    private final OpeningState openingState;
    private final ClientMenuState clientMenuState;
    private final ClerkMenuState clerkMenuState;
    private final ManagerMenuState managerMenuState;

    /**
     * transitionMatrix[row = state][col = event] = nextState
     * -1 = invalid / not used / handled specially.
     */
    private final int[][] transitionMatrix = {
        // EV:                C_LOGIN           K_LOGIN           M_LOGIN           BECOME_CLIENT      BECOME_CLERK       LOGOUT
        { ST_CLIENT_MENU,    ST_CLERK_MENU,    ST_MANAGER_MENU,  -1,               -1,                -1              }, // OPENING
        { -1,                -1,               -1,               -1,               -1,                -1              }, // CLIENT_MENU (logout handled specially)
        { -1,                -1,               -1,               ST_CLIENT_MENU,   -1,                ST_OPENING      }, // CLERK_MENU
        { -1,                -1,               -1,               -1,               ST_CLERK_MENU,     ST_OPENING      }  // MANAGER_MENU
    };

    public Context(Warehouse warehouse) {
        this.warehouse = warehouse;

        // wiring states with shared context and backend
        this.openingState = new OpeningState(this, warehouse);
        this.clientMenuState = new ClientMenuState(this, warehouse);
        this.clerkMenuState = new ClerkMenuState(this, warehouse);
        this.managerMenuState = new ManagerMenuState(this, warehouse);

        // starting in opening menu
        this.currentState = ST_OPENING;
    }

    public void run() {
        // looping while FSM is active and delegating to current state
        while (running) {
            getCurrentState().run();
        }
        System.out.println("Exiting system. Goodbye.");
    }

    // FSM core

    public void changeState(int event) {
        int next;

        // handling client logout based on how client menu was entered
        if (event == EV_LOGOUT && currentState == ST_CLIENT_MENU) {
            next = (clientEntrySource == ST_CLERK_MENU)
                    ? ST_CLERK_MENU
                    : ST_OPENING;
        } else {
            // using transition matrix for normal transitions
            next = transitionMatrix[currentState][event];
        }

        if (next == -1) {
            System.out.println("Invalid action for this state.");
            return;
        }

        // updating current state
        currentState = next;
    }

    private State getCurrentState() {
        // mapping state id to concrete state object
        switch (currentState) {
            case ST_OPENING:
                return openingState;
            case ST_CLIENT_MENU:
                return clientMenuState;
            case ST_CLERK_MENU:
                return clerkMenuState;
            case ST_MANAGER_MENU:
                return managerMenuState;
            default:
                return openingState;
        }
    }

    // client context helpers

    public void setCurrentClientId(String clientId, int entrySourceState) {
        // setting active client and remembering entry origin
        this.currentClientId = clientId;
        this.clientEntrySource = entrySourceState;
    }

    public String getCurrentClientId() {
        return currentClientId;
    }

    public void clearCurrentClient() {
        // clearing active client context
        this.currentClientId = null;
    }

    // shared across states

    public Warehouse getWarehouse() {
        return warehouse;
    }

    public Scanner getScanner() {
        return scanner;
    }

    public void stop() {
        // stopping main loop and shutting down FSM
        running = false;
    }
}
