/*
 * BeaconLoader.java
 *
 * Created on November 12, 2006, 7:22 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package mhsjava;

import java.awt.Point;
import java.io.* ;

/**
  * @author Mark Sattolo
  * 
  * @version 1.0  Nov.17/2006
  */
public class BeaconLoader
{
  /*
   *   FIELDS
   * ============================================= */
  
  static int INT_SPACE_CHAR = (int)' ' ;
  
  int DEBUG_LEVEL ;
  
  int numLoadedGames = 0 ;
  SavedGame[] games ;
  
  int lineSep1, lineSep2 = 0 ;
  boolean doubleLineSep = false ;
      
  String defaultDir = "C:\\Documents and Settings\\mhsatto\\My Documents\\IBM\\wsappdev51\\workspace\\Gbs\\saved\\" ;
  String gameSuffix = ".lsq" ;
  
  FileReader in ;
  BeaconServer gameview ;
  
  /*
   *   METHODS
   * ============================================= */
  
  /** Creates a new instance of BeaconLoader */
  public BeaconLoader( BeaconServer frame, int numGames )
  {
    DEBUG_LEVEL = frame.DEBUG_LEVEL ;
    if( DEBUG_LEVEL > 0 )
      System.out.println( "BeaconLoader.DEBUG_LEVEL = " + DEBUG_LEVEL );
    
    gameview = frame ;
    
    init( numGames );
  }

  private void init( int numGames )
  {
    games = new SavedGame[ numGames ];
    
    getSystemProperties();
  }
  
  private void getSystemProperties()
  {
    String lineSep = System.getProperty( "line.separator" );
    if( DEBUG_LEVEL > 0 )
      System.out.println( "System line separator length = " + lineSep.length() );
      
    lineSep1 = (int)lineSep.charAt(0);
    if( lineSep.length() > 1 )
    {
      lineSep2 = (int)lineSep.charAt(1);
      doubleLineSep = true ;
    }
      
    if( DEBUG_LEVEL > 0 )
      System.out.println( "System line separator is \"" + lineSep1 + ":"
                          + lineSep2 + "\" and space is \"" + INT_SPACE_CHAR + "\"" );
    
  }
  
  void loadGames( String dirName ) throws IOException
  {
    File dir = new File( dirName );
    if( dir.isDirectory() )
    {
      if( DEBUG_LEVEL > 0 )
        System.out.println( "BeaconLoader.loadGames(): Loading from Directory \""
                            + dirName + "\"" );
      
      File[] children = dir.listFiles();
      for( int i=0 ;
           i < gameview.numSavedGames  &&  i < children.length ;
           i++ )
      {
        if( !children[i].isDirectory() )
        {
          if( DEBUG_LEVEL > 0 )
            System.out.println( "BeaconLoader.loadGames(): Loading file \""
                                + children[i].toString() + "\"" );
          loadGame( children[i].toString() );
        }
      }// for( files )
    }
    else
    if( DEBUG_LEVEL > 0 )
      System.err.println( "loadGames() PROBLEM: \"" + dirName + "\" is NOT a Directory! " );
      
  }// loadGames()
  
  void loadGame( String fileName ) throws IOException
  {
    if( numLoadedGames == gameview.numSavedGames )
    {
      if( DEBUG_LEVEL > 0 )
        System.err.println( "BeaconLoader.loadGame(): MAX GAMES REACHED!" );
      return ;
    }
    else
        games[numLoadedGames] = new SavedGame( gameview.field.gridLength );
    
    int loadGameDEBUG_LEVEL = 1 ;

    boolean haveSqrCoords = false ;
    int num, procNum, col = 0, row = 0;
    try
    {
      in = new FileReader( new File(fileName) );

      if( DEBUG_LEVEL > loadGameDEBUG_LEVEL )
        System.out.println( "Row #" + row );
      // read til EOF
      while( (num = in.read()) != -1 )
      {
        // check for EOL
        if( num == lineSep1 )
        {
          if( DEBUG_LEVEL > loadGameDEBUG_LEVEL )
            System.out.print( " \\\\EOL1" );
          if( doubleLineSep )
          {
            in.read();
            if( DEBUG_LEVEL > loadGameDEBUG_LEVEL )
              System.out.println( " EOL2" );
          }
          else
              if( DEBUG_LEVEL > loadGameDEBUG_LEVEL )
                System.out.print( "\n" );
          
          // move to the next row
          row++ ;
          if( DEBUG_LEVEL > loadGameDEBUG_LEVEL  &&  row < gameview.field.gridLength )
            System.out.println( "Row #" + row );
        }
        else if( num != INT_SPACE_CHAR )
          {
            // process the int
            procNum = num - BeaconClient.BASE_KEYCODE_INDEX ;
            if( DEBUG_LEVEL > loadGameDEBUG_LEVEL )
              System.out.print( procNum );

            if( !haveSqrCoords ) // get the first non-SPACE int of a pair
            {
              // assign the col
              col = procNum ;
              haveSqrCoords = true ;
            }
            else // have a pair
              {
                // assign the val
                games[numLoadedGames].game[row][col] = procNum ;
                haveSqrCoords = false ;
                if( DEBUG_LEVEL > loadGameDEBUG_LEVEL )
                  System.out.print( " " );
              }
          }
          
      }// while( not EOF )
    
    }
    catch( IOException ioe )
    {
      if( DEBUG_LEVEL > 0 )
        // Auto-generated catch block
        ioe.printStackTrace();
    }
    finally
    {
      if( in != null )
      {
        in.close();
        in = null ;
      }
    }
    
    // TEST
    if( DEBUG_LEVEL > loadGameDEBUG_LEVEL + 1 )
      games[numLoadedGames].display();
    
    numLoadedGames++ ;
    
  }// loadGame()
  
  void loadBareFileNameGame( String bareFileName ) throws IOException
  {
    loadGame( defaultDir + bareFileName + gameSuffix );
  
  }// loadBareFileNameGame()
  
  /**
   * Get <code>Square</code> values from a particular loaded game 
   * to insert into the Grid.
   *   :TODO - specify and return games of differing difficulties
   * 
   * @param index
   * @param difficulty
   * @return int[][]
   */
  int[][] getGame( int index, int difficulty )
  {
    if( numLoadedGames > index )
    {
      if( DEBUG_LEVEL > 0 )
        System.out.println( "BeaconLoader.getGame(): returning Game #" + index );
      return games[index].game ;
    }
    else
      if( numLoadedGames > 0 )
      {
        if( DEBUG_LEVEL > 0 )
          System.err.println( "BeaconLoader.getGame() PROBLEM: index #" + index
                              + " is NOT valid -- returning game #" + (numLoadedGames-1) );
        return games[numLoadedGames-1].game ;
      }
      else
        { 
          if( DEBUG_LEVEL > 0 )
            System.err.println( "BeaconLoader.getGame(): NO GAMES LOADED!" );
          return null ;
        }
  }
  
  /*
   *   INNER CLASSES
   * ============================================= */
  
  /**
   * 
   */
  class SavedGame
  {
    private int[][] game ;
    private int length ;
    private int difficulty = 0;
    
    public SavedGame( int len )
    {
      if( DEBUG_LEVEL > 0 )
        System.out.println( "Create BeaconLoader.SavedGame" );
      
      length = len ;
      game = new int[ length ][ length ];
    }

    public SavedGame( int len, int diff )
    {
      this( len );
      
      difficulty = diff ;
    }

    void load( int row, int col, int val )
    {
      game[row][col] = val ;
    }
    
    // print to System.out
    void display()
    {
      System.out.println( "GAME: length = " + length 
                          + " & difficulty = " + difficulty );
      
      int i, j;
      for( i=0; i<length; i++ )
      {
        for( j=0; j<length; j++ )
          System.out.print( game[i][j] + " " );
        System.out.print( "\n" );
      }
    } // display()
    
  }/* Inner Class SavedGame */

}// CLASS BeaconLoader
