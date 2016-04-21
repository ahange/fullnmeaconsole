package nmea.ui.deviation.deviationcurve.util;

public final class SquareMatrix
{
  private int dimension;
  private double[][] matrixElements;

  public SquareMatrix()
  {
  }  
  
  public SquareMatrix(int n)
  {
    this(n, false);
  }

  public SquareMatrix(int n, boolean init)
  {
    this.dimension = n;
    matrixElements = new double[n][n];
    if (init)
    {
      for (int l=0; l<n; l++)
      {
        for (int c=0; c<n; c++)
          matrixElements[l][c] = 0d;
      }
    }
  }

  public void setDimension(int dim)
  {
    this.dimension = dim;
    matrixElements = new double[dim][dim];
  }

  public int getDimension()
  {
    return (this.dimension);
  }

  public void setElementAt(int row, int col, double val)
  {
    matrixElements[row][col] = val;
  }

  public double getElementAt(int row, int col)
  {
    return matrixElements[row][col];
  }

  public double[][] getmatrixElements()
  {
    return this.matrixElements;
  }

  public void setmatrixElements(double[][] me)
  {
    this.matrixElements = me;
  }
} 
