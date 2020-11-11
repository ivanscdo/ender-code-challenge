package com.ivansaucedo;

import com.ender.game.client.Bot;
import com.ender.game.client.EndersGameClient;
import com.ender.game.model.Direction;
import com.ender.game.model.Grid;
import com.ender.game.model.Player;

import java.util.Random;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class ThirdBot implements Bot {
	ArrayList<Point> exploreMap = new ArrayList<Point>();
	ArrayList<Integer> returnMap = new ArrayList<Integer>();
	ArrayList<Integer> depositMap = new ArrayList<Integer>();
	public int returnMapIndex = 0;
	public int depositMapIndex = 0;
	public boolean returnToBase = false;
	public boolean moveOrExtract = false;
	public boolean depositResource = false;
	public int exploreCount = 0;
	public int prevMoveIndex = 99;
	public boolean depositMapClockwise = true;
	
	  @Override
	  public String getName() {
	    return "ThirdBot142149";
	  }

	  @Override
	  public String getEmail() {
	    return "ivan.scdo+game.ender.com@gmail.com";
	  }
	  
	  @Override
	  public String getToken() {
		  return "valentine";
	  }

	  @Override
	  public void act(Player me, Grid grid) {
		  // unit is moving: exploring or returning to base
		  if (!moveOrExtract) {
			  
			  // unit is exploring
			  if (!returnToBase) {
				  moveOrExtract = true;
				  int moveIndex = generateMoveIndex();
				  // save moveIndex so unit won't immediately back trace; used in generateMoveIndex()
				  prevMoveIndex = moveIndex;
				  try {
					  grid.getUnits(me).forEach(unit -> unit.move(Direction.values()[moveIndex]));
					  // record where unit has been (except if it's an illegal move)
					  chartExploreMap(moveIndex);				  
				  // catch IllegalStateException and prevent illegal move from being recorded
				  } catch (Exception e) {
					  // if unit tries to move off edge of map, send unit in the oposite direction
					  int reversedMoveIndex = reverseIndex(moveIndex);
					  prevMoveIndex = reversedMoveIndex;
					  grid.getUnits(me).forEach(unit -> unit.move(Direction.values()[reversedMoveIndex]));
					  chartExploreMap(reversedMoveIndex);				  
				  }
		
			  // player is returning to base, but not depositing resource
			  } else if (returnToBase && !depositResource) {
				  int moveBackIndex = returnMap.get(returnMapIndex);
				  grid.getUnits(me).forEach(unit -> unit.move(Direction.values()[moveBackIndex]));
				  // unit is at base, will deposit on next turn 
				  if (returnMapIndex == returnMap.size()-1) {
					  depositResource = true;
					  chartDepositMap(depositMapClockwise);
				  }
				  returnMapIndex += 1;
				  
			  // unit is returning to base and depositing resource; unit is back at base
			  } else if (returnToBase && depositResource) {

				  try {
					  // unit tries to deposit by following circular pattern
					  grid.getUnits(me).forEach(unit -> unit.depositResource());
					  int depositMoveIndex = depositMap.get(depositMapIndex);
					  grid.getUnits(me).forEach(unit -> unit.move(Direction.values()[depositMoveIndex]));
					  depositMapIndex += 1;
					  if (depositMapIndex == 4) {
						  chartDepositMap(!depositMapClockwise);
						  depositMapIndex = 0;
					  }
				  } catch(Exception e) {
					  // if exception caught, deposit was made 
					  System.out.println(e);
					  resetCartographer();
				  }
				
			  }
			  
		  // unit is not moving, but making attempt to extract resources
		  } else if (moveOrExtract) {
			  try {
				  moveOrExtract = false;
				  grid.getUnits(me).forEach(unit -> unit.extractResource());
			  } catch(Exception e) {
				  System.out.println("attempted to extract 2nd resource, returning to base. " + e);
				  chartReturnMap();
			  }
		  }
	  }
	  
	  public int generateMoveIndex() {
		  int newMoveIndex = 0;
		  ArrayList<Integer> moveOptions = new ArrayList<>();

	      switch (prevMoveIndex) {
	      		// previously moved north, now can only move west, north, east  
	          	case 0:	Collections.addAll(moveOptions, 3,0,1);
	          			newMoveIndex = selectMove(moveOptions, 3);
	                  	break;
              	// previously moved east, now can only move north, east, south 
	          	case 1: Collections.addAll(moveOptions, 0,1,2);
	          			newMoveIndex = selectMove(moveOptions, 3);
	                  	break;
              	// previously moved south, now can only move east, south, west 
	          	case 2:	Collections.addAll(moveOptions, 1,2,3);
	          			newMoveIndex = selectMove(moveOptions, 3);
	                  	break;
              	// previously moved west, now can only move north, west, south 
	          	case 3:	Collections.addAll(moveOptions, 0,3,2);
	          			newMoveIndex = selectMove(moveOptions, 3);
	          			break;
				// First step, player can move in any direction
	          	case 99: Collections.addAll(moveOptions, 0,1,2,3);
	          			 newMoveIndex = selectMove(moveOptions, 4);
	          			 break;
	      }
	      
		  return newMoveIndex;
	  }
	  
	  public int selectMove(List<Integer> moveOptions, int numOptions) {
		  // chose a random direction based on move options given
		  Random rand = new Random();
		  int moveOptIndex = rand.nextInt(numOptions);
		  int randMoveIndex = moveOptions.get(moveOptIndex);
		  return randMoveIndex; 
	  }
	  
	  public int reverseIndex(int indexToReverse) {
		  int reverseMoveIndex = 0;
		  
		  switch (indexToReverse) {
		  		case 0: reverseMoveIndex = 2;
		  				break;
		  		case 1: reverseMoveIndex = 3;
  						break;
		  		case 2: reverseMoveIndex = 0;
  						break;
		  		case 3: reverseMoveIndex = 1;
  						break; 
		  }
		  return reverseMoveIndex;
	  }

	  
	  public void chartExploreMap(int indexToChart) {
		  int xCoord, yCoord;
		  
		  if (exploreCount == 0) { 
			  Point origin = new Point(0,0);
			  exploreMap.add(origin);
		  }
		  // get previous coordinates, then add or subtract to record change in reference to current location
		  Point prevCoord = exploreMap.get(exploreCount);
		  
		  switch (indexToChart) {
		  		// north 		  		
		  		case 0:	xCoord = (int)prevCoord.getX();
		      			yCoord = (int)prevCoord.getY() + 1;
		      			exploreMap.add(new Point(xCoord, yCoord));
		      			break;
		      	// east
		  		case 1:	xCoord = (int)prevCoord.getX() + 1;
		      			yCoord = (int)prevCoord.getY();
		      			exploreMap.add(new Point(xCoord, yCoord));
		      			break;
		      	// south
		  		case 2: xCoord = (int)prevCoord.getX();
		                yCoord = (int)prevCoord.getY() - 1;
	                    exploreMap.add(new Point(xCoord, yCoord));
		                break;
		        // west
		  		case 3:	xCoord = (int)prevCoord.getX() - 1;
		      			yCoord = (int)prevCoord.getY();
		      			exploreMap.add(new Point(xCoord, yCoord));
		      			break;
		  }
		  exploreCount += 1;
	  }
	  
	  public void chartReturnMap() {
		  returnToBase = true;

		  // reverse iterate through exploreMap to chart path back to base 
          for (int i = exploreMap.size()-1; i >= 1; i--) {
              int currentXCoord = (int)exploreMap.get(i).getX();
              int prevXCoord = (int)exploreMap.get(i-1).getX();
              int currentYCoord = (int)exploreMap.get(i).getY();
              int prevYCoord = (int)exploreMap.get(i-1).getY();

              // difference in following ints will determine direction
              // if difference == 0, this means no movement along that axis
              // if 1, move either up x-axis, or right y-axis
              // if -1, move down x-axis, or left y-axis
              int returnXCoord = prevXCoord - currentXCoord;
              int returnYCoord = prevYCoord - currentYCoord;
              // System.out.println(i + " -> " + (i-1) + " : (" + returnXCoord + "," + returnYCoord + ")");

              // movement will be along y-axis
              if (returnXCoord == 0) {
                  if (returnYCoord == 1) {
                      // north
                      returnMap.add(0);
                  } else if (returnYCoord == -1) {
                      // south
                      returnMap.add(2);
                  }
                  
              // movement will be along y-axis   
              } else if (returnYCoord == 0) {
                  if (returnXCoord == 1) {
                      // east
                      returnMap.add(1);
                  } else if (returnXCoord == -1) {
                      // west
                      returnMap.add(3);
                  }
              }
          }
	  }
	  
	  public void chartDepositMap(boolean clockwise) {	
		  // in the event origin is at a diagonal from the base, unit circles to deposit
		  if (clockwise) {
			  Collections.addAll(depositMap, 0,1,2,3);
		  } else {
			  depositMap.clear();
			  Collections.addAll(depositMap, 2,1,0,4);
		  }
		  
	  }
	  
	  public void resetCartographer() {
		  moveOrExtract = false;
		  returnToBase = false;
		  depositResource = false;
		  returnMapIndex = 0;
		  depositMapIndex = 0;
		  exploreCount = 0;
		  prevMoveIndex = 99;
		  exploreMap.clear();
		  returnMap.clear();
		  depositMap.clear();
	  }

	  public static void main(String[] args) {
	    // Option 1: play against StupidBot
	    EndersGameClient.run(new ThirdBot(), "StupidBot")
	        .openWebBrowserWhenMatchStarts();

	    // Option 2: wait in a queue to play against other people's bots
	    // EndersGameClient.run(new ThirdBot())
	    // .openWebBrowserWhenMatchStarts();
	  }

}
