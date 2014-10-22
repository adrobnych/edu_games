package com.example.snowflakegrowth;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;


public class MainActivity extends ActionBarActivity {
    List<Particle> particles = new LinkedList<MainActivity.Particle>();
    int DIMENTION_OF_ARRAY = 800;
    Particle[][] arrayOfParticles = new Particle[DIMENTION_OF_ARRAY][DIMENTION_OF_ARRAY];
    Point centerOfScreen = new Point();
    double temperature = 3.5;

    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(new DrawingPanel(this));
        
        initParticles();
    }
    
    @SuppressLint("NewApi") private Point getScreenSize(){
    	Display display = getWindowManager().getDefaultDisplay();
    	final Point size = new Point();
    	if(android.os.Build.VERSION.SDK_INT >= 13)
    		display.getSize(size);
    	else{
    		size.x = display.getWidth();
    		size.y = display.getHeight();
    	}
    	return size;
    }
    
    private void initParticles(){
    	Point size_of_view = getScreenSize();
    	int x = size_of_view.x/2;
    	int y = (size_of_view.y - 100)/2;
    	centerOfScreen.x = x;
    	centerOfScreen.y = y;
    	
    	Particle particleOne = new Particle(x, y);


    	particles.add(particleOne); 
    	  
    }
    
    private void initPaint(Particle particleOne){
    	particleOne.colorPaint = new Paint();
    	particleOne.colorPaint.setDither(true);
    	particleOne.colorPaint.setColor(0xFFFFFFFF);
    	particleOne.colorPaint.setStyle(Paint.Style.STROKE);
    	particleOne.colorPaint.setStrokeJoin(Paint.Join.ROUND);
    	particleOne.colorPaint.setStrokeCap(Paint.Cap.ROUND);
    	particleOne.colorPaint.setStrokeWidth(2);
    }
    
    public Point toScreenCoordinates(int sX, int sY){
		Point point = new Point();
		point.x = centerOfScreen.x + (sX - DIMENTION_OF_ARRAY/2) * 2;
		point.y = centerOfScreen.y + (sY - DIMENTION_OF_ARRAY/2) * 2;
		return point;
	}
    
    class Particle{
    	private Paint colorPaint;
        private int x, y;
        private ArrayList<Path> _graphics = new ArrayList<Path>();
        
    	public Particle(int x, int y) {
			this.x = x;
			this.y = y;
			create_graphics();
			Point positionInArray = toArrayCoordinates();
			arrayOfParticles[positionInArray.x][positionInArray.y] = this;
		}
    	
    	public Point toArrayCoordinates(){
    		Point point = new Point();
    		point.x = (x - centerOfScreen.x)/2  +  DIMENTION_OF_ARRAY/2;
    		point.y = (y - centerOfScreen.y)/2  +  DIMENTION_OF_ARRAY/2;
    		return point;
    	}
    	
		private void create_graphics() {
			initPaint(this);
			Path path = new Path();
        	path.moveTo(x,y);
        	path.lineTo(x+1, y);
        	_graphics.add(path);
        	path = new Path();
        	path.moveTo(x+1,y);
        	path.lineTo(x+1, y+1);
        	_graphics.add(path);
        	path = new Path();
        	path.moveTo(x+1,y);
        	path.lineTo(x, y+1);
			_graphics.add(path);
		}

		public List<Point> getNeighbours() {
			List<Point> resultList = new LinkedList<Point>();
			
			resultList.add(new Point(toArrayCoordinates().x - 1, toArrayCoordinates().y));
			resultList.add(new Point(toArrayCoordinates().x + 1, toArrayCoordinates().y));
			
			resultList.add(new Point(toArrayCoordinates().x - 1, toArrayCoordinates().y - 1));
			resultList.add(new Point(toArrayCoordinates().x, toArrayCoordinates().y - 1 ));
			
			resultList.add(new Point(toArrayCoordinates().x - 1, toArrayCoordinates().y + 1));
			resultList.add(new Point(toArrayCoordinates().x, toArrayCoordinates().y + 1 ));
			
			return resultList;
		}
    }

    class DrawingPanel extends SurfaceView implements SurfaceHolder.Callback 
    {
        private DrawingThread _thread;
        private Path path;

        public DrawingPanel(Context context) 
        {
           super(context);
           run_new_thread();
        }
        
        private void run_new_thread(){
        	getHolder().addCallback(this);
            _thread = new DrawingThread(getHolder(), this);
        }


        public boolean onTouchEvent(MotionEvent event) 
        {
            synchronized (_thread.getSurfaceHolder()) {
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                	_thread._non_stop = false;
//                }else if(event.getAction() == MotionEvent.ACTION_MOVE){
//                    path.lineTo(event.getX(), event.getY());
                }else if(event.getAction() == MotionEvent.ACTION_UP){
                	_thread._non_stop = true;
                }
                
                return true;
            }
        }

        @Override
        public void onDraw(Canvas canvas) {
        	next_step();
        	for(Particle p : particles){

        		for (Path path : p._graphics){
        			//Log.e("show_debug", "path: " + path);
                    canvas.drawPath(path, p.colorPaint);
                    
        		}
        	}
        }
        
        private void next_step(){
        	List<Particle> borned = new LinkedList<Particle>();
        	for(Particle p : particles){
        		born_particles(p, borned);
        	}
        	particles.addAll(borned);
        }
        
        private void born_particles(Particle p, List<Particle> borned){
        	List<Point> neighbours = p.getNeighbours();
        	for(Point n : neighbours){
        		Point screenCoords = toScreenCoordinates(n.x, n.y);
        		if(arrayOfParticles[n.x][n.y] == null){
        		Particle bornParticle = new Particle(screenCoords.x, screenCoords.y);
        		borned.add(bornParticle);
        		}
        	}
        }
        

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width,
                                   int height) {
            // TODO Auto-generated method stub

        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            // TODO Auto-generated method stub
            _thread.setRunning(true);
            _thread.start();
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            // TODO Auto-generated method stub
        	_thread.setRunning(false);
        }
    }

    @SuppressLint("WrongCall") class DrawingThread extends Thread {
        private SurfaceHolder _surfaceHolder;
        private DrawingPanel _panel;
        private boolean _run = false;
        public boolean _non_stop = true;

        public DrawingThread(SurfaceHolder surfaceHolder, DrawingPanel panel) {
            _surfaceHolder = surfaceHolder;
            _panel = panel;
        }

        public void setRunning(boolean run) {
            _run = run;
        }

        public SurfaceHolder getSurfaceHolder() {
            return _surfaceHolder;
        }

        @Override
        public void run() {
            Canvas c;
            while (_run) {
                c = null;
                try {
                    c = _surfaceHolder.lockCanvas(null);
                    synchronized (_surfaceHolder) {
                    	
                    	if(_run  && _non_stop){
                        _panel.onDraw(c);
                    	}
                    
                    }
                } finally {
                    if (c != null) {
                        _surfaceHolder.unlockCanvasAndPost(c);
                    }
                }
            }
        }
    }
}