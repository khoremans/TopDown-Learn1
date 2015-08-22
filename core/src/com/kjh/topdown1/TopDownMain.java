package com.kjh.topdown1;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class TopDownMain extends ApplicationAdapter {
	SpriteBatch batch;
	Texture img;
	BitmapFont font;
	FPSLogger fpsLogger;
	OrthographicCamera camera;
	Texture background;
	
	@Override
	public void create () {
		batch = new SpriteBatch();
		//img = new Texture("badlogic.jpg");
		font = new BitmapFont(Gdx.files.internal("verdana39.fnt"), Gdx.files.internal("verdana39.png"), false);
		font.setColor(Color.PURPLE);
		camera = new OrthographicCamera();
		camera.setToOrtho(false,800,400);
		background = new  Texture("background.png");

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

	}
	private void drawScene() {

	}
}
