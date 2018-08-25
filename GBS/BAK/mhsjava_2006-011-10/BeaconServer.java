/*
 * Created on 31-Oct-06
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
//package mhsjava;

import java.awt.* ;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.* ;

/**
 * @author Mark Sattolo
 * @version 1.2  Nov.10/2006
 */
public class BeaconServer extends JFrame
{
  /*
   *   FIELDS
   * ============================================= */
    
  static final String strGAME_VERSION = "GBS 1.2" ,
                         strGAME_NAME = "GbsGameFrame" ;

  private int TIMER_DELAY_MSEC = 1000;
  
  static final Dimension PREFERRED_SIZE = new Dimension( 600, 750 );

  static final Color COLOR_GAME_BKGRND = new Color( 248, 232, 216 );

  static final String
                        strZERO = "0",
                   strZERO_TIME = "00:00:00",
                   strFULL_TIME = "99:59:59",
                   strSEPARATOR = ":",
                 strSCORE_TITLE = "Score:",
                 strSCORE_FINAL = "Final ",
               strSQUARES_TITLE = "Squares left:",
                  strTIME_TITLE = "Time:",
                  strINFO_READY = "Ready to Go",
                strINFO_RUNNING = "Running...";

  boolean isRunning = false ;

  int seconds, // accumulated game time
      scoreMultiplier, // increase scoring according to density, etc
      maxScore, currentScore = 0;

  /** length, in pixels, of each side of the <code>BeaconClient</code>, 
   *  = (current side length of each square) * (# of squares on each side of field) */
  int fieldDim;

  private JPanel jContentPane = null;

  private JMenuBar gbsMenuBar = null;

  private JPanel topEnclosingPanel = null;
  private JPanel topBtnPanel = null;
  private JPanel topInfoPanel = null;
  private JPanel botBtnPanel = null;
  
  private JMenu gameMenu = null;
  private JMenu settingsMenu = null;
  private JMenuItem newGameMenuItem = null;
  private JMenuItem exitMenuItem = null;
  private JMenuItem delayMenuItem = null;
  private JMenuItem difficultyMenuItem = null;
  
  private JLabel squaresTitle = null;
  private JLabel squaresMesg = null;
  private JLabel timeTitle = null;
  private JLabel timeMesg = null;
  
  private JButton exitButton = null;
  private JButton redoButton = null;
  private JButton undoButton = null;
  private JButton newGameButton = null;
  private JButton solveButton = null;
  private JButton checkButton = null;
  
  private JPanel blankTopPanel = null;

  int DEBUG_LEVEL;

  JLayeredPane pane ; 
  BeaconClient field;
  Timer clock;
  ServListener listener;

  /*
   *   METHODS
   * ============================================= */
  
  /* MAIN */
  public static void main( String[] args )
  {
    BeaconServer gameFrame =
      new BeaconServer( args.length > 0 ? Integer.parseInt(args[0]) : 0 );

    gameFrame.setVisible( true );

  } // main()

  /**
   * This is the default constructor
   */
  public BeaconServer( int debug )
  {
    super();

    DEBUG_LEVEL = debug;
    if( DEBUG_LEVEL > 0 )
      System.out.println( "BeaconServer.DEBUG_LEVEL = " + DEBUG_LEVEL);

    initialize();
  }

  /**
   * This method initializes this
   */
  private void initialize()
  {
    if( DEBUG_LEVEL > 0 )
      System.out.println( "BeaconServer.initialize()" );

    setName( strGAME_NAME );
    setTitle( strGAME_VERSION );
    setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );

    getContentPane().setBackground( COLOR_GAME_BKGRND );
    setSize( PREFERRED_SIZE );

    listener = new ServListener();
    field = new BeaconClient( this );
    clock = new Timer( TIMER_DELAY_MSEC, listener );
    
    // get all the sub-components
    setJMenuBar( getGbsMenuBar() );
    setMyContentPane();

  } // initialize()

  /**
   * Stop a running game
   */
  protected void halt()
  {
    if( DEBUG_LEVEL > 0 )
      System.out.println( "BeaconServer.halt()" );

    field.removeMouseListener(field);
    field.removeKeyListener(field);

    clock.stop();

  } // halt()

  /**
   * Start a new game
   */
  protected void newGame()
  {
    if( DEBUG_LEVEL > 0 )
      System.out.println( "BeaconServer.newGame()" );

    clock.stop();
    isRunning = false ;

    squaresTitle.setText( strSQUARES_TITLE );
     squaresMesg.setText( strZERO          );

    timeTitle.setText( strTIME_TITLE );
     timeMesg.setText( strZERO_TIME  );

    field.newField();

    pane.repaint();

  } // newGame()

  /**
   * Start the clock
   */
  protected void startClock()
  {
    if( !isRunning )
    {
      isRunning = true;
      seconds = 0;
      timeMesg.setText( strZERO_TIME );
      squaresMesg.setText( strINFO_RUNNING );
      clock.start();
    }
  } // startClock()

  /**
   * Update the time display
   */
  protected void runClock()
  {
    if( isRunning && (getState() == JFrame.NORMAL) )
    {                // stop clock if game is iconified
      seconds++;

      int hrs = seconds / 3600;
      int mins = seconds / 60 - hrs * 60;
      int secs = seconds - mins * 60 - hrs * 3600;

      String strHr =
        hrs < 10 ? strZERO + Integer.toString(hrs) : Integer.toString(hrs);
      String strMin =
        mins < 10 ? strZERO + Integer.toString(mins) : Integer.toString(mins);
      String strSec =
        secs < 10 ? strZERO + Integer.toString(secs) : Integer.toString(secs);

      timeMesg.setText(strHr + strSEPARATOR + strMin + strSEPARATOR + strSec);
    }

  } // runClock()

  /**
   *  This method fills the JFrame's ContentPane
   */
  private void setMyContentPane()
  {
    if( DEBUG_LEVEL > 0 )
      System.out.println( "BeaconServer.setMyContentPane()" );

    getContentPane().add( getTopEnclosingPanel(), "North" );    

    // set up JLayeredPane
    pane = new JLayeredPane();
    pane.add( field, JLayeredPane.DEFAULT_LAYER );
    getContentPane().add( pane, "Center" );
 
    getContentPane().add( getBotBtnPanel(), "South" );    

  }// setMyContentPane()

  /**
   * This method initializes gbsMenuBar
   * 
   * @return JMenuBar
   */
  private JMenuBar getGbsMenuBar()
  {
    if( DEBUG_LEVEL > 0 )
      System.out.println( "BeaconServer.getGbsMenuBar()" );

    if( gbsMenuBar == null )
    {
      try
      {
        gbsMenuBar = new JMenuBar();
        gbsMenuBar.add(getGameMenu());
        gbsMenuBar.add(getSettingsMenu());
        gbsMenuBar.setPreferredSize( new java.awt.Dimension(60,30 ) );
      }
      catch( java.lang.Throwable e )
      {
        System.err.println( "PROBLEM: getGbsMenuBar()" );
      }
    }
    return gbsMenuBar;
  }

  /**
   * This method initializes botBtnPanel
   * 
   * @return JPanel
   */
  private JPanel getBotBtnPanel()
  {
    if( botBtnPanel == null )
    {
      try
      {
        botBtnPanel = new JPanel();
        
        java.awt.GridLayout layGridLayout4 = new java.awt.GridLayout();
        layGridLayout4.setRows(1);
        layGridLayout4.setColumns(3);
        layGridLayout4.setHgap(20 );
        layGridLayout4.setVgap(20 );
        botBtnPanel.setLayout(layGridLayout4);
        
        botBtnPanel.setPreferredSize( new java.awt.Dimension(289,66) );
        
        botBtnPanel.add( getNewGameButton(), null );
        botBtnPanel.add( getCheckButton(),   null );
        botBtnPanel.add( getSolveButton(),   null );
      }
      catch( java.lang.Throwable e )
      {
        System.err.println( "PROBLEM: getBotBtnPanel()" );
      }
    }
    return botBtnPanel;
  }
  
  /**
   * This method initializes topEnclosingPanel
   * 
   * @return JPanel
   */
  private JPanel getTopEnclosingPanel()
  {
    if( DEBUG_LEVEL > 0 )
      System.out.println( "BeaconServer.getTopEnclosingPanel()" );

    if( topEnclosingPanel == null )
    {
      try
      {
        topEnclosingPanel = new JPanel();
        
        java.awt.GridLayout gl = new java.awt.GridLayout( 2, 1 );
        topEnclosingPanel.setLayout( gl );

        topEnclosingPanel.add( getTopInfoPanel() );
        topEnclosingPanel.add( getTopBtnPanel() );
      }
      catch( java.lang.Throwable e )
      {
        System.err.println( "PROBLEM: getTopEnclosingPanel(): " + e );
      }
    }
    return topEnclosingPanel ;
  }
  
  /**
   * This method initializes topBtnPanel
   * 
   * @return JPanel
   */
  private JPanel getTopBtnPanel()
  {
    if( topBtnPanel == null )
    {
      try
      {
        topBtnPanel = new JPanel();
        
        java.awt.GridLayout layGridLayout2 = new java.awt.GridLayout();
        layGridLayout2.setRows(1);
        layGridLayout2.setColumns(4);
        layGridLayout2.setVgap(25);
        layGridLayout2.setHgap(20 );
        topBtnPanel.setLayout(layGridLayout2);
        
        topBtnPanel.add(getUndoButton(), null );
        topBtnPanel.add(getRedoButton(), null );
        topBtnPanel.add(getExitButton(), null );
        
        topBtnPanel.setPreferredSize( new java.awt.Dimension(223,66) );
      }
      catch( java.lang.Throwable e )
      {
        System.err.println( "PROBLEM: getTopBtnPanel(): " + e );
      }
    }
    return topBtnPanel;
  }
  /**
   * This method initializes topInfoPanel
   * 
   * @return JPanel
   */
  private JPanel getTopInfoPanel()
  {
    if( topInfoPanel == null )
    {
      try
      {
        topInfoPanel = new JPanel();
        
        java.awt.GridLayout layGridLayout3 = new java.awt.GridLayout();
        layGridLayout3.setRows(1);
        layGridLayout3.setColumns(5);
        layGridLayout3.setVgap(20 );
        layGridLayout3.setHgap(2);
        topInfoPanel.setLayout(layGridLayout3);
        
        topInfoPanel.add(getSquaresTitle(), null );
        topInfoPanel.add(getSquaresMesg(), null );
        topInfoPanel.add(getBlankTopPanel(), null );
        topInfoPanel.add(getTimeTitle(), null );
        topInfoPanel.add(getTimeMesg(), null );

        topInfoPanel.setBackground( COLOR_GAME_BKGRND );
      }
      catch( java.lang.Throwable e )
      {
        System.err.println( "PROBLEM: getTopInfoPanel(): " + e );
      }
    }
    return topInfoPanel;
  }
  /**
   * This method initializes gameMenu
   * 
   * @return JMenu
   */
  private JMenu getGameMenu()
  {
    if( gameMenu == null )
    {
      try
      {
        gameMenu = new JMenu();
        gameMenu.add(getNewGameMenuItem());
        gameMenu.add(getExitMenuItem());
        gameMenu.setPreferredSize( new java.awt.Dimension(50,50 ) );
        gameMenu.setText( "GAME" );
      }
      catch( java.lang.Throwable e )
      {
        System.err.println( "PROBLEM: getGameMenu(): " + e );
      }
    }
    return gameMenu;
  }
  /**
   * This method initializes settingsMenu
   * 
   * @return JMenu
   */
  private JMenu getSettingsMenu()
  {
    if( settingsMenu == null )
    {
      try
      {
        settingsMenu = new JMenu();
        settingsMenu.add(getDelayMenuItem());
        settingsMenu.add(getDifficultymenuitem());
        settingsMenu.setText( "SETTINGS" );
      }
      catch( java.lang.Throwable e )
      {
        System.err.println( "PROBLEM: getSettingsMenu(): " + e );
      }
    }
    return settingsMenu;
  }
  /**
   * This method initializes newGameMenuItem
   * 
   * @return JMenuItem
   */
  private JMenuItem getNewGameMenuItem()
  {
    if( newGameMenuItem == null )
    {
      try
      {
        newGameMenuItem = new JMenuItem();
        newGameMenuItem.setText( "New Game" );
      }
      catch( java.lang.Throwable e )
      {
        System.err.println( "PROBLEM: getNewGameMenuItem(): " + e );
      }
    }
    return newGameMenuItem;
  }
  /**
   * This method initializes exitMenuItem
   * 
   * @return JMenuItem
   */
  private JMenuItem getExitMenuItem()
  {
    if( exitMenuItem == null )
    {
      try
      {
        exitMenuItem = new JMenuItem();
        exitMenuItem.setText( "Exit" );
      }
      catch( java.lang.Throwable e )
      {
        System.err.println( "PROBLEM: getExitMenuItem(): " + e );
      }
    }
    return exitMenuItem;
  }
  /**
   * This method initializes delayMenuItem
   * 
   * @return JMenuItem
   */
  private JMenuItem getDelayMenuItem()
  {
    if( delayMenuItem == null )
    {
      try
      {
        delayMenuItem = new JMenuItem();
        delayMenuItem.setText( "Set Delay..." );
      }
      catch( java.lang.Throwable e )
      {
        System.err.println( "PROBLEM: getDelayMenuItem(): " + e );
      }
    }
    return delayMenuItem;
  }
  /**
   * This method initializes difficultyMenuItem
   * 
   * @return JMenuItem
   */
  private JMenuItem getDifficultymenuitem()
  {
    if( difficultyMenuItem == null )
    {
      try
      {
        difficultyMenuItem = new JMenuItem();
        difficultyMenuItem.setText( "Game Difficulty..." );
      }
      catch( java.lang.Throwable e )
      {
        System.err.println( "PROBLEM: getDifficultyMenuItem(): " + e );
      }
    }
    return difficultyMenuItem;
  }
  /**
   * This method initializes squaresTitle
   * 
   * @return JLabel
   */
  private JLabel getSquaresTitle()
  {
    if( squaresTitle == null )
    {
      try
      {
        squaresTitle = new JLabel();
        squaresTitle.setText( strSQUARES_TITLE );
        squaresTitle.setHorizontalAlignment( SwingConstants.CENTER );
      }
      catch( java.lang.Throwable e )
      {
        System.err.println( "PROBLEM: getSquaresTitle(): " + e );
      }
    }
    return squaresTitle;
  }
  /**
   * This method initializes squaresMesg
   * 
   * @return JLabel
   */
  private JLabel getSquaresMesg()
  {
    if( squaresMesg == null )
    {
      try
      {
        squaresMesg = new JLabel();
        squaresMesg.setText( strZERO );
        squaresMesg.setHorizontalAlignment( SwingConstants.CENTER );
      }
      catch( java.lang.Throwable e )
      {
        System.err.println( "PROBLEM: getSquaresMesg(): " + e );
      }
    }
    return squaresMesg;
  }
  /**
   * This method initializes timeTitle
   * 
   * @return JLabel
   */
  private JLabel getTimeTitle()
  {
    if( timeTitle == null )
    {
      try
      {
        timeTitle = new JLabel();
        timeTitle.setText( strTIME_TITLE );
        timeTitle.setHorizontalAlignment( SwingConstants.CENTER );
      }
      catch( java.lang.Throwable e )
      {
        System.err.println( "PROBLEM: getTimeTitle(): " + e );
      }
    }
    return timeTitle;
  }
  /**
   * This method initializes timeMesg
   * 
   * @return JLabel
   */
  private JLabel getTimeMesg()
  {
    if( timeMesg == null )
    {
      try
      {
        timeMesg = new JLabel();
        timeMesg.setText( strZERO_TIME );
        timeMesg.setHorizontalAlignment( SwingConstants.CENTER );
      }
      catch( java.lang.Throwable e )
      {
        System.err.println( "PROBLEM: getTimeMesg(): " + e );
      }
    }
    return timeMesg;
  }
  /**
   * This method initializes exitButton
   * 
   * @return JButton
   */
  private JButton getExitButton()
  {
    if( exitButton == null )
    {
      try
      {
        exitButton = new JButton();
        exitButton.setText( "EXIT" );
        exitButton.addActionListener( listener );
      }
      catch( java.lang.Throwable e )
      {
        System.err.println( "PROBLEM: getExitButton(): " + e );
      }
    }
    return exitButton;
  }
  /**
   * This method initializes redoButton
   * 
   * @return JButton
   */
  private JButton getRedoButton()
  {
    if( redoButton == null )
    {
      try
      {
        redoButton = new JButton();
        redoButton.setText( "REDO" );
        redoButton.addActionListener( listener );
      }
      catch( java.lang.Throwable e )
      {
        System.err.println( "PROBLEM: getRedoButton(): " + e );
      }
    }
    return redoButton;
  }
  /**
   * This method initializes undoButton
   * 
   * @return JButton
   */
  private JButton getUndoButton()
  {
    if( undoButton == null )
    {
      try
      {
        undoButton = new JButton();
        undoButton.setPreferredSize(new java.awt.Dimension(24, 8));
        undoButton.setText( "UNDO" );
        undoButton.addActionListener( listener );
      }
      catch( java.lang.Throwable e )
      {
        System.err.println( "PROBLEM: getUndoButton(): " + e );
      }
    }
    return undoButton;
  }
  /**
   * This method initializes checkButton
   * 
   * @return JButton
   */
  private JButton getCheckButton()
  {
    if( checkButton == null )
    {
      try
      {
        checkButton = new JButton();
        checkButton.setText( "Check" );
        checkButton.addActionListener( listener );
      }
      catch( java.lang.Throwable e )
      {
        System.err.println( "PROBLEM: getCheckButton(): " + e );
      }
    }
    return checkButton;
  }
  /**
   * This method initializes newGameButton
   * 
   * @return JButton
   */
  private JButton getNewGameButton()
  {
    if( newGameButton == null )
    {
      try
      {
        newGameButton = new JButton();
        newGameButton.setText( "New Game" );
        newGameButton.addActionListener( listener );
      }
      catch( java.lang.Throwable e )
      {
        System.err.println( "PROBLEM: getNewGameButton(): " + e );
      }
    }
    return newGameButton;
  }
  /**
   * This method initializes solveButton
   * 
   * @return JButton
   */
  private JButton getSolveButton()
  {
    if( solveButton == null )
    {
      try
      {
        solveButton = new JButton();
        solveButton.setText( "Solve" );
        solveButton.addActionListener( listener );
      }
      catch( java.lang.Throwable e )
      {
        System.err.println( "PROBLEM: getSolveButton(): " + e );
      }
    }
    return solveButton;
  }

  /**
   * This method initializes blankTopPanel
   * 
   * @return JPanel
   */
  private JPanel getBlankTopPanel()
  {
    if( blankTopPanel == null )
    {
      try
      {
        blankTopPanel = new JPanel();
        blankTopPanel.setBackground( COLOR_GAME_BKGRND );
      }
      catch( java.lang.Throwable e )
      {
        System.err.println( "PROBLEM: getBlankTopPanel(): " + e );
      }
    }
    return blankTopPanel;
  }

  /*
   *   INNER CLASSES
   * ============================================= */

  /** Handles all the action events for <code>BeaconServer</code>.
   *  i.e. function as the Controller for the app   */
  class ServListener implements ActionListener
  {
    Object source ;

    public void actionPerformed( ActionEvent ae )
    {
      source = ae.getSource();
      if( source == clock)
      {
        runClock();
        if( DEBUG_LEVEL > 2)
          System.out.println( "ServListener clock event" );
      }
      else // not the clock
      {
        if( DEBUG_LEVEL > 1)
          System.out.println( "ServListener ActionCommand: "
                              + ae.getActionCommand() );

        if( source == newGameButton || source == newGameMenuItem )
        {
          if( DEBUG_LEVEL == 1)
            System.out.println( "ServListener NEW GAME event" );
          newGame();
        }
        else if( source == exitMenuItem || source == exitButton )
        {
          if( DEBUG_LEVEL == 1)
            System.out.println( "ServListener EXIT event" );
          halt();
          dispose();
        }
        else if( source == undoButton)
        {
          if( DEBUG_LEVEL == 1)
            System.out.println( "ServListener UNDO event" );
        }
        else if( source == redoButton)
        {
          if( DEBUG_LEVEL == 1)
            System.out.println( "ServListener REDO event" );
        }
        else if( source == checkButton)
        {
          if( DEBUG_LEVEL == 1)
            System.out.println( "ServListener CHECK event" );
        }
        else if( source == solveButton)
        {
          if( DEBUG_LEVEL == 1)
            System.out.println( "ServListener SOLVE event" );
        }
      
      } // not the clock

    } // ServListener.actionPerformed()

  } /* inner class ServListener */

} // CLASS BeaconServer
