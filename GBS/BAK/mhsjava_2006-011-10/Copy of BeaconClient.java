/*
 * Created on 31-Oct-06
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
//package mhsjava;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.* ;
import javax.swing.*;

/**
 * @author Mark Sattolo
 * @version 1.1  Nov.9/2006
 */
public class BeaconClient extends JPanel implements MouseListener, KeyListener
{
  /*
   *   FIELDS
   * ============================================= */
  
  private double density = 0.25 ;

  private boolean paintAll = true  ;

  public boolean firstPress = true ;

  /** different types of <code>Squares</code> */
  static final int FIXED   =  9 , // starting givens
                   OPEN    = 10 , // starting blanks
                   TEMP    = 11 , // 2 or 3 POSSIBLE values
                   BAD     = 12 , // conflicting values after a CHECK action
                   SPECIAL = 13 ; // ?
        
  static final Color COLOR_FOCUS  =  Color.orange , 
                     COLOR_GUESS  =  Color.blue ,
                     COLOR_TEMP   =  Color.green.darker() ,
                     COLOR_BAD    =  Color.red ,
                     COLOR_LIGHT  =  Color.lightGray ,
                     COLOR_GRID   =  Color.gray ,
                     COLOR_DARK   =  Color.darkGray ,
                     COLOR_FIXED  =  Color.black ;
                       
  int DEBUG_LEVEL ;
  
  /** number of <code>Squares</code> on each side of the 2D mine array */
  int fieldLength ;
  /** length of sides (in pixels) of each individual <code>Square</code> */
  int squareLength ;
  
  java.awt.Point activeSqrXY ;
  
  int destx, desty, paintLeft, paintRight, paintTop, paintBottom ;
  
  /** reference to the enclosing class */
  BeaconServer gameview ;
  /** 2D array of individual <code>Squares</code> */
  Square[][] mine2dArray ;
  
  /*
   *   METHODS
   * ============================================= */
   
  /**
   *  This is the default constructor
   */
  public BeaconClient( BeaconServer frame )
  {
    super();
    
    DEBUG_LEVEL = frame.DEBUG_LEVEL ;
    if( DEBUG_LEVEL > 0 )
      System.out.println( "BeaconClient.DEBUG_LEVEL = " + DEBUG_LEVEL );
    
    gameview = frame ;
    
    init();
    newField();
  }

  private void init()
  {
    if( DEBUG_LEVEL > 0)
      System.out.println( "BeaconClient.init()" );
    
    squareLength = BeaconServer.SQUARE_SIZE ;
    fieldLength = BeaconServer.GRID_SIZE ;
    setSize( fieldLength*squareLength, fieldLength*squareLength );
    
    mine2dArray = new Square[ fieldLength ][ fieldLength ];
    int i, j ;
    for( i=0; i<fieldLength; i++ )
      for( j=0; j<fieldLength; j++ )
        mine2dArray[i][j] = new Square();
    
    activeSqrXY = new java.awt.Point( 0, 0 );
    
    addMouseListener( this );
    addKeyListener( this );
    
    setFont( gameview.myFont );
    setLocation( BeaconServer.X_BORDER, BeaconServer.Y_BORDER/2 );
    setBackground( BeaconServer.COLOR_GAME_BKGRND );
    
    setFocusable( true );
    setVisible( true );
  
  }// init()
  
  /** Reset the 2d array */
  protected void newField()
  {
    if( DEBUG_LEVEL > 0)
      System.out.println( "BeaconClient.newField()" );

    firstPress = true ;

    int i, j ;
    for( i=0; i<fieldLength; i++ )
      for( j=0; j<fieldLength; j++ )
      {
        mine2dArray[i][j].setOrder( 0 );
        mine2dArray[i][j].setActive( false );
                
        if( Math.random() <= density )
        {
          mine2dArray[i][j].setType( BeaconClient.FIXED );
          mine2dArray[i][j].setValue( (int)(1 + (Math.random() * fieldLength)) );
        }          
        else
          {
            mine2dArray[i][j].setType( BeaconClient.OPEN );
            mine2dArray[i][j].setValue( 0 );
          }
      }

    gameview.validate();
    
  }// newField()

  /**
   * Refresh the graphical representation of the grid.
   * - called by repaint()
   */
  public void paint( Graphics page )
  {
    if( DEBUG_LEVEL > 0 )
      System.out.println( "BeaconClient.paint(): Left = " + paintLeft + "; Right = " + paintRight
                          + "; Top = " + paintTop + "; Bottom = " + paintBottom );

    if( paintAll )
    {
      paintLeft = 0 ;
      paintRight = fieldLength-1 ;
      paintTop = 0 ;
      paintBottom = fieldLength-1 ;
    }

    int i, j ;
    if( page != null )
    {
      for( i=paintLeft; i <= paintRight; i++ )
        for( j=paintTop; j <= paintBottom; j++ )
        {
          reveal( i*squareLength, j*squareLength, mine2dArray[i][j], page );
        }
    }
    else
        System.err.println( "PROBLEM: Graphics page is NULL!" );
    
    paintAll = true ;
  
  }// paint()
      
  /**
   * Draw individual <code>Squares</code> depending on the content.
   * i.e. whether a regular guess, or TEMP, BAD, etc
   * - called ONLY by <code>paint()</code>
   */
  protected void reveal( int xc, int yc, Square s, Graphics page )
  {
    int type  = s.getType() ;
    int value = s.getValue();
    
    if( DEBUG_LEVEL > 2 )
      System.out.println( "BeaconClient.reveal(): xc = " + xc + "; yc = " + yc 
                          + "; type = " + type + "; value = " + value );

    int sl = squareLength ;
    
    drawSquare( xc, yc, s.isActive(), page );

    if( type == TEMP )
    {
      page.setColor( COLOR_TEMP );
    }
    else if( type == FIXED )
    {
      page.setColor( COLOR_FIXED );
    }
    else
        page.setColor( COLOR_GUESS );

    if( value > 0 )
    {
      String number = String.valueOf( value );
      page.drawString( number, xc + sl/4, yc + 3*sl/4 );
    }
    
    if( type == BAD )
    {
      page.setColor( COLOR_BAD );
      page.drawString( "X", xc + sl/4, yc + 3*sl/4 );
    }
    
  }// reveal()

  /** Draw a basic unrevealed <code>Square</code> with edge hilites
   * 
   *  @param xc    the <i>x</i> coordinate of the upper left corner
   *  @param yc    the <i>y</i> coordinate of the upper left corner
   *  @param page  Graphics object reference
   */
  protected void drawSquare( int xc, int yc, boolean focus, Graphics page )
  {
    if( DEBUG_LEVEL > 3)
      System.out.println( "BeaconClient.drawSquare()" );

    int sl = squareLength ;
    
    if( focus )
      page.setColor( COLOR_FOCUS );
    else
        page.setColor( COLOR_GRID );
        
    page.fillRect( xc, yc, squareLength, squareLength );
    
    page.setColor( COLOR_DARK );
    page.drawLine( xc     , yc+sl-1, xc+sl  , yc+sl-1 ); // bottom
    page.drawLine( xc+sl-1, yc     , xc+sl-1, yc+sl   ); // right
    
    page.setColor( COLOR_LIGHT );
    page.drawLine( xc, yc, xc+sl-1, yc      ); // top
    page.drawLine( xc, yc, xc     , yc+sl-1 ); // left
  
  }// drawSquare()

  /*
   *   ACTION EVENTS
   * ============================================= */
    
  /**
   * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
   */
  public void mouseClicked( MouseEvent me )
  {
    if( DEBUG_LEVEL > 0 )
      System.out.println( "BeaconClient.mouseClicked()" );

    int x = me.getX() / squareLength ;
    int y = me.getY() / squareLength ;

    if( DEBUG_LEVEL > 1 )
      System.out.println( "  Clicked Square[" + y + "][" + x + "]" );

    if( firstPress )
    {
      gameview.startClock();
      firstPress = false ;
    }

    if( x < fieldLength  &&  y < fieldLength )
    {
      mine2dArray[activeSqrXY.x][activeSqrXY.y].setActive( false );
      mine2dArray[x][y].setActive( true );

      activeSqrXY.x = x ;
      activeSqrXY.y = y ;

      gameview.repaint();
    }

    boolean haveFocus = isFocusOwner();
    if( !haveFocus )
      haveFocus = requestFocusInWindow();
    if( DEBUG_LEVEL > 0 )
    {
      if( !haveFocus )
        System.out.print( "  DO NOT" );
      System.out.println( "  Have Focus!" );
    }
    
  }// mouseClicked()

  /** @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
   */
  public void mousePressed(MouseEvent e)
  { }
  /** @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
   */
  public void mouseReleased(MouseEvent e)
  { }
  /** @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
   */
  public void mouseEntered(MouseEvent e)
  { }
  /** @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
   */
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
    
    input = ke.getKeyChar();
    intInput = (int)input ;
    
    if( DEBUG_LEVEL > 0 )
      System.out.println( "BeaconClient.keyTyped = " + input
                          + "; int value = " + intInput      );

    if( intInput > 47  &&  intInput < 58 )
    {
      if( mine2dArray[activeSqrXY.x][activeSqrXY.y].getType() != FIXED )
        mine2dArray[activeSqrXY.x][activeSqrXY.y].setValue( intInput - 48 );
    }
    
    gameview.repaint();
  }

  /** @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
   */
  public void keyPressed(KeyEvent e)
  { }
  /** @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
   */
  public void keyReleased(KeyEvent e)
  { }

  /*
   *   INNER CLASSES
   * ================= */
  
  /**
  * The data structure behind every <code>Square</code> in the Grid.
  * - class <code>BeaconClient</code> uses a 2D array of <code>Squares</code>,
  *   i.e. Square[][]
  */
  class Square
  {
    /**  i.e. FIXED, OPEN, BAD, TEMP, SPECIAL? */
    protected int type ;
    
    /** for UNDO/REDO */ 
    protected int order ;
    
    /** i.e. 0(blank) - 9 */
    protected int value ;
    
    /** Does this Square have Keyboard Focus ? */
    protected boolean active ;

    public Square()
    {
      type = BeaconClient.OPEN ;
      order = 0 ;
      value = 0 ;
    }

    /**
     * @return order
     */
    public int getOrder()
    {
      return order;
    }

    /**
     * @return type
     */
    public int getType()
    {
      return type;
    }

    /**
     * @return value
     */
    public int getValue()
    {
      return value;
    }

    /**
     * @param i
     */
    public void setOrder(int i)
    {
      order = i;
    }

    /**
     * @param i
     */
    public void setType(int i)
    {
      type = i;
    }

    /**
     * @param i
     */
    public void setValue(int i)
    {
      value = i;
    }

    /**
     * @return active
     */
    public boolean isActive()
    {
      return active;
    }

    /**
     * @param b
     */
    public void setActive(boolean b)
    {
      active = b;
    }

  }/* inner class Square */

}// class BeaconClient
