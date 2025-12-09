import java.util.ArrayList;

import Resources.Resource;


public class Player {
    private ArrayList<Resource> hull;
    private int credits;

    public Player() {
        hull = new ArrayList<>();
        credits = 20000; // Starting credits
    }

    public ArrayList<Resource> getHull() {
        return hull;
    }
    public int getCredits() {
        return credits;
    }
    public void setCredits(int credits) {
        this.credits = credits;
    }
}