import com.google.gson.*;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class Main
{
    private static final String JSON_PATH = "generated_climate.json";

    //Array with lowest temperatures per region and month matching input in main()
    // LOWEST_TEMPS[0] is Near Spine of the World
    // LOWEST_TEMPS[1] is Subarctic
    // LOWEST_TEMPS[2] is Temperate
    private static final int[][] LOWEST_TEMPS = {
            {-13, -14, -9, -3, +2, +7, +10, +9, +5, -1, -6, -9},
            {-10, -11, -7, -2, +3, +9, +12, +11, +6, +0, -3, -7},
            {-2, -3, -1, +2, +6, +10, +12, +12, +9, +5, +2, +1}};

    private static final String[] MONTH_DESCRIPTIONS = {
            "1st: Hammer (Deepwinter)", "2nd: Alturiak (The Claws of the Cold)", "3rd: Ches (The Claw of the Sunsets)",
            "4th: Tarsakh (The Claw of the Storms)", "5th: Mirtul (The Melting)", "6th: Kythorn (The Time of Flowers)",
            "7th: Flamerule (Summertide)", "8th: Eleasias (Highsun)", "9th: Eleint (The Fading)",
            "10th: Marpennoth (Leafall)", "11th: Uktar (The Rotting)", "12th: Nightal (The Drawing Down)"};

    private static final String[] REGION_DESCRIPTIONS = {"1: Near Spine of the World", "2: Subarctic", "3: Temperate"};

    private static Gson gson = new GsonBuilder().setPrettyPrinting().create();

    //Json format: {year: {month: {day: {"temperature": int, "wind": int, "precipitation": int, "saveDC": }}}}
    private static JsonObject climateObj;

    public static void main(String[] args)
    {
        setUpJson();

        Scanner scanner = new Scanner(System.in);

        System.out.println("Enter the current year: ");
        int year = scanner.nextInt();

        if (!climateObj.has("" + year))
            generateWeather(year);

        for (String month : MONTH_DESCRIPTIONS) System.out.println(month);
        System.out.println("Enter the current month: ");
        int monthIndex = scanner.nextInt() - 1;

        for (String region : REGION_DESCRIPTIONS) System.out.println(region);

        System.out.println("\nEnter current region:");
        int regionIndex = scanner.nextInt() - 1;


    }

    private static void generateWeather(int year)
    {

        JsonObject yearObj = new JsonObject();

        for (int month = 0; month < 12; month++)
        {
            JsonObject monthObj = new JsonObject();

            for (int day = 0; day < 30; day++)
            {
                JsonObject dayObj = new JsonObject();

                for (int region = 0; region < 3; region++)
                {
                    int temperature = rollDie(8) + LOWEST_TEMPS[region][month];
                    int windStrength = parseWeatherD20(rollDie(20));
                    int precipitationStrength = parseWeatherD20(rollDie(20));

                    int saveDC = 5 - temperature + 3 * (windStrength + precipitationStrength);

                    JsonObject regionObj = new JsonObject();

                    regionObj.addProperty("temperature", temperature);
                    regionObj.addProperty("wind", windStrength);
                    regionObj.addProperty("precipitation", precipitationStrength);
                    regionObj.addProperty("saveDC", saveDC);

                    dayObj.add(REGION_DESCRIPTIONS[region], regionObj);
                }

                monthObj.add("" + day, dayObj);
            }

            yearObj.add("" + month, monthObj);
        }
        climateObj.add("" + year, yearObj);
        writeJson(climateObj);
    }

    private static int parseWeatherD20(int roll)
    {
        if (roll <= 12) return 0;
        else if (roll <= 17) return 1;
        else return 2;
    }

    private static int rollDie(int numberOfSides)
    {
        return (int) Math.ceil(Math.random() * numberOfSides);
    }

    private static JsonObject parseJsonFile(String path) throws FileNotFoundException
    {
        JsonParser parser = new JsonParser();
        return (JsonObject) parser.parse(new FileReader(path));
    }

    private static void setUpJson()
    {
        try
        {
            climateObj = parseJsonFile(JSON_PATH);
        } catch (FileNotFoundException ignored)
        {
            JsonObject obj = new JsonObject();
            climateObj = obj;
            writeJson(obj);
        }
    }

    private static void writeJson(JsonElement obj)
    {
        try (FileWriter writer = new FileWriter(JSON_PATH))
        {
            writer.write(gson.toJson(obj));
        } catch (IOException fileWritingException)
        {
            System.err.println("Writing JsonElement to file has failed.\n" + fileWritingException);
        }
    }

}
