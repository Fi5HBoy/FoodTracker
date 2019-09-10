/*
* ReadMe:
*   Summary:
*       This is a food tracker to track daily meals
*       Three Files are used or generated during the program
*           FoodItems.csv  |  MenuItems.csv  |  mealData.csv
*
*       Data will be saved back to these files after the program exits
*       Any corrupted files will be renamed to save corrupted data and a new file will be created
*       All data is ordered by date, if available, then by alphabetical before it is saved to its file
*
*   Menu Structure:
*       Food Menu:
*           Food Editor:
*               Add, Edit, or Remove Food Data
*           Food Viewer:
*               View Food Data
*       Menu Item Menu:
*            Menu Item Editor:
*               Add, Edit or Remove Menu Item Data
*           Menu Item Viewer:
*               View Menu Item Data
*       Save A Meal:
*           Add Meal to Saved Meals
*
*   Usage:
*       Enter the number corresponding with the desired action
*       You will be asked to fill in any desired information as you go along
*       You can enter 'q' to quit any of these prompts and it will take you back to the last menu
*       A Food must exist to add it to a Menu Item
*       A Menu Item must exist to add it to Saved Meals
* */

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class FoodTracker {
    //Input Menu Option Values
    private static final int QUIT = 0;
    private static final int FOOD_OPTIONS = 1;
    private static final int MENU_OPTIONS = 2;
    private static final int SAVE_MEAL = 3;
    private static final int EDIT_EXISTING_FOOD_ITEM = 11;
    private static final int VIEW_FOOD_ITEM = 12;
    private static final int EDIT_EXISTING_MENU_ITEM = 21;
    private static final int VIEW_MENU_ITEM = 22;
    private static final String foodFile = "./FoodItems.csv";
    private static final String menuFile = "./MenuItems.csv";
    private static final String mealFile = "./MealItems.csv";
    private static final String foodHeader = "Name,Calories,Fat,Carbs,Protein,Serving Size (g)";
    private static final String menuHeader = "Name,Ingredients,Servings (g)";
    private static final String mealHeader = "Date,Meal,Items,Total Calories";

    //File Data Storage While FoodTracker Is Running
    private static List<Food> foodData;
    private static List<MenuItem> menuData;
    private static List<Meal> mealData;

    private static List<String> MealType = new ArrayList<>(Arrays.asList("Breakfast", "Brunch", "Lunch", "Dinner", "Dessert", "Snack"));

    public static void main(String[] args) {

        //Read In File Values
        try {
            foodData = ReadInFood();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            menuData = ReadMenuItems();
        } catch (CorruptedFileException e) {
            System.out.println(e.getMessage());
            menuData = new ArrayList<>();
        } catch (NumberFormatException e) {
            RenameCorruptedFile(new File(menuFile));
            menuData = new ArrayList<>();
            System.out.println("Corrupted File Detected: Invalid Values In Menu File");
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            mealData = ReadMealData();
        } catch (CorruptedFileException e) {
            System.out.println(e.getMessage());
            mealData = new ArrayList<>();
        } catch (NumberFormatException e) {
            RenameCorruptedFile(new File(mealFile));
            mealData = new ArrayList<>();
            System.out.println("Corrupted File Detected: Invalid Values In Meal File");
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("-----------------------");

        out:
        while (true) {
            DisplayMainMenu();
            int option;
            Scanner input = new Scanner(System.in);

            try {
                option = Integer.parseInt(input.nextLine());
                System.out.println();
            } catch (NumberFormatException e) {
                System.out.println("Invalid Input");
                System.out.println();
                continue;
            }
            switch (option) {
                case QUIT:
                    break out;
                case FOOD_OPTIONS:
                    DisplayFoodItems();
                    FoodOptionHandler(input);
                    break;
                case MENU_OPTIONS:
                    DisplayMenuItems();
                    MenuOptionHandler(input);
                    break;
                case SAVE_MEAL:
                    SaveMealHandler(input);
                    break;
                default:
                    System.out.println("Invalid Input");
                    System.out.println();
                    break;
            }
        }

        SaveToFiles();
    }

    private static void SaveToFiles() {
        System.out.println("Saving Food Tracker Data");
        if (foodData.size() > 1) foodData.sort((a, b) -> a.getName().compareToIgnoreCase(b.getName()));
        if (menuData.size() > 1) menuData.sort((a, b) -> a.getName().compareToIgnoreCase(b.getName()));

        //Sort Meals By Date Then By Meal Type
        if (mealData.size() > 1) {
            mealData.sort((a, b) -> {
                int x =  a.getMealDate().compareTo(b.getMealDate());
                int y = MealType.indexOf(a.getMealType()) - MealType.indexOf(b.getMealType());
                return x == 0? x : y;
            });
        }

        try {
            System.out.println("Saving Food");
            WriteFood();
            System.out.println("Saving Menu Items");
            WriteMenu();
            System.out.println("Saving Meals");
            WriteMeals();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        System.out.println("Exiting Program");
    }

    private static void WriteMeals() throws IOException {
        try {
            Files.deleteIfExists(Paths.get(mealFile));
            File file = new File(mealFile);
            PrintWriter writer = new PrintWriter(file);

            writer.println(mealHeader);
            for (Meal meal : mealData) {
                writer.println(meal.getMealDate());
                writer.println("," + meal.getMealType());
                for (MenuItem menuItem : meal.getMealItems()) {
                    writer.println(",," + menuItem.getName());
                }
                writer.println(",,," + meal.getTotalCalories());
            }

            writer.close();
        } catch (IOException e) {
            throw new IOException("Failed to write to Meal File");
        }
    }

    private static void WriteMenu() throws IOException {
        try {
            Files.deleteIfExists(Paths.get(menuFile));
            File file = new File(menuFile);

            PrintWriter writer = new PrintWriter(file);

            writer.println(menuHeader);
            for (MenuItem menuItem : menuData) {
                writer.println(menuItem.getName());
                for (Ingredient ingredient : menuItem.getIngredients()) {
                    writer.println("," + ingredient.getFood().getName() + "," + ingredient.getWeight());
                }
                writer.println(",," + menuItem.getTotalCalories());
            }

            writer.close();
        } catch (IOException e) {
            throw new IOException("Failed to write to Menu File");
        }
    }

    private static void WriteFood() throws IOException {
        try {
            Files.deleteIfExists(Paths.get(foodFile));
            File file = new File(foodFile);

            PrintWriter writer = new PrintWriter(file);

            writer.println(foodHeader);
            for (Food food : foodData) {
                writer.println(food.toString().replace("\t",","));
            }

            writer.close();
        } catch (IOException e) {
            throw new IOException("Failed to write to Food File");
        }
    }

    private static void SaveMealHandler(Scanner input) {
        List<MenuItem> mealItems = new ArrayList<>();
        double totalCalories = 0;
        System.out.println("Which Meal Is This?");

        for (String type : MealType) {
            System.out.print(type + ", ");
        }

        try {
            String mealType;
            Date date;

            System.out.println("Enter Meal:");
            mealType = input.nextLine();
            System.out.println();
            if (QuitPrompt(mealType)) return;
            mealType = ToTitleCase(mealType);

            while (!MealType.contains(mealType)) {
                System.out.println("Please Enter A Valid Meal Type:");
                mealType = input.nextLine();
                if (QuitPrompt(mealType)) return;
            }

            DisplayMenuItems();

            out:
            while (true) {
                System.out.println("Enter Menu Item To Add (or type done):");
                String itemName = input.nextLine();
                System.out.println();
                if (QuitPrompt(itemName)) return;

                if (itemName.equalsIgnoreCase("done")) {
                    break;
                }

                MenuItem item = GetMenuItemByName(itemName);

                while (item == null) {
                    System.out.println("Please Enter A Valid Menu Item:");
                    itemName = input.nextLine();
                    if (QuitPrompt(itemName)) return;

                    if (itemName.equalsIgnoreCase("done")) {
                        break out;
                    }

                    item = GetMenuItemByName(itemName);
                }

                mealItems.add(item);
                totalCalories += item.getTotalCalories();
            }

            System.out.println("What Day Is This Meal For (MM/DD/YYYY):");
            String day = input.next();
            if (QuitPrompt(day)) return;

            while (true) {
                try {
                    date = (new SimpleDateFormat("MM/dd/yyyy")).parse(day);
                    System.out.println();
                    break;
                } catch (ParseException e) {
                    System.out.println("Please Enter Valid Date (MM/DD/YYYY:");
                    day = input.nextLine();
                    System.out.println();
                    if (QuitPrompt(day)) return;
                }
            }

            mealData.add(new Meal(date, mealType, mealItems, totalCalories));
        } catch (NumberFormatException e) {
            System.out.println("Invalid Input");
            System.out.println();
            SaveMealHandler(input);
        }
    }

    private static void MenuOptionHandler(Scanner input) {
        while (true) {
            DisplayMenuOptions();
            int option;

            try {
                option = Integer.parseInt(input.nextLine());
                System.out.println();
            } catch (NumberFormatException e) {
                System.out.println("Invalid Input");
                System.out.println();
                MenuOptionHandler(input);
                continue;
            }

            switch (option) {
                case QUIT:
                    return;
                case EDIT_EXISTING_MENU_ITEM:
                    MenuEditor(input);
                    break;
                case VIEW_MENU_ITEM:
                    ViewMenuItem(input);
                    break;
                default:
                    System.out.println("Invalid Input");
                    System.out.println();
                    break;
            }
        }
    }

    private static void ViewMenuItem(Scanner input) {
        System.out.println("Enter Menu Item Name:");

        try {
            String itemName = input.nextLine();
            if (QuitPrompt(itemName)) return;

            MenuItem item = GetMenuItemByName(itemName);

            if (item == null) {
                System.out.println("Item does not exist");
                System.out.println();
                ViewMenuItem(input);
            } else {
                System.out.println(item.toString());
                System.out.println();
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid Input");
            System.out.println();
            ViewMenuItem(input);
        }
    }

    private static void InsertMenuItem(Scanner input, String itemName) {
        itemName = itemName.trim();

        try {
            if (QuitPrompt(itemName)) return;

            System.out.println(itemName);
            System.out.println("Is This Correct? (y/n):");
            char answer = input.nextLine().charAt(0);
            System.out.println();

            while (true) {
                if (answer == 'n' || answer == 'N') {
                    MenuEditor(input);
                    return;
                } else if (answer == 'y' || answer == 'Y') {
                    break;
                }  else if (answer == 'q' || answer == 'Q') {
                    return;
                } else {
                    System.out.println("Yes or No? (y/n):");
                    answer = input.nextLine().charAt(0);
                }
            }

            MenuItem item = new MenuItem(itemName,  new ArrayList<>());
            IngredientEditor(input, item);

            if (item.getIngredients().isEmpty()) {
                menuData.remove(item);
                return;
            }

            System.out.println("Add Another? (y/n):");
            char another = input.nextLine().charAt(0);
            System.out.println();

            while (another == 'y' || another == 'Y') {
                IngredientEditor(input, item);
                System.out.println("Add Another? (y/n):");
                another = input.nextLine().charAt(0);
                System.out.println();
            }

            menuData.add(item);
        } catch (NumberFormatException e) {
            System.out.println("Invalid Input");
            System.out.println();
            MenuEditor(input);
        }
    }

    private static void MenuEditor(Scanner input) {
        System.out.println("Menu Editor|Enter Menu Item Name:");

        try {
            String itemName = input.nextLine();
            System.out.println();
            if (QuitPrompt(itemName)) return;

            MenuItem item = GetMenuItemByName(itemName);

            if (item == null) {
                System.out.println("Creating New Menu Item");
                System.out.println();
                InsertMenuItem(input, itemName);
                return;
            } else {
                System.out.println(item.toString());
                System.out.println();
                System.out.println("Would You Like To Remove This Menu Item? (y/n):");

                char removeItem = input.nextLine().charAt(0);

                if (removeItem == 'y' || removeItem == 'Y') {
                    menuData.remove(GetMenuItemByName(itemName));
                    System.out.println();
                    MenuEditor(input);
                    return;
                }

                System.out.println("NOTE: Menu Items With No Ingredients Will Be Deleted");
                System.out.println();
            }

            IngredientEditor(input, item);
        } catch (NumberFormatException e) {
            System.out.println("Invalid Input");
            System.out.println();
            MenuEditor(input);
        }
    }

    private static void IngredientEditor(Scanner input, MenuItem item) {
        System.out.println("Enter Ingredient To Add, Edit, Or Remove:");
        String s;
        try {
            String ingredientName = input.nextLine();
            System.out.println();
            if (QuitPrompt(ingredientName)) return;

            Ingredient ingredient = item.getIngredientByName(ingredientName);

            if (ingredient != null) {
                System.out.println("Enter New Serving Size in grams (0 will remove the ingredient):");
                s = input.nextLine();
                System.out.println();

                if (QuitPrompt(s)) return;
                double weight = Double.parseDouble(s);

                if (weight <= 0) {
                    item.removeIngredient(ingredient);
                } else {
                    item.updateIngredient(ingredient, weight);
                    System.out.println(ingredient.toString());
                    System.out.println();
                }
            } else {
                Food food = GetFoodByName(ingredientName);

                if (food == null) {
                    System.out.println("Invalid Input: Food Does Not Exist, Please Enter An Existing Food");
                    System.out.println();
                    IngredientEditor(input, item);
                } else {
                    System.out.println("Enter Food Serving Size in grams (must be a positive number):");
                    double servingSize;
                    s = input.nextLine();
                    System.out.println();
                    if (QuitPrompt(s)) return;
                    servingSize = Double.parseDouble(s);

                    while (Double.isNaN(servingSize) || servingSize <= 0) {
                        System.out.println("Invalid Input:");
                        s = input.nextLine();
                        if (QuitPrompt(s)) return;
                        servingSize = Double.parseDouble(s);
                    }

                    ingredient = new Ingredient(food, servingSize);
                    item.addIngredient(ingredient);
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid Input");
            System.out.println();
            IngredientEditor(input, item);
        }
    }

    private static MenuItem GetMenuItemByName(String itemName) {
        itemName = itemName.trim();
        for (MenuItem item : menuData) {
            if (item.getName().equalsIgnoreCase(itemName)) {
                return item;
            }
        }

        return null;
    }

    private static void DisplayMenuOptions() {
        System.out.println("Options: \n" +
                "Menu Item Editor: \t\t\t21\n" +
                "View A Menu Item: \t\t\t22\n" +
                "Quit To Main Menu: \t\t\t0");
        System.out.println("Please Enter Number To Continue:");
    }

    private static void FoodOptionHandler(Scanner input) {
        while (true) {
            DisplayFoodOptions();
            int option;

            try {
                option = Integer.parseInt(input.nextLine());
                System.out.println();
            } catch (NumberFormatException e) {
                System.out.println("Invalid Input");
                System.out.println();
                FoodOptionHandler(input);
                continue;
            }

            switch (option) {
                case QUIT:
                    return;
                case EDIT_EXISTING_FOOD_ITEM:
                    FoodEditor(input);
                    break;
                case VIEW_FOOD_ITEM:
                    FoodViewer(input);
                    break;
                default:
                    System.out.println("Invalid Input");
                    System.out.println();
                    break;
            }
        }
    }

    private static void FoodViewer(Scanner input) {
        System.out.println("Enter Food Name:");

        try {
            String foodName = input.nextLine();
            System.out.println();
            if (QuitPrompt(foodName)) return;

            Food food = GetFoodByName(foodName);

            if (food == null) {
                System.out.println("Food does not exist");
                System.out.println();
                FoodViewer(input);
            } else {
                System.out.println(food.toString());
                System.out.println();
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid Input");
            System.out.println();
            FoodViewer(input);
        }
    }

    private static void FoodEditor(Scanner input) {
        System.out.println("Food Editor|Enter Food Name:");
        boolean newFood = false;

        String foodName = input.nextLine();
        if (QuitPrompt(foodName)) return;

        while (foodName.isBlank()) {
            foodName = input.nextLine();
            if (QuitPrompt(foodName)) return;
        }

        System.out.println();
        System.out.println(foodName);
        foodName = foodName.toLowerCase();

        System.out.println("Is This Correct? (y/n):");
        char answer = input.nextLine().charAt(0);
        System.out.println();

        while (true) {
            if (answer == 'n' || answer == 'N') {
                FoodEditor(input);
                return;
            } else if (answer == 'y' || answer == 'Y') {
                break;
            } else {
                System.out.println("Yes or No? (y/n):");
                answer = input.nextLine().charAt(0);
                System.out.println();
            }
        }

        try {
            Food food = GetFoodByName(foodName);
            String s;

            if (food == null) {
                System.out.println();
                System.out.println("Creating New Food");
                System.out.println();
                newFood = true;
            } else {
                System.out.println("Editing Existing Food");
                System.out.println(food.toString());
                System.out.println();
                System.out.println("Would You Like To Delete The Food? (y/n):");
                answer = input.nextLine().charAt(0);

                while (true) {
                    if (answer == 'n' || answer == 'N') {
                        break;
                    } else if (answer == 'y' || answer == 'Y') {
                        foodData.remove(food);
                        System.out.println();
                        FoodEditor(input);
                        return;
                    } else {
                        System.out.println("Yes or No? (y/n):");
                        answer = input.nextLine().charAt(0);
                    }
                }
                System.out.println();
            }

            System.out.println("Enter Food Fat:");
            double fat;
            s = input.nextLine();
            if (QuitPrompt(s)) return;
            fat = Double.parseDouble(s);

            while (Double.isNaN(fat) || fat < 0) {
                System.out.println("Invalid Input:");
                s = input.nextLine();
                if (QuitPrompt(s)) return;
                fat = Double.parseDouble(s);
            }

            System.out.println("Enter Food Carbs:");
            double carbs;
            s = input.nextLine();
            if (QuitPrompt(s)) return;
            carbs = Double.parseDouble(s);

            while (Double.isNaN(carbs) || carbs < 0) {
                System.out.println("Invalid Input:");
                s = input.nextLine();
                if (QuitPrompt(s)) return;
                carbs = Double.parseDouble(s);
            }

            System.out.println("Enter Food Protein:");
            double protein;
            s = input.nextLine();
            if (QuitPrompt(s)) return;
            protein = Double.parseDouble(s);

            while (Double.isNaN(protein) || protein < 0) {
                System.out.println("Invalid Input:");
                s = input.nextLine();
                if (QuitPrompt(s)) return;
                protein = Double.parseDouble(s);
            }

            System.out.println("Enter Food Serving Size:");
            double servingSize;
            s = input.nextLine();
            if (QuitPrompt(s)) return;
            servingSize = Double.parseDouble(s);

            while (Double.isNaN(servingSize) || servingSize < 0) {
                System.out.println("Invalid Input:");
                s = input.nextLine();
                if (QuitPrompt(s)) return;
                servingSize = Double.parseDouble(s);
            }

            double cals = (9 * fat) + (4 * (carbs + protein));

            if (newFood) {
                food = new Food(foodName, cals, fat, carbs, protein, servingSize);
                System.out.println();
                System.out.println("New Food Values:");
                System.out.println(food.toString());
                System.out.println();
                System.out.println("Is This Correct? (y/n):");
                answer = input.nextLine().charAt(0);

                while (true) {
                    System.out.println();
                    if (answer == 'n' || answer == 'N') {
                        FoodEditor(input);
                        return;
                    } else if (answer == 'y' || answer == 'Y') {
                        foodData.add(food);
                        FoodEditor(input);
                        return;
                    } else {
                        System.out.println("Yes or No? (y/n):");
                        answer = input.nextLine().charAt(0);
                    }
                }
            } else {
                System.out.println("New Food Values:");
                System.out.println(new Food(foodName, cals, fat, carbs, protein, servingSize).toString());
                System.out.println();
                System.out.println("Is This Correct? (y/n):");
                answer = input.nextLine().charAt(0);

                while (true) {
                    System.out.println();
                    if (answer == 'n' || answer == 'N') {
                        FoodEditor(input);
                        return;
                    } else if (answer == 'y' || answer == 'Y') {
                        food.setCals(cals);
                        food.setFat(fat);
                        food.setCarbs(carbs);
                        food.setProtein(protein);
                        food.setServingSize(servingSize);
                        FoodEditor(input);
                        return;
                    } else {
                        System.out.println("Yes or No? (y/n):");
                        answer = input.nextLine().charAt(0);
                    }
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid Input");
            System.out.println();
            FoodEditor(input);
        }
    }

    private static void DisplayFoodOptions() {
        System.out.println("Options: \n" +
                "Food Editor: \t\t\t\t11\n" +
                "View A Food Item: \t\t\t12\n" +
                "Quit To Main Menu: \t\t\t0");
        System.out.println("Please Enter Number To Continue:");
    }

    private static void DisplayMenuItems() {
        System.out.println("Item Name\tIngredients\tTotal Cals");

        for (MenuItem item : menuData) {
            System.out.println(item.toString());
        }

        System.out.println();
    }

    private static void DisplayFoodItems() {
        System.out.println("Food\tCalories\tFat\tCarbs\tProtein\tServing Size");

        for (Food food : foodData) {
            System.out.println(food.toString());
        }

        System.out.println();
    }

    private static List<Meal> ReadMealData() throws IOException, CorruptedFileException {
        List<Meal> mealData = new ArrayList<>();
        File file = new File(mealFile);
        Date date;
        String mealType;
        List<MenuItem> mealItems = new ArrayList<>();
        double totalCalories;
        String row;
        String[] rowData;
        boolean isHeader = true;

        if (file.isFile()) {
            BufferedReader csvReader = new BufferedReader(new FileReader(file));

            while ((row = csvReader.readLine()) != null && !row.isEmpty()) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }

                rowData = row.split(",");

                if (!rowData[0].isBlank()) {
                    try {
                        date = new SimpleDateFormat("MM/dd/yyyy").parse(rowData[0]);
                    } catch (ParseException e) {
                        e.printStackTrace();
                        RenameCorruptedFile(file);
                        throw new CorruptedFileException("Corrupted File Detected: Corrupted Date");
                    }

                    row = csvReader.readLine();
                    rowData = row.split(",");

                    if (!rowData[1].isBlank()) {
                        mealType = rowData[1];

                        if (!(MealType).contains(mealType)) {
                            RenameCorruptedFile(file);
                            throw new CorruptedFileException("Corrupted File Detected: Meal Type Invalid");
                        }

                        row = csvReader.readLine();
                        rowData = row.split(",");

                        while (!rowData[2].isBlank()) {
                            MenuItem mealItem = GetMenuItemByName(rowData[2]);

                            if (mealItem == null) {
                                RenameCorruptedFile(file);
                                throw new CorruptedFileException("Corrupted File Detected: Menu Item Does Not Exist");
                            }

                            mealItems.add(mealItem);
                            row = csvReader.readLine();
                            rowData = row.split(",");
                        }
                        totalCalories = Double.parseDouble(rowData[3]);

                        mealData.add(new Meal(date, mealType, mealItems, totalCalories));
                    } else {
                        RenameCorruptedFile(file);
                        throw new CorruptedFileException("Corrupted File Detected: Meal Type Missing");
                    }
                }
            }
            csvReader.close();
        } else {
            System.out.println("No Meal Items Available");
        }

        return mealData;
    }

    //This assumes that the file is not being modified by the user and that all food exists
    private static List<MenuItem> ReadMenuItems() throws IOException, CorruptedFileException, NumberFormatException {
        List<MenuItem> menuItems = new ArrayList<>();
        File file = new File(menuFile);
        boolean isHeader = true;

        if (file.isFile()) {
            BufferedReader csvReader = new BufferedReader(new FileReader(file));
            String row;

            while ((row = csvReader.readLine()) != null && !row.isEmpty()) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }

                String[] rowData = row.split(",");

                if (!rowData[0].isBlank()) {
                    List<Ingredient> ingredients = new ArrayList<>();
                    String name = rowData[0];
                    row = csvReader.readLine();
                    rowData = row.split(",");

                    if(rowData[1].isBlank()) {
                        RenameCorruptedFile(file);
                        throw new CorruptedFileException("Corrupted File Detected: Food Is Missing");
                    }

                    while (!rowData[1].isBlank()) {
                        Food food = GetFoodByName(rowData[1]);
                        if (food == null) {
                            RenameCorruptedFile(file);
                            throw new CorruptedFileException("Corrupted File Detected: Food Does Not Exist");
                        }
                        double weight = Double.parseDouble(rowData[2]);
                        ingredients.add(new Ingredient(food, weight));
                        row = csvReader.readLine();
                        rowData = row.split(",");
                    }

                    menuItems.add(new MenuItem(name, ingredients));
                } else {
                    RenameCorruptedFile(file);
                    throw new CorruptedFileException("Corrupted File Detected: Menu Item Missing");
                }
            }
            csvReader.close();
        } else {
            System.out.println("No Menu Items Available");
        }

        return menuItems;
    }

    private static Food GetFoodByName(String foodName) {
        foodName = foodName.trim();
        for (Food food : foodData) {
            if (food.getName().equalsIgnoreCase(foodName)) {
                return food;
            }
        }

        return null;
    }

    private static List<Food> ReadInFood() throws IOException {
        List<Food> foods = new ArrayList<>();
        File file = new File(foodFile);
        boolean isHeader = true;

        if (file.isFile()) {
            BufferedReader csvReader = new BufferedReader(new FileReader(file));
            String row;

            while ((row = csvReader.readLine()) != null && !row.isEmpty()) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }

                String[] rowData = row.split(",");
                String name = rowData[0];
                double cals = Double.parseDouble(rowData[1]);
                double fat = Double.parseDouble(rowData[2]);
                double carbs = Double.parseDouble(rowData[3]);
                double protein = Double.parseDouble(rowData[4]);
                double servingSize = Double.parseDouble(rowData[5]);
                Food food = new Food(name, cals, fat, carbs, protein, servingSize);
                foods.add(food);
            }

            csvReader.close();
        } else {
            System.out.println("No Food Data Available");
        }

        return foods;
    }

    private static void DisplayMainMenu() {
        System.out.println("Press q to quit any non-menu prompt\n\n" +
                "Main Menu: \n" +
                "Food Options: \t\t\t\t1\n" +
                "Menu Options: \t\t\t\t2\n" +
                "Save A Meal: \t\t\t\t3\n" +
                "Quit: \t\t\t\t\t\t0");
        System.out.println("Please Enter Number To Continue:");
    }

    private static boolean QuitPrompt(String s) {
        boolean quit = s.equalsIgnoreCase("q");
        if (quit) System.out.println();
        return quit;
    }

    private static String ToTitleCase(String inputString)
    {
        if (inputString.isBlank()) {
            return "";
        }

        if (inputString.length() == 1) {
            return inputString.toUpperCase();
        }

        return inputString.substring(0, 1).toUpperCase() + inputString.substring(1).toLowerCase();
    }

    private static void RenameCorruptedFile(File file) {
        boolean renamed = file.renameTo(new File(file.getName() + ".corrupted"));
        if (!renamed) {
            System.out.println("Could Not Rename Corrupted File: File Will Be Deleted On Program Exit");
        }
    }
}

class CorruptedFileException extends Exception {
    CorruptedFileException(String message) {
        super(message);
    }
}

class Meal {
    private Date mealDate;
    private String mealType;
    private List<MenuItem> mealItems;
    private double totalCalories;

    Meal(Date mealDate, String mealType, List<MenuItem> mealItems, double totalCalories) {
        this.mealType = mealType;
        this.mealItems = mealItems;
        this.mealDate = mealDate;
        this.totalCalories = totalCalories;
    }

    String getMealType() {
        return mealType;
    }

    List<MenuItem> getMealItems() {
        return mealItems;
    }

    Date getMealDate() {
        return mealDate;
    }

    double getTotalCalories() {
        return totalCalories;
    }
}

class MenuItem {
    private String name;
    private List<Ingredient> ingredients;
    private double totalCalories;

    MenuItem(String name, List<Ingredient> ingredients) {
        this.name = name;
        this.ingredients = ingredients;
        totalCalories = 0;
        for(Ingredient ingredient : ingredients) {
            Food food = ingredient.getFood();
            double weight = ingredient.getWeight();
            this.totalCalories += Food.CalculateCalories(food, weight);
        }
    }

    String getName() {
        return name;
    }

    void setName(String name) {
        this.name = name;
    }

    List<Ingredient> getIngredients() {
        return ingredients;
    }

    Ingredient getIngredientByName(String ingredientName) {
        ingredientName = ingredientName.trim();
        for (Ingredient ingredient : this.ingredients) {
            if (ingredient.getFood().getName().equalsIgnoreCase(ingredientName)) {
                return ingredient;
            }
        }

        return null;
    }

    void addIngredient(Ingredient ingredient) {
        this.ingredients.add(ingredient);
        Food food = ingredient.getFood();
        double weight = ingredient.getWeight();
        this.totalCalories += Food.CalculateCalories(food, weight);
    }

    void removeIngredient(Ingredient ingredient) {
        Food food;
        double weight;
        food = ingredient.getFood();
        weight = ingredient.getWeight();

        //This will never return false the way it is currently used, but may if this method is used a different way later.
        if (this.ingredients.remove(ingredient)) {
            this.totalCalories -= Food.CalculateCalories(food, weight);
        }

    }

    void updateIngredient(Ingredient ingredient, double weight) {
        //This will never return false the way it is currently used, but may if this method is used a different way later.
        if (this.ingredients.contains(ingredient)) {
            //This will make sure that the total calories stay up to date
            this.removeIngredient(ingredient);
            ingredient.setWeight(weight);
            this.addIngredient(ingredient);
        }

    }

    double getTotalCalories() { return totalCalories; }

    public void setTotalCalories(double totalCalories) { this.totalCalories = totalCalories; }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder(name + ":\t");
        for (Ingredient ingredient : ingredients) {
            s.append(ingredient.getFood().getName()).append(" ").append(ingredient.getWeight()).append("g, ");
        }
        s = new StringBuilder(s.substring(0, s.length() - 2));
        s.append(" | ").append(totalCalories).append(" calories");
        return s.toString();
    }
}

final class Ingredient {
    private final Food food;
    private double weight;

    Ingredient(Food food, double weight) {
        this.food = food;
        this.weight = weight;
    }

    Food getFood() {
        return food;
    }

    double getWeight() {
        return weight;
    }

    void setWeight(double weight) {
        this.weight = weight;
    }

    @Override
    public String toString() {
        return food + "\t" + weight;
    }
}

class Food {
    private String name;
    private double cals;
    private double fat;
    private double carbs;
    private double protein;
    private double servingSize;

    Food(String name, double cals, double fat, double carbs, double protein, double servingSize) {
        this.name = name;
        this.cals = cals;
        this.fat = fat;
        this.carbs = carbs;
        this.protein = protein;
        this.servingSize = servingSize;
    }

    String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private double getCals() {
        return cals;
    }

    void setCals(double cals) {
        this.cals = cals;
    }

    double getFat() {
        return fat;
    }

    void setFat(double fat) {
        this.fat = fat;
    }

    double getCarbs() {
        return carbs;
    }

    void setCarbs(double carbs) {
        this.carbs = carbs;
    }

    double getProtein() {
        return protein;
    }

    void setProtein(double protein) {
        this.protein = protein;
    }

    private double getServingSize() {
        return servingSize;
    }

    void setServingSize(double servingSize) {
        this.servingSize = servingSize;
    }

    static double CalculateCalories(Food food, double weight) {
        if (food == null) {
            return 0;
        }

        double servingSize = food.getServingSize();
        double servings = weight / servingSize;
        return servings * food.getCals();
    }

    @Override
    public String toString() {
        return name + "\t" +
                cals + "\t" +
                fat + "\t" +
                carbs + "\t" +
                protein + "\t" +
                servingSize;
    }
}
