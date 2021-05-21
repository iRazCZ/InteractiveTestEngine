package TestEngine;

import java.awt.Toolkit;
import java.awt.Dimension;
import java.awt.event.*;
import javax.swing.*;

public class Main extends JFrame
{

    public static Dimension dim;

    Main()
    {
        super("Interactive Test Engine");
        dim=Toolkit.getDefaultToolkit().getScreenSize();
        setSize(dim.width,95*dim.height/100);

        addWindowListener(new WindowAdapter( )
        {
            @Override
            public void windowClosing(WindowEvent e)
            {
                System.exit(0);
            }
        });

    }

    public static void main(String[] args)
    {
        Main MFrame = new Main();
        Page Frame1 = new FrontPage(MFrame);
        Frame1.getFrame();
    }

    public static class Session
    {

        public static String name;
        public static boolean timer;
        public static int seconds;
        public static int minutes;
        public static int hours;
        public static int timeTaken=-1;
        public static float markScored;
        public static int noOfRightAnswers;
        public static int noOfWrongAnswers;
        public static int noOfQuestionsMarked;
        public static int noOfQusetionsAttended;
        public static int noOfQuestionsNotAttended;

    }

}