/*
 * Arm.java
 *
 * Copyright 2018 Javrielle Domingo <domingjavr@love-a-coffee-cafe.ecs.vuw.ac.nz>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301, USA.
 *
 *
 */

import org.omg.CORBA.MARSHAL;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;
public class Arm {
	//fields
	private static final int PEN_DOWN = 1500;
	private static final int PEN_UP = 1300;
	private int motorLeftX = 270;
	private int motorLeftY = 480;
	private int motorRightX = 400;
	private int motorRightY = 480;
	private int motorDistance = motorRightX - motorLeftX;  //unit = pixels
	private int armRadius = 200; //unit = pixels

	//create an arrayList of coordinates (each coordinate is an array with an x and y value)
	private List<double[]> coordinates = new ArrayList<>();

	//constructor
	private Arm() {

		//create a new ImageProcessor object, and get the first image to load
		//ImageProcessor imageProcessor = new ImageProcessor();

        //get user input on what type of thing to draw
        Scanner input = new Scanner(System.in);
        System.out.println("Enter 0 to 5 inclusive for horizontal/vertical/diagonal/square/circle: ");
        int direction = input.nextInt();
        input.close();

		drawLine(direction);
	}

	//Draw Line
	private void drawLine(int direction) {
		coordinates.clear();

		//draws a horizontal line
		if(direction == 0){
			//using the getCoords method
			getCoords(280, 120, 350, 120);
		}
		//draws a vertical line
		else if (direction == 1){
			getCoords(300, 100, 300, 200);
		}
		//draws a diagonal line
		else if (direction == 2){
			getCoords(280, 100, 350, 200);
		}
        //draws a rectangle
		else if (direction == 3){
			//loop through some coordinates for a square line of 70px/70px
			getCoords(280, 100, 350, 100);	//top horizontal
			getCoords(350, 100, 350, 170);	//right vertical
			getCoords(350, 170, 280, 170);	//bottom horizontal
			getCoords(280, 170, 280, 100);	//left vertical
		}

		else if (direction == 4){
			//loop through some coordinates for a circular line.
			int centerX = 325;
			int centerY = 135;
			int radius = 20;
			for(double i = 0; i < 360; i += 2){

				double x = 0.9 * radius * Math.cos(Math.toRadians(i)) + centerX;
				double y = radius * Math.sin(Math.toRadians(i)) + centerY;

				// add new coordinates to arrayList
				coordinates.add(new double[]{x, y});
			}
		}

		else if (direction == 5) {
			//loop through some coordinates for 'SKYNET' letters drawing.
			int x = 170;
			int y = 290;

			

		}


		//write the new coordinates to file, if there are coordinates
		if(!coordinates.isEmpty()){
			writeCoordinatesToFile();
		}
		else{
			System.out.println("No coordinates to do");
		}
	}


	//get the coordinates for points on a line (given start & end points)
	private void getCoords(double startX, double startY, double endX, double endY){

		//number of points that make up the line
		int lineSplit = 100;

		//calculate the gradient between end and start points
		double xGradient = endX - startX;
		double yGradient = endY - startY;

		//check if x gradient is negative. if not then want positive increment
        if(startX < endX) {
            if(startY < endY) {
                //increment the x and y values, and add them to the list of coordinates
                while (startX <= endX && startY <= endY) {

                    //increment x and y
                    startX += xGradient / lineSplit;
                    startY += yGradient / lineSplit;

                    // add new coordinates to arraylist
                    coordinates.add(new double[]{startX, startY});
                }
            }
            //else want negative y increment
            else{
                //increment the x and y values, and add them to the list of coordinates
                while (startX <= endX && startY >= endY) {

                    //increment x and y
                    startX += xGradient / lineSplit;
                    startY -= yGradient / lineSplit;

                    // add new coordinates to arrayList
                    coordinates.add(new double[]{startX, startY});
                }
            }
        }
        //else endX is smaller so want negative x increment
        else{
            if(startY < endY){
                //increment the x and y values, and add them to the list of coordinates
                while (startX >= endX && startY <= endY) {

                    //increment x and y
                    startX -= xGradient / lineSplit;
                    startY += yGradient / lineSplit;

                    // add new coordinates to arrayList
                    coordinates.add(new double[]{startX, startY});
                }
            }
            else {
                //increment the x and y values, and add them to the list of coordinates
                while (startX >= endX && startY >= endY) {

                    //increment x and y
                    startX -= xGradient / lineSplit;
                    startY -= yGradient / lineSplit;

                    // add new coordinates to arrayList
                    coordinates.add(new double[]{startX, startY});
                }
            }
        }
	}


	private void writeCoordinatesToFile(){
		try{
			//pen up for first value
			double[] firstCoordinate = coordinates.get(0);
			double firstLeftArmAngle = findLeftArmAngle(firstCoordinate[0], firstCoordinate[1]);
			double firstRightArmAngle = findRightArmAngle(firstCoordinate[0], firstCoordinate[1]);
			double firstLeftPwmValue = leftAnglePwmConverter(firstLeftArmAngle);
			double firstRightPwmValue = rightAnglePwmConverter(firstRightArmAngle);
			PrintStream writer = new PrintStream(new FileOutputStream(new File("drawLine.txt")));
			writer.println( (int)firstLeftPwmValue + "," + (int)firstRightPwmValue + "," + PEN_UP);


			//Calculate motor control signals and print to writer
			for(double[] coordinate : coordinates){
				System.out.println(coordinate[0] + ", " + coordinate[1]);

				double leftArmAngle = findLeftArmAngle(coordinate[0], coordinate[1]);
				double rightArmAngle = findRightArmAngle(coordinate[0], coordinate[1]);

				System.out.println("Left angle: " + leftArmAngle + ". Right angle: " + rightArmAngle);

				double leftPwmValue = leftAnglePwmConverter(leftArmAngle);
				double rightPwmValue = rightAnglePwmConverter(rightArmAngle);

				writer.println( (int)leftPwmValue + "," + (int)rightPwmValue + "," + PEN_DOWN);
				if (coordinate == coordinates.get(coordinates.size() - 1)){
					writer.println( (int)leftPwmValue + "," + (int)rightPwmValue + "," + PEN_UP);
				}
			}

			coordinates.clear();
			writer.flush();
			writer.close();
		}catch(IOException e ){System.out.println("FileWrite IOExecption" + e);}
	}


	//find angle needed for right arm from tool position
	private double findRightArmAngle(double toolX, double toolY) {

		//distance between tool and motor
		double toolMotorDistance = Math.sqrt( Math.pow(toolX - motorRightX, 2) + Math.pow(toolY - motorRightY, 2));

		//midPoints
		double motorRightToolMidpointX = (toolX + motorRightX)/2;
		double motorRightToolMidpointY = (toolY + motorRightY)/2;

		//calculate distance between the midpoints and joints
		double midpointJointDistance = Math.sqrt( Math.pow(armRadius, 2) - Math.pow(toolMotorDistance/2, 2));
		System.out.println(midpointJointDistance);

		//angle between motors and first pen coord
		double motorJointAngle = Math.acos((motorRightX - toolX)/motorDistance);

		//calculate joint positions for left join
		double jointX = motorRightToolMidpointX + midpointJointDistance * Math.sin(motorJointAngle);
		double jointY = motorRightToolMidpointY - midpointJointDistance * Math.cos(motorJointAngle);

		//calculate joint positions for right joint
		return Math.atan2(jointY - motorRightY, jointX - motorRightX);
	}


	//find angle needed for left arm from tool position
	private double findLeftArmAngle(double toolX, double toolY){
		//distance between tool and motor
		double toolMotorDistance = Math.sqrt( Math.pow(toolX - motorLeftX, 2) + Math.pow(toolY - motorLeftY, 2));

		//midPoints
		double motorLeftToolMidpointX = (toolX + motorLeftX)/2;
		double motorLeftToolMidpointY = (toolY + motorLeftY)/2;

		//calculate distance between the midpoints and joints

		double midpointJointDistance = Math.sqrt( Math.pow(armRadius, 2) - Math.pow(toolMotorDistance/2, 2));

		System.out.println("MidpointJointDistance: " + midpointJointDistance);

		//angle between motors and first pen coord
		double motorJointAngle = Math.acos((motorLeftX - toolX)/motorDistance);

		System.out.println("MidpointJointAngle: " + motorJointAngle);

		//calculate joint positions for left joint
		double jointX = motorLeftToolMidpointX - midpointJointDistance * Math.sin(motorJointAngle);
		double jointY = motorLeftToolMidpointY + midpointJointDistance * Math.cos(motorJointAngle);

		//calculate joint positions for right joint
		return Math.atan2(jointY - motorLeftY, jointX - motorLeftX);

	}


	//converts angle to PWM for right arm
	private double rightAnglePwmConverter(double angle){
		double constant = 2343;
		double gradient = 657;
		return gradient * angle + constant;
	}

	//converts angle to PWM for left arm
	private double leftAnglePwmConverter(double angle){
		double constant = 2807;
		double gradient = 657;
		return gradient * angle + constant;
	}

	//Main
	public static void main(String[] args) {
		new Arm();
	}
}
