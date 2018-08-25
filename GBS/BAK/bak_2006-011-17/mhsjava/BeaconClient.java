/*
 * Created on 31-Oct-06
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package mhsjava;

import java.awt.* ;
import java.awt.event.* ;
import java.io.IOException;

import javax.swing.*;

/**
 * @author Mark Sattolo
 * @version 1.3  Nov.11/2006
 */
public class BeaconClient extends JPanel implements MouseListener, KeyListener
{
  /*
   *   FIELDS
   * ============================================= */
  
  static final int
           BASE_KEYCODE_INDEX = 48 , // ASCII for '0'
    
              SQR_VALUE_BLANK = 0  , // Squares with this value are BLANK
            DEFAULT_GRID_SIZE = 9  , // # of squares per side of the grid
          DEFAULT_SQUARE_SIZE = 48 , // length in pixels of each side of a Square 
                  
                    FONT_SIZE = DEFAULT_SQUARE_SIZE/2 + 6 ,
                    
                     X_BORDER = 80 ,  // extra space around the mine field
                     Y_BORDER = 60  ; // for the labels, buttons, etc

  static final String strDEFAULT_TYPEFACE = "Arial" ;

  /** different types of <code>Squares</code> */
  static final int
        SQR_TYPE_FIXED  =  0 , // starting GIVENs
        SQR_TYPE_OPEN   =  SQR_TYPE_FIXED  + 1 , // starting BLANKs
        SQR_TYPE_TEMP   =  SQR_TYPE_OPEN   + 1 , // 2 or 3 POSSIBLE values
        SQR_TYPE_BAD    =  SQR_TYPE_TEMP   + 1 , // conflicting values after a CHECK action
        SQR_TYPE_SOLVED =  SQR_TYPE_BAD    + 1 , // confirmed as proper value after a SOLVE action
        SQR_TYPE_FOCUS  =  SQR_TYPE_SOLVED + 1 , // active Square
         NUM_SQR_TYPES  =  SQR_TYPE_FOCUS  + 1 ; // MUST BE LAST ENTRY
        
  /** different colors for different types of <code>Squares</code>
   *  - MUST have the same number of entries as the previous array
   */
  static final Color[] TYPE_COLORS =
                  {
                     Color.black           , // COLOR_FIXED
                     Color.blue.darker()   , // COLOR_GUESS 
                     Color.green.darker()  , // COLOR_TEMP 
                     Color.red.darker()    , // COLOR_BAD  
                     Color.yellow.darker() , // COLOR_SOLVED  
                     Color.red.brighter()    // COLOR_FOCUS
                  };
  
  static final Color
                     COLOR_LIGHT  =  Color.lightGray ,
                     COLOR_GRID   =  Color.gray ,
                     COLOR_DARK   =  Color.darkGray ;

  static final Color[] ZONE_COLORS =
                   {
                     Color.PINK ,
                     Color.GREEN.brighter() ,
                     new Color( 141,51,231 ),// violet
                     Color.YELLOW ,
                     Color.GRAY ,
                     Color.CYAN ,
                     new Color( 183,123,63 ), // brown
                     Color.MAGENTA ,
                     Color.ORANGE  
                   };    
                       
  int DEBUG_LEVEL ;
  
  Font myFont ;

  private double density = 0.38 ;

  private boolean paintAll = true  ;

  public boolean firstPress = true ;

  private int blankSquares ;

  /** number of <code>Squares</code> on each side of the grid */
  int gridLength ;
  /** length of sides (in pixels) of each individual <code>Square</code> */
  int squareLength ;
  
  Point activeSqrPosn ;
  
  // may want to use a paintArea() type fxn at some point...
  int destx, desty, paintLeft, paintRight, paintTop, paintBottom ;
  
  /** reference to the enclosing class */
  BeaconServer gameview ;
  /** reference to the enclosing class */
  BeaconLoader gameloader ;

  /** 2D array of individual <code>Squares</code> */
  Square[][] grid2dArray ;
  /** array of <code>Zones</code> */
  Zone[] zoneArray ;
  
  /*
   *   METHODS
   * ============================================= */
   
  /**
   *  This is the default constructor
   */
  public BeaconClient( BeaconServer frame, BeaconLoader ldr )
  {
    super();
    
    DEBUG_LEVEL = frame.DEBUG_LEVEL ;
    if( DEBUG_LEVEL > 0 )
      System.out.println( "BeaconClient.DEBUG_LEVEL = " + DEBUG_LEVEL );
    
    gameview = frame ;
    gameloader = ldr ;
    
    init();
  }

  private void init()
  {
    if( DEBUG_LEVEL > 0 )
      System.out.println( "BeaconClient.init()" );
    
    // LATER, offer choices of grid & square size
    squareLength = DEFAULT_SQUARE_SIZE ;
    gridLength = DEFAULT_GRID_SIZE ;
    setSize( gridLength*squareLength, gridLength*squareLength );
    
    zoneArray = new Zone[ gridLength ];
    
    grid2dArray = new Square[ gridLength ][ gridLength ];
    int i, j ;
    for( i=0; i<gridLength; i++ )
      for( j=0; j<gridLength; j++ )
        grid2dArray[i][j] = new Square();
    
    activeSqrPosn = new Point( 0, 0 );
    
    addMouseListener( this );
    addKeyListener( this );
    
    setLocation( X_BORDER, Y_BORDER/2 );
    setBackground( BeaconServer.COLOR_GAME_BKGRND );
    
    myFont = new Font( strDEFAULT_TYPEFACE, Font.BOLD, FONT_SIZE );
    setFont( myFont );

    setFocusable( true );
    setVisible( true );
  
  }// init()
  
  /** Reset the 2d array */
  protected void newGrid()
  {
    if( DEBUG_LEVEL > 0 )
      System.out.println( "BeaconClient.newGrid()" );

    firstPress = true ;
    blankSquares = gridLength * gridLength ;
    
    Square activeSqr ;
    int[][] ldgame = gameloader.getGame( 0, 0 );
    
    int row, col ;
    for( row=0; row<gridLength; row++ )
      for( col=0; col<gridLength; col++ )
      {
        activeSqr = grid2dArray[row][col] ;
        
        activeSqr.setMyZone( 3*(row/3) + col/3 );
        activeSqr.setMyRow( row );
        activeSqr.setMyCol( col );
        activeSqr.setOrder( 0 );
        activeSqr.setActive( false );
        
        if( ldgame != null )
        {
          activeSqr.setValue( ldgame[row][col] );
          activeSqr.setType( ldgame[row][col] 
                             == SQR_VALUE_BLANK ? SQR_TYPE_OPEN : SQR_TYPE_FIXED );
          
          if( activeSqr.getType() == BeaconClient.SQR_TYPE_FIXED )
            --blankSquares ;
        }
        else // getGame() didn't work ??
        {  
          if( Math.random() <= density )
          {
            activeSqr.setType( BeaconClient.SQR_TYPE_FIXED );
            activeSqr.setValue( (int)(1 + (Math.random() * gridLength)) );
            --blankSquares ;
          }          
          else
            {
              activeSqr.setType( BeaconClient.SQR_TYPE_OPEN );
              activeSqr.setValue( 0 );
            }
        }
      }
    gameview.updateSquaresDisplay( blankSquares );
    gameview.validate();
    
  }// newGrid()

  public int blanks()
  {
    return blankSquares ;
  }
  
  /**
   * Refresh the graphical representation of the grid.
   * - called by repaint()
   */
  public void paint( Graphics page )
  {
    if( DEBUG_LEVEL > 1 )
      System.out.println( "BeaconClient.paint(): Left = " + paintLeft
                          + "; Right = " + paintRight
                          + "; Top = " + paintTop + "; Bottom = " + paintBottom );

    if( paintAll )
    {
      paintLeft = 0 ;
      paintRight = gridLength-1 ;
      paintTop = 0 ;
      paintBottom = gridLength-1 ;
    }

    int row, col ;
    if( page != null )
    {
      for( row=paintTop; row <= paintBottom; row++ )
        for( col=paintLeft; col <= paintRight; col++ )
        {
          paintSquare( col*squareLength, row*squareLength, grid2dArray[row][col], page );
        }
    }
    else if( DEBUG_LEVEL > 0 )
        System.err.println( "BeaconClient.paint() PROBLEM: Graphics page is NULL!" );
    
    paintAll = true ;
  
  }// paint()
      
  /**
   * Draw individual <code>Squares</code> depending on the content.
   * i.e. whether a regular guess, or SQR_TYPE_TEMP, SQR_TYPE_BAD, etc
   * - called ONLY by <code>paint()</code>
   */
  protected void paintSquare( int horiz, int vert, Square s, Graphics page )
  {
    int type  = s.getType() ;
    int value = s.getValue();
    
    if( DEBUG_LEVEL > 2 )
      System.out.println( "BeaconClient.paintSquare(): horizontal co-ord = " + horiz 
                          + "; vertical co-ord = " + vert 
                          + "; type = " + type + "; value = " + value );

    int sl = squareLength ;
    
    paintBlankSquare( horiz, vert, s, page );

    if( type == SQR_TYPE_TEMP )
    {
      page.setColor( TYPE_COLORS[SQR_TYPE_TEMP] );
    }
    else if( type == SQR_TYPE_FIXED )
    {
      page.setColor( TYPE_COLORS[SQR_TYPE_FIXED] );
    }
    else
        page.setColor( TYPE_COLORS[SQR_TYPE_OPEN] );
    
    if( value > 0 )
    {
      String number = String.valueOf( value );
      page.drawString( number, horiz + sl/4, vert + 3*sl/4 );
    }
    
    if( type == SQR_TYPE_BAD )
    {
      page.setColor( TYPE_COLORS[SQR_TYPE_BAD] );
      page.drawString( "X", horiz + sl/4, vert + 3*sl/4 );
    }
    
  }// paintSquare()

  /** Draw a basic unrevealed <code>Square</code> with edge hilites
   * 
   *  @param horiz  the <i>x</i> coordinate of the upper left corner
   *  @param vert   the <i>y</i> coordinate of the upper left corner
   *  @param s      the <code>Square</code> to draw
   *  @param page   Graphics object reference
   */
  protected void paintBlankSquare( int horiz, int vert, Square s, Graphics page )
  {
    if( DEBUG_LEVEL > 3)
      System.out.println( "BeaconClient.paintBlankSquare()" );

    int sl = squareLength ;
    
    if( s.isActive() )
      page.setColor( TYPE_COLORS[SQR_TYPE_FOCUS] );
    else
        page.setColor( ZONE_COLORS[s.getMyZone()] );

    page.fillRect( horiz, vert, squareLength, squareLength );
    
    page.setColor( COLOR_DARK );
    page.drawLine( horiz     , vert+sl-1, horiz+sl  , vert+sl-1 ); // bottom
    page.drawLine( horiz+sl-1, vert     , horiz+sl-1, vert+sl   ); // right
    
    page.setColor( COLOR_LIGHT );
    page.drawLine( horiz, vert, horiz+sl-1, vert      ); // top
    page.drawLine( horiz, vert, horiz     , vert+sl-1 ); // left
  
  }// paintBlankSquare()

  /*
   *   ACTION EVENTS
   * ============================================= */
    
  /**
   * Start the clock if necessary; give focus to active <code>Square</code>
   *
   * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
   */
  public void mouseClicked( MouseEvent me )
  {
    if( DEBUG_LEVEL > 0 )
      System.out.println( "BeaconClient.mouseClicked()" );

    int col = me.getX() / squareLength ; // horizontal co-ordinate
    int row = me.getY() / squareLength ; // vertical co-ordinate

    if( DEBUG_LEVEL > 0 )
      System.out.println( "  Clicked Square[" + row + "][" + col + "]" );

    if( firstPress )
    {
      gameview.startClock();
      firstPress = false ;
    }

    if( row < gridLength
        && col < gridLength
        &&  grid2dArray[row][col].getType() != SQR_TYPE_FIXED
      )
    {
      grid2dArray[activeSqrPosn.x][activeSqrPosn.y].setActive( false );
      grid2dArray[row][col].setActive( true );

      activeSqrPosn.x = row ;
      activeSqrPosn.y = col ;

      gameview.repaint();
    }

    boolean haveFocus = isFocusOwner();
    if( !haveFocus )
      haveFocus = requestFocusInWindow();
    if( DEBUG_LEVEL > 1 )
    {
      if( !haveFocus )
        System.out.print( "  DO NOT" );
      System.out.println( "  Have Focus!" );
    }
    
  }// mouseClicked()

  /** @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent) */
  public void mousePressed(MouseEvent e)
  { }
  /** @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent) */
  public void mouseReleased(MouseEvent e)
  { }
  /** @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent) */
  public void mouseEntered(MouseEvent e)
  { }
  /** @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent) */
  public void mouseExited(MouseEvent e)
  { }


  /**
   *  Enter the typed value into the <code>Square</code>
   *  @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
   */
  public void keyTyped( KeyEvent ke )
  {
    char input ;
    int  intInput ;
    int  newValue, origValue ;
    
    input = ke.getKeyChar();
    intInput = (int)input ;
    
    origValue = grid2dArray[activeSqrPosn.x][activeSqrPosn.y].getValue();
    newValue = intInput - BASE_KEYCODE_INDEX ;
    
    if( DEBUG_LEVEL > 0 )
      System.out.println( "BeaconClient.keyTyped = " + input
                          + "; int value = " + intInput      );

    if( intInput >= BASE_KEYCODE_INDEX  &&  intInput <= (BASE_KEYCODE_INDEX+gridLength) )
    {
      if( grid2dArray[activeSqrPosn.x][activeSqrPosn.y].getType() != SQR_TYPE_FIXED )
      {
        grid2dArray[activeSqrPosn.x][activeSqrPosn.y].setValue( newValue );
        if( origValue == 0  &&  newValue != 0 )
          --blankSquares ;
        else if( origValue != 0  &&  newValue == 0 )
          ++blankSquares ;
      }
    }
    
    gameview.updateSquaresDisplay( blankSquares );
    gameview.repaint();
    
  } // keyTyped()

  /** @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent) */
  public void keyPressed(KeyEvent e)
  {
    // use SHIFT to enter TEMP values
  }
  /** @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent) */
  public void keyReleased(KeyEvent e)
  { }

  /*
   *   INNER CLASSES
   * ================= */
  
  /**
   *  The nine Grid sub-sections, each comprising a 3x3 block of <code>Squares</code>
   *  - Class <code>BeaconClient</code> uses a <code>Zone</code>[]
   */
  class Zone
  {
    /**
     * 0 to gridLength-1
     */
    private int firstRow, firstCol ;
    
    private Square[] mySquares ;
    
    /** For each value, how many of my <code>Squares</code> 
        CANNOT be that value */
    private int[] nots ;
    
    /** Do I know the <code>Square</code> with this value yet? 
     *  - to identify the most promising <code>Zone</code> */
    private boolean[] haves ;
    private int numHaves ;
    
    /** Do I have 2 or 3 blank <code>Squares</code> that are in 
        the same row or col? */
    private boolean singleOpenLine ;
    /** Which row (x) or col (y) has my open line? */
    private Point openLine ;
    
    public Zone()
    {
      mySquares = new Square[ gridLength ];
      nots = new int[ gridLength ];
      haves = new boolean[ gridLength ];
      openLine = new Point( gridLength, gridLength ); // gridLength == false
    }
    
    public void scan()
    {
      //TODO: FILL IN FUNCTION
    }
  
  }/* inner class Zone */
  
  /**
   *  The data structure behind every <code>Square</code> in the Grid.
   *  - Class <code>BeaconClient</code> uses a 2D array of <code>Squares</code>,
   *    i.e. Square[][]
   */
  class Square
  {
    /**
     *  i.e. SQR_TYPE_FIXED, SQR_TYPE_OPEN, SQR_TYPE_BAD, SQR_TYPE_TEMP, SQR_TYPE_SPECIAL?
     */
    private int type ;
    
    /** for UNDO/REDO */ 
    private int order ;
    
    /** i.e. 0(blank) to gridLength */
    private int value ;
    
    /** Does this Square have Keyboard Focus ? */
    private boolean active ;
    
    /** Which zone am I in? */
    private int myZone;
    /** Which row am I in?  */
    private int myRow;
    /** Which col am I in?  */
    private int myCol;

    /** list of values I know I am NOT */
    private boolean[] nots ;
    /** how many NOT values I have so far */
    private int numNots ;

    /** default Constructor */
    public Square()
    {
      type  = SQR_TYPE_OPEN ;
      order = 0 ;
      value = 0 ;
      nots = new boolean[ gridLength ];
      numNots = 0 ;
    }
    
    public void setNot( int i )
    {
      if( i >= 0  &&  i < gridLength )
      {
        nots[i] = true ;
        numNots++ ;
      }
      else // should NEVER happen
        {
          if( DEBUG_LEVEL > 0 )
            System.out.println( "Square.setNot() PROBLEM: input = " + i );
        }
    }

    public boolean isNot( int i )
    {
      if( i >= 0  &&  i < gridLength )
        return nots[i] ;
      else // should NEVER happen
        {
          if( DEBUG_LEVEL > 0 )
            System.out.println( "Square.isNot() PROBLEM: input = " + i );
          return false ;
        }
    }
    
    /** @return active   */
    public boolean isActive()  { return active; }

    /** @return order  */
    public int getOrder()  { return order;  }

    /** @return type   */
    public int getType()   { return type;   }

    /** @return value  */
    public int getValue()  { return value;  }

    public int getMyZone() { return myZone; }

    public int getMyRow()  { return myRow;  }

    public int getMyCol()  { return myCol;  }

    /** @param b    */
    public void setActive(boolean b) { active = b; }

    /** @param i    */
    public void setOrder(int i)  { order = i;  }

    /** @param i    */
    public void setType( int i )
    {
      if( i >= 0  &&  i < NUM_SQR_TYPES )
      {
        type = i ;
      } 
      else // should NEVER happen
        {
          type = SQR_TYPE_OPEN ;
          if( DEBUG_LEVEL > 0 )
            System.out.println( "Square.setType() PROBLEM: input = " + i );
        }
    }

    /** @param i    */
    public void setValue( int i )
    {
      if( i >= 0  &&  i <= gridLength ) // 0 = blank; values = 1-9
      {
        value = i ;
      }
      else // should NEVER happen
        {
          value = SQR_VALUE_BLANK ;
          if( DEBUG_LEVEL > 0 )
            System.out.println( "Square.setValue() PROBLEM: input = " + i );
        }
    }

    public void setMyZone( int i )
    {
      if( i >= 0  &&  i < gridLength )
      {
        myZone = i ;
      }
      else // should NEVER happen
        {
          myZone = 4 ; // middle
          if( DEBUG_LEVEL > 0 )
            System.out.println( "Square.setMyZone() PROBLEM: input = " + i );
        }
    }

    public void setMyRow( int i )
    {
      if( i >= 0  &&  i < gridLength )
      {
        myRow = i ;
      } 
      else // should NEVER happen
        {
          myRow = 4 ; // middle
          if( DEBUG_LEVEL > 0 )
            System.out.println( "Square.setMyRow() PROBLEM: input = " + i );
        }
    }

    public void setMyCol( int i )
    {
      if( i >= 0  &&  i < gridLength )
      {
        myCol = i ;
      } 
      else // should NEVER happen
        {
          myCol = 4 ; // middle
          if( DEBUG_LEVEL > 0 )
            System.out.println( "Square.setMyCol() PROBLEM: input = " + i );
        }
    }

  }/* inner class Square */

}// class BeaconClient
