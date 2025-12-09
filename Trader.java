// I wrote this in C#.  Going to see if I can convert it to Java.
// This will be fun.

import Resources.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;


public class Trader {
    private Player player;
    private String oreDescription = "A chunk of rock containing valuable metals.";
    private String foodDescription = "Nutrient-rich sustenance for long space journeys.";
    private String weaponsDescription = "High-tech weaponry for defense and combat.";
    private String medicineDescription = "Advanced medical supplies for healing and recovery.";
    private Scanner scanner;
    private volatile boolean running;

    private String[] ports = {"Terra", "Alpha", "Kiber", "Epsilon", "Zeta"};
    
    public Trader() {
        scanner = new Scanner(System.in);
        running = true;
        player = new Player();

        ArrayList<Resource> resources = generateResources();
        int portIndex = new Random().nextInt(ports.length);
        
        while (running) {
            System.out.println("Welcome to port " + ports[portIndex] + "!");
            // System.out.println("\n\n");
            printTable(resources);
            System.out.println("You have " + player.getCredits() + " credits.");
            System.out.println("Your hull contains:");
            printTable(player.getHull());
            System.out.println("Would you like to buy, sell, leave, or quit?");
            String menu = scanner.nextLine();
            switch(menu) {
                case "buy":
                    resources = buyItem(resources);
                    break;
                case "sell":
                    resources = sellItem(resources);
                    break;
                case "leave":
                    portIndex++;
                    if (portIndex >= ports.length) {
                        portIndex = 0;
                    }
                    resources = generateResources(portIndex);
                    break;
                case "quit":
                    running = false;
                    break;
                default:
                    System.out.println("I don't know that input");
                    break;
            }
        }

    }

    private ArrayList<Resource> sellItem(ArrayList<Resource> resources) {
        System.out.println("Enter the item you want to sell:");
        String item = scanner.nextLine();
        System.out.println("Enter the quantity you want to sell:");
        String qty = scanner.nextLine();
        // find the hull item index
        int hullIdx = -1;
        for (int i = 0; i < player.getHull().size(); i++) {
            if (player.getHull().get(i).getName().equalsIgnoreCase(item)) {
                hullIdx = i;
                break;
            }
        }

        if (hullIdx == -1) {
            System.out.println("You don't have that item.");
            return resources;
        }

        int qtyInt;
        try {
            qtyInt = Integer.parseInt(qty);
        } catch (NumberFormatException e) {
            System.out.println("Invalid quantity.");
            return resources;
        }

        Resource hullRes = player.getHull().get(hullIdx);
        if (qtyInt > hullRes.getQuantity()) {
            System.out.println("You don't have that many to sell.");
            return resources;
        }

        // find market index (may not exist)
        int marketIdx = -1;
        for (int i = 0; i < resources.size(); i++) {
            if (resources.get(i).getName().equalsIgnoreCase(item)) {
                marketIdx = i;
                break;
            }
        }

        int saleValue;
        if (marketIdx >= 0) {
            Resource marketRes = resources.get(marketIdx);
            marketRes.setQuantity(marketRes.getQuantity() + qtyInt);
            saleValue = qtyInt * marketRes.getCost();
        } else {
            // create a new market entry using the hull item's metadata (cost/description)
            Resource newMarket = null;
            String desc = hullRes.getDescription();
            int cost = hullRes.getCost();
            switch (hullRes.getName().toLowerCase()) {
                case "ore":
                    newMarket = new Ore(qtyInt, cost, desc);
                    break;
                case "food":
                    newMarket = new Food(qtyInt, cost, desc);
                    break;
                case "weapons":
                    newMarket = new Weapons(qtyInt, cost, desc);
                    break;
                case "medicine":
                    newMarket = new Medicine(qtyInt, cost, desc);
                    break;
            }
            if (newMarket != null) {
                resources.add(newMarket);
            }
            saleValue = qtyInt * cost;
        }

        // credit the player
        player.setCredits(player.getCredits() + saleValue);

        // deduct from hull (remove if quantity becomes zero)
        if (qtyInt < hullRes.getQuantity()) {
            hullRes.setQuantity(hullRes.getQuantity() - qtyInt);
        } else {
            player.getHull().remove(hullIdx);
        }

        return resources;
    }

    private ArrayList<Resource> buyItem(ArrayList<Resource> resources) {
        System.out.println("Enter the item you want to buy:");
        String item = scanner.nextLine();
        List<String> validInput = new ArrayList<String>(){ 
            {
                add("ore");
                add("food"); 
                add("weapons");
                add("medicine");
            }   
        };
        if (validInput.contains(item.toLowerCase())) {

            System.out.println("Enter the quantity you want to buy:");
            String qty = scanner.nextLine();
            int idx = 0;
            for(Resource res : resources) {
                if (res.getName().equalsIgnoreCase(item)) {
                    break;
                } else {
                    idx++;
                }
            }
            // Can you afford it?
            int qtyInt;
            try {
                qtyInt = Integer.parseInt(qty);
            } catch (NumberFormatException e) {
                System.out.println("Invalid quantity.");
                return resources;
            }

            if (qtyInt * resources.get(idx).getCost() > player.getCredits()) {
                System.out.println("You can't afford that.");
            } else {
                if (qtyInt <= resources.get(idx).getQuantity()) {
                    // capture market values before we mutate/remove the market entry
                    String marketName = resources.get(idx).getName();
                    int marketCost = resources.get(idx).getCost();
                    String marketDesc = resources.get(idx).getDescription();

                    // deduct from market
                    int remaining = resources.get(idx).getQuantity() - qtyInt;
                    resources.get(idx).setQuantity(remaining);
                    if (remaining == 0) {
                        resources.remove(idx);
                    }

                    // add to hull: if already present, increment quantity; otherwise add a new Resource instance
                    int hullIdx = -1;
                    for (int i = 0; i < player.getHull().size(); i++) {
                        if (player.getHull().get(i).getName().equalsIgnoreCase(marketName)) {
                            hullIdx = i;
                            break;
                        }
                    }

                    if (hullIdx >= 0) {
                        Resource hullRes = player.getHull().get(hullIdx);
                        hullRes.setQuantity(hullRes.getQuantity() + qtyInt);
                    } else {
                        Resource newRes = null;
                        switch (marketName.toLowerCase()) {
                            case "ore":
                                newRes = new Ore(qtyInt, marketCost, marketDesc);
                                break;
                            case "food":
                                newRes = new Food(qtyInt, marketCost, marketDesc);
                                break;
                            case "weapons":
                                newRes = new Weapons(qtyInt, marketCost, marketDesc);
                                break;
                            case "medicine":
                                newRes = new Medicine(qtyInt, marketCost, marketDesc);
                                break;
                        }
                        if (newRes != null) {
                            player.getHull().add(newRes);
                        }
                    }

                    int totalCost = qtyInt * marketCost;
                    player.setCredits(player.getCredits() - totalCost);
                }
            }
            return resources;
        } else {
            System.out.println("I don't know that item.");
            return resources;
        }
    }

    private ArrayList<Resource> generateResources() {
        ArrayList<Resource> resources = new ArrayList<>();

        int oreQty = generateRandom(500);
        int oreCost = generateRandomBetween(15, 60);
        int foodQty = generateRandom(100);
        int foodCost = generateRandomBetween(10, 120);
        int weaponsQty = generateRandom(1200);
        int weaponsCost = generateRandomBetween(800, 2500);
        int medicineQty = generateRandom(2500);
        int medicineCost = generateRandomBetween(150, 1500);

        Resource ore = new Ore(oreQty, oreCost, oreDescription);
        Resource food = new Food(foodQty, foodCost, foodDescription);
        Resource weapons = new Weapons(weaponsQty, weaponsCost, weaponsDescription);
        Resource medicine = new Medicine(medicineQty, medicineCost, medicineDescription);


        if (oreQty > 0) {
            resources.add(ore);
        }
        if (foodQty > 0) {
            resources.add(food);
        }
        if (weaponsQty > 0) {
            resources.add(weapons);
        }
        if (medicineQty > 0) {
            resources.add(medicine);
        }

        return resources;
    }

    private void printTable(ArrayList<Resource> list) {
        System.out.println("|-----------------------|");
        System.out.println("| Item    | Qty  | Cost |");
        System.out.println("|-----------------------|");
        for(Resource res : list) {
            StringBuilder sb = new StringBuilder();
            sb.append("| ");
            sb.append(addPadding(res.getName(), 8));
            sb.append("| ");
            sb.append(addPadding(String.valueOf(res.getQuantity()), 5));
            sb.append("| ");
            sb.append(addPadding(String.valueOf(res.getCost()), 5));
            sb.append("|");
            System.out.println(sb.toString());
            System.out.println("|-----------------------|");
        }
    }

    private String addPadding(String item, int size) {
        String padding = "";
        if (item.length() < size) {
            int diff = size - item.length();
            for (int i = 0; i < diff; i++) {
                padding += ' ';
            }
        } 

        return item + padding;
    }

    private int generateRandomBetween(int min, int max) {
        Random rand = new Random();
        return rand.nextInt((max - min) + 1) + min; // Generates a random number between min and max (inclusive)
    }

    private int generateRandom(int max) {
        Random rand = new Random();
        return rand.nextInt(max); // Generates a random number between 0 and 99
    }
    public static void main(String[] args) {
        new Trader();
    }
}