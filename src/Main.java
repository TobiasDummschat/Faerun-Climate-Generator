import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class Main
{

    //Array with lowest temperatures per region and month matching input in main()
    // lowestTemps[0] is Near Spine of the World
    // lowestTemps[1] is Subarctic
    // lowestTemps[2] is Temperate
    private static final int[][] lowestTemps = {
            {-13, -14, -9, -3, +2, +7, +10, +9, +5, -1, -6, -9},
            {-10, -11, -7, -2, +3, +9, +12, +11, +6, +0, -3, -7},
            {-2, -3, -1, +2, +6, +10, +12, +12, +9, +5, +2, +1}};

    private static final String[] monthDescriptions = {
            "1st: Hammer (Deepwinter)", "2nd: Alturiak (The Claws of the Cold", "3rd: Ches (The Claw of the Sunsets",
            "4th: Tarsakh (The Claw of the Storms)", "5th: Mirtul (The Melting)", "6th: Kythorn (The Time of Flowers",
            "7th: Flamerule (Summertide)", "8th: Eleasias (Highsun)", "9th: Eleint (The Fading)",
            "10th: Marpennoth (Leafall)", "11th: Uktar (The Rotting)", "12th: Nightal (The Drawing Down)"};

    private static final String[] regionDescriptions = {"1: Near Spine of the World", "2: Subarctic", "3: Temperate"};

    //JSON format: {region: {year: {month: {"temperature": int[30], "wind": int[30], "precipitation": int[30]}}}}
    private static JSONObject _climateObj;

    public static void main(String[] args)
    {
        setUpJSON();

        Scanner scanner = new Scanner(System.in);

        System.out.println("Enter the current year: ");
        int year = scanner.nextInt();

        System.out.println(_climateObj.isEmpty());
        System.out.println(_climateObj.containsKey("" + year));
        if (_climateObj.isEmpty() || !_climateObj.containsKey("" + year))
            generateWeather(year);

        for (String month : monthDescriptions) System.out.println(month);
        System.out.println("Enter the current month: ");
        int monthIndex = scanner.nextInt() - 1;

        for (String region : regionDescriptions) System.out.println(region);

        System.out.println("\nEnter current region:");
        int regionIndex = scanner.nextInt() - 1;


    }

    private static void generateWeather(int year)
    {

        JSONObject yearObj = new JSONObject();

        for (int month = 0; month < 12; month++)
        {
            JSONObject monthObj = new JSONObject();

            for (int day = 0; day < 30; day++)
            {
                JSONObject dayObj = new JSONObject();

                for (int region = 0; region < 3; region++)
                {
                    int temperature = rollDie(8) + lowestTemps[region][month];
                    int windStrength = parseWeatherD20(rollDie(20));
                    int precipitationStrength = parseWeatherD20(rollDie(20));

                    int saveDC = 5 - temperature + 3 * (windStrength + precipitationStrength);

                    JSONObject regionObj = new JSONObject();

                    regionObj.put("temperature", temperature);
                    regionObj.put("wind", windStrength);
                    regionObj.put("precipitation", precipitationStrength);
                    regionObj.put("saveDC", saveDC);

                    dayObj.put(regionDescriptions[region], regionObj);
                }

                monthObj.put(day, dayObj);
            }

            yearObj.put(month, monthObj);
        }
        _climateObj.put(year, yearObj);
        writeJSON(_climateObj);
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

    private static void parseJSONFile()
    {
        JSONParser parser = new JSONParser();
        try
        {
            _climateObj = (JSONObject) parser.parse(new FileReader("generated_climate.json"));
        } catch (Exception e)
        {
            System.out.println(e);
        }
    }

    private static void setUpJSON()
    {
        File json = new File("generated_climate.json");
        if (json.exists())
            parseJSONFile();
        else
        {
            JSONObject obj = new JSONObject();
            _climateObj = obj;
            writeJSON(obj);
        }
    }

    private static void writeJSON(JSONObject obj)
    {
        try (FileWriter writer = new FileWriter("generated_climate.json"))
        {
            writer.write(obj.toJSONString());
        } catch (IOException e)
        {
            System.out.println(e);
        }
    }

}
