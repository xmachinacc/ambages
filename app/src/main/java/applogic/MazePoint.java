package applogic;

import java.util.Objects;

/**
 * Created on 8/24/2016.
 */

//TODO: Document and test! This app is just some untested and undocumented fun so far.

public class MazePoint {

    private final int x;
    private final int y;

    public MazePoint(int x, int y){
        this.x = x;
        this.y = y;
    }

    public int getX(){
        return x;
    }

    public int getY(){
        return y;
    }

    @Override
    public boolean equals(Object that){
        if(!(that instanceof MazePoint)){
            return false;
        }
        MazePoint thatMazePoint = (MazePoint) that;
        return thatMazePoint.getX() == getX() && thatMazePoint.getY() == getY();
    }

    @Override
    public int hashCode(){
        return Objects.hash(getX(),getY());
    }

    @Override
    public String toString(){
        return "("+x+","+y+")";
    }

}

