package com.kjh.topdown1;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class TopDownMain extends ApplicationAdapter {
	SpriteBatch batch;
	Texture img;
	BitmapFont font;
	FPSLogger fpsLogger = new FPSLogger();
	OrthographicCamera camera;
	Texture background;
	TextureRegion terrainBelow, terrainAbove;
	float terrainOffset=0;
	Animation plane;
	float planeAnimTime=0;
	
	@Override
	public void create () {
		batch = new SpriteBatch();
		//img = new Texture("badlogic.jpg");
		font = new BitmapFont(Gdx.files.internal("verdana39.fnt"), Gdx.files.internal("verdana39.png"), false);
		font.setColor(Color.PURPLE);
		camera = new OrthographicCamera();
		camera.setToOrtho(false,800,480);
		background = new  Texture("background.png");
		terrainBelow = new TextureRegion(new Texture("groundGrass.png"));
		terrainAbove = new TextureRegion(terrainBelow);
		terrainAbove.flip(true,true);
		plane = new Animation (0.05f, new TextureRegion(new Texture("planeRed1.png")),
									new TextureRegion(new Texture("planeRed2.png")),
									new TextureRegion(new Texture("planeRed3.png")),
									new TextureRegion(new Texture("planeRed2.png")));
		plane.setPlayMode(Animation.PlayMode.LOOP);

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
	public void dispose() {
		batch.dispose();
		font.dispose();

	}

	private void updateScene() {
		float deltaTime = Gdx.graphics.getDeltaTime();
		// try some scrolling! - terrainOffset -= 200*deltaTime;
		planeAnimTime+=deltaTime;
	}
	private void drawScene() {
		camera.update();
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		batch.disableBlending();
		batch.draw(background, 0, 0);
		batch.enableBlending();
		batch.draw(terrainBelow, terrainOffset, 0);
		batch.draw(terrainBelow, terrainOffset + terrainBelow.getRegionWidth(), 0);
		batch.draw(terrainAbove, terrainOffset, 480-terrainAbove.getRegionHeight());
		batch.draw(terrainAbove, terrainOffset + terrainAbove.getRegionWidth(), 480-terrainAbove.getRegionHeight());
		batch.draw(plane.getKeyFrame(planeAnimTime),350,200);
		batch.end();

	}
}
