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

    private static final String[] months = {
            "1st: Hammer (Deepwinter)", "2nd: Alturiak (The Claws of the Cold", "3rd: Ches (The Claw of the Sunsets",
            "4th: Tarsakh (The Claw of the Storms)", "5th: Mirtul (The Melting)", "6th: Kythorn (The Time of Flowers",
            "7th: Flamerule (Summertide)", "8th: Eleasias (Highsun)", "9th: Eleint (The Fading)",
            "10th: Marpennoth (Leafall)", "11th: Uktar (The Rotting)", "12th: Nightal (The Drawing Down)"};

    private static final String[] regions = {"1: Near Spine of the World", "2: Subarctic", "3: Temperate"};

    //JSON format: {region: {year: {month: {"temperature": int[30], "wind": int[30], "precipitation": int[30]}}}}
    private static JSONObject _jsonObject;

    public static void main(String[] args)
    {
        setUpJSON();

        Scanner scanner = new Scanner(System.in);

        System.out.println("Enter the current year: ");
        int year = scanner.nextInt();


        for (String month : months) System.out.println(month);
        System.out.println("Enter the current month: ");
        int monthIndex = scanner.nextInt() - 1;

        for (String region : regions) System.out.println(region);

        System.out.println("\nEnter current region:");
        int regionIndex = scanner.nextInt() - 1;

        //TODO look in JSON if weather already generated for these specifications

        generateWeather(monthIndex, regionIndex);
    }

    private static void generateWeather(int monthIndex, int regionIndex)
    {
        int[] temperatures = new int[30];
        int[] windStrengths = new int[30];
        int[] precipitationStrengths = new int[30];
        int[] saveDCs = new int[30];

        for (int i = 0; i < 30; i++)
        {
            temperatures[i] = rollDie(8) + lowestTemps[regionIndex][monthIndex];
            windStrengths[i] = parseWeatherD20(rollDie(20));
            precipitationStrengths[i] = parseWeatherD20(rollDie(20));

            saveDCs[i] = 5 - temperatures[i] + 3 * (windStrengths[i] + precipitationStrengths[i]);
        }

        _jsonObject.put("temperature", temperatures);
        _jsonObject.put("wind", windStrengths);
        _jsonObject.put("precipitation", precipitationStrengths);

        //TODO update JSON file
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
        JSONObject obj = new JSONObject();
        try
        {
            _jsonObject = (JSONObject) parser.parse(new FileReader("generated_climate.json"));
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
            for (String region : regions) obj.put(region, new JSONObject());

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
