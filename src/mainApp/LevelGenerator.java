package mainApp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;


  

//22 results in 69
//509 results in 420
public class LevelGenerator {
	private static final int AIR = 0;
	private static final int WALL = 1;
	private static final int PLAYER1 = 2;
	private static final int PLAT = 3;
	private static final int BOMB = 4;
	private static final int ENEMY1 = 5;
	private static final int ENEMY2 = 6;
	private static final int PLAYER2 = 7;
	public static void generateLevel(long seed, boolean genFile, int xdim, int ydim) {
		long absSeed = (seed);
		long testNum = (long)absSeed * (long)179424673 + (long)15485863;
		testNum = Math.abs(testNum);
		ArrayList<Integer[]> levelData = new ArrayList<Integer[]>();
		for(int i = 0; i < ydim; i++) {
			Integer[] rowType2 = new Integer[xdim];
			Integer[] rowType1 = new Integer[xdim];
			for (int j = 0; j < xdim; j++) {
				rowType1[j] = WALL;
				if (j == 0 || j == xdim - 1){
					rowType2[j] = WALL;
				} else {
					rowType2[j] = AIR;
				}
			}
			// Integer[] rowType2 = {1,0,0,0,0,0,0,0,0,1};
			// Integer[] rowType1 = {1,1,1,1,1,1,1,1,1,1};
			if(i == 0 || i == ydim-1) {
				levelData.add(rowType1);
			}else {
				levelData.add(rowType2);
			}
		}

		String strSeed = Long.toString(testNum) + Long.toString(testNum);
		
		if(Integer.parseInt(strSeed.substring(strSeed.length() - 2, strSeed.length())) == 69) {
			int vertY = Integer.parseInt(strSeed.substring(0, 1)) % 15;
			if(vertY == 0) {
				vertY += 1;
			}
			if(vertY > ydim - 2) {
				vertY = ydim - 2;
			}
			Integer[] nRow = levelData.get(vertY);
			Integer[] nRow1 = levelData.get(vertY + 1);
			nRow[0] = AIR;
			nRow[xdim - 1] = AIR;
			nRow1[0] = AIR;
			nRow1[xdim -1] = AIR;
			nRow1[0] = BOMB;
			nRow1[1] = BOMB;
			nRow1[xdim - 2] = BOMB;
			nRow1[xdim - 1] = BOMB;
			levelData.set(vertY, nRow);
			levelData.set(vertY + 1, nRow1);
			if(vertY + 2 < 14) {
				Integer[] nRow2 = levelData.get(vertY + 2); //{0,3,0,0,0,0,0,0,3,0}
				nRow2[1] = PLAT;
				nRow2[xdim - 2] = PLAT;
				levelData.set(vertY + 2, nRow2);
			}
		}
		if(Integer.parseInt(strSeed.substring(strSeed.length() - 3, strSeed.length())) == 420) {
			int horX = Integer.parseInt(strSeed.substring(1,2));
			if(horX == 0) {
				horX = 1;
			}
			if(horX > xdim - 2) {
				horX = xdim -2;
			}
			Integer[] nRow = new Integer[xdim];
			for (int i = 0; i < xdim; i++) {
				nRow[i] = WALL;
			}
			Integer[] nRow1 = levelData.get(2);
			nRow1[horX] = PLAT;
			nRow1[horX + 1] = PLAT;
			nRow[horX] = BOMB;
			nRow[horX + 1] = BOMB;
			levelData.set(0, nRow);
			levelData.set(2, nRow1);
			levelData.set(14, nRow);
		}
		int playX = (xdim - 2) / 2;
		//conditions [plattype , xseedpos, y1seedpos, y2seedpos, probdigit, probvalue, bombdigit, 5th plat]
		Integer[] tempCond = {2,(playX + playX/2)%(strSeed.length()-2),ydim / 2, ydim / 2,0,0,0,0};
		levelData = generatePlatform(strSeed, tempCond, levelData, xdim, ydim);
		Integer[] tempCond2 = {3,(playX + playX/4)%(strSeed.length()-2),ydim / 2, ydim / 2 - 1,0,0,1,0};
		levelData = generatePlatform(strSeed, tempCond2, levelData, xdim, ydim);
		Integer[] tempCond3 = {7,(playX - playX/2)%(strSeed.length()-2),(ydim - 2) / 2, (ydim - 2) / 2 + 1,2,1,2,0};
		levelData = generatePlatform(strSeed, tempCond3, levelData, xdim, ydim);
		Integer[] tempCond4 = {5,playX%(strSeed.length()-2),ydim / 2 + 1,ydim / 2 + 1,2,3,3,0};
		levelData = generatePlatform(strSeed, tempCond4, levelData, xdim, ydim);
		Integer[] tempCond5 = {6,(playX + (xdim-2)/3)%(strSeed.length()-2),(ydim - 2) / 6,(ydim - 2) / 2 + 1,3,9,4,1};
		levelData = generatePlatform(strSeed, tempCond5, levelData, xdim, ydim);
		//[typedigit, digitx, digity1, digity2]
		Integer[] tempCond6 = {7,xdim/3 + 1,1,3};
		levelData = generateLooseBombs(levelData, strSeed, tempCond6, xdim, ydim);
		Integer[] tempCond7 = {3,(xdim-2)/4,5,3};
		levelData = generateLooseBombs(levelData, strSeed, tempCond7, xdim, ydim);
		int enemyX1 = Integer.parseInt(strSeed.substring(4,5));
		int enemyY1 = (int)(Long.parseLong(strSeed.substring(2,3) + strSeed.substring(3,4)) % ydim);
		int enemyX2 = Integer.parseInt(strSeed.substring(5,6));
		int enemyY2 = (int)(Long.parseLong(strSeed.substring(0,1) + strSeed.substring(7)) % ydim);
		levelData = platformCorrect(levelData, xdim, ydim);
		if(enemyX1 == 0) {
			enemyX1 = 1;
		}else if(enemyX1 == xdim - 1) {
			enemyX1 = xdim - 2;
		}
		if(enemyY1 == 0) {
			enemyY1 = 1;
		}else if(enemyY1 >= ydim - 6) {
			enemyY1 = ydim - 6;
		}
		Integer[] enemy1Row = levelData.get(enemyY1);
		enemy1Row[enemyX1] = ENEMY1;
		levelData.set(enemyY1, enemy1Row);
		if(enemyX2 == 0) {
			enemyX2 = 1;
		}else if(enemyX2 == xdim - 1) {
			enemyX2 = xdim -2;
		}
		if(enemyY2 == 0) {
			enemyY2 = 1;
		}else if(enemyY2 >= ydim - 6) {
			enemyY2 = ydim - 6;
		}
		
		Integer[] playerRow = levelData.get(ydim -2);
		Integer[] enemy2Row = levelData.get(enemyY2);
		enemy1Row[enemyX2] = ENEMY2;
		levelData.set(enemyY2, enemy2Row);
		int player1X = Integer.parseInt(strSeed.substring(3,6))%(xdim-2) + 1;
		int player2X = Integer.parseInt(strSeed.substring(2,5))%(xdim-2) + 1;
		playerRow[player1X] = PLAYER1;
		playerRow[player2X] = PLAYER2;
		levelData.set(ydim - 2, playerRow);
		if(genFile) {
			String filename = "levels/level" + seed + ".csv";
			File newLevel = new File(filename);
			PrintWriter pw = null;
			try {
				pw = new PrintWriter(newLevel);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			for(Integer[] row : levelData) {
				String textRow = "";
				for(int num : row) {
					textRow += num + "," ;
				}
				pw.println(textRow);
			}
			pw.close();
		}else {
			String filename = "levelGen.csv";
			File newLevel = new File(filename);
			PrintWriter pw = null;
			try {
				pw = new PrintWriter(newLevel);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			for(Integer[] row : levelData) {
				String textRow = "";
				for(int num : row) {
					textRow += num + "," ;
				}
				pw.println(textRow);
			}
			pw.close();
		}
		
	}
	public static ArrayList<Integer[]> platformCorrect(ArrayList<Integer[]> level, int xdim, int ydim) {
		boolean foundPlat = true;
		boolean lastPlat = false;
		for(int i = 1; i < ydim - 4; i++) {
			for (int bombX = 1; bombX < xdim -1; bombX++) {
				boolean belowPlat = false;
				Integer[] row = level.get(i);
				int id = row[bombX];
				if(id != PLAT) {
					lastPlat = false;
					continue;
				}
				if(lastPlat){
					continue;
				}
				if(foundPlat) {
					row = level.get(i + 3);
					for(int j = bombX - 3; j < bombX + 3; j++) {
						if (j < 1) {
							continue;
						}
						if (j > xdim -2){
							break;
						}
						id = row[bombX];
						if(id == PLAT || id == WALL) {
							belowPlat = true;
							break;
						}
					}
				}
				int middle = (xdim-2)/2;
				if(foundPlat && !belowPlat) {
					Integer[] nRow = level.get(i+ 3);
					nRow[bombX] = PLAT;
					nRow[bombX + 1] = PLAT;
					level.set(i + 3, nRow);
				}
				lastPlat = true;
			}
		}
		return level;
	}
	public static int checkForCloseness(ArrayList<Integer[]> level, int currY, int ydim) {
		boolean checkUp = true;
		boolean checkDown = true;
		if(currY < 2) {
			checkUp = false;
		}else if(currY >= ydim - 4) {
			checkDown = false;
		}
		if(checkUp) {
			for(int i = 0; i < 3; i ++) {
				Integer[] row = level.get(currY - i);
				for(int num : row) {
					if(num == PLAT) {
						return -1 * i;
					}
				}
			}
		}
		if(checkDown){
			for(int i = 0; i < 3; i ++) {
				Integer[] row = level.get(currY + i);
				for(int num : row) {
					if(num == PLAT) {
						return i;
					}
				}
			}
		}
		return 0;
	}
	
	public static Integer[] createRow(Integer[] row, int platWidth, int platX) {
		for(int i = 0; i < platWidth; i++) {
			row[platX + i] = 3;
		}
		return row;
	}
	//[typedigit, digitx, digity1, digity2]
	public static ArrayList<Integer[]> generateLooseBombs(ArrayList<Integer[]> level, String seed, Integer[] conditions, int xdim, int ydim){
		int type = conditions[0] % 4;
		int bombX = conditions[1];
		int bombY = Integer.parseInt(seed.substring(conditions[2],conditions[2] + 1) + seed.substring(conditions[3],conditions[3] + 1)) % ydim;
		if(bombX == 0) {
			bombX = 1;
		}
		if(bombY == 0 && type != 2) {
			bombY = 1;
		}
		switch (type) {
		case 0:
			if(bombY > ydim -4) {
				bombY = ydim -4;
			}
			for(int i = 0; i < 3; i++) {
				Integer[] eRow = level.get(bombY + i);
				eRow[bombX] = BOMB;
				level.set(bombY + i, eRow);
			}
			if(bombY + 3 < ydim - 3) {
				Integer[] eRow = level.get(bombY + 3);
				eRow[bombX] = PLAT;
				level.set(bombY + 3, eRow);
			}
			break;
			
		case 1:
			if(bombX > xdim - 4) {
				bombX = xdim - 4;
			}
			if(bombY > ydim - 2) {
				bombY = ydim - 2;
			}
			Integer[] eRow = level.get(bombY);
			for(int i = 0; i < 3; i++) {
				eRow[bombX + i] = BOMB;
			}
			if(bombY < ydim - 2) {
				Integer[] eRow1 = level.get(bombY + 1);
				eRow1[bombX + 1] = PLAT;
				eRow1[bombX - 1] = PLAT;
				level.set(bombY + 1, eRow1);
			}
			break;
			
		case 2:
			
			if(bombX > xdim -4) {
				bombX = xdim - 4;
			}
			if(bombY < 3) {
				bombY = 3;
			}

			for(int i = 0; i < 3; i++) {
				Integer[] cRow = level.get(bombY - i);
				cRow[bombX + i] = BOMB;
				level.set(bombY - i, cRow);
			}
			if(bombY < ydim - 3) {
				Integer[] cRow = level.get(bombY + 1);
				cRow[bombX] = BOMB;
				level.set(bombY + 1, cRow);
			}
			break;
		case 3:
			if(bombX > xdim - 7) {
				bombX = xdim - 7;
			}
			if(bombY > ydim - 5) {
				bombY = ydim - 5;
			}
			for(int i = 0; i < 3; i++) {
				Integer[] cRow = level.get(bombY + 2 - i);
				if (cRow[bombX + 2*i] == WALL) {
					i--;
					bombY--;
					continue;
				}
				cRow[bombX + 2*i] = BOMB;
				level.set(bombY + 2 - i, cRow);
			}
			if(bombY + 3 < ydim-3 ) {
				Integer[] cRow = level.get(bombY + 3);
				cRow[bombX + 2] = PLAT;
				cRow[(bombX + 5)] = PLAT;
				level.set(bombY + 3, cRow);
			}
			break;
		}
		return level;
		
	}
	
	public static ArrayList<Integer[]> generatePlatBombs(ArrayList<Integer[]> level ,int platWidth, int platX, int platY, int bombNumber) {
		if(bombNumber % 2 == 1) {
			platY -= 1;
		}else {
			platY += 1;
		}
		Integer[] row = level.get(platY);
		if(platWidth == 3) {
			row[platX + 1] = BOMB;
		}else if(platWidth == 4){
			row[platX + 1] = BOMB;
			row[platX + 2] = BOMB;
		}else if(platWidth == 2) {
			row[platX] = BOMB;
			row[platX + 1] = BOMB;
		}
		level.set(platY, row);
		return level;
	}
	
	//conditions [plattype , xseedpos, y1seedpos, y2seedpos, probdigit, probvalue, bombdigit, 5th plat]
	public static ArrayList<Integer[]> generatePlatform(String seed, Integer[] conditions, ArrayList<Integer[]> level, int xdim, int ydim) {
		if(Integer.parseInt(seed.substring(conditions[4], conditions[4] + 1)) < conditions[5]) {
			return level;
		}
		
		int plat = (xdim/5) - Integer.parseInt(seed.substring(conditions[0], conditions[0] + 1)) % 3;
		int platX = (Integer.parseInt(seed.substring(conditions[1], conditions[1] + 2))) % xdim;
		if (platX > xdim - 1 - plat) {
			platX = xdim - 1 - plat;
		}else if(platX == 0) {
			platX = 1;
		}
		int platY = Integer.parseInt(seed.substring(conditions[2],conditions[2] + 1) + seed.substring(conditions[3],conditions[3] + 1)) % 15;
		if (platY > ydim - 4) {
			platY = ydim - 4;
		}else if(platY < 3) {
			platY = 3;
		}
		
		if(conditions[7] == 1) {
		int funnyNumber = (int)(Long.parseLong(seed.substring(0, 1) + seed.substring(7)) % Integer.MAX_VALUE);
		if(funnyNumber > 94) {
			Integer[] nRow = new Integer[xdim];
			int middle = (xdim - 2) / 2 ;
			for (int i = 0; i < xdim; i++) {
				if (i == middle||i == middle + 1){
					nRow[i] = PLAT;
				} else {
					nRow[i] = WALL;
				}
			}
			Integer[] tempRow = level.get(platY - 1);
			tempRow[middle] = BOMB;
			tempRow[middle + 1] = BOMB;
			level.set(platY, nRow);
			level.set(platY-1, tempRow);
			return level;
		}
		}
		
		int platYAdj = checkForCloseness(level, platY, ydim);
		if(platYAdj != 0) {
			if(checkForCloseness(level, platY - platYAdj, ydim) == 0) {
				platY += platYAdj;
				Integer[] nRow = createRow(level.get(platY), plat, platX);
				level.set(platY, nRow);
				level = generatePlatBombs(level, plat, platX, platY, conditions[6]);
			}
		}else {
			Integer[] nRow = createRow(level.get(platY), plat, platX);
			level.set(platY, nRow);
			level = generatePlatBombs(level, plat, platX, platY, conditions[6]);
		}
		
		return level;
	}

}