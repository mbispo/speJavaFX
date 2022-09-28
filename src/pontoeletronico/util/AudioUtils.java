package pontoeletronico.util;

/**
 * Created by diego.daniel on 18/12/2017.
 */
import java.io.*;
import javax.sound.sampled.*;

public class AudioUtils {

    public static final String ARQUIVO_ERRO = "audio/erroSound.au";
    public static final String ARQUIVO_OK = "audio/okSound.au";

    public static AudioInputStream stream;
    public static AudioFormat format;
    public static DataLine.Info info;
    public static Clip clip;

    public static void tocarAudio(String arquivo) {
        try {

            File fileSom = new File(Ambiente.getInstance().getSpeHome() + arquivo);

            stream = AudioSystem.getAudioInputStream(fileSom);
            format = stream.getFormat();
            info = new DataLine.Info(Clip.class, format);
            clip = (Clip) AudioSystem.getLine(info);
            clip.open(stream);
            clip.start();
            while (!clip.isRunning()) {
                Thread.sleep(10);
            }
            while (clip.isRunning()) {
                Thread.sleep(10);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}