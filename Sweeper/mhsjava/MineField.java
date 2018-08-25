/*
 * Created on 20-Oct-06
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package mhsjava;

import java.awt.* ;
import java.awt.event.*;
import javax.swing.*;

/**
 * Contains the actual array of game <code>Squares</code>.
 * - handles the event code for the mouse actions to set flags, reveal mines, etc
 *
 * @author Mark Sattolo (based on code by Mats Antell)
 * @version 1.8 - 2006/010/31
 */
public class MineField extends JLabel implements MouseListener
{
 /*
  *   FIELDS
  * ============================================= */
  
  static final boolean  PLUS = true,   // indicate if score should increase
                       MINUS = false ; // or decrease

  /** # of animation 'frames' in the explosion */
  static final int EXPLODE_INDEX = 9 ;
  /** pause (msec) between each explosion 'frame' */
  static final int EXPLODE_DELAY = 120 ;
       
  /** #s 1-8 are for <code>Squares</code> adjacent to mines */
  static final int MINE     =  9 ,
                   FLAG     = 10 , 
                   QMARK    = 11 , 
                   BLANK    = 12 , 
                   BADCLEAR = 13 , 
                   FATAL    = 14 ;
        
  static final Color COLOR_BRIGHT  = Color.white , 
                     COLOR_BLAZE   = Color.yellow ,
                     COLOR_BLAST   = Color.orange ,
                     COLOR_BOLD    = Color.red ,
                     COLOR_SHADE   = Color.pink ,
                     COLOR_LIGHT   = Color.lightGray ,
                     COLOR_MEDIUM  = Color.gray ,
                     COLOR_DARK    = Color.darkGray ,
                     COLOR_DARKEST = Color.black ,
                     COLOR_BRUN    = new Color( 107, 69, 38 ),
                     
                 COLOR_FLAG_BKGRND = Color.green.brighter() ,
                   COLOR_FLAG_BELT = Color.red.darker() ,
                  COLOR_FLAG_BURST = Color.yellow ,
                  
                        COLOR_NUM1 = Color.blue ,
                        COLOR_NUM2 = Color.green.darker().darker() ,
                        COLOR_NUM3 = Color.red ,
                        COLOR_NUM4 = Color.black ,
                        COLOR_NUM5 = Color.magenta ,
                        COLOR_NUM6 = Color.yellow ,
                        COLOR_NUM7 = Color.cyan ,
                        COLOR_NUM8 = Color.orange.darker() ;

  static final String strGAMETEXT_BADCLEAR = "Uh-Oh!" ,
                      strGAMETEXT_EXPLODE  = "!! BOOM !!" ;
  
  int DEBUG_LEVEL ;
  
  double density = MineSweeper.STANDARD_DENSITY ;
  
  /** number of mines remaining to be flagged */
  int minesHidden ;
  /** number of <code>Squares</code> on each side of the 2D mine array */
  int fieldLength ;
  /** length of sides (in pixels) of each individual <code>Square</code> */
  int squareLength ;
  
  int destx, desty, paintLeft, paintRight, paintTop, paintBottom ;
  
  /** reference to the enclosing class */
  MineSweeper gameview ;
  /** 2D array of individual mine <code>Squares</code> */
  Square[][] mine2dArray ;
  
  boolean firstScan, destroyed, cleared, badClear, 
          firstPress = true, btnDown = false,
            paintAll = true, lightup = false, shaded = false ;
  
  /** generates action events used by the <code>ExplodeListener</code> */
  Timer exploder ; 
  /** inner class that calls <code>repaint()</code> to produce the animation explosion */
  ExplodeListener listener ;
  
 /*
  *   METHODS
  * ============================================= */
  
  public MineField( MineSweeper game )
  {
    DEBUG_LEVEL = game.DEBUG_LEVEL ;
    if( DEBUG_LEVEL > 0 )
      System.out.println( "MineField.DEBUG_LEVEL = " + DEBUG_LEVEL );
    
    gameview = game ;

    squareLength = MineSweeper.GRID_SIZE_MED ;
    setup( MineSweeper.GRID_SIZE_MED );
    
    setFont( game.fontMEDIUM );
    setLocation( MineSweeper.X_BORDER, MineSweeper.Y_BORDER/2 );
    
    listener = new ExplodeListener();
    exploder = new Timer( EXPLODE_DELAY, listener );
    
  }// Constructor
  
  /** Reset the mine array */
  protected void reDraw()
  {
    firstPress = true ;
    gameview.validate();
    
    if( exploder.isRunning() )
      exploder.stop();
    
    setup( fieldLength );
    
  }// reDraw()

  /** Set the initial variables */
  protected void setup( int len )
  {
    fieldLength = len ;
    setSize( fieldLength*squareLength, fieldLength*squareLength );

    minesHidden = 0 ;
    layMines();

    cleared = badClear = destroyed = false ;
    count();

    setBackground( COLOR_DARK );
    addMouseListener( this );
    
  }// setup()

  /** have <code>MineSweeper</code> play a sound */
  protected void playSound( int snd ) { gameview.playSound( snd ); }

  /** have <code>MineSweeper</code> adjust the score */
  protected void setScore( boolean plus ) { gameview.setScore( plus ); }

  /** have <code>MineSweeper</code> display a message */
  protected void setGameText( String info ) { gameview.infoMesg.setText( info ); }

  /** update the display of the number of mines remaining */
  protected void setMinesText( int num ) { gameview.setMinesMesg( num ); }

  /** return minesHidden */
  public int getMines() { return minesHidden ;}

  /** return length */
  public int getFieldLength() { return fieldLength ;}

  /** change array length (number of <code>Squares</code> per side)
   *  and adjust overall array size */
  protected void setFieldLength( int len )
  {
    fieldLength = len ;
    setSize( squareLength*fieldLength, squareLength*fieldLength );
  }

  /** return side */
  public int getSquareLength() { return squareLength ;}

  /** change length (pixels) of each individual <code>Square</code> 
   *  and adjust overall array size */
  protected void setSquareLength( int $side )
  {
    squareLength = $side ;
    setSize( squareLength*fieldLength, squareLength*fieldLength );
  }

  /** return density */
  public double getDensity() { return density ;}

  /** change current mine density */
  protected void setDensity( double dens ) { density = dens ;}

  /** Clear any active question marks if user has de-activated them */
  protected void clearQmarks()
  {
    boolean haveQmarks = false ;
    int i, j ;
    
    for( i=0; i<fieldLength; i++ )
      for( j=0; j<fieldLength; j++ )
      {
        if( mine2dArray[i][j].hasQmark() )
        {
          if( DEBUG_LEVEL > 1 )
            System.out.println( "MineField.clearQmarks(): Square[" + i + "][" + j
                                + "] has a question mark" );
          
          mine2dArray[i][j].setQmark( false );
          haveQmarks = true ;
        }
      }
    if( haveQmarks ) repaint();
    
  }// clearQmarks()

  /** Randomly seed the field with mines based on the current density */
  protected void layMines()
  {
    int i, j ;
    mine2dArray = new Square[ fieldLength ][ fieldLength ];
    
    for( i=0; i<fieldLength; i++ )
      for( j=0; j<fieldLength; j++ )
      {
        mine2dArray[i][j] = new Square();
        if( Math.random() <= density )
        {
          mine2dArray[i][j].arm();
          minesHidden++ ;
        }
      }
  }// layMines()

  /**
   * Set the minecount variable of each <code>Square</code>.
   * - this is the total number of mines in all neighbouring <code>Squares</code>
   */
  protected void count()
  {
    int u, v, i, j, detected = 0 ;
    
    for( i=0; i<fieldLength; i++ )
      for( j=0; j<fieldLength; j++ , detected=0 )
      {
        // co-ordinates of the 3x3 (or smaller if near an edge) grid
        // containing all the adjacent Squares
        int left   = ( i == 0 ? 0 : i-1 );
        int right  = ( i == fieldLength-1 ? i : i+1 );
        int top    = ( j == 0 ? 0 : j-1 );
        int bottom = ( j == fieldLength-1 ? j : j+1 );
        
        for( u=left; u <= right; u++ )
          for( v=top; v <= bottom; v++ )
            if( mine2dArray[u][v].hasMine() )
              detected++ ;
        
        mine2dArray[i][j].setMinecount( detected );
      }
      
  }// count()

  /**
   * Find and reveal all adjacent blank space and bordering numbered <code>Squares</code>.
   * - gets called <bold>recursively</bold> for blank <code>Squares</code> 
   *   (i.e. those NOT adjacent to any mines)
   */
  protected void clearOut( int u, int v )
  {
    if( !mine2dArray[u][v].isRevealed() )
      mine2dArray[u][v].setRevealed();

    /* cannot do this with a method because of the recursive call below in the loop
     * - fields left, right, top, & bottom may be altered by an intervening rec call */
    int left   = ( u == 0 ? 0 : u-1 );
    int right  = ( u == fieldLength-1 ? u : u+1 );
    int top    = ( v == 0 ? 0 : v-1 );
    int bottom = ( v == fieldLength-1 ? v : v+1 );
    
    if( firstScan )
    {
      firstScan   = false ;
      paintLeft   = left ;
      paintRight  = right ;
      paintTop    = top ;
      paintBottom = bottom ;
    }
    else
      {
        if( left   < paintLeft   ) paintLeft   = left ;
        if( right  > paintRight  ) paintRight  = right ;
        if( top    < paintTop    ) paintTop    = top ;
        if( bottom > paintBottom ) paintBottom = bottom ;
      }

    int i, j ;
    for( i=left; i <= right; i++ )
      for( j=top; j <= bottom; j++ )
        if( !mine2dArray[i][j].isRevealed() )
        {
          mine2dArray[i][j].setRevealed();
          setScore( PLUS );
          // recursive call if any adjacent squares are also blank
          if( mine2dArray[i][j].isBlank() )
            clearOut( i, j ); 
        }
  
  }// clearOut()

  // @see java.awt.event.MouseListener#mouseXXXed(java.awt.event.MouseEvent)
  public void mouseClicked( MouseEvent me ) {}
  public void mouseDragged( MouseEvent me ) {}
  public void mouseMoved  ( MouseEvent me ) {}
  public void mouseEntered( MouseEvent me ) {}
  public void mouseExited ( MouseEvent me ) {}
  
  /**
   * Set a flag, or reveal a <code>Square</code>, or clear out an area, etc.
   * - handles all user input from the various mouse buttons
   * 
   * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent) */
  public void mousePressed( MouseEvent evt )
  {
    int flagcount, minecount, revealcount, i, j ;
    
    int x = evt.getX() / squareLength ;
    int y = evt.getY() / squareLength ;

    if( DEBUG_LEVEL > 1 )
      System.out.println( "MineField.mousePressed(): Square[" + x + "][" + y + "]" );

    Square activeSqr = mine2dArray[x][y] ;

    if( firstPress )
      gameview.startClock();

    if( cleared )
      return ;// prevent a 2nd press from erasing the clearOut() dimensions
    
    int pressed = evt.getModifiers();

    /* Need at least Java version 1.4 to use the getModifiersEx() fxns */
    if( DEBUG_LEVEL > 0 )
      System.out.println( "MineField.mousePressed(): " 
                          + InputEvent.getModifiersExText(evt.getModifiersEx()) );

    /* already Revealed */
    if( activeSqr.isRevealed() )
    {
      if( !btnDown )
      {
        paintLeft = paintRight = x ;
        paintTop = paintBottom = y ;
        // light up the background
        lightup = true ;
        paintArea();
        btnDown = true ;
      }
      else
      /* MULTIPLE BUTTONS */
      //  = clear around the square
      //  - if the square is numbered and all adjacent mines are properly flagged,
      //    will reveal all adjacent squares - including showing ALL attached blank space
      if( !activeSqr.isBlank() )
      {
        lightup = shaded = false ;
        flagcount = revealcount = 0 ;          
        minecount = activeSqr.getMinecount();

        // this calculation is done a few times in MineSweeper, but CANNOT be done with
        // fields and a separate method because clearOut() is called in a loop below and 
        // parameters left, right, top, & bottom may be altered in the recursive call
        int left   = ( x == 0 ? 0 : x-1 );
        int right  = ( x == fieldLength-1 ? x : x+1 );
        int top    = ( y == 0 ? 0 : y-1 );
        int bottom = ( y == fieldLength-1 ? y : y+1 );

        for( i=left; i <= right; i++ )
        {
          for( j=top; j <= bottom; j++ )
          {
            if( mine2dArray[i][j].hasFlag() )
            {
              flagcount++ ;
              /* game over if try to clear in vicinity of an incorrect flag */
              if( !mine2dArray[i][j].hasMine() )
              {
                playSound( MineSweeper.BAD_CLEAR );
                setGameText( strGAMETEXT_BADCLEAR );
                paintLeft = i ;
                paintTop = j ;
                badClear = true ;
                return ;
              }
            }
            if( mine2dArray[i][j].isRevealed() )
              revealcount++ ;
          }
        }  
        // how many squares in our adjacency grid?
        int gridsize = ( right - left + 1 ) * ( bottom - top + 1 );
        // all adjacent squares except mines are already revealed
        boolean opengrid = ( revealcount == (gridsize - minecount) );

        if( flagcount == minecount  &&  !opengrid )
        {
          paintLeft   = left ;
          paintRight  = right ;
          paintTop    = top ;
          paintBottom = bottom ;

          for( i=left; i <= right ; i++ )
          {
            for( j=top; j <= bottom ; j++ )
            {
              if( !mine2dArray[i][j].hasMine() 
                  && !mine2dArray[i][j].isRevealed()
                )
              {
                mine2dArray[i][j].setRevealed();
                setScore( PLUS );
                if( mine2dArray[i][j].isBlank() )
                  clearOut( i, j );
              }
            }
          }
        }
  
        playSound( MineSweeper.CHIRP );
  
      }/* multiple buttons pressed */
      
    }
    else /* Square NOT yet revealed */
    {
      paintLeft = paintRight = x ;
      paintTop = paintBottom = y ;

      /* BUTTON1 or BUTTON2 */
      if( pressed != InputEvent.BUTTON3_MASK )
      {
        if( !activeSqr.hasFlag() )
        {
          shaded = true ;
          paintArea();

          if( activeSqr.hasMine() )
          {
            if( firstPress ) // give user a break on the first press
            {
              playSound( MineSweeper.FLAG );
              activeSqr.setFlag( true );
              setMinesText( --minesHidden );
            }
            else // game over 
            {
              playSound( MineSweeper.BOOM );
              setGameText( strGAMETEXT_EXPLODE );
              destroyed = true ;
              return ;
            }
          }
          else
            if( activeSqr.isBlank() )
            {
              firstScan = true ;
              clearOut( x, y );
              cleared = true ;
            }
            else
                activeSqr.setRevealed();

          if( density == 0 )
          {
            paintAll = true ;
            repaint();
          }

          setScore( PLUS );
        }
      // else do nothing if square has a flag
      }
      else /* BUTTON3 */
      {
        if( activeSqr.hasFlag() ) // remove flag -- add qmark if they are active
        {
          activeSqr.setFlag( false );
          if( gameview.qMarksOn )
            activeSqr.setQmark( true );
          
          minesHidden++ ;
          setScore( MINUS );
        }
        else
          if( activeSqr.hasQmark() )
          {
            // remove question mark
            activeSqr.setQmark( false );
          }
          else
            { // add flag
              playSound( MineSweeper.FLAG );
              activeSqr.setFlag( true );
              minesHidden-- ;
              setScore( PLUS );
            }

        setMinesText( minesHidden );
      }

    } /* NOT revealed */
    
    if( firstPress ) firstPress = false ;
  
  }// mousePressed()

  /** Call to repaint the area depending on changes from the preceding mouse press */
  public void mouseReleased( MouseEvent evt )
  {
    btnDown = cleared = false ;
    lightup = shaded = false ;

    if( DEBUG_LEVEL > 0 )
      System.out.println( "MineField.mouseReleased(): " 
                          + InputEvent.getModifiersExText(evt.getModifiersEx()) );

    if( destroyed || badClear )
    {
      destx = paintLeft ;
      desty = paintTop ;
    
      if( DEBUG_LEVEL > 1 )
        System.out.println( "MineField.mouseReleased(): destx = " + destx 
                            + "; desty = " + desty );
                            
      if( destroyed ) // activate the explosion
      {
        listener.init();
        exploder.start();
      }

      paintAll = true ;
      repaint();
      
      gameview.halt();
    }
    else
        paintArea();

  }// mouseReleased()

  /**
   * Need to refresh only a certain portion of the array.
   * - calls <code>repaint( left, top, width, depth )</code>;
   */
  protected void paintArea()
  {
    if( DEBUG_LEVEL > 1 )
      System.out.println( "MineField.paintArea(): Left = " + paintLeft + "; Right = " + paintRight
                          + "; Top = " + paintTop + "; Bottom = " + paintBottom );

    paintAll = false ;
    repaint( paintLeft*squareLength, paintTop*squareLength,
             (paintRight-paintLeft+1)*squareLength, (paintBottom-paintTop+1)*squareLength );
    
  }// paintArea()

  /**
   * Refresh the graphical representation of the mine array.
   * - called by repaint()
   */
  public void paint( Graphics page )
  {
    if( DEBUG_LEVEL > 0 )
      System.out.println( "MineField.paint(): Left = " + paintLeft + "; Right = " + paintRight
                          + "; Top = " + paintTop + "; Bottom = " + paintBottom );

    if( paintAll )
    {
      paintLeft = 0 ;
      paintRight = fieldLength-1 ;
      paintTop = 0 ;
      paintBottom = fieldLength-1 ;
    }

    int i, j ;
    for( i=paintLeft; i <= paintRight; i++ )
      for( j=paintTop; j <= paintBottom; j++ )
      {
        if( mine2dArray[i][j].isRevealed() )
          reveal( i*squareLength, j*squareLength, mine2dArray[i][j].getMinecount(), page );
        else if( mine2dArray[i][j].hasFlag() )
          reveal( i*squareLength, j*squareLength, FLAG, page );
        else if( mine2dArray[i][j].hasQmark() )
          reveal( i*squareLength, j*squareLength, QMARK, page );
        else 
            reveal( i*squareLength, j*squareLength, BLANK, page );
      
        if( destroyed || badClear ) /* show all the mines */
          if( mine2dArray[i][j].hasMine() )
            reveal( i*squareLength, j*squareLength, MINE, page );
      }
    
    if( badClear )
      reveal( destx*squareLength, desty*squareLength, BADCLEAR, page ); // indicate bad flag

    if( destroyed )
      reveal( destx*squareLength, desty*squareLength, FATAL, page ); // blow up

    paintAll = true ;
  
  }// paint()
      
  /**
   * Draw individual <code>Squares</code> depending on the content.
   * i.e. different numbers of adjacent mines, revealed or not, flag, blow-up, etc
   * - called ONLY by <code>paint()</code>
   */
  protected void reveal( int xc, int yc, int type, Graphics page )
  {
    int sl = squareLength ;
    
    if( DEBUG_LEVEL > 2 )
      System.out.println( "MineField.reveal(): xc = " + xc + "; yc = " + yc 
                          + "; type = " + type );

    if( lightup )
      page.setColor( COLOR_BLAZE );
    else if( shaded )
      {
        page.setColor( COLOR_SHADE );
        type = 0 ;
      }
    else
        page.setColor( COLOR_LIGHT );

    page.fillRect( xc, yc, squareLength, squareLength ); // background

    page.setColor( COLOR_MEDIUM );
    page.drawLine( xc     , yc+sl-1, xc+sl  , yc+sl-1 ); // bottom
    page.drawLine( xc+sl-1, yc     , xc+sl-1, yc+sl   ); // right

    page.setColor( COLOR_DARK );
    page.drawLine( xc, yc, xc+sl, yc    ); // top
    page.drawLine( xc, yc, xc   , yc+sl ); // left
    
    switch( type )
    {
      // 1 - 8 = # of adjacent mines
      case 1:
                page.setColor( COLOR_NUM1 );
                break ;
      case 2:
                page.setColor( COLOR_NUM2 );
                break ;
      case 3:
                page.setColor( COLOR_NUM3 );
                break ;
      case 4:
                page.setColor( COLOR_NUM4 );
                break ;
      case 5:
                page.setColor( COLOR_NUM5 );
                break ;
      case 6:
                page.setColor( COLOR_NUM6 );
                break ;
      case 7:
                page.setColor( COLOR_NUM7 );
                break ;
      case 8:
                page.setColor( COLOR_NUM8 );
                break ;
    
      case MINE:
                drawSquare( xc, yc, page ); 
                page.setColor( COLOR_DARK );
                page.fillOval( xc + sl/8, yc + sl/8, 3*sl/4, 3*sl/4 );
                page.setColor( COLOR_DARKEST );
                page.fillOval( xc + sl/8 + 1, yc + sl/8 + 1, 3*sl/4 - 2, 3*sl/4 - 2 );
                page.setColor( COLOR_DARK );
                page.fillOval( xc + sl/3 - 2, yc + sl/3 - 2, sl/8 + 4, sl/8 + 4 );
                page.setColor( COLOR_MEDIUM );
                page.fillOval( xc + sl/3 - 1, yc + sl/3 - 1, sl/8 + 2, sl/8 + 2 );
                page.setColor( COLOR_BRIGHT );
                page.fillOval( xc + sl/3, yc + sl/3, sl/8, sl/8 );
                break ;
    
      case FLAG:
                drawSquare( xc, yc, page );
                page.setColor( COLOR_FLAG_BKGRND );
                page.fillOval( xc + sl/8, yc + sl/8, 3*sl/4, 3*sl/4 );
                page.setColor( COLOR_FLAG_BELT );
                page.fillRect( xc + sl/8, yc + 3*sl/8, 3*sl/4, sl/4 );
                page.setColor( COLOR_FLAG_BURST );
                page.drawString( "*", xc + 3*sl/8, yc + 7*sl/8 );
                break ;
    
      case QMARK:
                drawSquare( xc, yc, page );
                page.setColor( COLOR_DARKEST );
                page.drawString( "?", xc + sl/4, yc + 3*sl/4 );
                break ;
    
      case BLANK:
                drawSquare( xc, yc, page );
                break ;
    
      case BADCLEAR:
                drawSquare( xc, yc, page ); 
                page.setColor( COLOR_DARK );
                page.fillOval( xc + sl/8, yc + sl/8, 3*sl/4, 3*sl/4 );
                page.setColor( COLOR_DARKEST );
                page.fillOval( xc + sl/8 + 1, yc + sl/8 + 1, 3*sl/4 - 2, 3*sl/4 - 2 );
                page.setColor( COLOR_DARK );
                page.fillOval( xc + sl/3 - 2, yc + sl/3 - 2, sl/8 + 4, sl/8 + 4 );
                page.setColor( COLOR_MEDIUM );
                page.fillOval( xc + sl/3 - 1, yc + sl/3 - 1, sl/8 + 2, sl/8 + 2 );
                page.setColor( COLOR_BRIGHT );
                page.fillOval( xc + sl/3, yc + sl/3, sl/8, sl/8 );
                page.setColor( COLOR_BOLD );
                page.drawString( "X", xc + sl/4, yc + 3*sl/4 );
                break ;
    
      case FATAL: // animated explosion if user clicked on a mine
      {
        if( DEBUG_LEVEL > 1 )
          System.out.println( "MineField.reveal(): case FATAL" );
    
        int index = listener.index % EXPLODE_INDEX ;
    
        switch( index )
        {
          case 0 :
            page.setColor( COLOR_BRIGHT );
            page.fillOval( xc + sl/8  , yc + sl/8, sl - sl/4, sl - sl/4 );
            page.fillOval( xc - sl/3  , yc - sl/3, sl/5     , sl/3      );
            page.fillOval( xc + 4*sl/3, yc + sl/3, sl/5     , sl/3      );
            break ;
          case 1 :
            page.setColor( COLOR_BLAZE );
            page.fillOval( xc + sl/8, yc + sl/8  , sl - sl/4, sl - sl/4 );
            page.fillOval( xc - sl/2, yc - 2*sl/5, sl/10    , sl/10     );
            page.fillOval( xc + 2*sl, yc + sl/3  , sl/5     , sl/3      );
      
            page.setColor( COLOR_MEDIUM );
            page.fillOval( xc - sl/3  , yc - sl/3, sl/5, sl/3 );
            page.fillOval( xc + 4*sl/3, yc + sl/3, sl/5, sl/3 );
            break ;
          case 2 :
            page.setColor( COLOR_BOLD );
            page.fillOval( xc + sl/8  , yc + sl/8  , sl - sl/4, sl - sl/4 );
            page.fillOval( xc - 2*sl/3, yc - 3*sl/5, sl/10    , sl/10     );
            page.fillOval( xc + 9*sl/4, yc + sl/3  , sl/5     , sl/3      );
      
            page.setColor( COLOR_BLAZE );
            page.fillOval( xc + sl, yc     , sl/5, sl/5 );
            page.fillOval( xc     , yc + sl, sl/5, sl/5 );
            page.fillOval( xc + sl, yc + sl, sl/5, sl/5 );
      
            page.setColor( COLOR_MEDIUM );
            page.fillOval( xc - sl/2, yc - 2*sl/5, sl/10, sl/10 );
            page.fillOval( xc + 2*sl, yc + sl/3  , sl/5 , sl/3  );
            break ;
          case 3 :
            page.setColor( COLOR_DARKEST );
            page.fillOval( xc + sl/8, yc + sl/8, sl - sl/4, sl - sl/4 );
      
            page.setColor( COLOR_MEDIUM );
            page.fillOval( xc - 2*sl/3, yc - 3*sl/5, sl/10, sl/10 );
            page.fillOval( xc + 9*sl/4, yc + sl/3  , sl/5 , sl/3  );
      
            page.setColor( COLOR_BRIGHT );
            drawSparks( xc, yc, page );
      
            page.fillOval( xc + 5*sl/4, yc - sl/4  , sl/5, sl/5 );
            page.fillOval( xc - sl/4  , yc + 5*sl/4, sl/5, sl/5 );
            page.fillOval( xc + 5*sl/4, yc + 5*sl/4, sl/5, sl/5 );
      
            page.setColor( COLOR_MEDIUM );
            page.fillOval( xc + sl, yc     , sl/5, sl/5 );
            page.fillOval( xc     , yc + sl, sl/5, sl/5 );
            page.fillOval( xc + sl, yc + sl, sl/5, sl/5 );
            break ;
          case 4 :
            page.setColor( COLOR_BLAZE );
            drawSparks( xc, yc, page );
      
            page.fillOval( xc + 6*sl/4, yc - 2*sl/4, sl/7, sl/7 );
            page.fillOval( xc - 2*sl/4, yc + 6*sl/4, sl/7, sl/7 );
            page.fillOval( xc + 6*sl/4, yc + 6*sl/4, sl/7, sl/7 );
      
            page.setColor( COLOR_MEDIUM );
            page.fillOval( xc + 5*sl/4, yc - sl/4  , sl/5, sl/5 );
            page.fillOval( xc - sl/4  , yc + 5*sl/4, sl/5, sl/5 );
            page.fillOval( xc + 5*sl/4, yc + 5*sl/4, sl/5, sl/5 );
            break ;
          case 5 :
            page.setColor( COLOR_BLAST );
            drawSparks( xc, yc, page );
      
            page.fillOval( xc + 7*sl/4, yc - 3*sl/4, sl/10, sl/10 );
            page.fillOval( xc - 3*sl/4, yc + 7*sl/4, sl/10, sl/10 );
            page.fillOval( xc + 7*sl/4, yc + 7*sl/4, sl/10, sl/10 );
      
            page.setColor( COLOR_MEDIUM );
            page.fillOval( xc + 6*sl/4, yc - 2*sl/4, sl/7,  sl/7 );
            page.fillOval( xc - 2*sl/4, yc + 6*sl/4, sl/7,  sl/7 );
            page.fillOval( xc + 6*sl/4, yc + 6*sl/4, sl/7,  sl/7 );
            break ;
          case 6 :
            page.setColor( COLOR_BOLD );
            drawSparks( xc, yc, page );
      
            page.setColor( COLOR_MEDIUM );
            page.fillOval( xc + 7*sl/4, yc - 3*sl/4, sl/10, sl/10 );
            page.fillOval( xc - 3*sl/4, yc + 7*sl/4, sl/10, sl/10 );
            page.fillOval( xc + 7*sl/4, yc + 7*sl/4, sl/10, sl/10 );
            break ;
          case 7 :
            page.setColor( COLOR_BRUN );
            drawSparks( xc, yc, page );
            break ;
            
          default: // should always be == 8
            page.setColor( COLOR_BOLD.darker() );
            drawSparks( xc, yc, page );
              
            // turn off the animation
            exploder.stop();
            
        }// switch( index )
  
      }// end FATAL
      
    }// switch( type )
    
    if( type < MINE && type > 0 )
    {
      String number = String.valueOf( type );
      page.drawString( number, xc + sl/4, yc + 3*sl/4 );
    }
    
  }// reveal()

  /** Draw a basic unrevealed <code>Square</code> with edge hilites
   * 
   *  @param xc    the <i>x</i> coordinate of the upper left corner
   *  @param yc    the <i>y</i> coordinate of the upper left corner
   *  @param page  Graphics object reference
   */
  protected void drawSquare( int xc, int yc, Graphics page )
  {
    int sl = squareLength ;
    
    page.setColor( COLOR_MEDIUM );
    page.fillRect( xc, yc, squareLength, squareLength );
    
    page.setColor( COLOR_DARKEST );
    page.drawLine( xc     , yc+sl-1, xc+sl  , yc+sl-1 ); // bottom
    page.drawLine( xc+sl-1, yc     , xc+sl-1, yc+sl   ); // right
    
    page.setColor( COLOR_LIGHT );
    page.drawLine( xc, yc, xc+sl-1, yc      ); // top
    page.drawLine( xc, yc, xc     , yc+sl-1 ); // left
  
  }// drawSquare()
  
  /** Part of the animation explosion sequence 
   * 
   *  @param xc    the <i>x</i> coordinate of the upper left corner
   *  @param yc    the <i>y</i> coordinate of the upper left corner
   *  @param page  Graphics object reference
   */
  protected void drawSparks( int xc, int yc, Graphics page )
  {
    int sl = squareLength ;
    
    /*
     *  fillOval( x, y, width, height )
     *    -fill an oval bounded by the specified rectangle with the current color
     * 
     *  @param x       the <i>x</i> coordinate of the upper left corner 
     *                   of the oval to be filled.
     *  @param y       the <i>y</i> coordinate of the upper left corner 
     *                   of the oval to be filled.
     *  @param width   the width of the oval to be filled.
     *  @param height  the height of the oval to be filled.
     *  
     *  @see also java.awt.Graphics#drawOval
     */
    page.fillOval( xc + sl/2    , yc + sl/8    , sl/10, sl/10 );
    page.fillOval( xc + 7*sl/10 , yc + sl/2    , sl/10, sl/10 );
    page.fillOval( xc + 2*sl/10 , yc + 7*sl/10 , sl/10, sl/10 );
    page.fillOval( xc + sl/3    , yc + 3*sl/5  , sl/10, sl/10 );
    page.fillOval( xc + sl/4    , yc + sl/2    , sl/10, sl/10 );
    page.fillOval( xc + sl/8    , yc + sl/8    , sl/10, sl/10 );
    page.fillOval( xc + sl/4    , yc + 2*sl/3  , sl/10, sl/10 );
    page.fillOval( xc + 7*sl/10 , yc + 2*sl/5  , sl/10, sl/10 );
    page.fillOval( xc + sl/2    , yc + sl/5    , sl/10, sl/10 );
    page.fillOval( xc + 3*sl/4  , yc + sl/2    , sl/10, sl/10 );
    page.fillOval( xc + sl/3    , yc + sl/3    , sl/3 , sl/3  );
    page.fillOval( xc + 3*sl/5  , yc + sl/3    , sl/10, sl/10 );
    page.fillOval( xc + 2*sl/5  , yc + 2*sl/5  , sl/10, sl/10 );
    page.fillOval( xc + sl/2    , yc + sl/5    , sl/10, sl/10 );
    page.fillOval( xc + 2*sl/5  , yc + 1*sl/5  , sl/10, sl/10 );
    page.fillOval( xc + 3*sl/5  , yc + 4*sl/5  , sl/10, sl/10 );
  
  }// drawSparks()


  /*
   *   MineField INNER CLASSES
   * =========================== */
  
  /**
  * The data structure behind every square in the minefield.
  * - class <code>MineField</code> uses a 2D array of <code>Squares</code>,
  *   i.e. Square[][]
  */
  class Square
  {
    protected boolean hasMine = false, isRevealed = false,
                      hasFlag = false,   hasQmark = false ;

    /** The total number of mines in all neighbouring <code>Squares</code> */
    protected int mineCount ;

    public Square() { }

    /** Set the minecount variable of an individual <code>Square</code> 
     *  @param count  int */
    protected void setMinecount( int count ) { mineCount = count ;}

    /** return mineCount  */
    public int getMinecount() { return mineCount ;}

    /** No adjacent mines  */
    public boolean isBlank() { return mineCount == 0 ;}

    /** True if this <code>Square</code> has a mine, otherwise false  */
    public boolean hasMine() { return hasMine ;}

    /** Plants a mine in this <code>Square</code>  */
    protected void arm() { hasMine = true ;}

    /** Indicates whether a <code>Square</code> has been revealed or not  */
    public boolean isRevealed() { return isRevealed ;}

    /** Set value of isRevealed to TRUE  */
    protected void setRevealed() { isRevealed = true ;}

    /** Indicates whether a <code>Square</code> has a flag placed
     *  (indicating a mine underneath)  */
    public boolean hasFlag() { return hasFlag ;}

    /** Set value of hasFlag to parameter value
     *  @param flag  boolean  */
    protected void setFlag( boolean flag ) { hasFlag = flag ;}

    /** Indicates whether a <code>Square</code> has a question mark
     *  (indicating doubt about a mine placement)  */
    public boolean hasQmark() { return hasQmark ;}

    /** Set value of hasQmark to parameter value
     *  @param qmark  boolean  */
    protected void setQmark( boolean qmark ) { hasQmark = qmark ;}

  }/* inner class Square */

  /** Listens for the <code>Timer</code> events which drive the animation explosion */
  class ExplodeListener implements ActionListener
  {
    int index ;
    
    void init() { index = 0 ; }
    
    public void actionPerformed( ActionEvent ae )
    {
      if( ae.getSource() == exploder )
      {
        setMinesText( ++index );
  
        if( DEBUG_LEVEL > 0 )
          System.out.println( "ExplodeListener event: index = " + index );

        repaint();
      }
      else
          System.err.println( "ExplodeListener invalid event source!" );
    
    }// ExplodeListener.actionPerformed()

  }/* inner class ExplodeListener */

}/* class MineField */
