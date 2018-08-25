/*
 * Created on 26-Oct-06
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package mhsjava;

import java.awt.* ;
import java.awt.event.* ;

import javax.swing.* ;
import javax.swing.event.* ;

/**
 * @author Mark Sattolo
 * @version 1.0  26-Oct-06
 */
public class SudokuSettings extends JFrame implements ActionListener
{
 /*
  *   FIELDS
  * ============================================= */
  
  static final String    strPERCENT = " % " ,
                      strSIZE_TITLE = "Square size:" ,
                     strMINES_TITLE = "Mine density:" ,
          
                      strSmall_ITEM = "small" ,
                     strMedium_ITEM = "Medium" ,
                      strLarge_ITEM = "LARGE" ,

                         strNEW_BTN = "New game" ,
                        strSIZE_BTN = "Apply new size" ,
                      strCANCEL_BTN = "Cancel" ;
  
  static final int   WINDOW_DEFAULT_WIDTH  = 520 ,
                     WINDOW_DEFAULT_HEIGHT = 260 ,
                    
                     DENSITY_MAJOR_TICK_SPACING = 20 ,
                     DENSITY_MINOR_TICK_SPACING =  5 ;
  
  int DEBUG_LEVEL ;
  
  double density ;
  int percentDensity, squareLength ;
  
  /** identify the source of action events */
  Object source ;
  /** reference to the enclosing class */
  SudokuGame gameview ;
  
  JPanel denPanel, sizePanel, btnPanel ;
  JLabel denHead, denInfo, sizeHead ;
  
  JSlider denSlider ;
  JButton newBtn, sizeBtn, cancelBtn ;
  
  ButtonGroup sizeGroup ;
  JRadioButton smallRadBtn, medRadBtn, largeRadBtn ;
  
  boolean startNew = false ;

 /*
  *   METHODS
  * ============================================= */
  
  public SudokuSettings( SudokuGame game )
  {
    DEBUG_LEVEL = game.DEBUG_LEVEL ;
    if( DEBUG_LEVEL > 0 )
      System.out.println( "SudokuSettings.DEBUG_LEVEL = " + DEBUG_LEVEL );
    
    gameview = game ;
    squareLength = SudokuGame.SQUARE_SIZE_MED ;
    
    setFont( game.fontMEDIUM );
    setTitle( SudokuGame.strSETTINGS_ITEM );
    setSize( WINDOW_DEFAULT_WIDTH, WINDOW_DEFAULT_HEIGHT );

    addWindowListener(
      new WindowAdapter()
        { public void windowClosing( WindowEvent we )
                      {
                        gameview.settingsOpen = false ;
                        dispose();
                      }
        } );
    
    getContentPane().setLayout( new GridLayout(3,1,3,3) );
    
    // density slider
    denPanel = new JPanel();
    density = game.SudokuGrid.getDensity();
    percentDensity = (int)( density * 100  );
    denHead = new JLabel( strMINES_TITLE );
    denInfo = new JLabel( Integer.toString(percentDensity) + strPERCENT );

    denSlider = new JSlider( 0, 100, percentDensity );
    denSlider.setMajorTickSpacing( DENSITY_MAJOR_TICK_SPACING );
    denSlider.setMinorTickSpacing( DENSITY_MINOR_TICK_SPACING );
    denSlider.setPaintTicks( true );
    denSlider.setPaintLabels( true );
    
    denSlider.addChangeListener(
      new ChangeListener()
        { public void stateChanged( ChangeEvent ce )
                      {
                        setDensity( ((JSlider)ce.getSource()).getValue() );
                      }
        } );
    
    denPanel.add( denHead );
    denPanel.add( denInfo );
    denPanel.add( denSlider  );
    
    // radio buttons for Square size
    sizePanel = new JPanel();
    sizeHead  = new JLabel( strSIZE_TITLE );
    sizeGroup = new ButtonGroup();
    
    smallRadBtn = new JRadioButton( strSmall_ITEM, false );
    smallRadBtn.addActionListener( this );
    medRadBtn   = new JRadioButton( strMedium_ITEM, true );
    medRadBtn.addActionListener( this );
    largeRadBtn = new JRadioButton( strLarge_ITEM, false );
    largeRadBtn.addActionListener( this );
    
    sizeGroup.add( smallRadBtn );
    sizeGroup.add( medRadBtn );
    sizeGroup.add( largeRadBtn );
    
    sizePanel.add( sizeHead   );
    sizePanel.add( smallRadBtn );
    sizePanel.add( medRadBtn );
    sizePanel.add( largeRadBtn );
    
    // action buttons
    btnPanel  = new JPanel();
    newBtn    = new JButton( strNEW_BTN    );
    sizeBtn   = new JButton( strSIZE_BTN   );
    cancelBtn = new JButton( strCANCEL_BTN );
    
       newBtn.addActionListener( this );
      sizeBtn.addActionListener( this );
    cancelBtn.addActionListener( this );
    
    btnPanel.add( newBtn    );
    btnPanel.add( sizeBtn   );
    btnPanel.add( cancelBtn );
    
    // insert the panels
    getContentPane().add( denPanel  );
    getContentPane().add( sizePanel );
    getContentPane().add( btnPanel  );
    
    game.settingsOpen = true ;
  
  }//! Constructor


  /**
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed(ActionEvent ae)
  {
    String info = ae.getActionCommand();
    if( DEBUG_LEVEL > 0 )
      System.out.println( "SudokuSettings ActionCommand: " + info );
    
    source = ae.getSource();
    if( source == smallRadBtn )
      squareLength = SudokuGame.SQUARE_SIZE_SMALL ;
    else if( source == medRadBtn )
      squareLength = SudokuGame.SQUARE_SIZE_MED ;
    else if( source == largeRadBtn )
      squareLength = SudokuGame.SQUARE_SIZE_LARGE ;
    else if( source == newBtn )
      {
        startNew = true ;
        confirm();
        dispose();
      }
    else if( source == sizeBtn )
      {
        startNew = false ;
        confirm();
        dispose();
      }
    else if( source == cancelBtn )
      {
        gameview.settingsOpen = false ;
        dispose();
      }
  
  }// actionPerformed()

  /** Called by the density slider */
  public void setDensity( int percent )
  {
    density = percent/100.0 ;
    percentDensity = percent ;
    denInfo.setText( Integer.toString(percentDensity) + strPERCENT );
   
  }// setDensity()

  /** Accept new values and close */
  protected void confirm()
  {
    gameview.newSquareLength( squareLength );
   
    if( startNew )
    {
      gameview.adjustDensity( density );
      gameview.halt();
      gameview.newGame();
    }
    
    gameview.settingsOpen = false ;
    
  }// confirm()

}/* class SudokuSettings */
