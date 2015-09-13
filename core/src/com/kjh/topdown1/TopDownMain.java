package com.kjh.topdown1;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class TopDownMain extends ApplicationAdapter {
	SpriteBatch batch;
	//Texture img;
	//BitmapFont font;
	FPSLogger fpsLogger = new FPSLogger();
	OrthographicCamera camera;
	//Texture background;
	TextureRegion bgregion, terrainBelow, terrainAbove;
	float terrainOffset=0;
	Animation plane;
	float planeAnimTime=0;
	Vector2 planeVelocity = new Vector2();
	Vector2 planePosition = new Vector2();
	Vector2 planeDefaultPosition = new Vector2();
	Vector2 gravity = new Vector2();
	private static final Vector2 damping = new Vector2(0.99f, 0.99f);
	TextureAtlas atlas;
	Viewport viewport;
	// for input
	Vector3 touchPosition = new Vector3();
	Vector2 tmpVector = new Vector2();
	private static final int TOUCH_IMPULSE = 500;
	TextureRegion tapIndicator1, tapIndicator2;
	float tapDrawTime;
	private static final float TAP_DRAW_TIME_MAX=1.0f;
	Texture gameOver;
	Vector2 scrollVelocity = new Vector2();
	Array<Vector2> pillars = new Array<Vector2>();
	Vector2 lastPillarPosition = new Vector2(0,0);
	float deltaPosition;
	TextureRegion pillarUp, pillarDown;
	Rectangle planeRect = new Rectangle();
	Rectangle obstacleRect = new Rectangle();
	Music music;
	Sound tapSound;
	Sound crashSound;
	Sound spawnSound;
	// game state manager
	static enum GameState {
		INIT, ACTION, GAME_OVER
	}
	GameState gameState = GameState.INIT;


	@Override
	public void create () {
		resetScene();
		// for giggles lets try an alloc
		class inner1 {
			int blocktype;
			int subtype;
		}

		inner1[][] worldarray = new inner1[1024][1024];
		for (int a = 0 ;a<1024;a++) {
 			for (int b = 0; b < 1024; b++) {
				inner1 i = worldarray[a][b] = new inner1();
			}
		}
		Gdx.app.log("Val 7 7", Integer.toString(worldarray[7][7].blocktype));

		batch = new SpriteBatch();
		//img = new Texture("badlogic.jpg");
		//font = new BitmapFont(Gdx.files.internal("verdana39.fnt"), Gdx.files.internal("verdana39.png"), false);
		//font.setColor(Color.PURPLE);
		atlas = new TextureAtlas(Gdx.files.internal("TopDown.pack"));
		camera = new OrthographicCamera();
		//camera.setToOrtho(false,800,480);
		camera.position.set(400,240,0);// set to center of viewport
		viewport = new FitViewport(800,480,camera);
		bgregion = atlas.findRegion("background");

		//new  Texture("background.png");
		terrainBelow = atlas.findRegion("groundGrass");//new TextureRegion(new Texture("groundGrass.png"));
		terrainAbove = new TextureRegion(terrainBelow);
		terrainAbove.flip(true,true);
		plane = new Animation (0.05f,
								atlas.findRegion("planeRed1"),
								atlas.findRegion("planeRed2"),
								atlas.findRegion("planeRed3"),
								atlas.findRegion("planeRed2"));
								//	new TextureRegion(new Texture("planeRed1.png")),
								//	new TextureRegion(new Texture("planeRed2.png")),
								//	new TextureRegion(new Texture("planeRed3.png")),
								//	new TextureRegion(new Texture("planeRed2.png")));
		plane.setPlayMode(Animation.PlayMode.LOOP);
		tapIndicator2=atlas.findRegion("tap2");
		tapIndicator1 = atlas.findRegion("tap1");
		gameOver = new Texture("gameover.png");
		pillarUp = atlas.findRegion("rockGrassUp");
		pillarDown = atlas.findRegion("rockGrassDown");

		music = Gdx.audio.newMusic(Gdx.files.internal("sounds/journey.mp3"));
		music.setLooping(true);
		music.play();
		tapSound = Gdx.audio.newSound(Gdx.files.internal("sounds/pop.ogg"));
		crashSound = Gdx.audio.newSound(Gdx.files.internal("sounds/crash.ogg"));
		spawnSound = Gdx.audio.newSound(Gdx.files.internal("sounds/alarm.ogg"));
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		//batch.begin();
		//batch.draw(img, 0, 0);
		//font.draw(batch, "Hello World",200,200);
		//batch.end();
		fpsLogger.log();
		updateScene();
		drawScene();
	}

	@Override
	public void resize(int width, int height) {
		viewport.update(width, height);
	}

	@Override
	public void dispose() {
		batch.dispose();
		music.dispose();
		pillars.clear();
		atlas.dispose();
		tapSound.dispose();
		crashSound.dispose();
		spawnSound.dispose();
		//font.dispose();

	}

	private void updateScene() {

		if (Gdx.input.justTouched()) {
			tapSound.play();
			if (gameState == GameState.INIT) {
				gameState = GameState.ACTION;
				return;
			}
			if (gameState == GameState.GAME_OVER) {
				gameState = GameState.INIT;
				resetScene();
				return;
			}
			touchPosition.set(Gdx.input.getX(), Gdx.input.getY(),0);
			camera.unproject(touchPosition); // go from screen to world coords
			tmpVector.set(planePosition.x, planePosition.y);
			tmpVector.sub(touchPosition.x, touchPosition.y).nor();
			planeVelocity.mulAdd(tmpVector, TOUCH_IMPULSE- MathUtils.clamp(Vector2.dst(touchPosition.x, touchPosition.y, planePosition.x, planePosition.y),0,TOUCH_IMPULSE));
			tapDrawTime = TAP_DRAW_TIME_MAX;
		}

		if (gameState == GameState.INIT || gameState == GameState.GAME_OVER) {
			return;
		}
		float deltaTime = Gdx.graphics.getDeltaTime();
		tapDrawTime -= deltaTime;
		// try some scrolling! - terrainOffset -= 200*deltaTime;
		planeAnimTime+=deltaTime;
		planeVelocity.scl(damping);
		planeVelocity.add(gravity); // add gravity as a vector to velocity
		planeVelocity.add(scrollVelocity);
		planePosition.mulAdd(planeVelocity, deltaTime); //scalar multipy deltatime by velocity vector
		deltaPosition = planePosition.x-planeDefaultPosition.x;

		//Gdx.app.log("Velocity",planeVelocity.toString());
		//Gdx.app.log("TerrainOffset", Float.toString(terrainOffset));
		// shift the background, not the plane..
		terrainOffset -=planePosition.x-planeDefaultPosition.x;
		planePosition.x=planeDefaultPosition.x;
		if (terrainOffset*-1>terrainBelow.getRegionWidth()) {
			terrainOffset=0;
		}
		if (terrainOffset > 0) {
			terrainOffset =-terrainBelow.getRegionWidth();
		}
		if ((planePosition.y < terrainBelow.getRegionHeight()-35) || (planePosition.y+73 > 480-terrainBelow.getRegionHeight()+35)) {
			if (gameState != GameState.GAME_OVER) {
				gameState = GameState.GAME_OVER;
				crashSound.play();
			}
		}

		planeRect.set(planePosition.x + 16, planePosition.y, 50, 73);
		for(Vector2 vec: pillars)
		{
			vec.x-=deltaPosition;
			if(vec.x+pillarUp.getRegionWidth()<-10)
			{
				pillars.removeValue(vec, false);
			}
			if(vec.y==1)
			{
				obstacleRect.set(vec.x + 10, 0, pillarUp.getRegionWidth()-20, pillarUp.getRegionHeight()-10);
			}
			else
			{
				obstacleRect.set(vec.x + 10, 480-pillarDown.getRegionHeight()+10, pillarUp.getRegionWidth()-20, pillarUp.getRegionHeight());
			}
			if(planeRect.overlaps(obstacleRect))
			{
				if(gameState != GameState.GAME_OVER)
				{
					gameState = GameState.GAME_OVER;
					crashSound.play();
				}
			}
		}
		//Gdx.app.log("lpp",Float.toString(lastPillarPosition.x));
		if (lastPillarPosition.x < 400) {
			addPillar();
		}
	}
	private void drawScene() {
		camera.update();
		batch.setProjectionMatrix(camera.combined);

		batch.begin();
		batch.disableBlending();
		batch.draw(bgregion, 0, 0);
		batch.enableBlending();
		for (Vector2 v: pillars) {
			if (v.y == 1) {
				batch.draw(pillarUp, v.x, 0);
			} else {
				batch.draw(pillarDown, v.x, 480-pillarDown.getRegionHeight());
			}
		}
		batch.draw(terrainBelow, terrainOffset, 0);
		batch.draw(terrainBelow, terrainOffset + terrainBelow.getRegionWidth(), 0);
		batch.draw(terrainAbove, terrainOffset, 480-terrainAbove.getRegionHeight());
		batch.draw(terrainAbove, terrainOffset + terrainAbove.getRegionWidth(), 480-terrainAbove.getRegionHeight());
		batch.draw(plane.getKeyFrame(planeAnimTime),planePosition.x, planePosition.y);
		if (gameState == GameState.INIT) {
			batch.draw(tapIndicator1, planePosition.x, planePosition.y-80);
		}
		if (tapDrawTime>0) {
			batch.draw(tapIndicator2, touchPosition.x-29.5f, touchPosition.y-29.5f);
		}
		if (gameState == GameState.GAME_OVER) {
			batch.draw(gameOver, 400-206, 240-80);
		}

		batch.end();

	}

	private void resetScene() {
		terrainOffset = 0;
		planeAnimTime = 0;
		planeVelocity.set(0, 0);
		gravity.set(0, -2);
		planeDefaultPosition.set(400 - 88 / 2, 240 - 73 / 2); // center of image and screen
		planePosition.set(planeDefaultPosition.x, planeDefaultPosition.y);
		scrollVelocity.set(4, 0);

	}

	private void addPillar() {
		Vector2 pillarPosition = new Vector2();
		if (pillars.size == 0) {
			pillarPosition.x=(float)(800+Math.random()*600);
		} else {
			pillarPosition.x = lastPillarPosition.x+(float) (600+Math.random()*600);
		}
		if (MathUtils.randomBoolean()) {
			pillarPosition.y = 1;
		} else {
			pillarPosition.y = -1;
		}
		lastPillarPosition = pillarPosition;
		pillars.add(pillarPosition);

	}

}
