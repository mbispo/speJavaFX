package pontoeletronico;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * LogToFile class
 * This class is intended to be use with the default logging class of java
 * It save the log in an XML file  and display a friendly message to the user
 * @author Ibrabel <ibrabel@gmail.com>
 */
public class LogToFile {

    protected static final Logger logger=Logger.getLogger("MYLOG");
    /**
     * log Method 
     * enable to log all exceptions to a file and display user message on demand
     * @param ex
     * @param level
     * @param msg 
     */
    public static void log(Exception ex, String level, String msg){

        FileHandler fh = null;
        try {
            fh = new FileHandler("log2.txt",true);
            fh.setFormatter(new SimpleFormatter());
            
            logger.addHandler(fh);
            
            switch (level) {
                case "severe":
                    logger.log(Level.SEVERE, msg, ex);
                    if(!msg.equals(""))
                        JOptionPane.showMessageDialog(null,msg,
                            "Error", JOptionPane.ERROR_MESSAGE);
                    break;
                case "warning":
                    logger.log(Level.WARNING, msg, ex);
                    if(!msg.equals(""))
                        JOptionPane.showMessageDialog(null,msg,
                            "Warning", JOptionPane.WARNING_MESSAGE);
                    break;
                case "info":
                    logger.log(Level.INFO, msg, ex);
                    if(!msg.equals(""))
                        JOptionPane.showMessageDialog(null,msg,
                            "Info", JOptionPane.INFORMATION_MESSAGE);
                    break;
                case "config":
                    logger.log(Level.CONFIG, msg, ex);
                    break;
                case "fine":
                    logger.log(Level.FINE, msg, ex);
                    break;
                case "finer":
                    logger.log(Level.FINER, msg, ex);
                    break;
                case "finest":
                    logger.log(Level.FINEST, msg, ex);
                    break;
                default:
                    logger.log(Level.CONFIG, msg, ex);
                    break;
            }
        } catch (IOException | SecurityException ex1) {
            logger.log(Level.SEVERE, null, ex1);
        } finally{
            if(fh!=null)fh.close();
        }
    }

    public static void main(String[] args) {

        /*
            Create simple frame for the example
        */
        JFrame myFrame = new JFrame();
        myFrame.setTitle("LogToFileExample");
        myFrame.setSize(300, 100);
        myFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        myFrame.setLocationRelativeTo(null);
        JPanel pan = new JPanel();
        JButton severe = new JButton("severe");
        pan.add(severe);
        JButton warning = new JButton("warning");
        pan.add(warning);
        JButton info = new JButton("info");
        pan.add(info);

        /*
            Create an exception on click to use the LogToFile class
        */
        severe.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent ae) {
                int j = 20, i = 0;
                try {
                    System.out.println(j/i);
                } catch (ArithmeticException ex) {
                    log(ex,"severe","You can't divide anything by zero");
                }

            }

        });

        warning.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent ae) {
                int j = 20, i = 0;
                try {
                    System.out.println(j/i);
                } catch (ArithmeticException ex) {
                    log(ex,"warning","You can't divide anything by zero");
                }

            }

        });

        info.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent ae) {
                int j = 20, i = 0;
                try {
                    System.out.println(j/i);
                } catch (ArithmeticException ex) {
                    log(ex,"info","You can't divide anything by zero");
                }

            }

        });

        /*
            Add the JPanel to the JFrame and set the JFrame visible
        */
        myFrame.setContentPane(pan);
        myFrame.setVisible(true);
    }
}