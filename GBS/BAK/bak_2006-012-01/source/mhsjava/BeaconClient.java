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
 * @version 1.4  Nov.21/2006
 */
public class BeaconClient extends JPanel implements MouseListener, KeyListener
{
  /*
   *   FIELDS
   * ============================================= */
  
  static final int
           BASE_KEYCODE_INDEX = 48 , // ASCII for '0'
           
                 INT_NEGATIVE = -1 ,

               MAX_TEMP_VALUE = 999 , // max value for a temp Square
               
               GROUP_TYPE_ROW = 0 ,
               GROUP_TYPE_COL = 1 ,
              GROUP_TYPE_ZONE = 2 ,
    
              SQR_VALUE_BLANK = 0  , // Squares with this value are BLANK
            DEFAULT_GRID_SIZE = 9  , // # of squares per side of the grid
          DEFAULT_SQUARE_SIZE = 48 , // length in pixels of each side of a Square 
                  
                    REG_FONT_SIZE = DEFAULT_SQUARE_SIZE/2 + 6 ,
                    BIG_FONT_SIZE = DEFAULT_SQUARE_SIZE   + 6 ,
                    
                     X_BORDER = 80 ,  // extra space around the mine field
                     Y_BORDER = 60  ; // for the labels, buttons, etc

  static final String strDEFAULT_TYPEFACE = "Arial" ,
                          strALT_TYPEFACE = "Courier" ;

  /** different types of <code>Squares</code> */
  static final int
         SQR_TYPE_FIXED  =  0 , // starting GIVENs
        SQR_TYPE_BLANK   =  1 , // starting BLANKs
        SQR_TYPE_GUESS   =  2 , // regular guess
         SQR_TYPE_TEMP   =  3 , // 2 or 3 POSSIBLE values
         SQR_TYPE_BAD    =  4 , // conflicting values after a CHECK action
        SQR_TYPE_SOLVED  =  5 , // confirmed as proper value after a SOLVE action
          NUM_SQR_TYPES  =  6  ;// MUST BE LAST ENTRY
        
  /** different colors for different types of <code>Squares</code>
   *  - MUST have the same number of entries as the previous array
   */
  static final Color
                    COLOR_FIXED = Color.black           ,
                    COLOR_GUESS = Color.blue.darker()   ,
                     COLOR_TEMP = Color.green.darker()  ,
                      COLOR_BAD = Color.red.darker()    ,
                   COLOR_SOLVED = Color.yellow.darker() ,
                    COLOR_FOCUS = Color.red.brighter()   ;
  
  static final Color
                     COLOR_HILITE  =  Color.lightGray ,
                     COLOR_GRID    =  Color.gray ,
                     COLOR_SHADOW  =  Color.darkGray ;

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
  
  Font regFont, bigFont ;
  
  private double density = 0.38 ;
  
  private boolean  paintAll = true ,
                 firstPress = true ;
  
  private int blankSquares ;
  
  /** number of <code>Squares</code> on each side of the grid */
  int gridLength ;
  /** total number of <code>Squares</code> in the grid */
  int grid2dSize ;
  
  /** number of <code>Squares</code> on each side of a <code>Zone</code> */
  int zoneLength ;
  
  /**
   *  One of the Grid sub-sections, each comprising a zoneLength x zoneLength
   *    block of <code>Squares</code>
   *  - represented by a <code>Group</code>[ gridLength ] array
   */
  Group[] zones ;
  
  /**
   *  One of the Grid sub-sections, each comprising a row of gridLength
   *  <code>Squares</code> in the Grid 2d-array
   *  - represented by a <code>Group</code>[ gridLength ] array
   */
  Group[] rows ;
  
  /**
   *  One of the Grid sub-sections, each comprising a column of gridLength
   *  <code>Squares</code> in the Grid 2d-array
   *  - represented by a <code>Group</code>[ gridLength ] array
   */
  Group[] cols ;

  /** length of sides (in pixels) of each individual <code>Square</code> */
  int squareLength ;
  /** 2D array of individual <code>Squares</code> */
  Square[][] grid2dArray ;
  
  Point activeSqrPosn ;
  Point[] badValues ;
  
  int tempValue ;
  boolean tempActive ;
  
  // may want to use a paintArea() type fxn at some point...
  int destx, desty, paintLeft, paintRight, paintTop, paintBottom ;
  
  /** reference to the enclosing Class */
  BeaconServer gameview ;
  /** reference to the file access Class */
  BeaconLoader gameloader ;
  
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

    grid2dSize = gridLength * gridLength ;
    
    // Component method
    setSize( gridLength*squareLength, gridLength*squareLength );
    
    zoneLength = (int)Math.round( Math.sqrt( (double)gridLength ) );
    if( DEBUG_LEVEL > 1 )
      System.out.println( "zoneLength = " + zoneLength );
    
    int i, j ;
    grid2dArray = new Square[ gridLength ][ gridLength ];
    for( i=0; i<gridLength; i++ )
      for( j=0; j<gridLength; j++ )
        grid2dArray[i][j] = new Square();
    
    rows = new Group[ gridLength ];
    for( i=0; i<gridLength; i++ )
      rows[i] = new Group( GROUP_TYPE_ROW, i, 0 );
    if( DEBUG_LEVEL > 1 )
      for( i=0; i<gridLength; i++ )
        rows[i].display( "Row", i );
    
    cols = new Group[ gridLength ];
    for( i=0; i<gridLength; i++ )
      cols[i] = new Group( GROUP_TYPE_COL, 0, i );
    if( DEBUG_LEVEL > 1 )
      for( i=0; i<gridLength; i++ )
        cols[i].display( "Col", i );
    
    zones = new Group[ gridLength ];
    for( i=0; i<gridLength; i++ )
      zones[i] = new Group( GROUP_TYPE_ZONE,
                           (i/zoneLength)*zoneLength, (i%zoneLength)*zoneLength );
    if( DEBUG_LEVEL > 1 )
      for( i=0; i<gridLength; i++ )
        zones[i].display( "Zone", i );
    
    activeSqrPosn = new Point( 0, 0 );
    badValues = new Point[ grid2dSize ];
    
    tempValue = SQR_VALUE_BLANK ;
    tempActive = false ;
  
    addMouseListener( this );
    addKeyListener( this );
    
    setLocation( X_BORDER, Y_BORDER/2 );
    setBackground( BeaconServer.COLOR_GAME_BKGRND );
    
    regFont = new Font( strDEFAULT_TYPEFACE, Font.BOLD, REG_FONT_SIZE );
    bigFont = new Font( strALT_TYPEFACE, Font.PLAIN, BIG_FONT_SIZE );
    setFont( regFont );

    setFocusable( true );
    setVisible( true );
  
  }// init()
  
  /** Reset the 2d array */
  protected void newGrid()
  {
    if( DEBUG_LEVEL > 0 )
      System.out.println( "BeaconClient.newGrid()" );

    firstPress = true ;
    blankSquares = grid2dSize ;
    
    int choice = (int)Math.round( Math.random() * (gameloader.numLoadedGames - 1) );
    int[][] ldgame = gameloader.getGame( choice, 0 );
    
    int row, col ;
    Square activeSqr ;
    for( row=0; row < gridLength; row++ )
      for( col=0; col < gridLength; col++ )
      {
        activeSqr = grid2dArray[row][col] ;
        
        activeSqr.setMyZone( zoneLength*(row/zoneLength) + col/zoneLength );
        activeSqr.setMyRow( row );
        activeSqr.setMyCol( col );
        activeSqr.setActive( false );
        
        if( ldgame != null )
        {
          activeSqr.setValue( ldgame[row][col] );
          if( ldgame[row][col] != SQR_VALUE_BLANK )
          {
            activeSqr.setType( SQR_TYPE_FIXED );
            incBlankSquares( -1 );
          }
        }
        else // getGame() didn't work ??
        {  
          if( Math.random() <= density )
          {
            activeSqr.setValue( (int)(1 + (Math.random() * gridLength)) );
            activeSqr.setType( SQR_TYPE_FIXED );
            incBlankSquares( -1 );
          }          
          else
              activeSqr.setValue( SQR_VALUE_BLANK );
        }
      } // for( cols )
    // for( rows )
    
    if( DEBUG_LEVEL > 0 )
    {
      for( row=0; row < gridLength; row++ )
        grid2dArray[row][row].display( row, row );
        
      for( row=0; row < gridLength; row++ )
        rows[row].display( "Row", row );

      for( col=0; col < gridLength; col++ )
        cols[col].display( "Col", col );

      for( col=0; col < gridLength; col++ )
        zones[col].display( "Zone", col );
    }

    /* initialize all groups for nots & haves */
    
    gameview.updateSquaresDisplay( blankSquares );
    gameview.validate();
    
  }// newGrid()

  /**
   * @return blankSquares
   */
  public int getBlankSquares()
  {
    return blankSquares ;
  }

  /**
   * @param i  the amount to increment the blankSquares var: +ve or -ve
   */
  public void incBlankSquares( int i )
  {
    blankSquares = blankSquares + i ;
    if( blankSquares == 0  &&  check() == true )
      gameview.gameSolved();
  }

  // NEW version using arrays 'rows', 'cols', and 'zones'
  public boolean valuesCheck()
  {
    if( DEBUG_LEVEL > 0 )
      System.out.println( "BeaconClient.check()" );
    
    boolean valsOK = true ;
    int currentVal, currentType ;
    int[] foundValues = new int[ gridLength + 1 ]; // values 0 to 9
    
    //reset
    clearBads();
    
    /* check rows */
    for( int row=0; row < gridLength; row++ )
    {
      if( DEBUG_LEVEL > 0 )
        System.out.println( "  Checking row #" + row );
      
      clearArray( foundValues );
      for( int col=0; col < gridLength; col++ )
      {
        currentVal = grid2dArray[row][col].getValue();
        currentType = grid2dArray[row][col].getType() ;
        if( DEBUG_LEVEL > 1 )
          System.out.println( "Sqr[" + row + "][" + col + "] val = " + currentVal
                              + " ; type = " + currentType );
        
        // just ignore temp values
        if( currentVal > gridLength )
          currentVal = SQR_VALUE_BLANK ;
        
        if( foundValues[currentVal] > INT_NEGATIVE )
        {
          grid2dArray[row][col].setType( SQR_TYPE_BAD );
          setBad( row, col );
          grid2dArray[row][foundValues[currentVal]].setType( SQR_TYPE_BAD );
          setBad( row, foundValues[currentVal] );
          valsOK = false ;
          
          if( DEBUG_LEVEL > 0 )
            System.err.println( "Found a BAD Square at [" + row + "][" + col + "]"
                                + " & at [" + row + "][" + foundValues[currentVal] + "]" );
        }
        else
          { // keep track of the position of any non-blanks
            if( currentVal > SQR_VALUE_BLANK )
              foundValues[currentVal] = col ;
          }
      } // for( cols )
    }// for( rows )
    
    /* check cols */
    for( int col=0; col < gridLength; col++ )
    {
      if( DEBUG_LEVEL > 0 )
        System.out.println( "  Checking col #" + col );
      
      clearArray( foundValues );
      for( int row=0; row < gridLength; row++ )
      {
        currentVal = grid2dArray[row][col].getValue();
        currentType = grid2dArray[row][col].getType() ;
        if( DEBUG_LEVEL > 1 )
          System.out.println( "Sqr[" + row + "][" + col + "] val = " + currentVal
                              + " ; type = " + currentType );
        
        // just ignore temp values
        if( currentVal > gridLength )
          currentVal = SQR_VALUE_BLANK ;
          
        if( foundValues[currentVal] > INT_NEGATIVE )
        {
          grid2dArray[row][col].setType( SQR_TYPE_BAD );
          setBad( row, col );
          grid2dArray[foundValues[currentVal]][col].setType( SQR_TYPE_BAD );
          setBad( foundValues[currentVal], col );
          valsOK = false ;
          
          if( DEBUG_LEVEL > 0 )
            System.err.println( "Found a BAD Square at [" + row + "][" + col + "]"
                                + " & at [" + foundValues[currentVal] + "][" + col + "]" );
        }
        else
          { // keep track of the position of any non-blanks
            if( currentVal > SQR_VALUE_BLANK )
              foundValues[currentVal] = row ;
          }
      } // for( rows )
    }// for( cols )
    
    /* CHECK ZONES */
    for( int zone=0; zone < gridLength; zone++ )
    {
      if( DEBUG_LEVEL > 0 )
        System.out.println( "  Checking zone #" + zone );
      
      int startRow = zones[zone].mySquares[0].myRow ,
          startCol = zones[zone].mySquares[0].myCol ;
      
      clearArray( foundValues );
      for( int row=startRow; row < startRow+zoneLength; row++ )
        for( int col=startCol; col < startCol+zoneLength; col++ )
        {
          currentVal = grid2dArray[row][col].getValue();
          currentType = grid2dArray[row][col].getType() ;
          if( DEBUG_LEVEL > 1 )
            System.out.println( "Sqr[" + row + "][" + col + "] val = " + currentVal
                                + " ; type = " + currentType );
          
          // just ignore temp values
          if( currentVal > gridLength )
            currentVal = SQR_VALUE_BLANK ;
          
          if( foundValues[currentVal] > INT_NEGATIVE )
          {
            grid2dArray[row][col].setType( SQR_TYPE_BAD );
            setBad( row, col );
            grid2dArray[foundValues[currentVal]/gridLength][foundValues[currentVal]%gridLength].setType( SQR_TYPE_BAD );
            setBad( foundValues[currentVal]/gridLength, foundValues[currentVal]%gridLength );
            valsOK = false ;

            if( DEBUG_LEVEL > 0 )
              System.err.println( "Found a BAD Square at [" + row + "][" + col + "]"
                                  + " & at [" + foundValues[currentVal]/gridLength 
                                  + "][" + foundValues[currentVal]%gridLength + "]" );
          }
          else
            { // keep track of the position of any non-blanks
              if( currentVal > SQR_VALUE_BLANK )
                // need a unique value to indicate the row & col of this square
                foundValues[currentVal] = row*gridLength + col ;
            }
        } // for( cols )
      // for( rows )
      
    }// for( zones )
    
    return valsOK ;
    
  }// valuesCheck()
  
  public boolean check()
  {
    if( DEBUG_LEVEL > 0 )
      System.out.println( "BeaconClient.check()" );
    
    boolean valsOK = true ;
    int currentVal, currentType ;
    int[] foundValues = new int[ gridLength + 1 ]; // values 0 to 9
    
    //reset
    clearBads();
    
    /* check rows */
    for( int row=0; row < gridLength; row++ )
    {
      if( DEBUG_LEVEL > 0 )
        System.out.println( "  Checking row #" + row );
      
      clearArray( foundValues );
      for( int col=0; col < gridLength; col++ )
      {
        currentVal = grid2dArray[row][col].getValue();
        currentType = grid2dArray[row][col].getType() ;
        if( DEBUG_LEVEL > 1 )
          System.out.println( "Sqr[" + row + "][" + col + "] val = " + currentVal
                              + " ; type = " + currentType );
        
        // just ignore temp values
        if( currentVal > gridLength )
          currentVal = SQR_VALUE_BLANK ;
        
        if( foundValues[currentVal] > INT_NEGATIVE )
        {
          grid2dArray[row][col].setType( SQR_TYPE_BAD );
          setBad( row, col );
          grid2dArray[row][foundValues[currentVal]].setType( SQR_TYPE_BAD );
          setBad( row, foundValues[currentVal] );
          valsOK = false ;
          
          if( DEBUG_LEVEL > 0 )
            System.err.println( "Found a BAD Square at [" + row + "][" + col + "]"
                                + " & at [" + row + "][" + foundValues[currentVal] + "]" );
        }
        else
          { // keep track of the position of any non-blanks
            if( currentVal > SQR_VALUE_BLANK )
              foundValues[currentVal] = col ;
          }
      } // for( cols )
    }// for( rows )
    
    /* check cols */
    for( int col=0; col < gridLength; col++ )
    {
      if( DEBUG_LEVEL > 0 )
        System.out.println( "  Checking col #" + col );
      
      clearArray( foundValues );
      for( int row=0; row < gridLength; row++ )
      {
        currentVal = grid2dArray[row][col].getValue();
        currentType = grid2dArray[row][col].getType() ;
        if( DEBUG_LEVEL > 1 )
          System.out.println( "Sqr[" + row + "][" + col + "] val = " + currentVal
                              + " ; type = " + currentType );
        
        // just ignore temp values
        if( currentVal > gridLength )
          currentVal = SQR_VALUE_BLANK ;
          
        if( foundValues[currentVal] > INT_NEGATIVE )
        {
          grid2dArray[row][col].setType( SQR_TYPE_BAD );
          setBad( row, col );
          grid2dArray[foundValues[currentVal]][col].setType( SQR_TYPE_BAD );
          setBad( foundValues[currentVal], col );
          valsOK = false ;
          
          if( DEBUG_LEVEL > 0 )
            System.err.println( "Found a BAD Square at [" + row + "][" + col + "]"
                                + " & at [" + foundValues[currentVal] + "][" + col + "]" );
        }
        else
          { // keep track of the position of any non-blanks
            if( currentVal > SQR_VALUE_BLANK )
              foundValues[currentVal] = row ;
          }
      } // for( rows )
    }// for( cols )
    
    /* CHECK ZONES */
    for( int zone=0; zone < gridLength; zone++ )
    {
      if( DEBUG_LEVEL > 0 )
        System.out.println( "  Checking zone #" + zone );
      
      int startRow = zones[zone].mySquares[0].myRow ,
          startCol = zones[zone].mySquares[0].myCol ;
      
      clearArray( foundValues );
      for( int row=startRow; row < startRow+zoneLength; row++ )
        for( int col=startCol; col < startCol+zoneLength; col++ )
        {
          currentVal = grid2dArray[row][col].getValue();
          currentType = grid2dArray[row][col].getType() ;
          if( DEBUG_LEVEL > 1 )
            System.out.println( "Sqr[" + row + "][" + col + "] val = " + currentVal
                                + " ; type = " + currentType );
          
          // just ignore temp values
          if( currentVal > gridLength )
            currentVal = SQR_VALUE_BLANK ;
          
          if( foundValues[currentVal] > INT_NEGATIVE )
          {
            grid2dArray[row][col].setType( SQR_TYPE_BAD );
            setBad( row, col );
            grid2dArray[foundValues[currentVal]/gridLength][foundValues[currentVal]%gridLength].setType( SQR_TYPE_BAD );
            setBad( foundValues[currentVal]/gridLength, foundValues[currentVal]%gridLength );
            valsOK = false ;

            if( DEBUG_LEVEL > 0 )
              System.err.println( "Found a BAD Square at [" + row + "][" + col + "]"
                                  + " & at [" + foundValues[currentVal]/gridLength 
                                  + "][" + foundValues[currentVal]%gridLength + "]" );
          }
          else
            { // keep track of the position of any non-blanks
              if( currentVal > SQR_VALUE_BLANK )
                // need a unique value to indicate the row & col of this square
                foundValues[currentVal] = row*gridLength + col ;
            }
        } // for( cols )
      // for( rows )
      
    }// for( zones )
    
    return valsOK ;
    
  }// check()
  
  public void setBad( int row, int col )
  {
    int index = 0 ;
    while( badValues[index] != null )
    {
      index++ ;
    }
    badValues[index] = new Point( row, col) ;
    grid2dArray[row][col].setType( SQR_TYPE_BAD );
  }
  
  public void clearBads()
  {
    int index = 0 ;
    while( badValues[index] != null )
    {
      grid2dArray[badValues[index].x][badValues[index].y].setType( SQR_TYPE_GUESS );
      badValues[index] = null ;
      index++ ;
    }
  }
  
  public void clearArray( int[] array )
  {
    for( int i=0; i < array.length; i++ )
      array[i] = INT_NEGATIVE ;
  }
  
  /**
   * @return tempActive
   */
  public boolean isTempActive()
  {
    return tempActive;
  }

  /**
   * @param b  boolean
   */
  public void setTempActive( boolean b )
  {
    tempActive = b ;
    if( !tempActive )
      tempValue = SQR_VALUE_BLANK ;
    if( DEBUG_LEVEL > 0 )
      System.err.println( (tempActive ? "" : "DE-") + "ACTIVATED TEMP!" );
  }

  /*
   *   GRAPHICS EVENTS
   * ============================================= */
    
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
      page.setColor( COLOR_TEMP );
    }
    else if( type == SQR_TYPE_FIXED )
    {
      page.setColor( COLOR_FIXED );
    }
    else
        page.setColor( COLOR_GUESS );
    
    if( value > SQR_VALUE_BLANK )
    {
      String strNum = String.valueOf( value );
      if( value <= gridLength )
        page.drawString( strNum, horiz + sl/4, vert + 3*sl/4 );
      else
        if( value >= (gridLength+1)*(gridLength+1) ) // 3 digits
          page.drawString( strNum, horiz, vert + 3*sl/4 );
      else // 2 digits
          page.drawString( strNum, horiz + sl/8, vert + 3*sl/4 );
    }
    
    if( type == SQR_TYPE_BAD )
    {
      page.setColor( COLOR_BAD );
      page.setFont( bigFont );
      page.drawString( "X", horiz + sl/5, vert + 7*sl/8 );
      page.setFont( regFont );
    }
    
  }// paintSquare()

  /**
   * Draw a basic unrevealed <code>Square</code> with edge hilites
   * 
   * @param horiz  the <i>x</i> coordinate of the upper left corner
   * @param vert   the <i>y</i> coordinate of the upper left corner
   * @param s      the <code>Square</code> to draw
   * @param page   Graphics object reference
   */
  protected void paintBlankSquare( int horiz, int vert, Square s, Graphics page )
  {
    if( DEBUG_LEVEL > 3)
      System.out.println( "BeaconClient.paintBlankSquare()" );

    int sl = squareLength ;
    
    if( s.isActive() )
      page.setColor( COLOR_FOCUS );
    else
        page.setColor( ZONE_COLORS[s.getMyZone()] );

    page.fillRect( horiz, vert, squareLength, squareLength );
    
    page.setColor( COLOR_SHADOW );
    page.drawLine( horiz     , vert+sl-1, horiz+sl  , vert+sl-1 ); // bottom
    page.drawLine( horiz+sl-1, vert     , horiz+sl-1, vert+sl   ); // right
    
    page.setColor( COLOR_HILITE );
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

  /** @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent) */
  public void mouseEntered( MouseEvent me )
  {
    // turn OFF temp if focus left the grid while temp was activated
    if( tempActive )
    {
      setTempActive( false );
      if( DEBUG_LEVEL > 0 )
        System.err.println( "BeaconClient.mouseEntered(): setTempActive(false)" );
    }
  }
  /** @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent) */
  public void mouseExited(MouseEvent e)
  { }
  /** @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent) */
  public void mousePressed(MouseEvent e)
  { }
  /** @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent) */
  public void mouseReleased(MouseEvent e)
  { }


  /**
   *  Enter the typed value into the <code>Square</code>
   *  @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
   */
  public void keyTyped( KeyEvent ke )
  {
    int base = gridLength + 1 ;
    
    char input = ke.getKeyChar();
    int intInput = (int)input ;
    if( DEBUG_LEVEL > 0 )
      System.out.println( "BeaconClient.keyTyped = " + input
                          + "; int value = " + intInput 
                          + " Temp is " + (tempActive ? "" : "NOT ") + "Active" );
    
    int origValue = grid2dArray[activeSqrPosn.x][activeSqrPosn.y].getValue();
    int newValue = intInput - BASE_KEYCODE_INDEX ;
    
    if( newValue >= SQR_VALUE_BLANK  &&  newValue <= gridLength
        && grid2dArray[activeSqrPosn.x][activeSqrPosn.y].getType() != SQR_TYPE_FIXED
      )
    {
      if( tempActive  &&  newValue > SQR_VALUE_BLANK )
      {
        // keep track of up to 3 digits for a temp value and display them
        if( tempValue == SQR_VALUE_BLANK )
          tempValue = newValue ;
        else if( tempValue > base*base - 1 )
            tempValue = (tempValue/base)*base + newValue ;
          else
              tempValue = tempValue*base + newValue ;
          
        newValue = tempValue ;
      }
      // process a regular guess 
      grid2dArray[activeSqrPosn.x][activeSqrPosn.y].setValue( newValue );
      
      // keep track of the blank square count
      if( ( origValue == SQR_VALUE_BLANK  ||  origValue > gridLength )
          &&  newValue > SQR_VALUE_BLANK  &&  newValue <= gridLength
        )
         incBlankSquares( -1 );
      else if( origValue > SQR_VALUE_BLANK  &&  origValue <= gridLength
               && ( newValue == SQR_VALUE_BLANK  ||  newValue > gridLength )
             )
              incBlankSquares( +1 );
                
    }// got a digit & this is NOT a Fixed Square
    
    gameview.updateSquaresDisplay( blankSquares );
    gameview.repaint();
    
  } // keyTyped()

  /** @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent) */
  public void keyPressed( KeyEvent ke )
  {
    // Use ALT key to enter Temp values
    if( ke.getKeyCode() == KeyEvent.VK_ALT )
    {
      setTempActive( true ); 
      if( DEBUG_LEVEL > 1 )
        System.err.println( "BeaconClient.keyPressed()" );
    }
  }
  /** @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent) */
  public void keyReleased( KeyEvent ke )
  {
    /* IF SOME COMPONENT OTHER THAN BEACONCLIENT HAS FOCUS WHEN THE KEY IS RELEASED,
     * THIS EVENT WILL NOT BE PICKED UP, AND TEMP WILL STAY ACTIVE!
     * -- see mouseEntered() for one way of mitigating this.
     */
    if( ke.getKeyCode() == KeyEvent.VK_ALT )
    {
      setTempActive( false );
      if( DEBUG_LEVEL > 1 )
        System.err.println( "BeaconClient.keyReleased()" );
    }
  }

  /*
   *   INNER CLASSES
   * ================= */
  
  /**
   *  The Grid sub-sections, either rows, cols, or zones, 
   *  each being composed of 'gridLength' number of <code>Squares</code>
   *  - represented by <code>Square</code>[ gridLength ]
   */
  class Group
  {
    private int myType ;
    private Square[] mySquares ;
    
    /** For each value, how many of my <code>Squares</code> 
        CANNOT be that value */
    private int[] nots ;
    private int numNots = 0 ;/* ? NEEDED ? */
    
    /** Do I know the <code>Square</code> with this value yet? 
     *  - to identify the most promising <code>Groups</code> */
    private boolean[] haves ;
    private int numHaves = 0 ;
    
    /** Do I have ONLY 2 or 3 blank <code>Squares</code> left?
     *  - and are they in the same row or col? */
    private boolean haveBlock ;
    /** Which row (x) or col (y) has my block(s)? */
    private Point blockPosn ;
    
    public Group( int grpType, int startingRow, int startingCol )
    {
      if( DEBUG_LEVEL > 0 )
        System.out.println( "Group(): type = " + grpType + " ; starting row = "
                            + startingRow + " ; starting col = " + startingCol );

      myType = grpType ;
      nots = new int[ gridLength ];
      haves = new boolean[ gridLength ];
      blockPosn = new Point( INT_NEGATIVE, INT_NEGATIVE ); // none at start

      initMySquares( startingRow, startingCol ); //
    }
    
    public void initMySquares( int row, int col )
    {
      int i, j ;
      
      mySquares = new Square[ gridLength ];
      
      switch( myType )
      {
        case GROUP_TYPE_ROW:
            for( i=0; i < gridLength; i++ )
              mySquares[i] = grid2dArray[row][i] ;
            break ;
            
        case GROUP_TYPE_COL:
            for( i=0; i < gridLength; i++ )
              mySquares[i] = grid2dArray[i][col] ;
            break ;
        
        case GROUP_TYPE_ZONE:
            int k = 0 ;
            for( i=row; i < row+zoneLength; i++ )
              for( j=col; j < col+zoneLength; j++ )
              {
                if( i < gridLength  &&  j < gridLength  &&  k < gridLength )
                  mySquares[k] = grid2dArray[i][j] ;
                else
                  {
                    System.out.println( "PROBLEM WITH ZONE INIT!" );
                    System.out.println( "k = " + k + " i = " + i + " j = " + j );
                    return ;
                    //System.exit( k );
                  }
                k++ ;
              }
            break ;
        
        default:
            if( DEBUG_LEVEL > 0 )
              System.out.println( "Group.initMySquares() PROBLEM: group type = " + myType );
      }
    }
    
    public void scan()
    {
      //TODO: FILL IN FUNCTION
    }
    
    // print to System.out
    void display( String type, int num )
    {
      System.out.println( type + " #" + num );
      System.out.println( "-------------------------------------------------" );
      
      int i ;
      System.out.print( "Squares" );
      for( i=0; i < gridLength; i++ )
      {
        System.out.print( "[" + mySquares[i].getValue() + "]" );
      }
      System.out.print( " (" + numHaves + ")\n" );

      System.out.print( "Nots" );
      for( i=0; i < gridLength; i++ )
      {
        System.out.print( "[" + nots[i] + "]" );
      }
      System.out.print( " (" + numNots + ")\n" );

      System.out.print( "Haves" );
      for( i=0; i < gridLength; i++ )
      {
        System.out.print( "[" + haves[i] + "]" );
      }
      System.out.print( " (" + numHaves + ")\n" );
      System.out.println( "-------------------------------------------------" );
    
    } // display()
    
  }/* Inner Class Group */
  
  /**
   *  The data structure behind every <code>Square</code> in the Grid.
   *  - Class <code>BeaconClient</code> uses a 2D array of <code>Squares</code>,
   *    i.e. Square[][]
   */
  class Square
  {
    /**
     *  i.e. SQR_TYPE_FIXED, SQR_TYPE_BLANK, etc
     */
    private int type ;
    
    /** i.e. 0(SQR_VALUE_BLANK) to gridLength */
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
      type  = SQR_TYPE_BLANK ;
      value = 0 ;
      nots = new boolean[ gridLength ];
      numNots = 0 ;
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
    } // isNot()
    
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
    } // setNot()

    private void setAllNots( int i )
    {
      int j ;
      for( j=0; j < gridLength; j++ )
        if( j != i )
          nots[j] = true ;
      
      numNots = gridLength - 1 ;
      
    } // setAllNots()

    /** @return active   */
    public boolean isActive()  { return active; }

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
    public void setType( int i )
    {
      if( i >= 0  &&  i < NUM_SQR_TYPES )
      {
        if( type != SQR_TYPE_FIXED ) // Once FIXED, remain that way
          type = i ;
      } 
      else // should NEVER happen
        {
          type = SQR_TYPE_BLANK ;
          if( DEBUG_LEVEL > 0 )
            System.out.println( "Square.setType() PROBLEM: input = " + i );
        }
    } // setType()

    /** @param i    */
    public void setValue( int i )
    {
      if( i >= SQR_VALUE_BLANK  &&  i <= MAX_TEMP_VALUE )
      {
        value = i ; // 0 = blank; regular values = 1-9
        
        // can modify the type based on the submitted value
        if( i == SQR_VALUE_BLANK )
        {
          type = SQR_TYPE_BLANK ;
          // will have to re-scan for current valid nots if changing value to blank
        }
        else if( i > gridLength )
            type = SQR_TYPE_TEMP ;
        else
            {
              type = SQR_TYPE_GUESS ;
              setAllNots( i );
            }
      }
      else // should NEVER happen
        {
          value = SQR_VALUE_BLANK ;
          if( DEBUG_LEVEL > 0 )
            System.out.println( "Square.setValue() PROBLEM: input = " + i );
        }
    } // setValue()

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
    } // setMyZone()

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
    } // setMyRow()

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
    } // setMyCol()

    // print to System.out
    void display( int row, int col )
    {
      System.out.println( " I am Square[" + row + "][" + col + "]" );
      System.out.println( "-------------------------------------------------" );
      System.out.println( "my Zone = " + myZone + " & type = " + type + " & value = " + value );
      
      int i ;
      System.out.print( "Nots" );
      for( i=0; i < gridLength; i++ )
      {
        System.out.print( "[" + nots[i] + "]" );
      }
      System.out.print( " (" + numNots + ")\n" );
      System.out.println( "-------------------------------------------------" );

    }// display()
    
  }/* Inner Class Square */

}// CLASS BeaconClient
