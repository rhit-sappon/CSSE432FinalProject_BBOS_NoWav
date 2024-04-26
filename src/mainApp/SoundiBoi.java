package mainApp;
import java.io.File;
import java.io.IOException;
  
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
 
public class SoundiBoi {
	long currentFrame = 0;
	static Clip clip;
	String spostStatus;
	static AudioInputStream audioInputStream;
	public static void audioPlayer(int track) throws UnsupportedAudioFileException, IOException, LineUnavailableException{
		String fileName = "";
		switch(track) {
		case 1:
			fileName = "gmo.wav";
			break;
		case 2:
			fileName = "tda.wav";
			break;
		case 3:
			fileName = "bos.wav";
			break;
		case 4:
			fileName = "sum.wav";
			break;
		case 5:
			fileName = "clb.wav";
			break;
		case 6:
			fileName = "hmk.wav";
			break;
		case 7:
			fileName = "bll.wav";
			break;
		case 8:
			fileName = "tfy.wav";
			break;
		case 9:
			fileName = "rdb.wav";
			break;
		case 10:
			fileName = "dst.wav";
			break;
		default:
			fileName = "tda.wav";
			break;
		}
		audioInputStream = AudioSystem.getAudioInputStream(new File(fileName).getAbsoluteFile());
		clip = AudioSystem.getClip();
		clip.open(audioInputStream);
		clip.loop(Clip.LOOP_CONTINUOUSLY);
	}
	public static void playTrack() {
		SoundiBoi.clip.start();
	}
	public static void stopTrack() {
		SoundiBoi.clip.stop();
		SoundiBoi.clip.close();
	}

}
