import SimpleOpenNI.*;
import gab.opencv.*;
import gifAnimation.*;
import processing.opengl.*;

SimpleOpenNI  context = new SimpleOpenNI(this);

State _state= State.STATE_DETECT;

Gif progress;

int max_len = 0;

int progressTime = 0;

int i = 0;

boolean isCaptureFace = false;

PVector progressPosition = new PVector();

void setup() {

	background(200,0,0);

	context.enableDepth();
	context.enableRGB();
	context.setMirror(true);
	context.enableHand();
	context.startGesture(SimpleOpenNI.GESTURE_HAND_RAISE);	
	context.startGesture(SimpleOpenNI.GESTURE_WAVE);

	State.setNextState(State.STATE_DETECT);

	progress = new Gif(this,"05043120.gif");

	progress.play();
	max_len= (int)dist(context.rgbWidth() 
		, context.rgbHeight(),0,0);
	
	if(context.isInit() == false)
  	{
		println("Can't init SimpleOpenNI, maybe the camera is not connected!"); 
		exit();
		return;  
  	}

	size(max_len,max_len,P3D); 

}

void draw() {

	background(255);
	context.update();
	_state.Do(this,context);
	_state = State.getNextState();
	//image(progress, 10, 10);

	if(isCaptureFace && i < 50){

	  	translate(width/2-context.rgbWidth() /2
  			, height/2-context.rgbHeight()/2);
		image(progress, progressPosition.x, progressPosition.y);
		i++;
	}
	else if(isCaptureFace && i!=0){

		progress.stop();
	}
}

void onCompletedGesture(SimpleOpenNI curContext
  , int gestureType
  , PVector pos){
	println(gestureType);
	switch (gestureType) {
		case 0:
		if(!State.findHand && _state == State.STATE_DETECT_FINISH){
			context.startTrackingHand(pos);
			State.handPosition = pos;
			State.findHand = true;
		}
		break;
		case 2:
		if(_state == State.STATE_DETECT){

			context.convertRealWorldToProjective(pos,progressPosition);
			isCaptureFace = true;
			State.setNextState(State.STATE_DETECT_SAVE_FACE);
			//println(pos.toString());
		}
		break;	

	}
	
}

void mouseWheel(MouseEvent event){

	_state.deep+= 10*event.getCount();
	println(_state.deep);
}

void onLostHand(SimpleOpenNI curContext,int handId){
	State.setNextState(State.STATE_DETECT_FINISH);
	State.findHand = false;
}

void onTrackedHand(SimpleOpenNI curContext,int handId,PVector pos){
	State.handPosition = pos;
}
void onNewHand(SimpleOpenNI curContext,int handId,PVector pos)
{
  println("onNewHand - handId: " + handId + ", pos: " + pos);
}
