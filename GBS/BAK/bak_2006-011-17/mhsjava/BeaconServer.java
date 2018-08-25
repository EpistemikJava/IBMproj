/*
 * Created on 31-Oct-06
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package mhsjava;

import java.awt.* ;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.* ;

/**
 * @author Mark Sattolo
 * @version 1.3  Nov.11/2006
 */
public class BeaconServer extends JFrame
{
  /*
   *   FIELDS
   * ============================================= */
    
  static final String strGAME_VERSION = "GBS 1.3" ,
                         strGAME_NAME = "GbsGameFrame" ;

  static final Dimension PREFERRED_SIZE = new Dimension( 600, 750 );

  static final Color COLOR_GAME_BKGRND = new Color( 248, 232, 216 );

  static final String
                        strZERO = "0",
                   strZERO_TIME = "00:00:00",
                   strFULL_TIME = "99:59:59",
                   strSEPARATOR = ":",
                 strSCORE_TITLE = "Score:",
                 strSCORE_FINAL = "Final ",
               strSQUARES_TITLE = "Unknown Squares:  ",
                  strTIME_TITLE = "Time:  ",
                  strINFO_READY = "Ready to Go",
                strINFO_RUNNING = "Running...";

  boolean isRunning = false ;

  int seconds, // accumulated game time
      timerDelaySeconds = 1 ;

  private JMenuBar gbsMenuBar = null;
  private JPanel jContentPane = null;

  private JMenu gameMenu = null;
  private JMenu settingsMenu = null;
  private JMenuItem newGameMenuItem = null;
  private JMenuItem exitMenuItem = null;
  private JMenuItem delayMenuItem = null;
  private JMenuItem difficultyMenuItem = null;
  
  private JPanel topEnclosingPanel = null;
  private JPanel topInfoPanel = null;
  private JPanel topBtnPanel = null;
  private JPanel botBtnPanel = null;
  
  private JLabel squaresMesg = null;
  private JLabel infoMesg = null;
  private JLabel timeMesg = null;
  
  private JButton exitButton = null;
  private JButton redoButton = null;
  private JButton undoButton = null;
  private JButton newGameButton = null;
  private JButton solveButton = null;
  private JButton checkButton = null;
  
  int DEBUG_LEVEL;

  JLayeredPane pane ;
  
  int numSavedGames = 99 ;
  BeaconLoader loader ;
  
  BeaconClient field ;
  Timer clock ;
  ServListener listener ;

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
    loader = new BeaconLoader( this, numSavedGames );
    field = new BeaconClient( this, loader );
    
    // clock will send an event to listener every (timerDelaySeconds*1000) msec
    clock = new Timer( timerDelaySeconds*1000, listener );
    
    // get all the sub-components
    setJMenuBar( getGbsMenuBar() );
    setMyContentPane();

    // TRY OUT THE LOAD GAME
    try
    {
      loader.loadGame( "g1" );
    }
    catch( java.io.IOException ioe )
    {
      if( DEBUG_LEVEL > 0 )
        // Auto-generated catch block
        ioe.printStackTrace();
    }
    
    field.newGrid();
    
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

    updateTime( strZERO_TIME );
    
    infoMesg.setText( strINFO_READY );
    field.newGrid();

    pane.repaint();

  } // newGame()

  /**
   * Start the clock
   */
  protected void startClock()
  {
    if( DEBUG_LEVEL > 0 )
      System.out.println( "BeaconServer.startClock()" );

    if( !isRunning )
    {
      isRunning = true;
      seconds = 0;
      infoMesg.setText( strINFO_RUNNING );
      clock.start();
    }
  } // startClock()

  /**
   * Update the elapsed time
   */
  protected void runClock()
  {
    if( isRunning && (getState() == JFrame.NORMAL) )
    {                // stop clock if game is iconified
      
      seconds += timerDelaySeconds ;

      int hrs = seconds / 3600 ;
      int minsecs = seconds % 3600 ;
      int mins = minsecs / 60 ;
      int secs = minsecs % 60 ;

      String strHr =
         hrs < 10 ? (strZERO + Integer.toString(hrs)) : Integer.toString(hrs);
      String strMin =
        mins < 10 ? (strZERO + Integer.toString(mins)): Integer.toString(mins);
      String strSec =
        secs < 10 ? (strZERO + Integer.toString(secs)): Integer.toString(secs);
      
      updateTime( strHr + strSEPARATOR + strMin + strSEPARATOR + strSec );
    }

  } // runClock()
  
  /** Update the time display */
  void updateTime( String s )
  {
    timeMesg.setText( strTIME_TITLE + s );
  }
  
  /** Update the number of unknown <code>Squares</code> that remain */
  void updateSquaresDisplay( int i )
  {
    if( DEBUG_LEVEL > 0 )
      System.out.println( "BeaconServer.updateSquaresDisplay(" + i + ")" );

    squaresMesg.setText( strSQUARES_TITLE + Integer.toString(i) );
  }
  
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
        gbsMenuBar.add( getGameMenu()     );
        gbsMenuBar.add( getSettingsMenu() );
        gbsMenuBar.setPreferredSize( new Dimension(60,30) );
      }
      catch( Throwable e )
      {
        System.err.println( "PROBLEM: getGbsMenuBar()" );
      }
    }
    return gbsMenuBar;
  }
  
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
   * This method initializes topEnclosingPanel
   * 
   * @return JPanel
   */
  private JPanel getTopEnclosingPanel()
  {
    if( DEBUG_LEVEL > 1 )
      System.out.println( "BeaconServer.getTopEnclosingPanel()" );

    if( topEnclosingPanel == null )
    {
      try
      {
        topEnclosingPanel = new JPanel();
        
        GridLayout gl = new GridLayout( 2, 1 );
        topEnclosingPanel.setLayout( gl );

        topEnclosingPanel.add( getTopInfoPanel() );
        topEnclosingPanel.add( getTopBtnPanel()  );
      }
      catch( Throwable e )
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
        
        GridLayout gl = new GridLayout();
        gl.setRows(1);
        gl.setColumns(4);
        gl.setVgap(25);
        gl.setHgap(20 );
        topBtnPanel.setLayout( gl );
        
        topBtnPanel.add( getUndoButton(), null );
        topBtnPanel.add( getRedoButton(), null );
        topBtnPanel.add( getExitButton(), null );
        
        topBtnPanel.setPreferredSize( new Dimension(223,66) );
      }
      catch( Throwable e )
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
        
        GridLayout gl = new GridLayout();
        gl.setRows(1);
        gl.setColumns(5);
        gl.setVgap(20 );
        gl.setHgap(2);
        topInfoPanel.setLayout( gl );
        
        topInfoPanel.add( getSquaresMesg(), null );
        topInfoPanel.add( getInfoMesg(),    null );
        topInfoPanel.add( getTimeMesg(),    null );

        topInfoPanel.setBackground( COLOR_GAME_BKGRND );
      }
      catch( Throwable e )
      {
        System.err.println( "PROBLEM: getTopInfoPanel(): " + e );
      }
    }
    return topInfoPanel;
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
        
        GridLayout gl = new GridLayout();
        gl.setRows( 1 );
        gl.setColumns( 3 );
        gl.setHgap( 20 );
        gl.setVgap( 20 );
        botBtnPanel.setLayout( gl );
        
        botBtnPanel.setPreferredSize( new Dimension(289,66) );
        
        botBtnPanel.add( getNewGameButton(), null );
        botBtnPanel.add( getCheckButton(),   null );
        botBtnPanel.add( getSolveButton(),   null );
      }
      catch( Throwable e )
      {
        System.err.println( "PROBLEM: getBotBtnPanel()" );
      }
    }
    return botBtnPanel;
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
        gameMenu.add( getNewGameMenuItem() );
        gameMenu.add( getExitMenuItem()    );
        gameMenu.setPreferredSize( new Dimension(50,50) );
        gameMenu.setText( "GAME" );
      }
      catch( Throwable e )
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
        settingsMenu.add( getDelayMenuItem()      );
        settingsMenu.add( getDifficultymenuitem() );
        settingsMenu.setText( "SETTINGS" );
      }
      catch( Throwable e )
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
      catch( Throwable e )
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
      catch( Throwable e )
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
      catch( Throwable e )
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
      catch( Throwable e )
      {
        System.err.println( "PROBLEM: getDifficultyMenuItem(): " + e );
      }
    }
    return difficultyMenuItem;
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
        squaresMesg.setHorizontalAlignment( SwingConstants.CENTER );
        squaresMesg.setText( strSQUARES_TITLE + strZERO );
      }
      catch( Throwable e )
      {
        System.err.println( "PROBLEM: getSquaresMesg(): " + e );
      }
    }
    return squaresMesg;
  }
  
  /**
   * This method initializes infoMesg
   * 
   * @return infoMesg  JLabel
   */
  private JLabel getInfoMesg()
  {
    if( infoMesg == null )
    {
      try
      {
        infoMesg = new JLabel();
        infoMesg.setHorizontalAlignment( SwingConstants.CENTER );
        infoMesg.setText( strINFO_READY );
      }
      catch( Throwable e )
      {
        System.err.println( "PROBLEM: getInfoMesg(): " + e );
      }
    }
    return infoMesg;
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
        timeMesg.setHorizontalAlignment( SwingConstants.CENTER );
        timeMesg.setText( strTIME_TITLE + strZERO_TIME );
      }
      catch( Throwable e )
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
      catch( Throwable e )
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
      catch( Throwable e )
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
        undoButton.setPreferredSize( new Dimension(24,8) );
        undoButton.setText( "UNDO" );
        undoButton.addActionListener( listener );
      }
      catch( Throwable e )
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
      catch( Throwable e )
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
      catch( Throwable e )
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
      catch( Throwable e )
      {
        System.err.println( "PROBLEM: getSolveButton(): " + e );
      }
    }
    return solveButton;
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
      if( source == clock )
      {
        runClock();
        if( DEBUG_LEVEL > 2 )
          System.out.println( "ServListener clock event" );
      }
      else // not the clock
      {
        if( DEBUG_LEVEL > 1 )
          System.out.println( "ServListener ActionCommand: "
                              + ae.getActionCommand() );
        
        if( source == newGameButton || source == newGameMenuItem )
        {
          if( DEBUG_LEVEL == 1 )
            System.out.println( "ServListener NEW GAME event" );
          newGame();
        }
        else if( source == exitMenuItem || source == exitButton )
        {
          if( DEBUG_LEVEL == 1 )
            System.out.println( "ServListener EXIT event" );
          halt();
          dispose();
        }
        else if( source == undoButton )
        {
          if( DEBUG_LEVEL == 1 )
            System.out.println( "ServListener UNDO event" );
        }
        else if( source == redoButton )
        {
          if( DEBUG_LEVEL == 1 )
            System.out.println( "ServListener REDO event" );
        }
        else if( source == checkButton )
        {
          if( DEBUG_LEVEL == 1 )
            System.out.println( "ServListener CHECK event" );
        }
        else if( source == solveButton )
        {
          if( DEBUG_LEVEL == 1 )
            System.out.println( "ServListener SOLVE event" );
        }
      
      } // not the clock
    
    } // ServListener.actionPerformed()

  } /* inner class ServListener */

} // CLASS BeaconServer
