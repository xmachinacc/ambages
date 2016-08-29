package applogic;
import android.graphics.Bitmap;
import android.graphics.Color;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created on 8/24/2016.
 */

//TODO: Document and test! This app is just some untested and undocumented fun so far.

public class Maze {

    private int[][] maze;

    private int height;
    private int width;

    private int totalPixels;

    private MazePoint start;
    private MazePoint end;

    public Maze(Bitmap bitmap, double threshold, MazePoint start, MazePoint end){

        int[][] maze = new int[bitmap.getHeight()][bitmap.getWidth()];

        height = bitmap.getHeight();
        width = bitmap.getWidth();
        totalPixels = height*width;

        this.start = start;
        this.end = end;

        int colorIntensityBucket = 0;
        Integer pixelCount = 0;

        for (int y = 0; y < height; y++)
        {
            for (int x = 0; x < width; x++)
            {
                int c = bitmap.getPixel(x, y);

                pixelCount++;
                colorIntensityBucket += (Color.red(c) + Color.green(c) + Color.blue(c))/3;
            }
        }

        Integer averageColorIntensity = colorIntensityBucket/pixelCount;

        for (int y = 0; y < height; y++)
        {
            for (int x = 0; x < width; x++)
            {
                int c = bitmap.getPixel(x, y);

                int colorIntensity = (Color.red(c) + Color.green(c) + Color.blue(c))/3;

                if(colorIntensity > averageColorIntensity*threshold){
                    maze[y][x] = 0;
                }else{
                    maze[y][x] = 1;
                }

            }
        }

        this.maze = maze;
    }

    public List<MazePoint> getSolution() throws InterruptedException{

        if(start.equals(end)){
            return new ArrayList<>();
        }

        BlockingQueue<MazePoint> queue = new ArrayBlockingQueue<>(totalPixels);
        Set<MazePoint> discovered = new HashSet<>();
        discovered.add(start);

        Map<MazePoint, MazePoint> nodeToParent = new HashMap<>();
        nodeToParent.put(start, null);

        queue.add(start);

        while(!queue.isEmpty()){
            MazePoint node = queue.take();

            for(MazePoint childNode : successors(node)){

                if(!discovered.contains(childNode)) {
                    nodeToParent.put(childNode, node);
                    if (childNode.equals(end)) {
                        return extractPath(nodeToParent, childNode);
                    }
                    queue.put(childNode);
                    discovered.add(childNode);
                }
            }

        }

        return null;

    }

    private static List<MazePoint> extractPath(Map<MazePoint, MazePoint> nodeToParent, MazePoint end){
        List<MazePoint> path = new ArrayList<>();

        MazePoint node = end;

        MazePoint parent;
        while((parent = nodeToParent.get(node)) != null){
            path.add(parent);
            node = parent;
        }

        return path;
    }

    private List<MazePoint> successors(MazePoint coordinate){
        List<MazePoint> potential = new ArrayList<>();

        potential.add(new MazePoint(coordinate.getX()+1, coordinate.getY()+1));
        potential.add(new MazePoint(coordinate.getX()-1, coordinate.getY()-1));
        potential.add(new MazePoint(coordinate.getX()+1, coordinate.getY()-1));
        potential.add(new MazePoint(coordinate.getX()-1, coordinate.getY()+1));
        potential.add(new MazePoint(coordinate.getX()+1, coordinate.getY()));
        potential.add(new MazePoint(coordinate.getX(), coordinate.getY()+1));
        potential.add(new MazePoint(coordinate.getX(), coordinate.getY()-1));
        potential.add(new MazePoint(coordinate.getX()-1, coordinate.getY()));


        List<MazePoint> actual = new ArrayList<>();

        for(MazePoint potentialCoordinate : potential) {

            if (potentialCoordinate.getX() >= 0 && potentialCoordinate.getY() >= 0 && potentialCoordinate.getX() < width &&
                    potentialCoordinate.getY() < height && maze[potentialCoordinate.getY()][potentialCoordinate.getX()] == 0) {
                actual.add(potentialCoordinate);
            }
        }

        return actual;

    }

    public static Bitmap drawSolution(Bitmap bitmap, List<MazePoint> path){
        Bitmap copy = bitmap.copy(bitmap.getConfig(), true);

        int color = Color.RED;

        for(MazePoint coordinate : path){
            copy.setPixel(coordinate.getX(), coordinate.getY(), color);
        }

        return copy;
    }
}
