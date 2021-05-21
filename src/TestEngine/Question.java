package TestEngine;

import java.sql.*;
import java.util.*;

enum Choice
{
    Single,Multi;
};

interface IQuestionConstraint
{
    public static final int MAX_NO_OF_CHOICES_PER_QUESTION=6;
    public static final int MIN_NO_OF_CHOICES_PER_QUESTION=2;
}

public class Question implements IQuestionConstraint
{

    public static int noOfQuestions;
    private ChoiceList choiceList;
    private String qname;
    private int answer;
    private int userAnswer;
    private boolean mark;

    Question(String qname, int noOfChoices, Choice type, ArrayList<String> clist)
    {
        this.qname=qname;
        this.choiceList=new ChoiceList(noOfChoices,type,clist);
    }

    public String getQuestion()
    {
        return this.qname;
    }

    public int getChoiceLength()
    {
        return this.choiceList.getChoiceLength();
    }

    public String getChoiceAtIndex(int index)
    {
        return this.choiceList.getChoiceAtIndex(index);
    }

    public void setAnswer(int answer)
    {
        this.answer=answer;
    }

    public int getAnswer()
    {
        return this.answer;
    }

    public void setUserAnswer(int userAnswer)
    {
        this.userAnswer=userAnswer;
    }

    public int getUserAnswer()
    {
        return this.userAnswer;
    }

    public void setMark(boolean mark)
    {
        this.mark=mark;
    }

    public boolean getMark()
    {
        return this.mark;
    }

}

class ChoiceList
{

    private int noOfChoices;
    private Choice type;
    private ArrayList<String> clist;

    ChoiceList(int noOfChoices, Choice type, ArrayList<String> clist)
    {
        this.noOfChoices=noOfChoices;
        this.type=type;
        this.clist=clist;
    }

    public int getChoiceLength()
    {
        return this.noOfChoices;
    }

    public Choice getChoiceType()
    {
        return this.type;
    }

    public String getChoiceAtIndex(int index)
    {
        if(!this.clist.isEmpty() && index<this.clist.size())
            return this.clist.get(index);
        else
            return null;
    }

}

class DBConnection
{

    private Connection conn;
    private Statement s;
    private ResultSet rs;

    DBConnection(String dsn) throws ClassNotFoundException, SQLException
    {
        Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
        this.conn=DriverManager.getConnection("jdbc:odbc:"+dsn);
        this.s=conn.createStatement();
    }

    ResultSet executeQuery(String SQL) throws SQLException
    {
        this.rs=s.executeQuery(SQL);
        return this.rs;
    }

    void executeMetaQuery(String SQL) throws SQLException
    {
        this.s.executeUpdate(SQL);
    }

    void closeConnection() throws SQLException
    {
        this.s.close();
        this.conn.close();
    }
    
}
