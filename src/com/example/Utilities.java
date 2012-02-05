package com.example;

/**
 * Created by IntelliJ IDEA.
 * User: joojoo
 * Date: 2/4/12
 * Time: 11:35 AM
 * To change this template use File | Settings | File Templates.
 */
import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Vibrator;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import java.util.Scanner;


public class Utilities {





    public static ArrayList<Point> readTheLevelGraph(int level) throws FileNotFoundException {


        int x , y;
        ArrayList<Point> points = new ArrayList<Point>();
        Scanner scanner = new Scanner(new File("level"+String.valueOf(level)+".dat"));
        while(scanner.hasNext())
        {
            points.add(new Point(scanner.nextInt(), scanner.nextInt()));

        }

        return points;

    }


}




