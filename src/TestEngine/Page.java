package TestEngine;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

enum Button
{

    ENTER("Enter"),NEXT("Next"),PREVIOUS("Previous"),OK("OK"),MARK("Mark For Review"),
    LIST_MARKED_QUESTIONS("List Marked Questions"), FINISH("Finish"),
    LIST_UNATTENDED_QUESTIONS("List Unattended Questions"), CLEAR_SELECTION("Clear Selection"), EXIT("Exit");

    private String value;

    Button(String value)
    {
        this.value=value;
    }

    public String getValue()
    {
        return value;
    }

    public boolean equals(String value)
    {
        if((value instanceof String) && (this.value.equals(value)))
            return true;
        else
            return false;
    }

};

abstract class Page extends JFrame implements ActionListener
{

    protected JFrame frame;
    protected String buttonAction;

    protected Page(JFrame frame)
    {
        this.frame=frame;
    }

    protected abstract void getFrame();

    protected void hideFrame()
    {
        frame.setVisible(false);
    }

}

class FrontPage extends Page
{

    JButton enter;
    JLabel label1;
    JPanel[] panel;

    FrontPage(JFrame frame)
    {
        super(frame);
        enter=null;
        label1=new JLabel(new ImageIcon("images/Index.JPG"));
        panel =new JPanel[]{new JPanel(), new JPanel()};
    }

    @Override
    public void getFrame()
    {
        enter=new JButton("Enter");
        enter.addActionListener(this);
        panel[0].setLayout(new FlowLayout(FlowLayout.CENTER));
        panel[0].setBackground(Color.BLACK);
        panel[0].add(label1);
        panel[1].setLayout(new FlowLayout(FlowLayout.CENTER));
        panel[1].setBackground(Color.darkGray);
        panel[1].add(enter);
        JRootPane JRP=frame.getRootPane();
        JRP.setDefaultButton(enter);
        frame.add(panel[0],BorderLayout.CENTER);
        frame.add(panel[1],BorderLayout.PAGE_END);
        frame.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        buttonAction=e.getActionCommand();

        if(Button.ENTER.equals(buttonAction))
        {

            for(JPanel JP : panel)
                frame.remove(JP);

            new FormPage(frame).getFrame();
        }

    }
}

class FormPage extends Page
{

    JLabel[] label;
    JTextField text1;
    JCheckBox cbox1;
    JButton ok;
    JPanel[] panel;

    FormPage(JFrame frame)
    {
        super(frame);
        panel=new JPanel[4];
        label=new JLabel[3];

        for(int i=0;i<4;i++)
        panel[i]=new JPanel();

        label[0]=new JLabel("<HTML><H1><FONT COLOR=BLUE>FILL THE FORM..</FONT></H1><BR/><BR/><BR/></HTML>");
        label[1]=new JLabel("<HTML><H3><FONT COLOR=WHITE>ENTER YOUR NAME HERE</FONT></H3></HTML>",JLabel.RIGHT);
        label[2]=new JLabel("<HTML><H3><FONT COLOR=WHITE>PRESS OK BUTTON TO BEGIN THE TEST..</FONT></H3></HTML>",JLabel.CENTER);
        text1=new JTextField(20);
        text1.setPreferredSize( text1.getPreferredSize( ) );
        cbox1=new JCheckBox("ENABLE TIMER",true);
        ok=new JButton("OK");
        ok.addActionListener(this);
    }

    @Override
    public void getFrame()
    {
        label[1].setLabelFor(text1);
        panel[0].setLayout(new FlowLayout(FlowLayout.CENTER));
        panel[0].setBackground(Color.LIGHT_GRAY);
        panel[0].add(label[0]);
        panel[1].setLayout(new FlowLayout(FlowLayout.CENTER));
        panel[1].add(label[1]);
        panel[1].add(text1);
        panel[2].setLayout(new GridLayout(1,1));
        panel[2].add(cbox1);
        panel[3].setLayout(new FlowLayout(FlowLayout.CENTER));
        panel[3].add(label[2]);
        panel[3].add(ok);
        JRootPane JRP=frame.getRootPane();
        JRP.setDefaultButton(ok);

        for(int i=1;i<4;i++)
        panel[i].setBackground(Color.GRAY);

        frame.add(panel[0],BorderLayout.NORTH);
        frame.add(panel[1],BorderLayout.CENTER);
        frame.add(panel[2],BorderLayout.EAST);
        frame.add(panel[3],BorderLayout.SOUTH);
        frame.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        String temp=text1.getText().trim();
        buttonAction=e.getActionCommand();

        if(Button.OK.equals(buttonAction) && !temp.equals("") && temp.length()<=25 && new InputValidation().isValid(temp))
        {
            Main.Session.name=temp;
            Main.Session.timer=cbox1.isSelected();

            for(JPanel JP : panel)
                frame.remove(JP);

            JOptionPane.showMessageDialog(null, "Test is about to begin..." +
                    ((Main.Session.timer) ? "" : "")
                    +"\nFor every wrong answer, one-half of the mark will be deducted.",
                    "Get Ready!!", JOptionPane.INFORMATION_MESSAGE);

            frame.dispose();
            new TestPage().show();
        }
        else
        {
            JOptionPane.showMessageDialog(null, "Invalid Input!\n" +
                    "Name should contain only alphabets and should not exceed more than 25 characters",
                    "Form Incomplete", JOptionPane.WARNING_MESSAGE);
        }

    }
}