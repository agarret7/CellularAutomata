import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.*;

import java.util.ArrayList;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;

public class GraphicsArea implements Runnable {
	private Thread thread;
	private boolean running = true;

	private long window;
	private boolean showResources;

	private World world;

	public GraphicsArea(World worldObject, boolean showResources) {
		this.showResources = showResources;
		world = worldObject;
	}

	public void start() {
		running = true;
		thread = new Thread(this, "Simulation");
		thread.start();
	}

	private void init() {
		if (glfwInit() == false) {
			// TODO: Handle it.
		}

		glfwWindowHint(GL_TRUE, GL_TRUE);
		window = glfwCreateWindow(World.AREA_WIDTH, World.AREA_HEIGHT, "Simulation", NULL, NULL);
		if (window == NULL) {
			// TODO: Handle
			return;
		}
	
		GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
		glfwSetWindowPos(window, vidmode.width() - World.AREA_WIDTH / 2, vidmode.height() - world.AREA_HEIGHT / 2);

		glfwMakeContextCurrent(window);
		glfwShowWindow(window);
		GL.createCapabilities();

		glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
	}

	public void run() {
		init();
		while (running) {
			update();
			render();

			if (glfwWindowShouldClose(window)) {
				running = false;
			}
		}
	}

	private void update() {
		glfwPollEvents();
	}

	private void render() {
		glClear(GL_COLOR_BUFFER_BIT);
		
		for (Agent agent : world.agentObjects) {
			for (Child child : agent.children) {
				float xPixelsToOpenGL = (float)world.PIXELS_PER_CELL * 2f / (float)world.AREA_WIDTH;
				float yPixelsToOpenGL = (float)world.PIXELS_PER_CELL * 2f / (float)world.AREA_HEIGHT;
				float xAbsPos = child.cell.position[0] * xPixelsToOpenGL - 1;
				float yAbsPos = child.cell.position[1] * yPixelsToOpenGL - 1;
				float[] drawColor = {agent.color[0], agent.color[1], agent.color[2], child.cell.influence.get(agent).floatValue()};
				drawRect(drawColor, xAbsPos, yAbsPos, 
						 xAbsPos + xPixelsToOpenGL, yAbsPos,
						 xAbsPos + xPixelsToOpenGL, yAbsPos + yPixelsToOpenGL,
						 xAbsPos, yAbsPos + yPixelsToOpenGL
						);
			}
		}
		
		if (showResources) {
			for (ArrayList<Cell> row : world.cells) {
				for (Cell cell : row) {
					float xPixelsToOpenGL = (float)world.PIXELS_PER_CELL * 2f / (float)world.AREA_WIDTH;
					float yPixelsToOpenGL = (float)world.PIXELS_PER_CELL * 2f / (float)world.AREA_HEIGHT;
					float xAbsPos = cell.position[0] * xPixelsToOpenGL - 1;
					float yAbsPos = cell.position[1] * yPixelsToOpenGL - 1;
					
					float[] drawColor = {0f, 0f, 0f, -(float) cell.influencePerTurn*2};
					drawRect(drawColor, xAbsPos, yAbsPos, 
							 xAbsPos + xPixelsToOpenGL, yAbsPos,
							 xAbsPos + xPixelsToOpenGL, yAbsPos + yPixelsToOpenGL,
							 xAbsPos, yAbsPos + yPixelsToOpenGL
							);
				}
			}
		}
		
//		for (float gridX = -1; gridX < 1; gridX += 2 / (float)world.X_NUM_CELLS)
//			drawLine(gridX, -1, gridX, 1);
//
//		for (float gridY = -1; gridY < 1; gridY += 2 / (float)world.Y_NUM_CELLS)
//			drawLine(-1, gridY, 1, gridY);
		
		world.update();

		glfwSwapBuffers(window);
	}

	private void drawLine(float xStart, float yStart, float xEnd, float yEnd) {
		glBegin(GL_LINES);
		glLineWidth(0f);
		glColor4f(0f, 0f, 0f, 0.25f);
		glVertex2f(xStart, yStart);
		glVertex2f(xEnd, yEnd);
		glEnd();
		glColor4f(1,1,1,1);
	}
	
	private void drawRect(float[] color, float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4) {
		glBegin(GL_QUADS);
			glColor4f(color[0], color[1], color[2], color[3]);
			glVertex2f(x1,y1);
			glVertex2f(x2,y2);
			glVertex2f(x3,y3);
			glVertex2f(x4,y4);
		glEnd();
		glColor4f(1,1,1,1);
	}
}