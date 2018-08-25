/*
 * Created on 31-Oct-06
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package mhsjava;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JLabel;

/**
 * @author Mark Sattolo
 * @version 1.0  31-Oct-06
 */
public class BeaconClient extends JLabel implements MouseListener, KeyListener
{
  /*
   *   FIELDS
   * ============================================= */
  
  private double density = 0.25 ;

  private boolean 
                  shaded = false ,
                 lightup = false ,
                paintAll = true  ;

  public boolean firstPress = true ;

  /** different types of <code>Squares</code> */
  static final int BLANK   =  9 ,
                   FIXED   = 10 , 
                   GOOD    = 11 , 
                   BAD     = 12 , 
                   SPECIAL = 13  ;
        
  static final Color COLOR_BRIGHT  = Color.white , 
                     COLOR_BLAZE   = Color.yellow ,
                     COLOR_BLAST   = Color.orange ,
                     COLOR_BOLD    = Color.red ,
                     COLOR_SHADE   = Color.pink ,
                     COLOR_LIGHT   = Color.lightGray ,
                     COLOR_MEDIUM  = Color.gray ,
                     COLOR_DARK    = Color.darkGray ,
                     COLOR_DARKEST = Color.black ,
                     COLOR_BRUN    = new Color( 107, 69, 38 );
  
  int DEBUG_LEVEL ;
  
  /** number of <code>Squares</code> on each side of the 2D mine array */
  int fieldLength ;
  /** length of sides (in pixels) of each individual <code>Square</code> */
  int squareLength ;
  
  int destx, desty, paintLeft, paintRight, paintTop, paintBottom ;
  
  /** reference to the enclosing class */
  BeaconServer gameview ;
  /** 2D array of individual mine <code>Squares</code> */
  Square[][] mine2dArray ;
  
  /**
  * This is the default constructor
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
    squareLength = BeaconServer.SQUARE_SIZE ;
    fieldLength = BeaconServer.GRID_SIZE ;
    setSize( fieldLength*squareLength, fieldLength*squareLength );

    mine2dArray = new Square[ fieldLength ][ fieldLength ];
    int i, j ;
    for( i=0; i<fieldLength; i++ )
      for( j=0; j<fieldLength; j++ )
        mine2dArray[i][j] = new Square();

    //setBackground( BeaconServer.COLOR_GAME_BKGRND );
    
    addMouseListener( this );
    addKeyListener( this );
    
    setFont( gameview.myFont );
    //setLocation( BeaconServer.X_BORDER, BeaconServer.Y_BORDER/2 );
    
  }// init()
  
  /** Reset the 2d array */
  protected void newField()
  {
    firstPress = true ;

    int i, j ;
    for( i=0; i<fieldLength; i++ )
      for( j=0; j<fieldLength; j++ )
      {
        if( Math.random() <= density )
          mine2dArray[i][j].setType( BeaconClient.FIXED );
        else
            mine2dArray[i][j].setType( BeaconClient.BLANK );
      }

    gameview.validate();
    
  }// newField()

  /**
   * Need to refresh only a certain portion of the array.
   * - calls <code>repaint( left, top, width, depth )</code>;
   */
  protected void paintArea()
  {
    if( DEBUG_LEVEL > 1 )
      System.out.println( "BeaconClient.paintArea(): Left = " + paintLeft + "; Right = " + paintRight
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
          reveal( i*squareLength, j*squareLength,
                  mine2dArray[i][j].getType(), page );
        }
    }
    else
        System.err.println( "PROBLEM: Graphics page is NULL!" );
    
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
      System.out.println( "BeaconClient.reveal(): xc = " + xc + "; yc = " + yc 
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
      case GOOD:
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
    
      case FIXED:
                drawSquare( xc, yc, page );
                page.setColor( COLOR_BRUN );
                page.fillOval( xc + sl/8, yc + sl/8, 3*sl/4, 3*sl/4 );
                page.setColor( COLOR_SHADE );
                page.fillRect( xc + sl/8, yc + 3*sl/8, 3*sl/4, sl/4 );
                page.setColor( COLOR_BLAZE );
                page.drawString( "*", xc + 3*sl/8, yc + 7*sl/8 );
                break ;
    
      case SPECIAL:
                drawSquare( xc, yc, page );
                page.setColor( COLOR_DARKEST );
                page.drawString( "?", xc + sl/4, yc + 3*sl/4 );
                break ;
    
      case BLANK:
                drawSquare( xc, yc, page );
                break ;
      
      case BAD:
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
    
    }// switch Square type
    
    if( type < BLANK && type > 0 )
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
  
  /* (non-Javadoc)
   * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
   */
  public void mouseClicked(MouseEvent e)
  {
    // TODO Auto-generated method stub

  }

  /* (non-Javadoc)
   * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
   */
  public void mousePressed(MouseEvent e)
  {
    // TODO Auto-generated method stub

  }

  /* (non-Javadoc)
   * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
   */
  public void mouseReleased(MouseEvent e)
  {
    // TODO Auto-generated method stub

  }

  /* (non-Javadoc)
   * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
   */
  public void mouseEntered(MouseEvent e)
  {
    // TODO Auto-generated method stub

  }

  /* (non-Javadoc)
   * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
   */
  public void mouseExited(MouseEvent e)
  {
    // TODO Auto-generated method stub

  }

  /* (non-Javadoc)
   * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
   */
  public void keyTyped(KeyEvent e)
  {
    // TODO Auto-generated method stub

  }

  /* (non-Javadoc)
   * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
   */
  public void keyPressed(KeyEvent e)
  {
    // TODO Auto-generated method stub

  }

  /* (non-Javadoc)
   * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
   */
  public void keyReleased(KeyEvent e)
  {
    // TODO Auto-generated method stub

  }

 /*
  *   INNER CLASSES
  * ================= */
  
  /**
  * The data structure behind every square in the BeaconClient.
  * - class <code>BeaconClient</code> uses a 2D array of <code>Squares</code>,
  *   i.e. Square[][]
  */
  class Square
  {
    protected int type, order, value ;

    public Square()
    {
      type = BeaconClient.BLANK ;
      order = 0 ;
      value = 0 ;
    }

    /**
     * @return
     */
    public int getOrder()
    {
      return order;
    }

    /**
     * @return
     */
    public int getType()
    {
      return type;
    }

    /**
     * @return
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

  }/* inner class Square */

}
