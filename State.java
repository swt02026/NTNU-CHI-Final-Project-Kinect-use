import processing.core.*;

import SimpleOpenNI.*;

import java.awt.Rectangle;

import gab.opencv.*;

import processing.opengl.*;

enum State {

	STATE_DETECT{

		@Override public void Do(PApplet p,SimpleOpenNI kinect) {
			faceImg = p.loadImage("1.jpg");
			//System.out.println("detect");
			DrawImage(p,kinect.rgbImage());
			Rectangle[] faces = DetectFace(p,kinect);
			MarkFace(p,kinect,faces);
		}

		void MarkFace(PApplet p,SimpleOpenNI kinect,Rectangle[] faces){

			p.noFill();
		    p.stroke(0, 255, 0);
    		p.strokeWeight(3);

    		p.pushMatrix();

	    	p.translate(p.width/2-kinect.rgbWidth() /2
      			, p.height/2-kinect.rgbHeight()/2);

			for (Rectangle face : faces) {

				PImage faceImg = p.get(face.x, face.y, face.width, face.height);
				p.rect(face.x, face.y, face.width, face.height);
			}

			p.popMatrix();
		}
	},

	STATE_DETECT_SAVE_FACE{

		Step step = Step.INIT;
		float offset = 0;
		final float eachStep = 16f;

		@Override public void Do(PApplet p,SimpleOpenNI kinect){

			DrawImage(p,kinect.rgbImage());

			switch (step) {

				case INIT:

					max_len = (int)p.dist(0,0,p.width,p.height);
					step = Step.MASKING;
					offset = max_len/2;
					p.noStroke();
				break;

				case MASKING:

					if(offset > 0){
						DrawBlackMask(p);		
						offset-=eachStep;
					}
					else {
						step = Step.UNMASKING;
					}
				break;

				case UNMASKING:

					if (offset < max_len/2) {
						DrawBlackMask(p);
						offset+=eachStep;
					}
					else {
						step = Step.FINAL;
					}
				break;

				case FINAL:

					SaveFace(p,kinect);
					nextState = State.STATE_DETECT_FINISH;
				break;
			}
			
			//System.out.println("save face image");
		}

		void DrawBlackMask(PApplet p){

			for (int i = 45; i < 360; i+=90) {

				DrawBlackRectangle(p,p.radians(i));
			}
		}

		void DrawBlackRectangle(PApplet p,float degree){

			p.fill(0);
			p.pushMatrix();
				p.translate(p.width/2,p.height/2);
				p.rotate(degree);
				p.translate(0,offset);
				p.rect(0,0,max_len,max_len);
			p.popMatrix();
		}

		void SaveFace(PApplet p,SimpleOpenNI kinect){

			p.background(255);
			p.image(kinect.rgbImage(), 0, 0);
			Rectangle[] faces = DetectFace(p,kinect);
	    	
	    	for (Rectangle face : faces) {

	    		faceImg = p.get(face.x, face.y, face.width, face.height);
	    		int indexOfFace = java.util.Arrays.asList(faces).indexOf(face);
      			faceImg.save(Integer.toString(indexOfFace)+".jpg");
	    	}
		}
	},

	STATE_DETECT_FINISH{
		@Override public void Do(PApplet p,SimpleOpenNI kinect) {

			//System.out.println("detect finish");
			p.background(255);
			PImage startGame = p.loadImage("logo.png");
			DrawImage(p,startGame);

			if(findHand){
				nextState = STATE_GAME_START;
			}
		}

	},

	STATE_GAME_START{
		@Override public void Do(PApplet p,SimpleOpenNI kinect){
			
			p.background(0);
			p.pushMatrix();
				p.translate(p.width/2, p.height/2, -250);

				DrawBox(p);
				DrawKinectScene(p,kinect);

			p.popMatrix();
			
		}

		void DrawKinectScene(PApplet p,SimpleOpenNI kinect){

			p.pushMatrix();
  			p.rotateX(p.radians(180));
  			p.translate(0,0,100);

			PVector[] depthPoints = kinect.depthMapRealWorld();

			p.beginShape(p.POINTS);
				for (int i = 0; i < depthPoints.length; i+=4){

	     			PVector currentPoint = depthPoints[i];
	     			p.stroke(kinect.rgbImage().pixels[i]);
	     			p.vertex(currentPoint.x, currentPoint.y, currentPoint.z); 
					//System.out.println("in 3D");     		
				}
			p.endShape();
			p.popMatrix();

		}

		void DrawBox(PApplet p){
			p.pushMatrix();
			System.out.println(handPosition.toString());
			p.translate(handPosition.x,-handPosition.y,-handPosition.z);
			p.rotateX(p.radians(rotateAngle));
			p.noStroke();

			p.box(100);
			TexBox(p,100);

			if(rotateAngle < 360.0f){
				rotateAngle+=5f;
			}
			else {
				rotateAngle-=360;
			}

			p.popMatrix();
		}
		void TexBox(PApplet p,float boxEdge){
			for (int i = 0; i< 360 ; i+=90) {

				p.pushMatrix();
					p.rotateY(p.radians(i));
					drawTex(p,boxEdge);
  				p.popMatrix();
  			}
  			for (int i = 90; i >=-90 ; i-=180) {

  				p.pushMatrix();
  					p.rotateX(p.radians(i));
  					drawTex(p,boxEdge);
  				p.popMatrix();		
  			}
		}
		void drawTex(PApplet p,float boxEdge){
			p.beginShape();
			p.texture(faceImg);
			p.vertex(-boxEdge/2, -boxEdge/2, 50,
			 0,   0);
			
			p.vertex( boxEdge/2, -boxEdge/2, 50,
			 faceImg.width, 0);
			
			p.vertex( boxEdge/2, boxEdge/2, 50, 
				faceImg.width, faceImg.height);
			p.vertex(-boxEdge/2, boxEdge/2, 50, 
				0,   faceImg.height);
			p.endShape();
		}

	};

	OpenCV opencv;

	static State nextState ;

	int max_len = 0;

	static public int deep = -200;

	static PImage faceImg;

	static public PVector handPosition = new PVector();

	static public boolean findHand = false;

	float rotateAngle = 0.0f;

	enum Step{

		INIT,
		MASKING,
		UNMASKING,
		FINAL
	}

	public abstract void Do(PApplet p,SimpleOpenNI kinect);

	void DrawImage(PApplet p,PImage img){
		
		p.pushMatrix();

	    p.translate(p.width/2-img.width /2
      		, p.height/2-img.height/2);
		
		p.background(255);
		p.image(img,0,0);

		p.popMatrix();
	}

	Rectangle[] DetectFace(PApplet p,SimpleOpenNI kinect){

		opencv = new OpenCV(p,kinect.rgbImage());
		opencv.loadCascade(OpenCV.CASCADE_FRONTALFACE); 
		return opencv.detect();
	}

	public static void setNextState(State _state){

		nextState = _state ;
	}

	public static State getNextState(){

		return nextState;
	}

}


