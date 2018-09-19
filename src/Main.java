import com.google.gson.*;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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

    //Json format: {year: {region: {month: {day: {"temperature": int, "wind": int, "precipitation": int, "saveDC": }}}}}
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
        int month = scanner.nextInt();

        for (String region : REGION_DESCRIPTIONS) System.out.println(region);

        System.out.println("\nEnter current region:");
        String region = REGION_DESCRIPTIONS[scanner.nextInt() - 1];

        System.out.println("\nEnter DC-counting-threshold: ");
        int threshold = scanner.nextInt();

        //TODO actually output the wanted information

        output(year, month, region, threshold);

    }

    private static void output(int year, int month, String region, int threshold)
    {
        List<Integer> smallDCs = new ArrayList<>();
        List<Integer> largeDCs = new ArrayList<>();

        climateObj.getAsJsonObject("" + year).getAsJsonObject(region).getAsJsonObject("" + month).keySet().forEach(
                dayKey ->
                {
                    int dc = climateObj.getAsJsonObject("" + year)
                                       .getAsJsonObject(region)
                                       .getAsJsonObject("" + month)
                                       .getAsJsonObject(dayKey)
                                       .get("saveDC").getAsInt();
                    if (dc <= threshold) smallDCs.add(dc);
                    else largeDCs.add(dc);
                }
        );
        Collections.sort(largeDCs);
        Collections.reverse(largeDCs);
        largeDCs.forEach(dc -> System.out.print(dc + ", "));
        System.out.println("\nDCs below " + threshold + ": " + smallDCs.size());
        System.out.println("DCs over " + threshold + ": " + largeDCs.size());
    }

    private static void generateWeather(int year)
    {

        JsonObject yearObj = new JsonObject();
        for (int region = 1; region <= 3; region++)
        {
            JsonObject regionObj = new JsonObject();

            for (int month = 1; month <= 12; month++)
            {
                JsonObject monthObj = new JsonObject();

                for (int day = 1; day <= 30; day++)
                {


                    int temperature = rollDie(8) + LOWEST_TEMPS[region - 1][month - 1];
                    int windStrength = parseWeatherD20(rollDie(20));
                    int precipitationStrength = parseWeatherD20(rollDie(20));

                    int saveDC = Math.max(0, 5 - temperature + 3 * (windStrength + precipitationStrength));

                    JsonObject dayObj = new JsonObject();

                    dayObj.addProperty("temperature", temperature);
                    dayObj.addProperty("wind", windStrength);
                    dayObj.addProperty("precipitation", precipitationStrength);
                    dayObj.addProperty("saveDC", saveDC);

                    monthObj.add("" + day, dayObj);
                }
                regionObj.add("" + month, monthObj);
            }

            yearObj.add(REGION_DESCRIPTIONS[region - 1], regionObj);
        }

        climateObj.add("" + year, yearObj);
        updateJson(climateObj);
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
            updateJson(new JsonObject());
        }
    }

    private static void sortYears()
    {
        JsonObject sortedClimateObj = new JsonObject();

        List<String> sortedKeyList = new ArrayList<>(climateObj.keySet());
        Collections.sort(sortedKeyList);

        sortedKeyList.forEach(key -> sortedClimateObj.add(key, climateObj.get(key)));
        climateObj = sortedClimateObj;
    }

    /**
     * sets obj as new climateObj, sorts it by years, and writes it to JSON_PATH
     *
     * @param obj the new climateObj. Will be sorted.
     */
    private static void updateJson(JsonObject obj)
    {
        climateObj = obj;
        sortYears();
        writeJson(climateObj);
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
