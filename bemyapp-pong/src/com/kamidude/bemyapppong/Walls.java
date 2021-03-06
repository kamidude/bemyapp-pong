package com.kamidude.bemyapppong;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Disposable;

public class Walls implements Disposable {
	private ShaderProgram mWallShader;
	private Quad mLeftWall;
	private Body mLeftWallBody;
	private Quad mRightWall;
	private Body mRightWallBody;
	private Texture mTexture;
	private static final Matrix4 IDENTITY = new Matrix4();
	private static final float HALF_WALL_HEIGHT = 1f;
	
	public Walls() {
		FileHandle vs = Gdx.files.internal("data/wall.vs");
		FileHandle fs = Gdx.files.internal("data/wall.fs");
		mWallShader = new ShaderProgram(vs, fs);
		
		mTexture = new Texture(Gdx.files.internal("data/metal2_1024.png"), true);
		mTexture.setFilter(TextureFilter.MipMapLinearNearest, TextureFilter.MipMapLinearNearest);
		mTexture.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
		if(!mWallShader.isCompiled()) {
			throw new RuntimeException(mWallShader.getLog());
		}
	}
	
	public void render(ILevel level) {
		mWallShader.begin();
		mWallShader.setUniformMatrix("u_normalMat", level.getNormalMatrix(IDENTITY));
		mWallShader.setUniformMatrix("u_world", IDENTITY);
		mWallShader.setUniformMatrix("u_view", level.getCamera().view);
		mWallShader.setUniformMatrix("u_proj", level.getCamera().projection);
		mWallShader.setUniformf("u_halfWallHeight", HALF_WALL_HEIGHT);
		mWallShader.setUniformf("u_lightDir", level.getLightDir());
		mWallShader.setUniformi("u_tex", 0);
		Vector3 ballPos = level.getBallPosition();
		mWallShader.setUniformf("u_pointLightPos", ballPos);
		
		mTexture.bind(0);
		
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		
		mLeftWall.render(mWallShader, level.getCamera());
		mRightWall.render(mWallShader, level.getCamera());
		
		mWallShader.end();
	}
	
	public void resize(World world, Vector2 topLeft, Vector2 topRight, Vector2 bottomLeft, Vector2 bottomRight) {
		if(mLeftWallBody != null)
			mLeftWallBody.getWorld().destroyBody(mLeftWallBody);
		if(mRightWallBody != null)
			mLeftWallBody.getWorld().destroyBody(mRightWallBody);
		
		mLeftWallBody = makeWallBody(world, topLeft, bottomLeft);
		mRightWallBody = makeWallBody(world, topRight, bottomRight);
		
		float inclination = 0.2f;
		if(mLeftWall != null)
			mLeftWall.dispose();
		if(mRightWall != null)
			mRightWall.dispose();
		
		mLeftWall = new Quad(
				new Vector3(topLeft.x+inclination, topLeft.y, -HALF_WALL_HEIGHT), new Vector3(bottomLeft.x+inclination, bottomLeft.y, -HALF_WALL_HEIGHT),
				new Vector3(topLeft.x-inclination, topLeft.y, HALF_WALL_HEIGHT), new Vector3(bottomLeft.x-inclination, bottomLeft.y, HALF_WALL_HEIGHT)
				);
		mRightWall = new Quad(
				new Vector3(topRight.x-inclination, topRight.y, -HALF_WALL_HEIGHT), new Vector3(bottomRight.x-inclination, bottomRight.y, -HALF_WALL_HEIGHT),
				new Vector3(topRight.x+inclination, topRight.y, HALF_WALL_HEIGHT), new Vector3(bottomRight.x+inclination, bottomRight.y, HALF_WALL_HEIGHT)
				);
	}
	
	//
	// utils
	//
	
	private Body makeWallBody(World world, Vector2 firstPoint, Vector2 secondPoint) {
		BodyDef wallBodyDef = new BodyDef();
		Body rv = world.createBody(wallBodyDef);
		EdgeShape shape = new EdgeShape();
		shape.set(firstPoint, secondPoint);
		rv.createFixture(shape, 0);
		shape.dispose();
		return rv;
	}
	
	//
	// Disposable implementation
	//
	
	@Override
	public void dispose() {
		if(mLeftWallBody != null) {
			mLeftWallBody.getWorld().destroyBody(mLeftWallBody);
		}
		
		if(mRightWallBody != null) {
			mRightWallBody.getWorld().destroyBody(mRightWallBody);
		}
		
		mLeftWall.dispose();
		mRightWall.dispose();
		
		mTexture.dispose();
	}
}
