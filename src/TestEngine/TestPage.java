package TestEngine;

import java.sql.*;
import java.util.*;

public class TestPage extends javax.swing.JFrame implements Runnable
{
    ResultSet rs;
    DBConnection dbc;
    ArrayList<String> qlist;
    ArrayList<String> clist;
    Question[] q;
    boolean[] flag;
    boolean threadSync;
    private int cursor;
    Thread t;

    public TestPage()
    {
        initComponents();
        initDatabase();
        setSize(Main.dim.width,95*Main.dim.height/100);
        comboContents();
        displayItem(0,0);
        t=new Thread(this);
        t.start();
    }

    private void initDatabase()
    {
        try
        {
            dbc = new DBConnection("TestEngine");
            rs=dbc.executeQuery("SELECT COUNT(*) FROM QUESTION");
            rs.next();
            Question.noOfQuestions=Integer.parseInt(rs.getString(1));
            q=new Question[Question.noOfQuestions];
            flag=new boolean[Question.noOfQuestions];
            qlist = new ArrayList<String>();
            int qcount=1;
            int ccount;

            while(qcount<=Question.noOfQuestions)
            {
                rs=this.dbc.executeQuery("SELECT * FROM QUESTION WHERE QUESTION.QID="+qcount);
                rs.next();
                qlist.add(rs.getString(1)+")  "+rs.getString(2));
                rs=this.dbc.executeQuery("SELECT COUNT(*) FROM CHOICE WHERE CHOICE.QID="+qcount);
                rs.next();
                clist = new ArrayList<String>();
                ccount=Integer.parseInt(rs.getString(1));
                rs=this.dbc.executeQuery("SELECT CNAME FROM CHOICE WHERE CHOICE.QID="+qcount);

                while(ccount<=Question.MAX_NO_OF_CHOICES_PER_QUESTION && ccount >=Question.MIN_NO_OF_CHOICES_PER_QUESTION && rs.next())
                    clist.add(rs.getString(1));

                q[qcount-1]=new Question(qlist.get(qcount-1), clist.size(), Choice.Single, clist);
                rs=this.dbc.executeQuery("SELECT QUESTION.ANSWER FROM QUESTION WHERE QUESTION.QID="+qcount);
                rs.next();
                q[qcount-1].setAnswer(Integer.parseInt(rs.getString(1)));
                qcount++;
            }

            this.dbc.executeMetaQuery("DELETE * FROM ANSWERSESSION");
            this.jProgressBar1.setMinimum(0);
            this.jProgressBar1.setMaximum(Question.noOfQuestions);
        }
        catch (ClassNotFoundException ex)
        {
        }
        catch (SQLException ex)
        {
        }
    }

    private synchronized void displayItem(int index, int choice)
    {
        this.jTextPane1.setText(q[index].getQuestion());
        jRadioButton1.setText(q[index].getChoiceAtIndex(0));
        jRadioButton2.setText(q[index].getChoiceAtIndex(1));
        jRadioButton3.setText(q[index].getChoiceAtIndex(2));
        jRadioButton4.setText(q[index].getChoiceAtIndex(3));
        jRadioButton5.setText(q[index].getChoiceAtIndex(4));
        jRadioButton6.setText(q[index].getChoiceAtIndex(5));

        switch(choice)
        {
            case 1:
                jRadioButton1.setSelected(true);
                break;
            case 2:
                jRadioButton2.setSelected(true);
                break;
            case 3:
                jRadioButton3.setSelected(true);
                break;
            case 4:
                jRadioButton4.setSelected(true);
                break;
            case 5:
                jRadioButton5.setSelected(true);
                break;
            case 6:
                jRadioButton6.setSelected(true);
                break;
            default :
                buttonGroup1.clearSelection();
                break;
        }

        if(this.q[this.cursor].getMark())
        {
            this.mark.setSelected(true);
            this.mark.setText("Unmark");
        }
        else
        {
            this.mark.setSelected(false);
            this.mark.setText("Mark For Review");
        }

    }

    @Override
    public void run()
    {
        String timer;

        if(Main.Session.timer)
        {
            double diffLevel=0.6;
            double sec=Math.floor(((diffLevel*Question.noOfQuestions)/0.6)*60);
            Main.Session.minutes=(int)sec/60;
            Main.Session.hours=(Main.Session.minutes/60);

            try
            {
                for(int i=(int)sec;i>=0;i--)
                {
                    Main.Session.timeTaken++;
                    Main.Session.seconds = i % 60;

                    if (Main.Session.seconds == 59)
                    {
                        if (Main.Session.minutes == 0)
                        {
                            Main.Session.hours--;
                            Main.Session.minutes=60;
                        }

                        Main.Session.minutes--;
                        Main.Session.minutes = (Main.Session.minutes) % 60;
                    }

                    timer="TIME REMAINING  =    0"+Main.Session.hours+" : "+((Main.Session.minutes<10)?"0":"")+(Main.Session.minutes)+" : "+((Main.Session.seconds<10)?"0":"")+(Main.Session.seconds);
                    this.jTextPane3.setText(timer);
                    Thread.sleep(1000);
                }

                try
                {
                    if(this.threadSync)
                        this.t.suspend();

                    javax.swing.JOptionPane.showMessageDialog(null, "Time Up!!\nPress OK button to see your score.", "End of Test Session", javax.swing.JOptionPane.INFORMATION_MESSAGE);
                    this.finish();
                }
                catch (SQLException ex)
                {
                }
            }
            catch (InterruptedException ex)
            {
            }
        }
        else
        {
            timer="TIME REMAINING   :   NA";
            this.jTextPane3.setText(timer);
        }

    }

    private synchronized void evaluate()
    {
        for (Question eval : this.q)
        {
            if (eval.getAnswer() == eval.getUserAnswer())
            {
                Main.Session.markScored += 1.0f;
                Main.Session.noOfRightAnswers++;
            }
            else
            {
                if (eval.getUserAnswer() != 0)
                {
                    Main.Session.markScored -= 0.5f;
                    Main.Session.noOfWrongAnswers++;
                }
            }
        }
    }

    private synchronized void insert() throws SQLException
    {
            int counter=1;
            javax.swing.JRadioButton[] JRB={jRadioButton1, jRadioButton2, jRadioButton3, jRadioButton4, jRadioButton5, jRadioButton6};

            for(javax.swing.JRadioButton iterJRB : JRB)
            {

                if(iterJRB.isSelected())
                {

                    if(q[this.cursor].getUserAnswer()==0 && !flag[this.cursor])
                    {
                        String SQL="INSERT INTO ANSWERSESSION VALUES("+(this.cursor+1)+","+counter+")";
                        this.dbc.executeMetaQuery(SQL);
                        q[this.cursor].setUserAnswer(counter);
                        flag[this.cursor]=false;
                    }
                    else
                    {
                        String SQL="UPDATE ANSWERSESSION SET ANSWERSESSION.ANSWER="+counter+" WHERE ANSWERSESSION.QID="+(this.cursor+1);
                        this.dbc.executeMetaQuery(SQL);
                        q[this.cursor].setUserAnswer(counter);
                    }
                    
                    break;
                }

                counter++;
            }

            if(counter==7)
            {
                if(!flag[this.cursor])
                {
                    this.dbc.executeMetaQuery("INSERT INTO ANSWERSESSION VALUES("+(this.cursor+1)+","+0+")");
                    flag[this.cursor]=true;
                }
                else
                {
                    this.q[this.cursor].setUserAnswer(0);
                    String SQL="UPDATE ANSWERSESSION SET ANSWERSESSION.ANSWER="+(0)+" WHERE ANSWERSESSION.QID="+(this.cursor+1);
                    this.dbc.executeMetaQuery(SQL);
                }
            }

    }

    private synchronized void updateProgressBar() throws SQLException
    {
            rs = dbc.executeQuery("SELECT COUNT(*) FROM ANSWERSESSION WHERE ANSWERSESSION.ANSWER <> 0");
            rs.next();
            Main.Session.noOfQusetionsAttended = Integer.parseInt(rs.getString(1));
            this.jProgressBar1.setValue(Main.Session.noOfQusetionsAttended);
    }
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        previous = new javax.swing.JButton();
        next = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextPane2 = new javax.swing.JTextPane();
        jProgressBar1 = new javax.swing.JProgressBar();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTextPane3 = new javax.swing.JTextPane();
        list_unattended_questions = new javax.swing.JButton();
        list_marked_questions = new javax.swing.JButton();
        finish = new javax.swing.JButton();
        mark = new javax.swing.JToggleButton();
        clear_selection = new javax.swing.JButton();
        jComboBox1 = new javax.swing.JComboBox();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextPane1 = new javax.swing.JTextPane();
        jPanel3 = new javax.swing.JPanel();
        jRadioButton2 = new javax.swing.JRadioButton();
        jRadioButton3 = new javax.swing.JRadioButton();
        jRadioButton1 = new javax.swing.JRadioButton();
        jRadioButton4 = new javax.swing.JRadioButton();
        jRadioButton5 = new javax.swing.JRadioButton();
        jRadioButton6 = new javax.swing.JRadioButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Interactive Test Engine");
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                TestPage.this.windowClosing(evt);
            }
        });

        jPanel1.setBackground(new java.awt.Color(51, 51, 51));
        jPanel1.setBorder(new javax.swing.border.MatteBorder(null));

        previous.setMnemonic('P');
        previous.setText("Previous");
        previous.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        previous.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        previous.setEnabled(false);
        previous.setOpaque(false);
        previous.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                previousActionPerformed(evt);
            }
        });

        next.setMnemonic('N');
        next.setText("Next");
        next.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        next.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        next.setOpaque(false);
        next.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nextActionPerformed(evt);
            }
        });

        jScrollPane2.setBackground(java.awt.Color.darkGray);

        jTextPane2.setBackground(new java.awt.Color(51, 51, 51));
        jTextPane2.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jTextPane2.setEditable(false);
        jTextPane2.setFont(new java.awt.Font("Trebuchet MS", 1, 14));
        jTextPane2.setForeground(new java.awt.Color(255, 255, 255));
        jTextPane2.setText("Welcome "+Main.Session.name+"!");
        jTextPane2.setAutoscrolls(false);
        jScrollPane2.setViewportView(jTextPane2);

        jProgressBar1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jTextPane3.setBackground(new java.awt.Color(51, 51, 51));
        jTextPane3.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jTextPane3.setEditable(false);
        jTextPane3.setFont(new java.awt.Font("Trebuchet MS", 1, 14));
        jTextPane3.setForeground(new java.awt.Color(255, 255, 255));
        jTextPane3.setText("TIME REMAINING  :  ");
        jScrollPane3.setViewportView(jTextPane3);

        list_unattended_questions.setMnemonic('U');
        list_unattended_questions.setText("List Unattended Questions");
        list_unattended_questions.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        list_unattended_questions.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        list_unattended_questions.setOpaque(false);
        list_unattended_questions.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                list_unattended_questionsActionPerformed(evt);
            }
        });

        list_marked_questions.setMnemonic('M');
        list_marked_questions.setText("List Marked Questions");
        list_marked_questions.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        list_marked_questions.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        list_marked_questions.setOpaque(false);
        list_marked_questions.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                list_marked_questionsActionPerformed(evt);
            }
        });

        finish.setMnemonic('F');
        finish.setText("Finish");
        finish.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        finish.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        finish.setOpaque(false);
        finish.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                finishActionPerformed(evt);
            }
        });

        mark.setMnemonic('M');
        mark.setText("Mark For Review");
        mark.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        mark.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        mark.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                markActionPerformed(evt);
            }
        });

        clear_selection.setMnemonic('C');
        clear_selection.setText("Clear Selection");
        clear_selection.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        clear_selection.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        clear_selection.setOpaque(false);
        clear_selection.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clear_selectionActionPerformed(evt);
            }
        });

        jComboBox1.setMaximumRowCount(6);
        jComboBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(list_marked_questions, javax.swing.GroupLayout.DEFAULT_SIZE, 116, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(list_unattended_questions, javax.swing.GroupLayout.PREFERRED_SIZE, 130, Short.MAX_VALUE)))
                .addGap(13, 13, 13)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(69, 69, 69)
                        .addComponent(mark, javax.swing.GroupLayout.DEFAULT_SIZE, 89, Short.MAX_VALUE)
                        .addGap(61, 61, 61)
                        .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(16, 16, 16)
                        .addComponent(previous, javax.swing.GroupLayout.DEFAULT_SIZE, 98, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addComponent(next, javax.swing.GroupLayout.DEFAULT_SIZE, 90, Short.MAX_VALUE)
                        .addGap(26, 26, 26)
                        .addComponent(finish, javax.swing.GroupLayout.DEFAULT_SIZE, 113, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jProgressBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 241, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(51, 51, 51)
                        .addComponent(clear_selection, javax.swing.GroupLayout.DEFAULT_SIZE, 114, Short.MAX_VALUE)
                        .addGap(48, 48, 48)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 162, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jProgressBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(clear_selection, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(29, 29, 29)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(list_marked_questions, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(finish, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(next, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(previous, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(list_unattended_questions, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(mark, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(20, 20, 20))
        );

        jPanel2.setBackground(java.awt.Color.gray);
        jPanel2.setBorder(new javax.swing.border.MatteBorder(null));

        jTextPane1.setBackground(java.awt.SystemColor.inactiveCaption);
        jTextPane1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jTextPane1.setEditable(false);
        jTextPane1.setFont(new java.awt.Font("Tahoma", 1, 16));
        jTextPane1.setForeground(new java.awt.Color(51, 51, 51));
        jTextPane1.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jScrollPane1.setViewportView(jTextPane1);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 885, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 79, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        buttonGroup1.add(jRadioButton2);
        jRadioButton2.setFont(new java.awt.Font("Trebuchet MS", 0, 14));
        jRadioButton2.setAutoscrolls(true);
        jRadioButton2.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jRadioButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton2ActionPerformed(evt);
            }
        });

        buttonGroup1.add(jRadioButton3);
        jRadioButton3.setFont(new java.awt.Font("Trebuchet MS", 0, 14));
        jRadioButton3.setAutoscrolls(true);
        jRadioButton3.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jRadioButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton3ActionPerformed(evt);
            }
        });

        buttonGroup1.add(jRadioButton1);
        jRadioButton1.setFont(new java.awt.Font("Trebuchet MS", 0, 14));
        jRadioButton1.setAutoscrolls(true);
        jRadioButton1.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        buttonGroup1.add(jRadioButton4);
        jRadioButton4.setFont(new java.awt.Font("Trebuchet MS", 0, 14));
        jRadioButton4.setAutoscrolls(true);
        jRadioButton4.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jRadioButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton4ActionPerformed(evt);
            }
        });

        buttonGroup1.add(jRadioButton5);
        jRadioButton5.setFont(new java.awt.Font("Trebuchet MS", 0, 14));
        jRadioButton5.setAutoscrolls(true);
        jRadioButton5.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jRadioButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton5ActionPerformed(evt);
            }
        });

        buttonGroup1.add(jRadioButton6);
        jRadioButton6.setFont(new java.awt.Font("Trebuchet MS", 0, 14));
        jRadioButton6.setAutoscrolls(true);
        jRadioButton6.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jRadioButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton6ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jRadioButton1, javax.swing.GroupLayout.DEFAULT_SIZE, 57, Short.MAX_VALUE)
                    .addComponent(jRadioButton3, javax.swing.GroupLayout.DEFAULT_SIZE, 57, Short.MAX_VALUE)
                    .addComponent(jRadioButton5, javax.swing.GroupLayout.DEFAULT_SIZE, 57, Short.MAX_VALUE)
                    .addComponent(jRadioButton4, javax.swing.GroupLayout.DEFAULT_SIZE, 57, Short.MAX_VALUE)
                    .addComponent(jRadioButton2, javax.swing.GroupLayout.DEFAULT_SIZE, 57, Short.MAX_VALUE)
                    .addComponent(jRadioButton6, javax.swing.GroupLayout.DEFAULT_SIZE, 57, Short.MAX_VALUE))
                .addGap(820, 820, 820))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jRadioButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(33, 33, 33)
                .addComponent(jRadioButton2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(26, 26, 26)
                .addComponent(jRadioButton3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(27, 27, 27)
                .addComponent(jRadioButton4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(27, 27, 27)
                .addComponent(jRadioButton5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(26, 26, 26)
                .addComponent(jRadioButton6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(24, 24, 24))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void previousActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_previousActionPerformed
        try
        {
            this.insert();
            this.updateProgressBar();
            this.cursor--;

            if (this.cursor == 0)
            {
                this.previous.setEnabled(false);
            }

            if (q[this.cursor].getUserAnswer() != 0)
            {
                this.displayItem(this.cursor, q[this.cursor].getUserAnswer());
            }
            else
            {
                this.displayItem(this.cursor, 0);
            }
        }
        catch (SQLException ex)
        {
        }
    }//GEN-LAST:event_previousActionPerformed

    private void jRadioButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton3ActionPerformed

    }//GEN-LAST:event_jRadioButton3ActionPerformed

    private void jRadioButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton2ActionPerformed

    }//GEN-LAST:event_jRadioButton2ActionPerformed

    private void jRadioButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton4ActionPerformed

    }//GEN-LAST:event_jRadioButton4ActionPerformed

    private void jRadioButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton5ActionPerformed

    }//GEN-LAST:event_jRadioButton5ActionPerformed

    private void list_marked_questionsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_list_marked_questionsActionPerformed
        int index = 1;
        int a = Main.Session.noOfQuestionsMarked;
        int b = 0;
        String markedQuestions = "";

        if(a==0)
            markedQuestions = "None of the questions were marked !";

        for(Question qtemp : this.q)
        {

            if(qtemp.getMark())
            {
                markedQuestions += index;
                markedQuestions += ((a>2)?", ":((a!=1)?" & ":""));
                a--;

                if((++b)%2 == 0)
                    markedQuestions += "\n";

            }

            index++;
        }

        javax.swing.JOptionPane.showMessageDialog(null, markedQuestions, "Marked Questions List", javax.swing.JOptionPane.INFORMATION_MESSAGE);
    }//GEN-LAST:event_list_marked_questionsActionPerformed

    private void nextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nextActionPerformed
        try 
        {
            this.previous.setEnabled(true);
            this.insert();
            this.updateProgressBar();

            if((++this.cursor)<Question.noOfQuestions)
            {
                this.displayItem(this.cursor,q[this.cursor].getUserAnswer());
            }
            else
            {
                if(this.cursor==Question.noOfQuestions)
                {
                    javax.swing.JOptionPane.showMessageDialog(null, "This is the last question.\n"+
                            "Press Finish button to submit your answers.", "Session end reached", javax.swing.JOptionPane.WARNING_MESSAGE);
                }

                this.cursor--;
            }

        }
        catch (SQLException ex)
        {
        }
    }//GEN-LAST:event_nextActionPerformed

    private void finishActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_finishActionPerformed
        this.threadSync=true;

        int value=javax.swing.JOptionPane.showConfirmDialog(null, "Are you sure you want to submit your answers to be evaluated?", "Finishing..",
                javax.swing.JOptionPane.OK_CANCEL_OPTION);

        if(value==javax.swing.JOptionPane.OK_OPTION)
        {
            try
            {
                this.finish();
            }
            catch (SQLException ex)
            {
            }
        }
        else
        {
            this.threadSync=false;
            this.t.resume();
        }

    }//GEN-LAST:event_finishActionPerformed

    private void finish() throws SQLException
    {
        this.insert();
        this.updateProgressBar();
        this.evaluate();
        this.dbc.closeConnection();
        this.dispose();
        new ResultPage(this.q,this.qlist).show();
    }

    private void comboContents()
    {
        for(int i=0;i<Question.noOfQuestions;i++)
        {
            jComboBox1.addItem(new Integer(i+1));
        }
    }

    private void jRadioButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton6ActionPerformed
      
    }//GEN-LAST:event_jRadioButton6ActionPerformed

    private void windowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_windowClosing
        try
        {
            this.dbc.closeConnection();
        }
        catch (SQLException ex)
        {
        }
    }//GEN-LAST:event_windowClosing

    private void list_unattended_questionsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_list_unattended_questionsActionPerformed
        int index = 1;
        int a=Main.Session.noOfQuestionsNotAttended=Question.noOfQuestions-Main.Session.noOfQusetionsAttended;
        int b = 0;
        String nAttQuestions = "";

        for(Question qtemp : this.q)
        {
            if(qtemp.getUserAnswer()==0)
            {

                nAttQuestions += index;
                nAttQuestions += ((a>2)?", ":((a!=1)?" & ":""));
                a--;

                if((++b)%2 == 0)
                    nAttQuestions += "\n";

            }

            index++;
        }

        javax.swing.JOptionPane.showMessageDialog(null, nAttQuestions, "List of Questions not Attended", javax.swing.JOptionPane.INFORMATION_MESSAGE);
}//GEN-LAST:event_list_unattended_questionsActionPerformed

    private void markActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_markActionPerformed
        if(this.mark.isSelected())
        {
            this.mark.setText("Unmark");
            this.q[this.cursor].setMark(true);
            Main.Session.noOfQuestionsMarked++;
        }
        else
        {
            this.mark.setText("Mark For Review");
            this.q[this.cursor].setMark(false);
            Main.Session.noOfQuestionsMarked--;
        }

    }//GEN-LAST:event_markActionPerformed

    private void clear_selectionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clear_selectionActionPerformed
        this.buttonGroup1.clearSelection();
        flag[this.cursor]=true;
    }//GEN-LAST:event_clear_selectionActionPerformed

    private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox1ActionPerformed
        try
        {
            Integer i = (Integer) ((javax.swing.JComboBox)evt.getSource( )).getSelectedItem( );
            this.insert();
            this.updateProgressBar();
            this.cursor=i-1;

            if (this.cursor == 0)         
                this.previous.setEnabled(false);
            else
                this.previous.setEnabled(true);

            this.displayItem(this.cursor, q[this.cursor].getUserAnswer());
        }
        catch (SQLException ex)
        {
        }
    }//GEN-LAST:event_jComboBox1ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton clear_selection;
    private javax.swing.JButton finish;
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JProgressBar jProgressBar1;
    private javax.swing.JRadioButton jRadioButton1;
    private javax.swing.JRadioButton jRadioButton2;
    private javax.swing.JRadioButton jRadioButton3;
    private javax.swing.JRadioButton jRadioButton4;
    private javax.swing.JRadioButton jRadioButton5;
    private javax.swing.JRadioButton jRadioButton6;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTextPane jTextPane1;
    private javax.swing.JTextPane jTextPane2;
    private javax.swing.JTextPane jTextPane3;
    private javax.swing.JButton list_marked_questions;
    private javax.swing.JButton list_unattended_questions;
    private javax.swing.JToggleButton mark;
    private javax.swing.JButton next;
    private javax.swing.JButton previous;
    // End of variables declaration//GEN-END:variables

}
