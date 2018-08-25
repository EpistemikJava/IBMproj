/*
 * Created on 26-Oct-06
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package mhsjava;

import java.awt.* ;
import java.awt.event.* ;
import java.applet.* ;
import java.net.URL ;
import javax.swing.* ;

/**
 * @author Mark Sattolo
 * @version 1.0  26-Oct-06
 */
public class SudokuGame extends JFrame
{
 /*
  *   FIELDS
  * ============================================= */

  static final String VERSION = "SudokuGame 1.0";

  static final int TIMER_DELAY_MSEC = 1000 ;

  static final double STANDARD_DENSITY = 0.25 ; // default

  static final Color
            COLOR_GAME_BKGRND = Color.cyan.darker()   , 
           COLOR_SCORE_BKGRND = Color.yellow.darker() ,
            COLOR_INFO_BKGRND = Color.yellow.darker() ,
            COLOR_INFO_FRGRND = Color.blue.darker()   ;

  static final int
              GRID_SIZE_SMALL =  9 , // # of squares per side of the grid
                GRID_SIZE_MED = 16 ,  
              GRID_SIZE_LARGE = 25 , 
          
            SQUARE_SIZE_SMALL = 20 , // length in pixels of each side of an
              SQUARE_SIZE_MED = 26 , // individual square 
            SQUARE_SIZE_LARGE = 32 , 
          
              FONT_SIZE_SMALL = SQUARE_SIZE_SMALL/2 + 2 ,
                FONT_SIZE_MED =   SQUARE_SIZE_MED/2 + 4 ,
              FONT_SIZE_LARGE = SQUARE_SIZE_LARGE/2 + 6 ,
            
                     X_BORDER = 80 , // extra space around the grid
                     Y_BORDER = 60 ; // for menus, labels, buttons, etc

  static final String
          strDEFAULT_TYPEFACE = "Arial" ,
        
          strZERO          = "0"             ,
          strZERO_TIME     = "00:00:00"      ,
          strFULL_TIME     = "99:59:59"      ,
          strSEPARATOR     = ":"             ,
          strSCORE_TITLE   = "Score:"        ,
          strSCORE_FINAL   = "Final "        ,
          strMINES_TITLE   = "Squares left:" ,
          strTIME_TITLE    = "Time:"         ,
          strINFO_READY    = "Ready to Go"   ,
          strINFO_RUNNING  = "Running..."    ,
        
          strMINESMSG_ZERODENSITY = " (wow?)" ,
          strSCOREMSG_ZERODENSITY = "IDIOT!"  ,
        
          strINFOMSG_LOWDENSITY  = "Too easy :(" ,
          strINFOMSG_MIDDENSITY  = "Neat ;)"     ,
          strINFOMSG_HIGHDENSITY = "AWESOME!"    ,
          strINFOMSG_FULLDENSITY = "Having fun?" ,
        
          strINFOMSG_LOWSCORE = "A bit slow..."  ,
          strINFOMSG_NEGSCORE = "VERY slow!"     ,
        
          strGAME_MENU     = "Game"               ,
          strSETTINGS_MENU = "Settings"           ,
          strRESET_BTN     = "SOLVE"              ,
          strGO_SND_BTN    = "Activate Sounds"    ,
          strNO_SND_BTN    = "De-activate Sounds" ,
        
          strSmall_ITEM    = "New Small Grid (" + GRID_SIZE_SMALL + " sqr/side)" ,
          strMedium_ITEM   = "New Medium Grid (" + GRID_SIZE_MED + " sqr/side)"  ,
          strLarge_ITEM    = "New Large Grid (" + GRID_SIZE_LARGE + " sqr/side)" ,
          strExit_ITEM     = "eXit"            ,
          strQMARKS_ITEM   = "Show Order?" ,
          strNEWGAME_ITEM  = "New Game"        ,
          strSETTINGS_ITEM = "Change settings" ;

  static final int BOOM      = 0 , 
                   CHIRP     = BOOM      + 1 ,
                   FLAG      = CHIRP     + 1 ,
                   NEAT      = FLAG      + 1 , 
                   EASY      = NEAT      + 1 ,
                   AWESOME   = EASY      + 1 , 
                   RESET     = AWESOME   + 1 , 
                   BAD_CLEAR = RESET     + 1 , 
                   SLOW      = BAD_CLEAR + 1 ,
             NUM_SOUND_CLIPS = SLOW      + 1 ; // must be last entry

  static final String[] strSOUND_CLIPS
                         = { "sounds/redalert.au"   ,
                             "sounds/kookaburra.au" ,
                             "sounds/newmail.au"    ,
                             "sounds/drums.au"      ,
                             "sounds/2Drums.au"     ,
                             "sounds/spacemusic.au" ,
                             "sounds/zubetube.au"   ,
                             "sounds/exit.au"       ,
                             "sounds/message.au"    } ;

  int DEBUG_LEVEL ;

  Font fontSMALL, fontMEDIUM, fontLARGE ;
        
  boolean isRunning = false, settingsOpen = false, qMarksOn = false ;

  int seconds, // accumulated game time
      scoreMultiplier, // increase scoring according to density, etc
      maxScore, currentScore = 0 ;

  /** length, in pixels, of each side of the <code>SudokuGrid</code>, 
   *  = (current side length of each square) * (# of squares on each side of field) */
  int fieldDim ;

  boolean      loadedSound = false, playSounds = false ;
  AudioClip[]  soundFX ;
  String       strBaseURL ;

      SudokuGrid  SudokuGrid ;
    JLayeredPane  pane ; 
  SudokuSettings  settingsFrame ;

          JMenuBar   gameBar ;
          JMenu      gameMenu, settingsMenu ;
          JMenuItem  mitemSmallGrid, mitemMedGrid, mitemLargeGrid,
                     mitemExitGame, mitemNewGame, mitemShowSettings ;
  JCheckBoxMenuItem  mitemShowOrder ;
          KeyStroke  ks ;

  JPanel   scorePanel, infoPanel ;
  JButton  soundBtn, resetBtn ;
  Box      infoBox ;
  Label    infoMesg ; /* does NOT resize properly as a JLabel */

  JLabel   scoreTitle, scoreMesg ,
         squaresTitle, squaresMesg ,
            timeTitle, timeMesg  ;
  
  Timer clock ;
  SudokuListener listener ;

 /*
  *   METHODS
  * ============================================= */

  /** MAIN */ 
  public static void main( String[] args )
  {
    SudokuGame gameFrame = new SudokuGame( args.length > 0 
                                             ? Integer.parseInt(args[0]) : 0 );
    gameFrame.setVisible( true ); // show() is DEPRECATED 
  
  }// main()

  public SudokuGame( int debug )
  {
    DEBUG_LEVEL = debug ;
    System.out.println( "SudokuGame.DEBUG_LEVEL = " + DEBUG_LEVEL );

    setTitle( VERSION );
    getContentPane().setBackground( COLOR_GAME_BKGRND );

    addWindowListener(
      new WindowAdapter()
        { public void windowClosing( WindowEvent we )
                      {
                        end(); // shut down SudokuGame/SudokuSettings
                        dispose(); // system call
                      }
        } );
  
    listener = new SudokuListener();
  
    fontSMALL  = new Font( strDEFAULT_TYPEFACE, Font.BOLD, FONT_SIZE_SMALL );
    fontMEDIUM = new Font( strDEFAULT_TYPEFACE, Font.BOLD, FONT_SIZE_MED   );
    fontLARGE  = new Font( strDEFAULT_TYPEFACE, Font.BOLD, FONT_SIZE_LARGE );
  
    SudokuGrid = new SudokuGrid( this );
    SudokuGrid.setGridLength( GRID_SIZE_SMALL );
    SudokuGrid.setSquareLength( SQUARE_SIZE_LARGE );
  
    adjustSize();
    adjustScore();
  
    clock = new Timer( TIMER_DELAY_MSEC, listener );
    settingsFrame = new SudokuSettings( this );
  
    // create Menu Bar
    gameBar = new JMenuBar();
    setJMenuBar( gameBar ); 

    // Game Menu
    gameMenu = new JMenu( strGAME_MENU );
  
    mitemSmallGrid = new JMenuItem( strSmall_ITEM );
    ks = KeyStroke.getKeyStroke( KeyEvent.VK_S, Event.ALT_MASK ); 
    mitemSmallGrid.setAccelerator( ks ); 
    mitemSmallGrid.setMnemonic( 'S' ); 
    mitemSmallGrid.addActionListener( listener );

    mitemMedGrid = new JMenuItem( strMedium_ITEM );
    ks = KeyStroke.getKeyStroke( KeyEvent.VK_M, Event.ALT_MASK ); 
    mitemMedGrid.setAccelerator( ks ); 
    mitemMedGrid.setMnemonic( 'M' ); 
    mitemMedGrid.addActionListener( listener );

    mitemLargeGrid = new JMenuItem( strLarge_ITEM );
    ks = KeyStroke.getKeyStroke( KeyEvent.VK_L, Event.ALT_MASK ); 
    mitemLargeGrid.setAccelerator( ks ); 
    mitemLargeGrid.setMnemonic( 'L' ); 
    mitemLargeGrid.addActionListener( listener );

    mitemExitGame = new JMenuItem( strExit_ITEM );
    ks = KeyStroke.getKeyStroke( KeyEvent.VK_X, Event.ALT_MASK ); 
    mitemExitGame.setAccelerator( ks ); 
    mitemExitGame.setMnemonic( 'X' ); 
    mitemExitGame.addActionListener( listener );
    
    gameMenu.add( mitemSmallGrid  );
    gameMenu.add( mitemMedGrid );
    gameMenu.add( mitemLargeGrid  );
    gameMenu.addSeparator();
    gameMenu.add( mitemExitGame   );
    
    // Setup Menu
    settingsMenu = new JMenu( strSETTINGS_MENU );
    
    mitemShowOrder = new JCheckBoxMenuItem( strQMARKS_ITEM, qMarksOn );
    ks = KeyStroke.getKeyStroke( KeyEvent.VK_Q, Event.ALT_MASK ); 
    mitemShowOrder.setAccelerator( ks ); 
    mitemShowOrder.setMnemonic( 'Q' ); 
    mitemShowOrder.addActionListener( listener );
    
    mitemNewGame = new JMenuItem( strNEWGAME_ITEM );
    ks = KeyStroke.getKeyStroke( KeyEvent.VK_F2, 0 ); 
    mitemNewGame.setAccelerator( ks ); 
    mitemNewGame.addActionListener( listener );
    
    mitemShowSettings = new JMenuItem( strSETTINGS_ITEM );
    ks = KeyStroke.getKeyStroke( KeyEvent.VK_C, Event.ALT_MASK ); 
    mitemShowSettings.setAccelerator( ks ); 
    mitemShowSettings.setMnemonic( 'C' ); 
    mitemShowSettings.addActionListener( listener );
    
    settingsMenu.add( mitemShowOrder );
    settingsMenu.add( mitemNewGame );
    settingsMenu.add( mitemShowSettings );
    
    gameBar.add( gameMenu  );
    gameBar.add( settingsMenu );
    
    // Score Panel
    scorePanel = new JPanel( new GridLayout(1,6) );
    scorePanel.setBackground( COLOR_SCORE_BKGRND );
    
    scoreTitle = new JLabel( strSCORE_TITLE );
    scoreTitle.setHorizontalAlignment( SwingConstants.CENTER );
    scoreMesg = new JLabel( strZERO );
    scorePanel.add( scoreTitle );   
    scorePanel.add( scoreMesg );
    
    squaresTitle = new JLabel( strMINES_TITLE );
    squaresMesg = new JLabel( strZERO );
    setMinesMesg( SudokuGrid.getMines() );
    scorePanel.add( squaresTitle );
    scorePanel.add( squaresMesg );
    
    timeTitle = new JLabel( strTIME_TITLE );
    timeMesg = new JLabel( strZERO_TIME );
    scorePanel.add( timeTitle );
    scorePanel.add( timeMesg );
    
    getContentPane().add( scorePanel, "North" );    
    
    // set up JLayeredPane
    pane = new JLayeredPane();
    pane.add( SudokuGrid, JLayeredPane.DEFAULT_LAYER );
    getContentPane().add( pane, "Center" );
    
    // Info Panel & Box
    infoPanel = new JPanel( new GridLayout(1,1) );
    infoBox = Box.createHorizontalBox();
    
    // setBackground() does NOT work with a Box
    infoPanel.setBackground( COLOR_INFO_BKGRND );
    
    soundBtn = new JButton( strGO_SND_BTN ); 
    soundBtn.addActionListener( listener );
    infoBox.add( Box.createGlue() );
    infoBox.add( soundBtn );
    
    resetBtn = new JButton( strRESET_BTN ); 
    resetBtn.addActionListener( listener );
    infoBox.add( Box.createGlue() );
    infoBox.add( resetBtn );
    
    infoMesg = new Label( strINFO_READY, Label.CENTER );
    infoMesg.setFont( fontLARGE );
    infoMesg.setForeground( COLOR_INFO_FRGRND );
    infoBox.add( Box.createGlue() );
    infoBox.add( infoMesg );
    infoBox.add( Box.createGlue() );
    
    infoPanel.add( infoBox );
    getContentPane().add( infoPanel, "South" );    
    
  }// Constructor

  protected void halt()
  {
    SudokuGrid.shaded = false ;
    validate();// so the last square gets painted properly
    
    SudokuGrid.removeMouseListener( SudokuGrid );
    SudokuGrid.firstPress = true ;

    isRunning = false ;
    clock.stop();
  
  }// halt()

  protected void newGame()
  {
    playSound( RESET );
  
     timeTitle.setText( strTIME_TITLE  );
      timeMesg.setText( strZERO_TIME   );
      infoMesg.setText( strINFO_READY  );
    scoreTitle.setText( strSCORE_TITLE );
  
    adjustScore();
    currentScore = 0 ;
    scoreMesg.setText( strZERO );
  
    SudokuGrid.reDraw();
    setMinesMesg( SudokuGrid.getMines() );
  
    pane.repaint();
  
  }// newGame()

  protected void loadSound()
  {
    URL clip ;
    strBaseURL = "file:" + System.getProperty( "user.dir" ) 
                 + System.getProperty( "file.separator" );

    if( DEBUG_LEVEL > 0 )
      System.out.println( "SudokuGame.loadSound(): Base URL = " + strBaseURL );

    try
    {
      soundFX = new AudioClip[ NUM_SOUND_CLIPS ];

      for( int i=0; i < NUM_SOUND_CLIPS; i++ )
      {
        clip = new URL( strBaseURL + strSOUND_CLIPS[i] );
        if( DEBUG_LEVEL > 0 )
          System.out.println( "URL[" + i + "] = " + clip.toString() );
  
        soundFX[i] = Applet.newAudioClip( clip );
      }
    }
    catch( Exception e )
      {
        System.err.println( "SudokuGame.loadSound() Error: " + e );
      }
  
  }// loadSound()

  protected void playSound( int track ) 
  {
    if( !playSounds ) return ;
  
    if( track >= NUM_SOUND_CLIPS )
    {
      System.err.println( "SudokuGame.playSound(): track # " + track + " NOT VALID !" );
      return ;
    }
  
    if( soundFX[track] == null )
    {
      System.err.println( "SudokuGame.playSound(): "
                          + strSOUND_CLIPS[track] + " NOT LOADED ??!!" );
      return ;
    }

    soundFX[track].play();
    if( DEBUG_LEVEL > 1 )
      System.out.println( "Playing " + strSOUND_CLIPS[track] );

  }// playSound()

  protected void newSquareLength( int len )
  {
    // tell the mine field that Settings has changed the Square size
    SudokuGrid.setSquareLength( len );

    if( len == SQUARE_SIZE_SMALL )
    {
      SudokuGrid.setFont( fontSMALL );
    }
    else if( len == SQUARE_SIZE_LARGE )
    {
      SudokuGrid.setFont( fontLARGE );
    }
    else // default: should only ever be == SQUARE_SIZE_MED
        SudokuGrid.setFont( fontMEDIUM );
  
    adjustSize();

  }// newSquareLength()

  /** Adjust the size of the game frame as the <code>Square</code> size 
   *  or number of <code>Squares</code> in the field have changed */
  protected void adjustSize()
  {
    fieldDim = SudokuGrid.getSquareLength() * SudokuGrid.getGridLength();
    setSize( fieldDim + X_BORDER*2, fieldDim + Y_BORDER*3 );
    validate();
  }

  /** Adjust the size of the game frame and start a new game */
  protected void newSize()
  {
    adjustSize();
    halt();
    newGame();
  } 

  protected void adjustScore()
  {
    scoreMultiplier = (int)( SudokuGrid.getDensity() * 100 );
    maxScore = scoreMultiplier 
               * (int)Math.pow( (double)SudokuGrid.getGridLength(), 2.0 ) ;
  }

  protected void adjustDensity( double density )
  {
    SudokuGrid.setDensity( density );
    SudokuGrid.repaint();
  }

  protected void startClock()
  {
    if( !isRunning )
    {
      isRunning = true ;
      seconds = 0 ;
      timeMesg.setText( strZERO_TIME    );
      infoMesg.setText( strINFO_RUNNING );
      clock.start();
    }
  }// startClock()

  protected void runClock()
  {
    if( isRunning && (getState() == JFrame.NORMAL) )
    {                 // stop clock if game is iconified
      seconds++ ;

      int hrs  = seconds/3600 ; 
      int mins = seconds/60 - hrs*60 ;
      int secs = seconds - mins*60 - hrs*3600 ;

      String strHr  =  hrs < 10 ? strZERO + Integer.toString( hrs  )
                                : Integer.toString( hrs );
      String strMin = mins < 10 ? strZERO + Integer.toString( mins )
                                : Integer.toString( mins );
      String strSec = secs < 10 ? strZERO + Integer.toString( secs )
                                : Integer.toString( secs );

      timeMesg.setText( strHr + strSEPARATOR + strMin + strSEPARATOR + strSec );
    }

  }// runClock()

  /** display text (converted from int) indicating # of mines left
   *  @param numMines  int */
  protected void setMinesMesg( int numMines )
  {
    squaresMesg.setText( " " + Integer.toString(numMines) );
  }

  /** calculate the game score and update the display
   *  - show appropriate messages and play sounds if activated
   *  @param increase  boolean indicating if increasing or decreasing score */
  protected void setScore( boolean increase )
  {
    if( increase )
    {
      currentScore += scoreMultiplier ;
      scoreMesg.setText( " " + Integer.toString(currentScore) );
    
      /* all done */
      if( currentScore == maxScore  &&  SudokuGrid.getMines() == 0 )
      {
        double d = SudokuGrid.getDensity();
      
        currentScore += ( maxScore - (100 - scoreMultiplier)*(seconds/2) );
        scoreTitle.setText( strSCORE_FINAL + strSCORE_TITLE );
        timeTitle.setText( strSCORE_FINAL + strTIME_TITLE );
  
        if( d == 0.0 )
        { 
          timeMesg.setText( strFULL_TIME );
          squaresMesg.setText( strZERO + strMINESMSG_ZERODENSITY );
          scoreMesg.setText( strSCOREMSG_ZERODENSITY );
        }
        else
            scoreMesg.setText( " " + Integer.toString(currentScore) );

        if( currentScore >= maxScore*0.5 )
        {
          if( d > 0.0 && d <= STANDARD_DENSITY*0.5 )
          {
            playSound( EASY );
            infoMesg.setText( strINFOMSG_LOWDENSITY );
          }
          else if( d > STANDARD_DENSITY*0.5 && d < STANDARD_DENSITY )
            {
              playSound( NEAT );     
              infoMesg.setText( strINFOMSG_MIDDENSITY );
            }
          else if( d >= STANDARD_DENSITY && d < 1.0 )
            {
              playSound( AWESOME );
              infoMesg.setText( strINFOMSG_HIGHDENSITY );
            }
          else // d == 1.0
            { 
              infoMesg.setText( strINFOMSG_FULLDENSITY );
              scoreMesg.setText( Integer.toString((int)d) );
            }
        }
        else // currentScore < maxScore*0.5
        {
          playSound( SLOW );
          infoMesg.setText( (currentScore > 0) ? strINFOMSG_LOWSCORE : strINFOMSG_NEGSCORE );
        }

        SudokuGrid.paintArea(); // so the last flag is painted
        halt();

      }// if done
    
    }// if increase
    else
      {
        currentScore -= scoreMultiplier ;
        scoreMesg.setText( Integer.toString(currentScore) );
      }
    
  }// setScore()

  protected void showSudokuSettings()
  {
    settingsFrame.setLocation( scorePanel.getLocationOnScreen() );
    settingsFrame.setVisible( true ); // show() is DEPRECATED
  }

  protected void setMessage( String msg )
  {
    infoMesg.setText(msg);
  }    

  /** shut down <code>SudokuGame</code> & <code>SudokuSettings</code> (if open) */
  protected void end()
  {
    isRunning = false ;
  
    if( SudokuGrid.exploder.isRunning() )
      SudokuGrid.exploder.stop();
  
    if( settingsOpen )
      settingsFrame.dispose();
  
  }// end() 

  /*
   *   INNER CLASSES
   * ============================================= */

  /** Handles all the action events for <code>SudokuGame</code>.
   *  i.e. function as the Controller for the app   */
  class SudokuListener implements ActionListener
  {
    Object source ;
  
    public void actionPerformed( ActionEvent ae )
    {
      source = ae.getSource();
      if( source == clock )
      {
        runClock();
        if( DEBUG_LEVEL > 2 )
          System.out.println( "SudokuListener clock event" );
      }
      else // not the clock
      {
        if( DEBUG_LEVEL > 0 )
          System.out.println( "SudokuListener ActionCommand: "
                              + ae.getActionCommand()        );
      
        if( source == resetBtn || source == mitemNewGame )
        {
          halt();
          newGame();
        }
        else if( source == soundBtn )
        {
          if( !loadedSound )
          {
            loadSound();
            loadedSound = true ;
          }
    
          if( playSounds )
          {
            playSounds = false ;
            soundBtn.setText( strGO_SND_BTN );
          }
          else
            {
              playSounds = true ;
              soundBtn.setText( strNO_SND_BTN );
            }
        }
        else if( source == mitemExitGame )
        {
          end();
          dispose();
        }
        else if( source == mitemSmallGrid )
        {
          SudokuGrid.setGridLength( GRID_SIZE_SMALL );
          newSize();
        }
        else if( source == mitemMedGrid )
        {
          SudokuGrid.setGridLength( GRID_SIZE_MED );
          newSize();
        }
        else if( source == mitemLargeGrid )
        {
          SudokuGrid.setGridLength( GRID_SIZE_LARGE );
          newSize();
        }
        else if( source == mitemShowOrder )
        {
          qMarksOn = mitemShowOrder.isSelected();
        
          if( !qMarksOn )
            SudokuGrid.clearQmarks();
        }
        else if( source == mitemShowSettings )
            showSudokuSettings();
  
      }// not the clock

    }// SudokuListener.actionPerformed()
  
  }/* inner class SudokuListener */
  
}/* class SudokuGame */
