/*
 * Created on 31-Oct-06
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package mhsjava;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.JFrame;

/**
 * @author Mark Sattolo
 * @version 1.0  31-Oct-06
 */
public class BeaconServer extends JFrame
{
  private int TIMER_DELAY_MSEC = 1000;

  static final Color COLOR_GAME_BKGRND = new Color( 248, 232, 216 );

  static final int
                    GRID_SIZE = 9  , // # of squares per side of the grid
                    
                  SQUARE_SIZE = 32 , // length in pixels of each side of a Square 
                  
                    FONT_SIZE = SQUARE_SIZE / 2 + 6 ,
                    
                     X_BORDER = 80 ,  // extra space around the mine field
                     Y_BORDER = 60  ; // for the labels, buttons, etc

  static final String
            strDEFAULT_TYPEFACE = "Arial",
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

  Font myFont;

  boolean isRunning = false ;

  int seconds, // accumulated game time
      scoreMultiplier, // increase scoring according to density, etc
      maxScore, currentScore = 0;

  /** length, in pixels, of each side of the <code>BeaconClient</code>, 
   *  = (current side length of each square) * (# of squares on each side of field) */
  int fieldDim;

  private javax.swing.JPanel jContentPane = null;

  private javax.swing.JMenuBar gbsMenuBar = null;

  private javax.swing.JPanel botBtnPanel = null;
  private javax.swing.JPanel gridPanel = null;
  private javax.swing.JPanel topBtnPanel = null;
  private javax.swing.JPanel infoPanel = null;
  private javax.swing.JMenu gameMenu = null;
  private javax.swing.JMenu settingsMenu = null;
  private javax.swing.JMenuItem newGameMenuItem = null;
  private javax.swing.JMenuItem exitMenuItem = null;
  private javax.swing.JMenuItem delayMenuItem = null;
  private javax.swing.JMenuItem sizeMenuItem = null;
  private javax.swing.JLabel squaresTitle = null;
  private javax.swing.JLabel squaresMesg = null;
  private javax.swing.JLabel timeTitle = null;
  private javax.swing.JLabel timeMesg = null;
  private javax.swing.JButton exitButton = null;
  private javax.swing.JButton redoButton = null;
  private javax.swing.JButton undoButton = null;
  private javax.swing.JButton newGameButton = null;
  private javax.swing.JButton solveButton = null;
  private javax.swing.JButton checkButton = null;
  private javax.swing.JPanel blankTopPanel = null;

  int DEBUG_LEVEL;

  BeaconClient field;
  Timer clock;
  ServListener listener;

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
    if( DEBUG_LEVEL > 0)
      System.out.println("BeaconServer.DEBUG_LEVEL = " + DEBUG_LEVEL);

    initialize();
  }

  /**
   * This method initializes this
   * 
   * @return void
   */
  private void initialize()
  {
    this.setName("GbsFrame");
    this.setTitle("GBS 1.0");
    getContentPane().setBackground( COLOR_GAME_BKGRND );

    listener = new ServListener();
    
    field = new BeaconClient( this );
    field.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    field.setVerticalAlignment(javax.swing.SwingConstants.CENTER);
    field.setVisible(true);
    
    clock = new Timer( TIMER_DELAY_MSEC, listener );

    myFont = new Font( strDEFAULT_TYPEFACE, Font.BOLD, FONT_SIZE );

    this.setJMenuBar( getGbsMenuBar() );
    this.setDefaultCloseOperation( javax.swing.JFrame.EXIT_ON_CLOSE );

    this.setSize(600, 800);
    this.setContentPane( getJContentPane() );

    adjustSize();

  } // initialize()

  /**
   * Stop a running game
   * 
   * @return void
   */
  protected void halt()
  {
    validate(); // so the last square gets painted properly

    field.removeMouseListener(field);
    field.removeKeyListener(field);

    field.firstPress = true;

    isRunning = false;
    clock.stop();

  } // halt()

  /**
   * Start a new game
   * 
   * @return void
   */
  protected void newGame()
  {
    squaresTitle.setText( strSQUARES_TITLE );
     squaresMesg.setText( strZERO          );

    timeTitle.setText( strTIME_TITLE );
     timeMesg.setText( strZERO_TIME  );

    field.newField();

    jContentPane.repaint();

  } // newGame()

  /**
   * Start the clock
   * 
   * @return void
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
   * 
   * @return void
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
   * Update the dimensions
   * 
   * @return void
   */
  private void adjustSize()
  {
    fieldDim = SQUARE_SIZE * GRID_SIZE;
    setSize( fieldDim + X_BORDER*2, fieldDim + Y_BORDER*3 );
    validate();

  } // adjustSize()

  /**
   * This method initializes jContentPane
   * 
   * @return void
   */
  private javax.swing.JPanel getJContentPane()
  {
    jContentPane = new javax.swing.JPanel();
    java.awt.GridBagConstraints consGridBagConstraints12 = new java.awt.GridBagConstraints();
    java.awt.GridBagConstraints consGridBagConstraints11 = new java.awt.GridBagConstraints();
    java.awt.GridBagConstraints consGridBagConstraints13 = new java.awt.GridBagConstraints();
    java.awt.GridBagConstraints consGridBagConstraints14 = new java.awt.GridBagConstraints();
    consGridBagConstraints12.insets = new java.awt.Insets(0,40,0,0);
    consGridBagConstraints12.ipady = 10;
    consGridBagConstraints12.fill = java.awt.GridBagConstraints.BOTH;
    consGridBagConstraints12.weighty = 1.0;
    consGridBagConstraints12.weightx = 1.0;
    consGridBagConstraints12.gridy = 1;
    consGridBagConstraints12.gridx = 1;
    consGridBagConstraints14.ipadx = 274;
    consGridBagConstraints14.fill = java.awt.GridBagConstraints.BOTH;
    consGridBagConstraints14.weighty = 1.0;
    consGridBagConstraints14.weightx = 1.0;
    consGridBagConstraints14.gridwidth = 2;
    consGridBagConstraints14.gridy = 2;
    consGridBagConstraints14.gridx = 0;
    consGridBagConstraints11.ipadx = -8;
    consGridBagConstraints11.fill = java.awt.GridBagConstraints.BOTH;
    consGridBagConstraints11.weighty = 1.0;
    consGridBagConstraints11.weightx = 1.0;
    consGridBagConstraints11.gridwidth = 2;
    consGridBagConstraints11.gridy = 0;
    consGridBagConstraints11.gridx = 0;
    consGridBagConstraints13.insets = new java.awt.Insets(0,0,0,40);
    consGridBagConstraints13.ipady = 60;
    consGridBagConstraints13.fill = java.awt.GridBagConstraints.BOTH;
    consGridBagConstraints13.weighty = 4.0;
    consGridBagConstraints13.weightx = 4.0;
    consGridBagConstraints13.gridy = 1;
    consGridBagConstraints13.gridx = 0;

    jContentPane.setLayout(new java.awt.GridBagLayout());
    jContentPane.add(getInfoPanel(), consGridBagConstraints12);
    jContentPane.add(getTopBtnPanel(), consGridBagConstraints11);
    jContentPane.add(getGridPanel(), consGridBagConstraints13);
    jContentPane.add(getBotBtnPanel(), consGridBagConstraints14);
    
    return jContentPane ;
  }

  /**
   * This method initializes gbsMenuBar
   * 
   * @return javax.swing.JMenuBar
   */
  private javax.swing.JMenuBar getGbsMenuBar()
  {
    if( gbsMenuBar == null)
    {
      try
      {
        gbsMenuBar = new javax.swing.JMenuBar();
        gbsMenuBar.add(getGameMenu());
        gbsMenuBar.add(getSettingsMenu());
        gbsMenuBar.setPreferredSize(new java.awt.Dimension(52, 26));
      }
      catch (java.lang.Throwable e)
      {
        //  Do Something
      }
    }
    return gbsMenuBar;
  }

  /**
   * This method initializes botBtnPanel
   * 
   * @return javax.swing.JPanel
   */
  private javax.swing.JPanel getBotBtnPanel()
  {
    if( botBtnPanel == null)
    {
      try
      {
        botBtnPanel = new javax.swing.JPanel();
        java.awt.GridLayout layGridLayout4 = new java.awt.GridLayout();
        layGridLayout4.setRows(1);
        layGridLayout4.setColumns(3);
        layGridLayout4.setHgap(20);
        layGridLayout4.setVgap(20);
        botBtnPanel.setLayout(layGridLayout4);
        botBtnPanel.setPreferredSize(new java.awt.Dimension(289, 66));
        botBtnPanel.add(getNewGameButton(), null);
        botBtnPanel.add(getSolveButton(), null);
        botBtnPanel.add(getCheckButton(), null);
      }
      catch (java.lang.Throwable e)
      {
        System.err.println( "PROBLEM: getBotBtnPanel()" );
      }
    }
    return botBtnPanel;
  }
  
  /**
   * This method initializes gridPanel
   * 
   * @return javax.swing.JPanel
   */
  private javax.swing.JPanel getGridPanel()
  {
    if( gridPanel == null)
    {
      try
      {
        gridPanel = new javax.swing.JPanel();
        gridPanel.setLayout(new java.awt.FlowLayout());
        gridPanel.add(field, null);
        gridPanel.setPreferredSize(new java.awt.Dimension(600,600));
      }
      catch (java.lang.Throwable e)
      {
        System.err.println( "PROBLEM: getGridPanel(): " + e );
      }
    }
    return gridPanel;
  }
  /**
   * This method initializes topBtnPanel
   * 
   * @return javax.swing.JPanel
   */
  private javax.swing.JPanel getTopBtnPanel()
  {
    if( topBtnPanel == null)
    {
      try
      {
        topBtnPanel = new javax.swing.JPanel();
        java.awt.GridLayout layGridLayout2 = new java.awt.GridLayout();
        layGridLayout2.setRows(1);
        layGridLayout2.setColumns(4);
        layGridLayout2.setVgap(25);
        layGridLayout2.setHgap(20);
        topBtnPanel.setLayout(layGridLayout2);
        topBtnPanel.add(getUndoButton(), null);
        topBtnPanel.add(getRedoButton(), null);
        topBtnPanel.add(getExitButton(), null);
        topBtnPanel.setPreferredSize(new java.awt.Dimension(223,66));
      }
      catch (java.lang.Throwable e)
      {
        System.err.println( "PROBLEM: getTopBtnPanel():" + e );
      }
    }
    return topBtnPanel;
  }
  /**
   * This method initializes infoPanel
   * 
   * @return javax.swing.JPanel
   */
  private javax.swing.JPanel getInfoPanel()
  {
    if( infoPanel == null)
    {
      try
      {
        infoPanel = new javax.swing.JPanel();
        java.awt.GridLayout layGridLayout3 = new java.awt.GridLayout();
        layGridLayout3.setRows(1);
        layGridLayout3.setColumns(5);
        layGridLayout3.setVgap(20);
        layGridLayout3.setHgap(2);
        infoPanel.setLayout(layGridLayout3);
        infoPanel.add(getSquaresTitle(), null);
        infoPanel.add(getSquaresMesg(), null);
        infoPanel.add(getBlankTopPanel(), null);
        infoPanel.add(getTimeTitle(), null);
        infoPanel.add(getTimeMesg(), null);
      }
      catch (java.lang.Throwable e)
      {
        System.err.println( "PROBLEM: getInfoPanel():" + e );
      }
    }
    return infoPanel;
  }
  /**
   * This method initializes gameMenu
   * 
   * @return javax.swing.JMenu
   */
  private javax.swing.JMenu getGameMenu()
  {
    if( gameMenu == null)
    {
      try
      {
        gameMenu = new javax.swing.JMenu();
        gameMenu.add(getNewGameMenuItem());
        gameMenu.add(getExitMenuItem());
        gameMenu.setPreferredSize(new java.awt.Dimension(50, 50));
        gameMenu.setText("GAME");
      }
      catch (java.lang.Throwable e)
      {
        System.err.println( "PROBLEM: getGameMenu():" + e );
      }
    }
    return gameMenu;
  }
  /**
   * This method initializes settingsMenu
   * 
   * @return javax.swing.JMenu
   */
  private javax.swing.JMenu getSettingsMenu()
  {
    if( settingsMenu == null)
    {
      try
      {
        settingsMenu = new javax.swing.JMenu();
        settingsMenu.add(getDelayMenuItem());
        settingsMenu.add(getSizeMenuItem());
        settingsMenu.setText("SETTINGS");
      }
      catch (java.lang.Throwable e)
      {
        System.err.println( "PROBLEM: getSettingsMenu():" + e );
      }
    }
    return settingsMenu;
  }
  /**
   * This method initializes newGameMenuItem
   * 
   * @return javax.swing.JMenuItem
   */
  private javax.swing.JMenuItem getNewGameMenuItem()
  {
    if( newGameMenuItem == null)
    {
      try
      {
        newGameMenuItem = new javax.swing.JMenuItem();
        newGameMenuItem.setText("New Game");
      }
      catch (java.lang.Throwable e)
      {
        System.err.println( "PROBLEM: getNewGameMenuItem():" + e );
      }
    }
    return newGameMenuItem;
  }
  /**
   * This method initializes exitMenuItem
   * 
   * @return javax.swing.JMenuItem
   */
  private javax.swing.JMenuItem getExitMenuItem()
  {
    if( exitMenuItem == null)
    {
      try
      {
        exitMenuItem = new javax.swing.JMenuItem();
        exitMenuItem.setText("Exit");
      }
      catch (java.lang.Throwable e)
      {
        System.err.println( "PROBLEM: getExitMenuItem():" + e );
      }
    }
    return exitMenuItem;
  }
  /**
   * This method initializes delayMenuItem
   * 
   * @return javax.swing.JMenuItem
   */
  private javax.swing.JMenuItem getDelayMenuItem()
  {
    if( delayMenuItem == null)
    {
      try
      {
        delayMenuItem = new javax.swing.JMenuItem();
        delayMenuItem.setText("Set Delay");
      }
      catch (java.lang.Throwable e)
      {
        System.err.println( "PROBLEM: getDelayMenuItem():" + e );
      }
    }
    return delayMenuItem;
  }
  /**
   * This method initializes sizeMenuItem
   * 
   * @return javax.swing.JMenuItem
   */
  private javax.swing.JMenuItem getSizeMenuItem()
  {
    if( sizeMenuItem == null)
    {
      try
      {
        sizeMenuItem = new javax.swing.JMenuItem();
        sizeMenuItem.setText("Change Size");
      }
      catch (java.lang.Throwable e)
      {
        System.err.println( "PROBLEM: getSizeMenuItem():" + e );
      }
    }
    return sizeMenuItem;
  }
  /**
   * This method initializes squaresTitle
   * 
   * @return javax.swing.JLabel
   */
  private javax.swing.JLabel getSquaresTitle()
  {
    if( squaresTitle == null)
    {
      try
      {
        squaresTitle = new javax.swing.JLabel();
        squaresTitle.setText("Squares left:");
        squaresTitle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
      }
      catch (java.lang.Throwable e)
      {
        System.err.println( "PROBLEM: getSquaresTitle():" + e );
      }
    }
    return squaresTitle;
  }
  /**
   * This method initializes squaresMesg
   * 
   * @return javax.swing.JLabel
   */
  private javax.swing.JLabel getSquaresMesg()
  {
    if( squaresMesg == null)
    {
      try
      {
        squaresMesg = new javax.swing.JLabel();
        squaresMesg.setText("xxx");
        squaresMesg.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
      }
      catch (java.lang.Throwable e)
      {
        System.err.println( "PROBLEM: getSquaresMesg():" + e );
      }
    }
    return squaresMesg;
  }
  /**
   * This method initializes timeTitle
   * 
   * @return javax.swing.JLabel
   */
  private javax.swing.JLabel getTimeTitle()
  {
    if( timeTitle == null)
    {
      try
      {
        timeTitle = new javax.swing.JLabel();
        timeTitle.setText("Time:");
        timeTitle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
      }
      catch (java.lang.Throwable e)
      {
        System.err.println( "PROBLEM: getTimeTitle():" + e );
      }
    }
    return timeTitle;
  }
  /**
   * This method initializes timeMesg
   * 
   * @return javax.swing.JLabel
   */
  private javax.swing.JLabel getTimeMesg()
  {
    if( timeMesg == null)
    {
      try
      {
        timeMesg = new javax.swing.JLabel();
        timeMesg.setText(strZERO_TIME);
        timeMesg.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
      }
      catch (java.lang.Throwable e)
      {
        System.err.println( "PROBLEM: getTimeMesg():" + e );
      }
    }
    return timeMesg;
  }
  /**
   * This method initializes exitButton
   * 
   * @return javax.swing.JButton
   */
  private javax.swing.JButton getExitButton()
  {
    if( exitButton == null)
    {
      try
      {
        exitButton = new javax.swing.JButton();
        exitButton.setText("EXIT");
        exitButton.addActionListener(listener);
      }
      catch (java.lang.Throwable e)
      {
        System.err.println( "PROBLEM: getExitButton():" + e );
      }
    }
    return exitButton;
  }
  /**
   * This method initializes redoButton
   * 
   * @return javax.swing.JButton
   */
  private javax.swing.JButton getRedoButton()
  {
    if( redoButton == null)
    {
      try
      {
        redoButton = new javax.swing.JButton();
        redoButton.setText("REDO");
        redoButton.addActionListener(listener);
      }
      catch (java.lang.Throwable e)
      {
        System.err.println( "PROBLEM: getRedoButton():" + e );
      }
    }
    return redoButton;
  }
  /**
   * This method initializes undoButton
   * 
   * @return javax.swing.JButton
   */
  private javax.swing.JButton getUndoButton()
  {
    if( undoButton == null)
    {
      try
      {
        undoButton = new javax.swing.JButton();
        undoButton.setPreferredSize(new java.awt.Dimension(24, 8));
        undoButton.setText("UNDO");
        undoButton.addActionListener(listener);
      }
      catch (java.lang.Throwable e)
      {
        System.err.println( "PROBLEM: getUndoButton():" + e );
      }
    }
    return undoButton;
  }
  /**
   * This method initializes checkButton
   * 
   * @return javax.swing.JButton
   */
  private javax.swing.JButton getCheckButton()
  {
    if( checkButton == null)
    {
      try
      {
        checkButton = new javax.swing.JButton();
        checkButton.setText("Check");
        checkButton.addActionListener(listener);
      }
      catch (java.lang.Throwable e)
      {
        System.err.println( "PROBLEM: getCheckButton():" + e );
      }
    }
    return checkButton;
  }
  /**
   * This method initializes newGameButton
   * 
   * @return javax.swing.JButton
   */
  private javax.swing.JButton getNewGameButton()
  {
    if( newGameButton == null)
    {
      try
      {
        newGameButton = new javax.swing.JButton();
        newGameButton.setText("New Game");
        newGameButton.addActionListener(listener);
      }
      catch (java.lang.Throwable e)
      {
        System.err.println( "PROBLEM: getNewGameButton():" + e );
      }
    }
    return newGameButton;
  }
  /**
   * This method initializes solveButton
   * 
   * @return javax.swing.JButton
   */
  private javax.swing.JButton getSolveButton()
  {
    if( solveButton == null)
    {
      try
      {
        solveButton = new javax.swing.JButton();
        solveButton.setText("Solve");
        solveButton.addActionListener(listener);
      }
      catch (java.lang.Throwable e)
      {
        System.err.println( "PROBLEM: getSolveButton():" + e );
      }
    }
    return solveButton;
  }

  /**
   * This method initializes blankTopPanel
   * 
   * @return javax.swing.JTextField
   */
  private javax.swing.JPanel getBlankTopPanel()
  {
    if( blankTopPanel == null)
    {
      try
      {
        blankTopPanel = new javax.swing.JPanel();
        //blankTopPanel.setBackground( COLOR_GAME_BKGRND );
      }
      catch (java.lang.Throwable e)
      {
        System.err.println( "PROBLEM: getBlankTextField():" + e );
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

        if( source == newGameButton || source == newGameMenuItem)
        {
          if( DEBUG_LEVEL > 0)
            System.out.println( "ServListener NEW GAME event" );
          halt();
          newGame();
        }
        else if( source == exitMenuItem || source == exitButton)
        {
          if( DEBUG_LEVEL > 0)
            System.out.println( "ServListener EXIT event" );
          halt();
          dispose();
        }
        else if( source == undoButton)
        {
          if( DEBUG_LEVEL > 0)
            System.out.println( "ServListener UNDO event" );
        }
        else if( source == redoButton)
        {
          if( DEBUG_LEVEL > 0)
            System.out.println( "ServListener REDO event" );
        }
        else if( source == checkButton)
        {
          if( DEBUG_LEVEL > 0)
            System.out.println( "ServListener CHECK event" );
        }
        else if( source == solveButton)
        {
          if( DEBUG_LEVEL > 0)
            System.out.println("ServListener SOLVE event");
        }

      } // not the clock

    } // ServListener.actionPerformed()

  } /* inner class ServListener */

} //  @jve:visual-info  decl-index=0 visual-constraint="10,10"
