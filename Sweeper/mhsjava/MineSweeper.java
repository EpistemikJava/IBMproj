/*
 * Created on 20-Oct-06
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
 * A Java Minesweeper inspired by the classic Windows game.
 *
 * @author Mark Sattolo (based on code by Mats Antell)
 * @version 1.18 - 2006/010/31
 */
public class MineSweeper extends JFrame
{
 /*
  *   FIELDS
  * ============================================= */
  
  static final String VERSION = "MineSweeper 1.18";
  
  static final int TIMER_DELAY_MSEC = 1000 ;

  static final double STANDARD_DENSITY = 0.25 ; // default
  
  static final Color
            COLOR_GAME_BKGRND = Color.cyan.darker()   , 
           COLOR_SCORE_BKGRND = Color.yellow.darker() ,
            COLOR_INFO_BKGRND = Color.yellow.darker() ,
            COLOR_INFO_FRGRND = Color.blue.darker()   ;

  static final int
              GRID_SIZE_SMALL = 16 , // # of squares per side of the grid
                GRID_SIZE_MED = 20 , 
              GRID_SIZE_LARGE = 24 , 
                    
            SQUARE_SIZE_SMALL = 18 , // length in pixels of each side of an
              SQUARE_SIZE_MED = 24 , // individual square 
            SQUARE_SIZE_LARGE = 30 , 
                      
              FONT_SIZE_SMALL = SQUARE_SIZE_SMALL/2 + 2 ,
                FONT_SIZE_MED =   SQUARE_SIZE_MED/2 + 4 ,
              FONT_SIZE_LARGE = SQUARE_SIZE_LARGE/2 + 6 ,
              
                     X_BORDER = 80 , // extra space around the mine field
                     Y_BORDER = 60 ; // for the labels, buttons, etc
  
  static final String
          strDEFAULT_TYPEFACE = "Arial" ,
          
          strZERO          = "0"           ,
          strZERO_TIME     = "00:00:00"    ,
          strFULL_TIME     = "99:59:59"    ,
          strSEPARATOR     = ":"           ,
          strSCORE_TITLE   = "Score:"      ,
          strSCORE_FINAL   = "Final "      ,
          strMINES_TITLE   = "Mines left:" ,
          strTIME_TITLE    = "Time:"       ,
          strINFO_READY    = "Ready to Go" ,
          strINFO_RUNNING  = "Running..."  ,
          
          strMINESMSG_ZERODENSITY = " (wow?)" ,
          strSCOREMSG_ZERODENSITY = "IDIOT!"  ,
          
          strINFOMSG_LOWDENSITY  = "Too easy :(" ,
          strINFOMSG_MIDDENSITY  = "Neat ;)"     ,
          strINFOMSG_HIGHDENSITY = "AWESOME!"    ,
          strINFOMSG_FULLDENSITY = "Having fun?" ,
          
          strINFOMSG_LOWSCORE = "A bit slow..." ,
          strINFOMSG_NEGSCORE = "VERY slow!"    ,
          
          strGAME_MENU     = "Game"               ,
          strSETTINGS_MENU = "Settings"           ,
          strRESET_BTN     = "RESET"              ,
          strGO_SND_BTN    = "Activate Sounds"    ,
          strNO_SND_BTN    = "De-activate Sounds" ,
          
          strSmall_ITEM    = "New Small Grid (" + GRID_SIZE_SMALL + " sqr/side)" ,
          strMedium_ITEM   = "New Medium Grid (" + GRID_SIZE_MED + " sqr/side)"  ,
          strLarge_ITEM    = "New Large Grid (" + GRID_SIZE_LARGE + " sqr/side)" ,
          strExit_ITEM     = "eXit"            ,
          strQMARKS_ITEM   = "Question Marks?" ,
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
  
  /** length, in pixels, of each side of the <code>MineField</code>, 
   *  = (current side length of each square) * (# of squares on each side of field) */
  int fieldDim ;
  
  boolean      loadedSound = false, playSounds = false ;
  AudioClip[]  soundFX ;
  String       strBaseURL ;
  
     MineField  mineField ;
  JLayeredPane  pane ; 
  MineSettings  settingsFrame ;
  
          JMenuBar   gameBar ;
          JMenu      gameMenu, settingsMenu ;
          JMenuItem  smallFieldItem, medFieldItem, largeFieldItem,
                     exitGameItem, newGameItem, showSettingsItem ;
  JCheckBoxMenuItem  qMarkItem ;
          KeyStroke  ks ;

  JPanel   scorePanel, infoPanel ;
  JButton  soundBtn, resetBtn ;
  Box      infoBox ;
  Label    infoMesg ; /* does NOT resize properly as a JLabel */

  JLabel   timeTitle, timeMesg ,
          scoreTitle, scoreMesg,
          minesTitle, minesMesg ;
    
  Timer clock ;
  MineListener listener ;

 /*
  *   METHODS
  * ============================================= */
  
  /** MAIN */ 
  public static void main( String[] args )
  {
    MineSweeper gameFrame = new MineSweeper( args.length > 0 
                                             ? Integer.parseInt(args[0]) : 0 );
    gameFrame.setVisible( true );
    
  }// main()

  public MineSweeper( int debug )
  {
    DEBUG_LEVEL = debug ;
    System.out.println( "MineSweeper.DEBUG_LEVEL = " + DEBUG_LEVEL );

    setTitle( VERSION );
    getContentPane().setBackground( COLOR_GAME_BKGRND );

    addWindowListener(
      new WindowAdapter()
        { public void windowClosing( WindowEvent we )
                      {
                        end(); // shut down Minesweeper/MineSettings
                        dispose(); // system call
                      }
        } );
    
    listener = new MineListener();
    
    fontSMALL  = new Font( strDEFAULT_TYPEFACE, Font.BOLD, FONT_SIZE_SMALL );
    fontMEDIUM = new Font( strDEFAULT_TYPEFACE, Font.BOLD, FONT_SIZE_MED   );
    fontLARGE  = new Font( strDEFAULT_TYPEFACE, Font.BOLD, FONT_SIZE_LARGE );
    
    mineField = new MineField( this );
    mineField.setFieldLength( GRID_SIZE_MED );
    mineField.setSquareLength( SQUARE_SIZE_MED );
    
    adjustSize();
    adjustScore();
    
    clock = new Timer( TIMER_DELAY_MSEC, listener );
    settingsFrame = new MineSettings( this );
    
    // create Menu Bar
    gameBar = new JMenuBar();
    setJMenuBar( gameBar ); 

    // Game Menu
    gameMenu = new JMenu( strGAME_MENU );
    
    smallFieldItem = new JMenuItem( strSmall_ITEM );
    ks = KeyStroke.getKeyStroke( KeyEvent.VK_S, Event.ALT_MASK ); 
    smallFieldItem.setAccelerator( ks ); 
    smallFieldItem.setMnemonic( 'S' ); 
    smallFieldItem.addActionListener( listener );

    medFieldItem = new JMenuItem( strMedium_ITEM );
    ks = KeyStroke.getKeyStroke( KeyEvent.VK_M, Event.ALT_MASK ); 
    medFieldItem.setAccelerator( ks ); 
    medFieldItem.setMnemonic( 'M' ); 
    medFieldItem.addActionListener( listener );

    largeFieldItem = new JMenuItem( strLarge_ITEM );
    ks = KeyStroke.getKeyStroke( KeyEvent.VK_L, Event.ALT_MASK ); 
    largeFieldItem.setAccelerator( ks ); 
    largeFieldItem.setMnemonic( 'L' ); 
    largeFieldItem.addActionListener( listener );

    exitGameItem = new JMenuItem( strExit_ITEM );
    ks = KeyStroke.getKeyStroke( KeyEvent.VK_X, Event.ALT_MASK ); 
    exitGameItem.setAccelerator( ks ); 
    exitGameItem.setMnemonic( 'X' ); 
    exitGameItem.addActionListener( listener );
    
    gameMenu.add( smallFieldItem  );
    gameMenu.add( medFieldItem );
    gameMenu.add( largeFieldItem  );
    gameMenu.addSeparator();
    gameMenu.add( exitGameItem   );
    
    // Settings Menu
    settingsMenu = new JMenu( strSETTINGS_MENU );
    
    qMarkItem = new JCheckBoxMenuItem( strQMARKS_ITEM, qMarksOn );
    ks = KeyStroke.getKeyStroke( KeyEvent.VK_Q, Event.ALT_MASK ); 
    qMarkItem.setAccelerator( ks ); 
    qMarkItem.setMnemonic( 'Q' ); 
    qMarkItem.addActionListener( listener );

    newGameItem = new JMenuItem( strNEWGAME_ITEM );
    ks = KeyStroke.getKeyStroke( KeyEvent.VK_F2, 0 ); 
    newGameItem.setAccelerator( ks ); 
    newGameItem.addActionListener( listener );

    showSettingsItem = new JMenuItem( strSETTINGS_ITEM );
    ks = KeyStroke.getKeyStroke( KeyEvent.VK_C, Event.ALT_MASK ); 
    showSettingsItem.setAccelerator( ks ); 
    showSettingsItem.setMnemonic( 'C' ); 
    showSettingsItem.addActionListener( listener );

    settingsMenu.add( qMarkItem );
    settingsMenu.add( newGameItem );
    settingsMenu.add( showSettingsItem );

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
    
    minesTitle = new JLabel( strMINES_TITLE );
    minesMesg = new JLabel( strZERO );
    setMinesMesg( mineField.getMines() );
    scorePanel.add( minesTitle );
    scorePanel.add( minesMesg );
    
    timeTitle = new JLabel( strTIME_TITLE );
    timeMesg = new JLabel( strZERO_TIME );
    scorePanel.add( timeTitle );
    scorePanel.add( timeMesg );
    
    getContentPane().add( scorePanel, "North" );    

    // set up JLayeredPane
    pane = new JLayeredPane();
    pane.add( mineField, JLayeredPane.DEFAULT_LAYER );
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
    mineField.shaded = false ;
    validate();// so the last square gets painted properly
    
    mineField.removeMouseListener( mineField );
    mineField.firstPress = true ;

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
    
    mineField.reDraw();
    setMinesMesg( mineField.getMines() );
    
    pane.repaint();
    
  }// newGame()

  protected void loadSound()
  {
    URL clip ;
    strBaseURL = "file:" + System.getProperty( "user.dir" ) 
                 + System.getProperty( "file.separator" );

    if( DEBUG_LEVEL > 0 )
      System.out.println( "MineSweeper.loadSound(): Base URL = " + strBaseURL );

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
        System.err.println( "MineSweeper.loadSound() Error: " + e );
      }
    
  }// loadSound()

  protected void playSound( int track ) 
  {
    if( !playSounds ) return ;
    
    if( track >= NUM_SOUND_CLIPS )
    {
      System.err.println( "MineSweeper.playSound(): track # " + track + " NOT VALID !" );
      return ;
    }
    
    if( soundFX[track] == null )
    {
      System.err.println( "MineSweeper.playSound(): "
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
    mineField.setSquareLength( len );

    if( len == SQUARE_SIZE_SMALL )
    {
      mineField.setFont( fontSMALL );
    }
    else if( len == SQUARE_SIZE_LARGE )
    {
      mineField.setFont( fontLARGE );
    }
    else // default: should only ever be == SQUARE_SIZE_MED
        mineField.setFont( fontMEDIUM );
    
    adjustSize();

  }// newSquareLength()
  
  /** Adjust the size of the game frame as the <code>Square</code> size 
   *  or number of <code>Squares</code> in the field have changed */
  protected void adjustSize()
  {
    fieldDim = mineField.getSquareLength() * mineField.getFieldLength();
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
    scoreMultiplier = (int)( mineField.getDensity() * 100 );
    maxScore = scoreMultiplier 
               * (int)Math.pow( (double)mineField.getFieldLength(), 2.0 ) ;
  }
  
  protected void adjustDensity( double density )
  {
    mineField.setDensity( density );
    mineField.repaint();
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
    minesMesg.setText( " " + Integer.toString(numMines) );
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
      if( currentScore == maxScore  &&  mineField.getMines() == 0 )
      {
        double d = mineField.getDensity();
        
        currentScore += ( maxScore - (100 - scoreMultiplier)*(seconds/2) );
        scoreTitle.setText( strSCORE_FINAL + strSCORE_TITLE );
        timeTitle.setText( strSCORE_FINAL + strTIME_TITLE );
    
        if( d == 0.0 )
        { 
          timeMesg.setText( strFULL_TIME );
          minesMesg.setText( strZERO + strMINESMSG_ZERODENSITY );
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
  
        mineField.paintArea(); // so the last flag is painted
        halt();
  
      }// if done
      
    }// if increase
    else
      {
        currentScore -= scoreMultiplier ;
        scoreMesg.setText( Integer.toString(currentScore) );
      }
      
  }// setScore()

  protected void showMineSettings()
  {
    settingsFrame.setLocation( scorePanel.getLocationOnScreen() );
    settingsFrame.setVisible( true ); // show() is DEPRECATED
  }

  protected void setMessage( String msg )
  {
    infoMesg.setText(msg);
  }    

  /** shut down <code>Minesweeper</code> & <code>MineSettings</code> (if open) */
  protected void end()
  {
    isRunning = false ;
    
    if( mineField.exploder.isRunning() )
      mineField.exploder.stop();
    
    if( settingsOpen )
      settingsFrame.dispose();
    
  }// end() 

  /*
   *   INNER CLASSES
   * ============================================= */
  
  /** Handles all the action events for <code>MineSweeper</code>.
   *  i.e. function as the Controller for the app   */
  class MineListener implements ActionListener
  {
    Object source ;
    
    public void actionPerformed( ActionEvent ae )
    {
      source = ae.getSource();
      if( source == clock )
      {
        runClock();
        if( DEBUG_LEVEL > 2 )
          System.out.println( "MineListener clock event" );
      }
      else // not the clock
      {
        if( DEBUG_LEVEL > 0 )
          System.out.println( "MineListener ActionCommand: "
                              + ae.getActionCommand()        );
        
        if( source == resetBtn || source == newGameItem )
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
        else if( source == exitGameItem )
        {
          end();
          dispose();
        }
        else if( source == smallFieldItem )
        {
          mineField.setFieldLength( GRID_SIZE_SMALL );
          newSize();
        }
        else if( source == medFieldItem )
        {
          mineField.setFieldLength( GRID_SIZE_MED );
          newSize();
        }
        else if( source == largeFieldItem )
        {
          mineField.setFieldLength( GRID_SIZE_LARGE );
          newSize();
        }
        else if( source == qMarkItem )
        {
          qMarksOn = qMarkItem.isSelected();
          
          if( !qMarksOn )
            mineField.clearQmarks();
        }
        else if( source == showSettingsItem )
            showMineSettings();
    
      }// not the clock

    }// MineListener.actionPerformed()
    
  }/* inner class MineListener */
    
}/* class MineSweeper */
