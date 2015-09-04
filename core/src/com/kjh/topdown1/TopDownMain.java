package com.kjh.topdown1;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
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
	TextureRegion tapIndicator;
	float tapDrawTime;
	private static final float TAP_DRAW_TIME_MAX=1.0f;
	
	@Override
	public void create () {
		resetScene();
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
		tapIndicator=atlas.findRegion("tap2");
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
		//font.dispose();

	}

	private void updateScene() {
		float deltaTime = Gdx.graphics.getDeltaTime();
		// try some scrolling! - terrainOffset -= 200*deltaTime;
		planeAnimTime+=deltaTime;
		planeVelocity.scl(damping);
		planeVelocity.add(gravity); // add gravity as a vector to velocity
		planeVelocity.add(5.0f,0);
		planePosition.mulAdd(planeVelocity, deltaTime); //scalar multipy deltatime by velocity vector
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
		if (Gdx.input.justTouched()) {
			touchPosition.set(Gdx.input.getX(), Gdx.input.getY(),0);
			camera.unproject(touchPosition); // go from screen to world coords
			tmpVector.set(planePosition.x, planePosition.y);
			tmpVector.sub(touchPosition.x, touchPosition.y).nor();
			planeVelocity.mulAdd(tmpVector, TOUCH_IMPULSE- MathUtils.clamp(Vector2.dst(touchPosition.x, touchPosition.y, planePosition.x, planePosition.y),0,TOUCH_IMPULSE));
			tapDrawTime = TAP_DRAW_TIME_MAX;
		}
		tapDrawTime -= deltaTime;

	}
	private void drawScene() {
		camera.update();
		batch.setProjectionMatrix(camera.combined);

		batch.begin();
		batch.disableBlending();
		batch.draw(bgregion, 0, 0);
		batch.enableBlending();
		batch.draw(terrainBelow, terrainOffset, 0);
		batch.draw(terrainBelow, terrainOffset + terrainBelow.getRegionWidth(), 0);
		batch.draw(terrainAbove, terrainOffset, 480-terrainAbove.getRegionHeight());
		batch.draw(terrainAbove, terrainOffset + terrainAbove.getRegionWidth(), 480-terrainAbove.getRegionHeight());
		batch.draw(plane.getKeyFrame(planeAnimTime),planePosition.x, planePosition.y);
		if (tapDrawTime>0) {
			batch.draw(tapIndicator, touchPosition.x-29.5f, touchPosition.y-29.5f);
		}
		batch.end();

	}

	private void resetScene() {
		terrainOffset = 0;
		planeAnimTime = 0;
		planeVelocity.set(0,0);
		gravity.set(0,-2);
		planeDefaultPosition.set(400-88/2, 240-73/2); // center of image and screen
		planePosition.set(planeDefaultPosition.x, planeDefaultPosition.y);
	}

}
