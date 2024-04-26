package mainApp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

//22 results in 69
//509 results in 420
public class LevelGenerator {
	public static void generateLevel(long seed, boolean genFile) {
		long absSeed = (seed);
		long testNum = (long)absSeed * (long)179424673 + (long)15485863;
		testNum = Math.abs(testNum);
		ArrayList<Integer[]> levelData = new ArrayList<Integer[]>();
		for(int i = 0; i < 15; i++) {
			Integer[] rowType2 = {1,0,0,0,0,0,0,0,0,1};
			Integer[] rowType1 = {1,1,1,1,1,1,1,1,1,1};
			if(i == 0 || i == 14) {
				levelData.add(rowType1);
			}else {
				levelData.add(rowType2);
			}
		}

		String strSeed = Long.toString(testNum);
		if(Integer.parseInt(strSeed.substring(strSeed.length() - 2, strSeed.length())) == 69) {
			int vertY = Integer.parseInt(strSeed.substring(0, 1)) % 15;
			if(vertY == 0) {
				vertY += 1;
			}
			if(vertY > 13) {
				vertY = 13;
			}
			Integer[] nRow = levelData.get(vertY);
			Integer[] nRow1 = levelData.get(vertY + 1);
			nRow[0] = 0;
			nRow[9] = 0;
			nRow1[0] = 0;
			nRow1[9] = 0;
			nRow1[0] = 4;
			nRow1[1] = 4;
			nRow1[8] = 4;
			nRow1[9] = 4;
			levelData.set(vertY, nRow);
			levelData.set(vertY + 1, nRow1);
			if(vertY + 2 < 14) {
				Integer[] nRow2 = levelData.get(vertY + 2); //{0,3,0,0,0,0,0,0,3,0}
				nRow2[1] = 3;
				nRow2[8] = 3;
				levelData.set(vertY + 2, nRow2);
			}
		}
		if(Integer.parseInt(strSeed.substring(strSeed.length() - 3, strSeed.length())) == 420) {
			int horX = Integer.parseInt(strSeed.substring(1,2));
			if(horX == 0) {
				horX = 1;
			}
			if(horX > 7) {
				horX = 7;
			}
			Integer[] nRow = {1,1,1,1,1,1,1,1,1,1};
			Integer[] nRow1 = levelData.get(2);
			nRow1[horX] = 3;
			nRow1[horX + 1] = 3;
			nRow[horX] = 4;
			nRow[horX + 1] = 4;
			levelData.set(0, nRow);
			levelData.set(2, nRow1);
			levelData.set(14, nRow);
		}
		//conditions [plattype , xseedpos, y1seedpos, y2seedpos, probdigit, probvalue, bombdigit, 5th plat]
		Integer[] tempCond = {2,3,5,6,0,0,0,0};
		levelData = generatePlatform(strSeed, tempCond, levelData);
		Integer[] tempCond2 = {3,2,6,5,0,0,1,0};
		levelData = generatePlatform(strSeed, tempCond2, levelData);
		Integer[] tempCond3 = {7,1,6,7,2,1,2,0};
		levelData = generatePlatform(strSeed, tempCond3, levelData);
		Integer[] tempCond4 = {4,5,7,7,2,3,3,0};
		levelData = generatePlatform(strSeed, tempCond4, levelData);
		Integer[] tempCond5 = {6,4,2,6,2,9,4,1};
		levelData = generatePlatform(strSeed, tempCond5, levelData);
		//[typedigit, digitx, digity1, digity2]
		Integer[] tempCond6 = {7,4,1,3};
		levelData = generateLooseBombs(levelData, strSeed, tempCond6);
		Integer[] tempCond7 = {3,2,5,3};
		levelData = generateLooseBombs(levelData, strSeed, tempCond7);
		int enemyX1 = Integer.parseInt(strSeed.substring(4,5));
		int enemyY1 = Integer.parseInt(strSeed.substring(2,3) + strSeed.substring(3,4)) % 15;
		int enemyX2 = Integer.parseInt(strSeed.substring(5,6));
		int enemyY2 = Integer.parseInt(strSeed.substring(0,1) + strSeed.substring(7)) % 15;
		levelData = platformCorrect(levelData);
		if(enemyX1 == 0) {
			enemyX1 = 1;
		}else if(enemyX1 == 9) {
			enemyX1 = 8;
		}
		if(enemyY1 == 0) {
			enemyY1 = 1;
		}else if(enemyY1 >=9) {
			enemyY1 = 9;
		}
		Integer[] enemy1Row = levelData.get(enemyY1);
		enemy1Row[enemyX1] = 5;
		levelData.set(enemyY1, enemy1Row);
		if(enemyX2 == 0) {
			enemyX2 = 1;
		}else if(enemyX2 == 9) {
			enemyX2 = 8;
		}
		if(enemyY2 == 0) {
			enemyY2 = 1;
		}else if(enemyY2 >= 9) {
			enemyY2 = 9;
		}
		
		Integer[] playerRow = levelData.get(13);
		Integer[] enemy2Row = levelData.get(enemyY2);
		enemy1Row[enemyX2] = 6;
		levelData.set(enemyY2, enemy2Row);
		playerRow[4] = 2;
		levelData.set(13, playerRow);
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
	public static ArrayList<Integer[]> platformCorrect(ArrayList<Integer[]> level) {
		for(int i = 0; i < 11; i++) {
			boolean foundPlat = false;
			boolean belowPlat = false;
			for(int id: level.get(i)) {
				if(id == 3) {
					foundPlat = true;
					break;
				}
			}
			if(foundPlat) {
				for(int id: level.get(i + 3)) {
					if(id == 3) {
						foundPlat = true;
						break;
					}
				}
			}
			if(foundPlat && !belowPlat) {
				Integer[] nRow = level.get(i+ 3);
				nRow[4] = 3;
				nRow[5] = 3;
				level.set(i + 3, nRow);
			}
		}
		return level;
	}
	public static int checkForCloseness(ArrayList<Integer[]> level, int currY) {
		boolean checkUp = true;
		boolean checkDown = true;
		if(currY < 2) {
			checkUp = false;
		}else if(currY > 11) {
			checkDown = false;
		}
		if(checkUp) {
			for(int i = 0; i < 3; i ++) {
				Integer[] row = level.get(currY - i);
				for(int num : row) {
					if(num == 3) {
						return -1 * i;
					}
				}
			}
		}
		if(checkDown){
			for(int i = 0; i < 3; i ++) {
				Integer[] row = level.get(currY + i);
				for(int num : row) {
					if(num == 3) {
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
	public static ArrayList<Integer[]> generateLooseBombs(ArrayList<Integer[]> level, String seed, Integer[] conditions){
		int type = conditions[0] % 4;
		int bombX = conditions[1];
		int bombY = Integer.parseInt(seed.substring(conditions[2],conditions[2] + 1) + seed.substring(conditions[3],conditions[3] + 1)) % 15;
		if(bombX == 0) {
			bombX = 1;
		}
		if(bombY == 0 && type != 2) {
			bombY = 1;
		}
		switch (type) {
		case 0:
			if(bombY > 11) {
				bombY = 11;
			}
			for(int i = 0; i < 3; i++) {
				Integer[] eRow = level.get(bombY + i);
				eRow[bombX] = 4;
				level.set(bombY + i, eRow);
			}
			if(bombY + 3 < 13) {
				Integer[] eRow = level.get(bombY + 3);
				eRow[bombX] = 3;
				level.set(bombY + 3, eRow);
			}
			break;
			
		case 1:
			if(bombX > 6) {
				bombX = 6;
			}
			if(bombY == 14) {
				bombY = 13;
			}
			Integer[] eRow = level.get(bombY);
			for(int i = 0; i < 3; i++) {
				eRow[bombX + i] = 4;
			}
			if(bombY < 13) {
				Integer[] eRow1 = level.get(bombY + 1);
				eRow1[bombX + 1] = 3;
				level.set(bombY + 1, eRow1);
			}
			break;
			
		case 2:
			
			if(bombX > 6) {
				bombX = 6;
			}
			if(bombY < 3) {
				bombY = 3;
			}
			for(int i = 0; i < 3; i++) {
				Integer[] cRow = level.get(bombY - i);
				cRow[bombX + i] = 4;
				level.set(bombY - i, cRow);
			}
			if(bombY < 12) {
				Integer[] cRow = level.get(bombY + 1);
				cRow[bombX] = 3;
				level.set(bombY + 1, cRow);
			}
			break;
		case 3:
			if(bombX > 6) {
				bombX = 6;
			}
			if(bombY > 11) {
				bombY = 11;
			}
			for(int i = 0; i < 3; i++) {
				Integer[] cRow = level.get(bombY + i);
				cRow[bombX + i] = 4;
				level.set(bombY + i, cRow);
			}
			if(bombY + 3 < 12 ) {
				Integer[] cRow = level.get(bombY + 3);
				cRow[bombX + 2] = 3;
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
			row[platX + 1] = 4;
		}else if(platWidth == 4){
			row[platX + 1] = 4;
			row[platX + 2] = 4;
		}else if(platWidth == 2) {
			row[platX] = 4;
			row[platX + 1] = 4;
		}
		level.set(platY, row);
		return level;
	}
	
	//conditions [plattype , xseedpos, y1seedpos, y2seedpos, probdigit, probvalue, bombdigit, 5th plat]
	public static ArrayList<Integer[]> generatePlatform(String seed, Integer[] conditions, ArrayList<Integer[]> level) {
		if(Integer.parseInt(seed.substring(conditions[4], conditions[4] + 1)) < conditions[5]) {
			return level;
		}
		
		int plat = 4 - Integer.parseInt(seed.substring(conditions[0], conditions[0] + 1)) % 3;
		int platX = (Integer.parseInt(seed.substring(conditions[1], conditions[1] + 1))) % 10;
		if (platX > 9 - plat) {
			platX = 9 - plat;
		}else if(platX == 0) {
			platX = 1;
		}
		int platY = Integer.parseInt(seed.substring(conditions[2],conditions[2] + 1) + seed.substring(conditions[3],conditions[3] + 1)) % 15;
		if (platY > 11) {
			platY = 11;
		}else if(platY < 3) {
			platY = 3;
		}
		
		if(conditions[7] == 1) {
		int funnyNumber = Integer.parseInt(seed.substring(0, 1) + seed.substring(7));
		if(funnyNumber > 94) {
			Integer[] nRow = {1,1,1,1,3,3,1,1,1,1};
			Integer[] tempRow = level.get(platY - 1);
			tempRow[4] = 4;
			tempRow[5] = 4;
			level.set(platY, nRow);
			level.set(platY-1, tempRow);
			return level;
		}
		}
		
		int platYAdj = checkForCloseness(level, platY);
		if(platYAdj != 0) {
			if(checkForCloseness(level, platY - platYAdj) == 0) {
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